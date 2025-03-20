package io.playersjob.application.services

import io.playersjob.core.domain.JobState
import io.playersjob.core.ports.JobStateRepository
import io.playersjob.core.ports.PlayerProvider
import io.playersjob.core.ports.PlayerRepository
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class PlayerFetcher @Inject constructor(
    private val playerProvider: PlayerProvider,
    private val playerRepository: PlayerRepository,
    private val jobStateRepository: JobStateRepository
) {

    fun fetchAndSavePlayers(clubId: Int): Uni<Void> {
        return jobStateRepository.getLastJobState()
            .flatMap { lastJobState ->
                playerProvider.getPlayersForClub(clubId)
                    .flatMap { players ->
                        val playersToProcess = players.dropWhile { it.id != lastJobState?.lastProcessedPlayerId }
                        val unis = playersToProcess.map { player ->
                        playerRepository.save(player)
                            .flatMap { jobStateRepository.save(JobState(player.id)) }
                    }
                        Uni.join().all(unis)
                            .andCollectFailures()
                            .replaceWithVoid()
                    }
            }
    }
}