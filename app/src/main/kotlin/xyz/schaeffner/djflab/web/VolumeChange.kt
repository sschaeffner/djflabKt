package xyz.schaeffner.djflab.web

import kotlinx.serialization.Serializable

@Serializable
data class VolumeChange(
    val roomId: RoomId,
    val percent: Int
)