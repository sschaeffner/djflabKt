package xyz.schaeffner.djflab

import xyz.schaeffner.djflab.snapcast.Server
import xyz.schaeffner.djflab.web.Notification
import xyz.schaeffner.djflab.web.Room
import xyz.schaeffner.djflab.web.RoomId
import xyz.schaeffner.djflab.web.SourceId

// TODO make configurable
val clients = mapOf(
    "9a:dc:ed:1f:ad:08" to RoomId.WORK_ZONE,
    "e2:6f:8b:05:0b:3f" to RoomId.LASER_ZONE,
    "00:00:00:00:00:03" to RoomId.SOCIAL_ZONE,
    "00:00:00:00:00:04" to RoomId.CREATIVE_ZONE
)

val sources = mapOf(
    "src1" to SourceId.SPOTIFY,
    "src2" to SourceId.AIRPLAY,
    "src3" to SourceId.AUX
)

fun Notification.Companion.from(server: Server): Notification {
    val rooms: Map<RoomId, Room> = server.groups.flatMap { group ->
        println("group $group")
        sources[group.streamId]?.let { sourceId ->
            println("sourceId $sourceId")
            group.clients.flatMap {
                println("client $it")
                clients[it.id]?.let { roomId ->
                    println("roomId $roomId")
                    listOf(
                        roomId to Room(
                            volumePercent = it.config.volume.percent,
                            sourceId = sourceId
                        )
                    )
                } ?: listOf()
            }
        } ?: listOf()
    }.toMap()

    return Notification(rooms)
}
