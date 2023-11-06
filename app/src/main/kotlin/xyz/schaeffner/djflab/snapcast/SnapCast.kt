package xyz.schaeffner.djflab.snapcast

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import xyz.schaeffner.djflab.loggerFactory

class SnapCast(
    private val baseUrl: String
) {
    private val log: Logger = loggerFactory(this::class.java)

    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = false
                encodeDefaults = true
            })
        }
        install(WebSockets) {
            pingInterval = 10_000
        }
    }

    private suspend inline fun <reified REQ : Request> makeRequest(body: REQ): HttpResponse {
        log.debug("makeRequest: body={}", body)

        val result = httpClient.post("http://$baseUrl/jsonrpc") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }

        // TODO check for error response (HTTP 200 response code, but error in body):
        //      {"error":{"code":-32603,"data":"Client not found","message":"Internal error"},"id":689324952,"jsonrpc":"2.0"}

        if (!result.status.isSuccess()) {
            log.error("Request not successful: {} {}", result, result.bodyAsText())
        } else {
            log.debug("result: {}, {}", result, result.bodyAsText())
        }

        return result
    }

    suspend fun listenForUpdates(channel: Channel<Unit>) {
        val json = Json {
            ignoreUnknownKeys = true
        }

        httpClient.webSocket(urlString = "ws://$baseUrl/jsonrpc") {
            while (true) {
                val othersMessage = incoming.receive() as? Frame.Text
                val text = othersMessage?.readText()
                log.debug("listenForUpdates: received {}", text)

                text?.let {
                    val notification = json.decodeFromString<Notification>(it)
                    when(notification.method) {
                        "Client.OnVolumeChanged" -> {
                            log.debug("client volume changed")
                            channel.send(Unit)
                        }
                        "Server.OnUpdate" -> {
                            log.debug("generic update")
                            channel.send(Unit)
                        }
                        else -> log.debug("unknown notification method: ${notification.method}")
                    }
                }
            }
        }
    }

    suspend fun getStatus(): Server {
        log.debug("getStatus")
        val response = makeRequest(GetStatusRequest(id = Random.nextInt().absoluteValue))
        val status = response.body<StatusResponse>().result
        log.debug("status server: {}", status)
        return status.server
    }

    private fun getGroupListeningTo(server: Server, streamId: String): Group? {
        return server.groups.find { it.streamId == streamId }
    }

    private suspend fun setGroupClients(groupId: String, clients: List<String>) {
        log.debug("setGroupClients: groupId={}, clients={}", groupId, clients)
        makeRequest(
            GroupSetClientsRequest(
                id = Random.nextInt().absoluteValue,
                params = GroupSetClientsRequest.Params(
                    id = groupId,
                    clients = clients
                )
            )
        )
    }

    private suspend fun setGroupStream(groupId: String, streamId: String) {
        log.debug("setGroupStream: groupId={}, streamId={}", groupId, streamId)
        makeRequest(
            GroupSetStreamRequest(
                id = Random.nextInt().absoluteValue,
                params = GroupSetStreamRequest.Params(
                    id = groupId,
                    streamId = streamId
                )
            )
        )
    }

    suspend fun setClientVolume(clientId: String, volumePercent: Int) {
        log.debug("setClientVolume: clientId={}, volumePercent={}", clientId, volumePercent)
        makeRequest(
            ClientSetVolumeRequest(
                id = Random.nextInt().absoluteValue,
                params = ClientSetVolumeRequest.Params(
                    id = clientId,
                    volume = Volume(
                        muted = false,
                        percent = volumePercent
                    )
                )
            )
        )
    }

    private suspend fun moveClientToNewGroupListeningTo(clientId: String, streamId: String) {
        log.debug("moveClientToNewGroupListeningTo: clientId={}, streamId={}", clientId, streamId)
        val initialServer = getStatus()
        val initialGroup: Group =
            initialServer.groups.find { group -> group.clients.any { client -> client.id == clientId } }
                ?: throw RuntimeException("Not able to find client group")

        setGroupClients(
            initialGroup.id,
            initialGroup.clients.map { it.id }.toList() - setOf(clientId)
        )

        val newServer = getStatus()
        val newGroup = newServer.groups.find { group -> group.clients.any { client -> client.id == clientId } }
            ?: throw RuntimeException("Not able to find client group")

        setGroupStream(newGroup.id, streamId)
    }

    /**
     * Un-assigns client from its current group, server implicitly creates a new group for that client,
     * sets stream to new client's group.
     */
    suspend fun setClientStream(clientId: String, streamId: String) {
        log.debug("setClientStream: clientId={}, streamId={}", clientId, streamId)
        val server = getStatus()

        // check if there is already a group with the given stream id
        val existingGroupListeningToStream = getGroupListeningTo(server, streamId)

        existingGroupListeningToStream?.let { group ->
            log.debug("setClientStream: existing group found: {}", group)
            setGroupClients(
                group.id,
                group.clients.map { it.id }.toList() + listOf(clientId)
            )
        }

        // else create a new group listening to that stream id
        if (existingGroupListeningToStream == null) {
            log.debug("setClientStream: no existing group found")
            moveClientToNewGroupListeningTo(clientId, streamId)
        }
    }
}