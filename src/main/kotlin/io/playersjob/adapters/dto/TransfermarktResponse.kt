package io.playersjob.adapters.dto

data class TransfermarktResponse(
    val updatedAt: String,
    val id: String,
    val players: List<TransfermarktPlayer>
)