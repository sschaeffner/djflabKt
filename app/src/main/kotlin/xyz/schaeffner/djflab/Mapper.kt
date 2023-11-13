package xyz.schaeffner.djflab

import xyz.schaeffner.djflab.snapcast.Client
import xyz.schaeffner.djflab.snapcast.Group
import xyz.schaeffner.djflab.snapcast.Server
import xyz.schaeffner.djflab.web.Notification
import xyz.schaeffner.djflab.web.Room
import xyz.schaeffner.djflab.web.RoomId
import xyz.schaeffner.djflab.web.SourceId

// TODO make configurable
val clients = mapOf(
    "32:7f:d0:da:10:21" to RoomId.WORK_ZONE,
    "62:97:a7:94:11:ed" to RoomId.LASER_ZONE,
    "00:00:00:00:00:03" to RoomId.SOCIAL_ZONE,
    "00:00:00:00:00:04" to RoomId.CREATIVE_ZONE
)

val clientHostnames = mapOf(
    "workzone" to RoomId.WORK_ZONE,
    "laserzone" to RoomId.LASER_ZONE,
    "socialzone" to RoomId.SOCIAL_ZONE,
    "creativezone" to RoomId.CREATIVE_ZONE,
)

val sources = mapOf(
    "src1" to SourceId.SPOTIFY,
    "src2" to SourceId.AIRPLAY,
    "src3" to SourceId.AUX
)

fun Notification.Companion.from(server: Server): Notification {
    val rooms: Map<RoomId, Room> = server.groups.flatMap { group ->
        sources[group.streamId]?.let { sourceId ->
            group.clients.flatMap {
                clients[it.id]?.let { roomId ->
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

fun RoomId.toClientId(): String = clients.entries.first { (_, v) -> v == this }.key

fun RoomId.Companion.fromHostname(hostname: String): RoomId = clientHostnames[hostname]!!

fun SourceId.toStreamId(): String = sources.entries.first { (_, v) -> v == this}.key

fun SourceId.Companion.fromStreamId(streamId: String): SourceId = sources[streamId]!!

fun Server.getClient(roomId: RoomId): Client {
    val clientId = roomId.toClientId()
    return this.groups.flatMap { it.clients }.first { it.id == clientId }
}

fun Server.getClientGroup(roomId: RoomId): Group {
    val clientId = roomId.toClientId()
    return this.groups.first { g -> g.clients.any { it.id == clientId } }
}
