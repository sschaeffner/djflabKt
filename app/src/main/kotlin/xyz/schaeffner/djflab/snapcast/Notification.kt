package xyz.schaeffner.djflab.snapcast

import kotlinx.serialization.Serializable

@Serializable
data class Notification(
    val jsonrpc: String,
    val method: String
)
