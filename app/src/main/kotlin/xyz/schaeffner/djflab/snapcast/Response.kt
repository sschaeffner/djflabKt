package xyz.schaeffner.djflab.snapcast

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias StatusResponse = Response<Status>

@Serializable
data class Response<T>(
    val id: Int,
    val jsonrpc: String,
    val result: T
)

@Serializable
data class Status(
    val server: Server
)

@Serializable
data class Server(
    val groups: List<Group>,
    val server: ServerServer,
    val streams: List<Stream>
)

@Serializable
data class Group(
    val clients: List<Client>,
    val id: String,
    val muted: Boolean,
    val name: String,
    @SerialName("stream_id") val streamId: String
)

@Serializable
data class ServerServer(
    val host: Host,
    val snapserver: SnapServer
)

@Serializable
data class Host(
    val arch: String,
    val ip: String,
    val mac: String,
    val name: String,
    val os: String
)

@Serializable
data class SnapServer(
    val controlProtocolVersion: Int,
    val name: String,
    val protocolVersion: Int,
    val version: String,
)

@Serializable
data class Client(
    val config: ClientConfig,
    val connected: Boolean,
    val host: Host,
    val id: String,
    val lastSeen: LastSeen,
    val snapclient: SnapClient
)

@Serializable
data class ClientConfig(
    val instance: Int,
    val latency: Int,
    val name: String,
    val volume: Volume
)

@Serializable
data class Volume(
    val muted: Boolean,
    val percent: Int
)

@Serializable
data class LastSeen(
    val sec: Long,
    val usec: Long
)

@Serializable
data class SnapClient(
    val name: String,
    val protocolVersion: Int,
    val version: String,
)

@Serializable
data class Stream(
    val id: String,
    val properties: StreamProperties,
    val status: String,
    val uri: StreamUri
)

@Serializable
data class StreamProperties(
    val canControl: Boolean,
    val canGoNext: Boolean,
    val canGoPrevious: Boolean,
    val canPause: Boolean,
    val canPlay: Boolean,
    val canSeek: Boolean
)

@Serializable
data class StreamUri(
    val fragment: String,
    val host: String,
    val path: String,
    val query: StreamUriQuery,
    val raw: String,
    val scheme: String
)

@Serializable
data class StreamUriQuery(
    @SerialName("chunk_ms") val chunkMs: String,
    val codec: String,
    val name: String,
    val sampleformat: String,
)