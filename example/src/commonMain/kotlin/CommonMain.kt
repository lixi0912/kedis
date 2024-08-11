import io.github.domgew.kedis.KedisClient
import io.github.domgew.kedis.KedisConfiguration
import io.github.domgew.kedis.arguments.SetOptions
import io.github.domgew.kedis.arguments.SyncOption
import io.github.domgew.kop.KotlinObjectPool
import io.github.domgew.kop.KotlinObjectPoolConfig
import io.github.domgew.kop.KotlinObjectPoolStrategy
import io.github.domgew.kop.withObject
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.request.receiveText
import io.ktor.server.response.header
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

fun commonMain() {
    embeddedServer(
        factory = CIO,
        port = 8080,
    ) {
        val kedisPool = KotlinObjectPool(
            KotlinObjectPoolConfig(
                maxSize = 3,
                keepAliveFor = 2.minutes,
                strategy = KotlinObjectPoolStrategy.LIFO,
            ) {
                println("kedisPool: Creating new instance")
                KedisClient(
                    configuration = KedisConfiguration(
                        endpoint = KedisConfiguration.Endpoint.HostPort(
                            host = "127.0.0.1",
                            port = 6379,
                        ),
                        authentication = KedisConfiguration.Authentication.NoAutoAuth,
                        connectionTimeoutMillis = 250,
                        keepAlive = true,
                    ),
                )
            }
        )

        routing {
            route("/info") {
                get {
                    call.respondText { "Running" }
                }
            }

            route("/cache/flush") {
                post {
                    kedisPool.withObject {
                        it.flushAll(sync = SyncOption.SYNC)
                    }
                }
            }

            route("/cache/{key}") {
                get {
                    val key = call.getParameterOrFail("key")
                        ?: return@get

                    val fromCache = withContext(Dispatchers.IO) {
                        kedisPool.withObject {
                            it.get(key)
                        }
                    }

                    if (fromCache == null) {
                        call.respondNotFound()
                        return@get
                    }

                    call.respondText { fromCache }
                }

                post {
                    val key = call.getParameterOrFail("key")
                        ?: return@post

                    val fromCache = kedisPool.withObject {
                        it.set(key, call.receiveText())
                    }

                    call.respondText(
                        status = HttpStatusCode.Accepted,
                    ) { "Accepted: $fromCache" }
                }

                patch {
                    val key = call.getParameterOrFail("key")
                        ?: return@patch

                    val result = kedisPool.withObject {
                        it.getOrCallback(
                            key = key,
                        ) {
                            // this would probably be an expensive call to some API or similar
                            call.receiveText()
                        }
                    }

                    call.response.header("X-From-Cache", result.first.toString())

                    call.respondText { result.second }
                }
            }
        }
    }
        .start(
            wait = true,
        )
}

// you probably want to ensure the connection is fast enough by adding timeouts
private suspend fun KedisClient.getOrCallback(
    key: String,
    ttl: Duration = 1.hours,
    block: suspend () -> String,
): Pair<Boolean, String> {
    val availability = isAvailable()
    if (availability != null) {
        println("KedisClient.getOrCallback: Cache not available: ${availability.message}")
        return Pair(false, block())
    }

    val valueFromCache = try {
        get(key)
    } catch (th: Throwable) {
        coroutineContext.ensureActive()
        println("KedisClient.getOrCallback: Could not get value from cache: ${th.message}")
        return Pair(false, block())
    }

    if (valueFromCache != null) {
        return Pair(true, valueFromCache)
    }

    val value = block()

    try {
        set(
            key = key,
            value = value,
            options = SetOptions(
                previousKeyHandling = SetOptions.PreviousKeyHandling.OVERRIDE,
                getPreviousValue = false,
                expire = SetOptions.ExpireOption.ExpiresInMilliseconds(
                    milliseconds = ttl.inWholeMilliseconds,
                ),
            ),
        )
    } catch (th: Throwable) {
        coroutineContext.ensureActive()
        println("KedisClient.getOrCallback: Could not write value to cache: ${th.message}")
    }

    return Pair(false, value)
}

private suspend fun KedisClient.isAvailable(): Throwable? {
    if (isConnected) {
        return null
    }

    try {
        connect()
        return null
    } catch (th: Throwable) {
        coroutineContext.ensureActive()
        return th
    }
}

private suspend fun ApplicationCall.respondNotFound() {
    respondText(
        contentType = ContentType.Text.Plain,
        status = HttpStatusCode.NotFound,
    ) { "Not found" }
}

private suspend fun ApplicationCall.getParameterOrFail(
    parameter: String,
): String? =
    getParameterOrFail(
        parameter = parameter,
    ) {
        it
    }

private suspend fun <T> ApplicationCall.getParameterOrFail(
    parameter: String,
    transform: (v: String) -> T,
): T? {
    val result = parameters[parameter]
        ?.trim()
        ?.ifEmpty { null }
        ?: let {
            respondText(
                contentType = ContentType.Text.Plain,
                status = HttpStatusCode.BadRequest,
            ) {
                "Missing parameter: $parameter"
            }
            null
        }

    return result
        ?.let { transform(it) }
}
