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
import jakarta.transaction.Transactional.TxType
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

    @Inject
    lateinit var insertInterruptionSimulator: InsertInterruptionSimulator

    private val logger = LoggerFactory.getLogger(FetchClubPlayersJob::class.java)

    @Transactional
    fun fetchAndSavePlayers(clubId: Int) {
        logger.info("Starting player fetch job for club ID: {}", clubId)

        val players = try {
            fetchPlayersFromApi(clubId)
        } catch (e: Exception) {
            logger.error("Exception during fetch players operation: {}", e.message, e)
            throw e
        }

        try {
            val jobState = getOrCreateJobState()
            val playersToProcess = filterPlayersToProcess(players, jobState)

            logger.info("Players fetched: {}", players.size)
            logger.info("Players to process: {}", playersToProcess.size)

            playersToProcess.forEach { player ->
                savePlayer(player, jobState)
            }

            val totalProcessedPlayers = processedPlayerRepository.findByJobState(jobState).size
            if (totalProcessedPlayers >= players.size) {
                updateJobState(jobState, "COMPLETED")
                logger.info("Player fetch job completed for club ID: {}", clubId)
            } else {
                logger.info("Player fetch job in progress for club ID: {}", clubId)
            }
        } catch (e: Exception) {
            logger.error("Exception during fetchAndSavePlayers: {}", e.message, e)
            throw e
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

    @Transactional(TxType.REQUIRES_NEW)
    fun getOrCreateJobState(): JobStateEntity {
        return try {
            val lastJobState = jobStateRepository.getLastJobState()
            if (lastJobState != null && lastJobState.status == "IN_PROGRESS") {
                lastJobState
            } else {
                jobStateRepository.startNewJob()
            }
        } catch (e: Exception) {
            logger.error("Error during get job state", e)
            throw e
        }
    }

    @Transactional(TxType.REQUIRES_NEW)
    fun savePlayer(player: Player, jobState: JobStateEntity) {
        try {
            val existingPlayer = playerRepository.findPlayerById(player.id)
            if (existingPlayer == null) {
                playerRepository.save(player)
            } else {
                logger.debug("Player {} already exists, skipping insert", player.id)
            }
            processedPlayerRepository.save(player.id, jobState)

            // SIMULO INTERRUZIONE
            val processedCount = processedPlayerRepository.findByJobState(jobState).size
            insertInterruptionSimulator.checkForInterruption(processedCount)

        } catch (e: Exception) {
            logger.error("Error during saving of player with id {}", player.id, e)
            throw e
        }
    }

    @Transactional(TxType.REQUIRES_NEW)
    fun updateJobState(jobState: JobStateEntity, newStatus: String) {
        try {
            val attachedJobState =
                jobStateRepository.findById(jobState.id)
                    ?: throw IllegalArgumentException("Job state with ${jobState.id} not found")
            attachedJobState.status = newStatus
            jobStateRepository.persistAndFlush(attachedJobState)
            logger.info("Job {} updated to status: {}", attachedJobState.id, newStatus)
        } catch (e: Exception) {
            logger.error("Error during updating job {} to status: {}", jobState, newStatus, e)
            throw e
        }
    }
}