package io.playersjob.application.services

import io.playersjob.adapters.persistence.JpaJobStateRepository
import io.playersjob.adapters.persistence.JpaPlayerRepository
import io.playersjob.adapters.persistence.JpaProcessedPlayerRepository
import io.playersjob.adapters.persistence.entities.JobStateEntity
import io.playersjob.adapters.web.exceptions.WebException
import io.playersjob.adapters.web.transfermarkt.TransfermarktClient
import io.playersjob.core.domain.Player
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory

@ApplicationScoped
class FetchClubPlayersJob {

    @Inject
    lateinit var transfermarktClient: TransfermarktClient

    @Inject
    lateinit var playerRepository: JpaPlayerRepository

    @Inject
    lateinit var jobStateRepository: JpaJobStateRepository

    @Inject
    lateinit var processedPlayerRepository: JpaProcessedPlayerRepository

    private val logger = LoggerFactory.getLogger(FetchClubPlayersJob::class.java)

    @Transactional
    fun fetchAndSavePlayers(clubId: Int) {
        logger.info("Starting player fetch job for club ID: {}", clubId)

        val jobState = jobStateRepository.getLastJobState() ?: jobStateRepository.startNewJob()

        val players = fetchPlayersFromApi(clubId)
        val playersToProcess = filterPlayersToProcess(players, jobState)

        logger.info("Players fetched: {}", players.size)
        logger.info("Players to process: {}", playersToProcess.size)

        playersToProcess.forEach { player ->
            savePlayerWithTransaction(player,jobState)
        }

        val totalProcessedPlayers = processedPlayerRepository.findByJobState(jobState).size
        if (totalProcessedPlayers >= players.size) {
            completeJobWithTransaction(jobState)
            logger.info("Player fetch job completed for club ID: {}", clubId)
        } else {
            logger.info("Player fetch job in progress for club ID: {}", clubId)
        }
    }

    private fun fetchPlayersFromApi(clubId: Int): List<Player> {
        return try {
            transfermarktClient.getPlayersForClub(clubId)
        } catch (e: WebException) {
            logger.error("Error during Transfermarkt API call", e)
            throw e
        } catch (e: Exception) {
            logger.error("Unexpected error during API call", e)
            throw WebException("Unexpected error during API call: ${e.message}", cause = e)
        }
    }

    private fun filterPlayersToProcess(players: List<Player>, jobState: JobStateEntity): List<Player> {
        val processedPlayers = processedPlayerRepository.findByJobState(jobState)
        val processedPlayerIds = processedPlayers.map { it.playerId }
        return players.filter { it.id !in processedPlayerIds }
    }

    @Transactional
    fun savePlayerWithTransaction(player: Player, jobState: JobStateEntity) {
        try {
            val existingPlayer = playerRepository.findPlayerById(player.id)
            if (existingPlayer == null) {
                playerRepository.save(player)
            } else {
                logger.debug("Player {} already exists, skipping insert", player.id)
            }
            processedPlayerRepository.save(player.id, jobState)
        } catch (e: Exception) {
            logger.error("Error during saving of player with id {}", player.id, e)
            throw e
        }
    }

    @Transactional
    fun completeJobWithTransaction(jobState: JobStateEntity) {
        try {
            jobStateRepository.completeJob(jobState)
        } catch (e: Exception) {
            logger.error("Error during complete job {}", jobState, e)
            throw e
        }
    }
}