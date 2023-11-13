package xyz.schaeffner.djflab

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import java.time.Duration
import java.util.Collections
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.MDC
import org.slf4j.event.Level
import xyz.schaeffner.djflab.snapcast.SnapCast
import xyz.schaeffner.djflab.web.ChangeVolumeCommand
import xyz.schaeffner.djflab.web.Command
import xyz.schaeffner.djflab.web.NextSourceCommand
import xyz.schaeffner.djflab.web.Notification
import xyz.schaeffner.djflab.web.Room
import xyz.schaeffner.djflab.web.RoomId
import xyz.schaeffner.djflab.web.SetSourceCommand
import xyz.schaeffner.djflab.web.SourceId
import xyz.schaeffner.djflab.web.StreamChange
import xyz.schaeffner.djflab.web.VolumeChange

class App(private val config: Config) {
    private val log: Logger = loggerFactory(this::class.java)

    private val sc = SnapCast(config.snapcastBaseUrl)
    private val updateNotificationChannel = Channel<Unit>()

    private fun ApplicationRequest.toLogStringWithColors(): String = "${httpMethod.value} - ${path()}"

    private val connections: MutableSet<DefaultWebSocketSession> = Collections.synchronizedSet(HashSet())

    private val moduleConfiguration: Application.() -> Unit = {
        install(WebSockets) {
            pingPeriod = Duration.ofSeconds(5)
            timeout = Duration.ofSeconds(15)
            maxFrameSize = Long.MAX_VALUE
            masking = false
        }

        install(ContentNegotiation) {
            json(Json {
                prettyPrint = false
                isLenient = false
            })
        }

        install(CallLogging) {
            logger = log
            level = Level.DEBUG
            disableDefaultColors()
            mdc("startTimestamp") { System.nanoTime().toString() }
            format { call ->
                val startTimestamp = MDC.get("startTimestamp").toLong()
                val endTimestamp = System.nanoTime()
                val delayMillis = (endTimestamp - startTimestamp) / 1_000_000.0

                when (val status = call.response.status() ?: "Unhandled") {
                    HttpStatusCode.Found -> "${status as HttpStatusCode} (${delayMillis}ms): " +
                            "${call.request.toLogStringWithColors()} -> ${call.response.headers[HttpHeaders.Location]} (Principal=${call.principal<UserIdPrincipal>()?.name})"

                    "Unhandled" -> "$status (${delayMillis}ms): ${call.request.toLogStringWithColors()} (Principal=${call.principal<UserIdPrincipal>()?.name})"
                    else -> "${status as HttpStatusCode} (${delayMillis}ms): ${call.request.toLogStringWithColors()} (Principal=${call.principal<UserIdPrincipal>()?.name})"
                }
            }
        }

        routing {
            route("/api") {
                post("/volume") {
                    val body: VolumeChange = call.receive()
                    sc.setClientVolume(body.roomId.toClientId(), body.percent)
                    call.respond(HttpStatusCode.OK)
                }

                post("/source") {
                    val body: StreamChange = call.receive()
                    sc.setClientStream(body.roomId.toClientId(), body.sourceId.toStreamId())
                    call.respond(HttpStatusCode.OK)
                }

                webSocket("/ws") {
                    send("welcome to DJFlab")
                    log.debug("current connections: {}", connections)
                    log.debug("adding new connection: {}", this)
                    synchronized(connections) {
                        connections.add(this)
                    }

                    try {
                        for (frame in incoming) {
                            log.debug("received {} from {}", frame, this)

                            frame as? Frame.Text ?: continue
                            val text = frame.readText()
                            log.debug("received \"{}\" from {}", text, this)

                            try {
                                val command: Command = Json.decodeFromString(text)
                                handleCommand(command)
                            } catch (e: SerializationException) {
                                log.error("not able to deserialize: " + e.localizedMessage)
                                e.printStackTrace()
                            } catch (e: Exception) {
                                log.error("not able to handle command: " + e.localizedMessage)
                                e.printStackTrace()
                            }
                        }
                    } catch (e: Exception) {
                        log.error("Error while receiving from websocket: " + e.localizedMessage)
                    } finally {
                        log.warn("Closed connection {} of for reason {}", this, closeReason.await())

                        synchronized(connections) {
                            log.debug("removing connection: {}", this)
                            connections.remove(this)
                            log.debug("current connections: {}", connections)
                        }
                    }

                    log.debug("ending WebSocket handling for {}", this)
                }
            }

            get("/health") {
                call.respond(HttpStatusCode.OK)
            }
        }
    }

    private fun startServer() {
        embeddedServer(Netty, environment = applicationEngineEnvironment {
            module(moduleConfiguration)
            connector {
                port = 8080
            }
        }).start(wait = false)
    }

    private suspend fun printClients() {
        val server = sc.getStatus()

        log.debug("--- Clients ---")
        log.debug("id - hostname - name - room")
        server.groups.flatMap { it.clients }.forEach {
            log.debug("{} - {} - {} - {}", it.id, it.host.name, it.config.name, RoomId.fromHostname(it.host.name))
        }
        log.debug("---------------")
    }

    private suspend fun handleCommand(command: Command) {
        log.debug("handling command: {}", command)

        val server = sc.getStatus()

        when (command) {
            is ChangeVolumeCommand -> {
                val client = server.getClient(command.roomId)
                val newVolumePercent = (client.config.volume.percent + command.deltaPercent).coerceIn(0 .. 100)
                sc.setClientVolume(command.roomId.toClientId(), newVolumePercent)
            }
            is NextSourceCommand -> {
                val group = server.getClientGroup(command.roomId)
                val nextSource: SourceId = SourceId.fromStreamId(group.streamId).next()
                sc.setClientStream(command.roomId.toClientId(), nextSource.toStreamId())
            }
            is SetSourceCommand -> {
                sc.setClientStream(command.roomId.toClientId(), command.sourceId.toStreamId())
            }
        }
    }

    private suspend fun handleSnapcastUpdates() {
        for (u in updateNotificationChannel) {
            val notification = Notification.from(sc.getStatus())
            log.debug("received update through channel -> {}", notification)

            val notificationJson = Json.encodeToString(notification)

            connections.forEach {
                log.debug("forwarding {} to {}", notificationJson, it)
                it.send(notificationJson)
            }
        }
    }

    fun start() {
        log.info("starting App...")
        log.trace("Config: {}", config)

        val cmd: Command = ChangeVolumeCommand(RoomId.CREATIVE_ZONE, -20)
        log.debug("sample command json: ${Json.encodeToString(cmd)}")

        val not = Notification(
            mapOf(
                RoomId.SOCIAL_ZONE to Room(volumePercent = 100, sourceId = SourceId.SPOTIFY),
                RoomId.CREATIVE_ZONE to Room(volumePercent = 90, sourceId = SourceId.AIRPLAY),
                RoomId.LASER_ZONE to Room(volumePercent = 80, sourceId = SourceId.SPOTIFY),
                RoomId.WORK_ZONE to Room(volumePercent = 70, sourceId = SourceId.AUX)
            )
        )
        log.debug("sample notification json: ${Json.encodeToString(not)}")

        runBlocking {
            printClients()
            launch { sc.listenForUpdates(updateNotificationChannel) }
            launch { handleSnapcastUpdates() }
            startServer()
        }
    }
}

fun main() {
    App(Config.fromEnv()).start()
}
