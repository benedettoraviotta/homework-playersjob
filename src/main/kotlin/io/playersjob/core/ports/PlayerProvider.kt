package io.playersjob.core.ports

import io.playersjob.core.domain.Player
import io.smallrye.mutiny.Uni

interface PlayerProvider {
    fun getPlayersForClub(clubId: Int): List<Player>
}