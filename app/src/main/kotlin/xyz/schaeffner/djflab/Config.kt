package xyz.schaeffner.djflab

import kotlin.random.Random

data class Config(
    val apiUsers: Map<String, String>,
    val wsPassword: String,
    val snapcastBaseUrl: String
) {
    companion object {
        fun fromEnv(): Config {
            val apiUsers = readEnvMap("FAB_API_USERS", mapOf())
            val wsPassword = readEnvString("FAB_WS_PASSWORD", Random.nextBytes(32).decodeToString())
            val snapcastBaseUrl = readEnvString("SNAPCAST_BASE_URL", "10.100.1.50:1780")
            return Config(
                apiUsers,
                wsPassword,
                snapcastBaseUrl
            )
        }

        private fun readEnvString(name: String, default: String): String {
            return System.getenv(name).takeUnless { it.isNullOrEmpty() } ?: default
        }

        private fun readEnvMap(name: String, default: Map<String, String>): Map<String, String> {
            return System.getenv(name).takeUnless { it.isNullOrEmpty() }?.let {
                it.split(",")
                    .map {
                        val pairs = it.split(":")
                        if (pairs.size != 2) {
                            throw RuntimeException(
                                "Invalid configuration: \"$pairs\" does not have length 2!"
                            )
                        }
                        pairs[0] to pairs[1]
                    }
                    .toMap()
            } ?: default
        }
    }
}