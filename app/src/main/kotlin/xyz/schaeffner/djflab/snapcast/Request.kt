package xyz.schaeffner.djflab.snapcast

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
abstract class Request {
    abstract val id: Int
    val jsonrpc: String = "2.0"
    abstract val method: String
}

@Serializable
data class GetRPCVersionRequest(
    override val id: Int,
    override val method: String = "Server.GetRPCVersion"
) : Request()

@Serializable
data class GetStatusRequest(
    override val id: Int,
    override val method: String = "Server.GetStatus"
) : Request()

@Serializable
data class GroupSetClientsRequest(
    override val id: Int,
    override val method: String = "Group.SetClients",
    val params: Params,
) : Request() {

    @Serializable
    data class Params(
        val id: String,
        val clients: List<String>
    )
}

@Serializable
data class GroupSetStreamRequest(
    override val id: Int,
    override val method: String = "Group.SetStream",
    val params: Params
) : Request() {

    @Serializable
    data class Params(
        val id: String,
        @SerialName("stream_id") val streamId: String
    )
}

@Serializable
data class ClientSetVolumeRequest(
    override val id: Int,
    override val method: String = "Client.SetVolume",
    val params: Params
) : Request() {

    @Serializable
    data class Params(
        val id: String,
        val volume: Volume
    )
}