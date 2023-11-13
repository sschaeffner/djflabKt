@file:DependsOn("io.ktor:ktor-client-core-jvm:2.3.6")
@file:DependsOn("io.ktor:ktor-client-cio-jvm:2.3.6")
@file:DependsOn("io.ktor:ktor-client-websockets-jvm:2.3.6")

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.send
import io.ktor.websocket.readText
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

val host = "127.0.0.1"
val port = 8080
val path = "/api/ws"

val client = HttpClient {
    install(WebSockets) {
        pingInterval = 5_000
    }
}

suspend fun DefaultClientWebSocketSession.printIncomingMessages() {
    try {
        for (message in incoming) {
            message as? Frame.Text ?: continue
            println(message.readText())
        }
    } catch (e: Exception) {
        println("Error while receiving: " + e.localizedMessage)
    } finally {
        println("Closed connection for reason ${closeReason.await()}")
    }
}

suspend fun DefaultClientWebSocketSession.sendMessagesFromStdin() {
    while (true) {
        val message = readlnOrNull() ?: ""
        if (message.equals("exit", true)) return
        try {
            send(message)
        } catch (e: Exception) {
            println("Error while sending: " + e.localizedMessage)
            return
        }
    }
}

runBlocking {
    client.webSocket(HttpMethod.Get, host, port, path) {

        val printIncomingMessagesRoutine = launch { printIncomingMessages() }
        val sendMessagesFromStdinRoutine = launch { sendMessagesFromStdin() }

        sendMessagesFromStdinRoutine.join() // Wait for completion; either "exit" or error
        printIncomingMessagesRoutine.cancelAndJoin()
    }
}

client.close()
println("Connection closed. Goodbye!")
