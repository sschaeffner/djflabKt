package xyz.schaeffner.djflab.web

import kotlinx.serialization.Serializable

@Serializable
data class StreamChange(
    val clientId: String,
    val streamId: String
)