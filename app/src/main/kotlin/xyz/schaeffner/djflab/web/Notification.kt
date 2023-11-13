package xyz.schaeffner.djflab.web

import kotlinx.serialization.Serializable

/**
 * Notification from backend to panel about current status of all volumes, sources per room
 */
@Serializable
data class Notification(
    val rooms: Map<RoomId, Room>
) {
    companion object
}

@Serializable
data class Room(
    val volumePercent: Int,
    val sourceId: SourceId
)
