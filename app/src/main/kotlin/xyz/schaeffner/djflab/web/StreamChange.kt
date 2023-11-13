package xyz.schaeffner.djflab.web

import kotlinx.serialization.Serializable

@Serializable
data class StreamChange(
    val roomId: RoomId,
    val sourceId: SourceId
)