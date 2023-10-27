package xyz.schaeffner.djflab

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
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
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import java.time.Duration
import org.slf4j.Logger
import org.slf4j.MDC
import org.slf4j.event.Level

class App(private val config: Config) {
    private val log: Logger = loggerFactory(this::class.java)

    private fun ApplicationRequest.toLogStringWithColors(): String = "${httpMethod.value} - ${path()}"

    private val moduleConfiguration: Application.() -> Unit = {
        install(WebSockets) {
            pingPeriod = Duration.ofSeconds(5)
            timeout = Duration.ofSeconds(15)
            maxFrameSize = Long.MAX_VALUE
            masking = false
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

                    "Unhandled" -> "${status} (${delayMillis}ms): ${call.request.toLogStringWithColors()} (Principal=${call.principal<UserIdPrincipal>()?.name})"
                    else -> "${status as HttpStatusCode} (${delayMillis}ms): ${call.request.toLogStringWithColors()} (Principal=${call.principal<UserIdPrincipal>()?.name})"
                }
            }
        }

        routing {
            route("/api") {
                get("/helloworld") {
                    call.respond(HttpStatusCode.OK, "hello, world")
                }
            }

            get("/health") {
                call.respond(HttpStatusCode.OK)
            }
        }
    }

    fun start() {
        log.info("starting App...")
        log.debug("Config: $config")

        embeddedServer(Netty, environment = applicationEngineEnvironment {
            module(moduleConfiguration)
            connector {
                port = 8080
            }
        }).start(wait = true)
    }
}

fun main() {
    App(Config.fromEnv()).start()
}
