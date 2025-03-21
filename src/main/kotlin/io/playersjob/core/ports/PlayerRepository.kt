package io.playersjob.core.ports

import io.playersjob.core.domain.Player
import io.smallrye.mutiny.Uni

interface PlayerRepository{
    fun save(player: Player)
    fun findPlayerById(playerId: String): Player?
}