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
    "b8:27:eb:ae:7e:1b" to RoomId.WORK_ZONE,
    "b8:27:eb:0c:8f:d0" to RoomId.LASER_ZONE,
    "b8:27:eb:3e:90:ba" to RoomId.SOCIAL_ZONE,
    "b8:27:eb:19:34:8d" to RoomId.CREATIVE_ZONE
)

val clientHostnames = mapOf(
    "klient2" to RoomId.WORK_ZONE,
    "klient1" to RoomId.LASER_ZONE,
    "klient0" to RoomId.SOCIAL_ZONE,
    "klient3" to RoomId.CREATIVE_ZONE,
)

val sources = mapOf(
    "Spotify" to SourceId.SPOTIFY,
    "Airplay" to SourceId.AIRPLAY,
    "Aux" to SourceId.AUX
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

fun RoomId.Companion.fromHostname(hostname: String): RoomId? = clientHostnames[hostname]

fun SourceId.toStreamId(): String = sources.entries.first { (_, v) -> v == this }.key

fun SourceId.Companion.fromStreamId(streamId: String): SourceId = sources[streamId]!!

fun Server.getClient(roomId: RoomId): Client {
    val clientId = roomId.toClientId()
    return this.groups.flatMap { it.clients }.first { it.id == clientId }
}

fun Server.getClientGroup(roomId: RoomId): Group {
    val clientId = roomId.toClientId()
    return this.groups.first { g -> g.clients.any { it.id == clientId } }
}
