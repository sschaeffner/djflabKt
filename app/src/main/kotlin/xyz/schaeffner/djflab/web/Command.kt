package xyz.schaeffner.djflab.web

import kotlinx.serialization.Serializable

/**
 * Command from panel to backend to change volume/source per room
 */
@Serializable
sealed class Command

@Serializable
data class ChangeVolumeCommand(
    val roomId: RoomId,
    val deltaPercent: Int
) : Command()

@Serializable
data class NextSourceCommand(
    val roomId: Int
) : Command()

@Serializable
data class SetSourceCommand(
    val roomId: Int,
    val sourceId: SourceId
) : Command()
