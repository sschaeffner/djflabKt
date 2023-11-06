package xyz.schaeffner.djflab.web

import kotlinx.serialization.Serializable

@Serializable
data class VolumeChange(
    val clientId: String,
    val percent: Int
)