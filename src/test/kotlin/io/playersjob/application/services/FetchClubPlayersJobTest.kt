package io.playersjob.application.services

import io.playersjob.adapters.persistence.JpaJobStateRepository
import io.playersjob.adapters.persistence.JpaPlayerRepository
import io.playersjob.adapters.persistence.JpaProcessedPlayerRepository
import io.playersjob.adapters.persistence.entities.JobStateEntity
import io.playersjob.adapters.persistence.entities.PlayerEntity
import io.playersjob.adapters.persistence.entities.ProcessedPlayerEntity
import io.playersjob.adapters.web.transfermarkt.TransfermarktClient
import io.playersjob.adapters.web.exceptions.WebException
import io.playersjob.core.domain.Player
import io.quarkus.test.InjectMock
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`

@QuarkusTest
class FetchClubPlayersJobTest {

    @Inject
    lateinit var playerRepository: JpaPlayerRepository

    @Inject
    lateinit var jobStateRepository: JpaJobStateRepository

    @Inject
    lateinit var processedPlayerRepository: JpaProcessedPlayerRepository

    @Inject
    lateinit var fetchClubPlayersJob: FetchClubPlayersJob

    @Inject
    lateinit var em: EntityManager

    @InjectMock
    lateinit var transfermarktClient: TransfermarktClient

    @BeforeEach
    @Transactional
    fun setUp() {
        em.createQuery("DELETE FROM PlayerEntity").executeUpdate()
        em.createQuery("DELETE FROM ProcessedPlayerEntity").executeUpdate()
        em.createQuery("DELETE FROM JobStateEntity").executeUpdate()
    }

    @Test
    @Transactional
    fun `test fetchPlayers integration - happy path`() {
        val clubId = 5
        val players = listOf(
            Player("1", "Player 1", "Forward", "1990-01-01", 30, listOf("Country 1"), 180, "Right", "2020-01-01", "Club A", "2023-01-01", 1000000, "Active"),
            Player("2", "Player 2", "Midfielder", "1992-01-01", 28, listOf("Country 2"), 175, "Left", "2020-01-01", "Club B", "2023-01-01", 800000, "Active")
        )
        `when`(transfermarktClient.getPlayersForClub(clubId)).thenReturn(players)

        fetchClubPlayersJob.fetchAndSavePlayers(clubId)

        val savedPlayers = em.createQuery("SELECT p FROM PlayerEntity p", PlayerEntity::class.java).resultList
        assertEquals(2, savedPlayers.size)
        assertEquals(players.map { it.id }, savedPlayers.map { it.id })

        val processedPlayers = processedPlayerRepository.findAll().list<ProcessedPlayerEntity>()
        assertEquals(2, processedPlayers.size)
        assertEquals(players.map { it.id }, processedPlayers.map { it.playerId })

        val jobState = jobStateRepository.findAll().list<JobStateEntity>()
        assertEquals(1, jobState.size)
        assertEquals("COMPLETED", jobState[0].status)
    }

    @Test
    @Transactional
    fun `test fetchPlayers integration - with existing job state`() {
        val clubId = 5
        val players = listOf(
            Player("1", "Player 1", "Forward", "1990-01-01", 30, listOf("Country 1"), 180, "Right", "2020-01-01", "Club A", "2023-01-01", 1000000, "Active"),
            Player("2", "Player 2", "Midfielder", "1992-01-01", 28, listOf("Country 2"), 175, "Left", "2020-01-01", "Club B", "2023-01-01", 800000, "Active"),
            Player("3", "Player 3", "Defender", "1995-01-01", 25, listOf("Country 3"), 185, "Left", "2020-01-01", "Club C", "2023-01-01", 700000, "Active")
        )
        `when`(transfermarktClient.getPlayersForClub(clubId)).thenReturn(players)

        val jobState = jobStateRepository.startNewJob()
        playerRepository.save(players[0])
        playerRepository.save(players[1])
        processedPlayerRepository.save(players[0].id, jobState)
        processedPlayerRepository.save(players[1].id, jobState)

        fetchClubPlayersJob.fetchAndSavePlayers(clubId)

        val savedPlayers = em.createQuery("SELECT p FROM PlayerEntity p", PlayerEntity::class.java).resultList
        assertEquals(3, savedPlayers.size)
        assertEquals(players.map { it.id }, savedPlayers.map { it.id })

        val processedPlayers = processedPlayerRepository.findAll().list<ProcessedPlayerEntity>()
        assertEquals(3, processedPlayers.size)
        assertEquals(players.map { it.id }, processedPlayers.map { it.playerId })

        val jobStates = jobStateRepository.findAll().list<JobStateEntity>()
        assertEquals(1, jobStates.size)
        assertEquals("COMPLETED", jobStates[0].status)
    }

    @Test
    @Transactional
    fun `test fetchPlayers integration - interruption and resume before first player`() {
        val clubId = 5
        val players = listOf(
            Player("1", "Player 1", "Forward", "1990-01-01", 30, listOf("Country 1"), 180, "Right", "2020-01-01", "Club A", "2023-01-01", 1000000, "Active"),
            Player("2", "Player 2", "Midfielder", "1992-01-01", 28, listOf("Country 2"), 175, "Left", "2020-01-01", "Club B", "2023-01-01", 800000, "Active"),
            Player("3", "Player 3", "Defender", "1995-01-01", 25, listOf("Country 3"), 185, "Left", "2020-01-01", "Club C", "2023-01-01", 700000, "Active")
        )
        `when`(transfermarktClient.getPlayersForClub(clubId)).thenReturn(players)

        val jobState = jobStateRepository.startNewJob()

        simulateJobInterruption(players, jobState, interruptAfter = 0)

        verifyPartialProcessing(emptyList())

        fetchClubPlayersJob.fetchAndSavePlayers(clubId)

        verifyCompleteProcessing(players)
    }

    @Test
    @Transactional
    fun `test fetchPlayers integration - interruption and resume after first player`() {
        val clubId = 5
        val players = listOf(
            Player("1", "Player 1", "Forward", "1990-01-01", 30, listOf("Country 1"), 180, "Right", "2020-01-01", "Club A", "2023-01-01", 1000000, "Active"),
            Player("2", "Player 2", "Midfielder", "1992-01-01", 28, listOf("Country 2"), 175, "Left", "2020-01-01", "Club B", "2023-01-01", 800000, "Active"),
            Player("3", "Player 3", "Defender", "1995-01-01", 25, listOf("Country 3"), 185, "Left", "2020-01-01", "Club C", "2023-01-01", 700000, "Active")
        )
        `when`(transfermarktClient.getPlayersForClub(clubId)).thenReturn(players)

        val jobState = jobStateRepository.startNewJob()

        simulateJobInterruption(players, jobState, interruptAfter = 1)

        verifyPartialProcessing(players.subList(0, 1))

        fetchClubPlayersJob.fetchAndSavePlayers(clubId)

        verifyCompleteProcessing(players)
    }

    @Test
    @Transactional
    fun `test fetchPlayers integration - interruption and resume after last player`() {
        val clubId = 5
        val players = listOf(
            Player("1", "Player 1", "Forward", "1990-01-01", 30, listOf("Country 1"), 180, "Right", "2020-01-01", "Club A", "2023-01-01", 1000000, "Active"),
            Player("2", "Player 2", "Midfielder", "1992-01-01", 28, listOf("Country 2"), 175, "Left", "2020-01-01", "Club B", "2023-01-01", 800000, "Active"),
            Player("3", "Player 3", "Defender", "1995-01-01", 25, listOf("Country 3"), 185, "Left", "2020-01-01", "Club C", "2023-01-01", 700000, "Active")
        )
        `when`(transfermarktClient.getPlayersForClub(clubId)).thenReturn(players)

        val jobState = jobStateRepository.startNewJob()

        simulateJobInterruption(players, jobState, interruptAfter = players.size)

        fetchClubPlayersJob.fetchAndSavePlayers(clubId)

        verifyCompleteProcessing(players)
    }

    @Test
    @Transactional
    fun `test fetchPlayers integration - interruption and resume after second player`() {
        val clubId = 5
        val players = listOf(
            Player("1", "Player 1", "Forward", "1990-01-01", 30, listOf("Country 1"), 180, "Right", "2020-01-01", "Club A", "2023-01-01", 1000000, "Active"),
            Player("2", "Player 2", "Midfielder", "1992-01-01", 28, listOf("Country 2"), 175, "Left", "2020-01-01", "Club B", "2023-01-01", 800000, "Active"),
            Player("3", "Player 3", "Defender", "1995-01-01", 25, listOf("Country 3"), 185, "Left", "2020-01-01", "Club C", "2023-01-01", 700000, "Active")
        )
        `when`(transfermarktClient.getPlayersForClub(clubId)).thenReturn(players)

        val jobState = jobStateRepository.startNewJob()

        simulateJobInterruption(players, jobState, interruptAfter = 2)

        verifyPartialProcessing(players.subList(0, 2))

        fetchClubPlayersJob.fetchAndSavePlayers(clubId)

        verifyCompleteProcessing(players)
    }

    @Test
    @Transactional
    fun `test fetchPlayers integration - error in retrieve player API`() {
        val clubId = 5

        `when`(transfermarktClient.getPlayersForClub(clubId)).thenThrow(WebException("API error"))

        val exception = assertThrows(WebException::class.java) {
            fetchClubPlayersJob.fetchAndSavePlayers(clubId)
        }

        assertEquals("API error", exception.message)

        assertEquals(0, playerRepository.findAll().list<PlayerEntity>().size)
        assertEquals(0, processedPlayerRepository.findAll().list<ProcessedPlayerEntity>().size)
        assertEquals(1, jobStateRepository.findAll().list<JobStateEntity>().size)
    }

    private fun simulateJobInterruption(players: List<Player>, jobState: JobStateEntity, interruptAfter: Int) {
        try {
            for (i in 0 until interruptAfter) {
                fetchClubPlayersJob.savePlayerWithTransaction(players[i], jobState)
            }
            if (interruptAfter < players.size) {
                throw RuntimeException("Simulated job interruption")
            }
        } catch (e: RuntimeException) {
            assertEquals("Simulated job interruption", e.message)
        }
    }

    private fun verifyPartialProcessing(players: List<Player>) {
        val savedPlayers = em.createQuery("SELECT p FROM PlayerEntity p", PlayerEntity::class.java).resultList
        assertEquals(players.size, savedPlayers.size)
        assertEquals(players.map { it.id }, savedPlayers.map { it.id })

        val jobStates = jobStateRepository.findAll().list<JobStateEntity>()
        assertEquals(1, jobStates.size)
        assertEquals("IN_PROGRESS", jobStates[0].status)
    }

    private fun verifyCompleteProcessing(players: List<Player>) {
        val savedPlayers = em.createQuery("SELECT p FROM PlayerEntity p", PlayerEntity::class.java).resultList
        assertEquals(players.size, savedPlayers.size)
        assertEquals(players.map { it.id }, savedPlayers.map { it.id })

        val processedPlayers = processedPlayerRepository.findAll().list<ProcessedPlayerEntity>()
        assertEquals(players.size, processedPlayers.size)
        assertEquals(players.map { it.id }, processedPlayers.map { it.playerId })

        val jobStates = jobStateRepository.findAll().list<JobStateEntity>()
        assertEquals(1, jobStates.size)
        assertEquals("COMPLETED", jobStates[0].status)
    }
}