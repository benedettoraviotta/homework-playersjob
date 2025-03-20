package io.playersjob.adapters.job

import io.playersjob.application.services.PlayerFetcher
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class FetchClubPlayersJob @Inject constructor(
    private val playerFetcher: PlayerFetcher
) {
    fun fetchPlayers(clubId: Int? = 5) {
        playerFetcher.fetchAndSavePlayers(clubId ?: 5).subscribe().with { }
    }
}