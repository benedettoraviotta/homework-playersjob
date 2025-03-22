package io.playersjob.application.services

import io.playersjob.adapters.persistence.JpaJobStateRepository
import io.playersjob.adapters.persistence.JpaPlayerRepository
import io.playersjob.adapters.persistence.JpaProcessedPlayerRepository
import io.playersjob.adapters.persistence.entities.JobStateEntity
import io.playersjob.adapters.web.exceptions.WebException
import io.playersjob.adapters.web.transfermarkt.TransfermarktClient
import io.playersjob.core.domain.Player
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import io.quarkus.test.InjectMock
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
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

    @InjectMock
    lateinit var transfermarktClient: TransfermarktClient

    val clubId = 5

    @BeforeEach
    @Transactional
    fun setUp() {
        playerRepository.deleteAll()
        processedPlayerRepository.deleteAll()
        jobStateRepository.deleteAll()
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    fun dbStateAlreadyPresent(): JobStateEntity {
        return jobStateRepository.startNewJob()
    }

    fun addPlayerAndProcessedPlayerToDb(player: Player, jobState: JobStateEntity) {
        playerRepository.save(player)
        processedPlayerRepository.save(player.id, jobState)
    }

    @Test
    @Transactional
    fun `test fetchPlayers integration - happy path`() {
        val players = listOf(
            Player("1", "Player 1", "Forward", "1990-01-01", 30, listOf("Country 1"), 180, "Right", "2020-01-01", "Club A", "2023-01-01", 1000000, "Active"),
            Player("2", "Player 2", "Midfielder", "1992-01-01", 28, listOf("Country 2"), 175, "Left", "2020-01-01", "Club B", "2023-01-01", 800000, "Active")
        )
        `when`(transfermarktClient.getPlayersForClub(clubId)).thenReturn(players)

        fetchClubPlayersJob.fetchAndSavePlayers(clubId)

        verifyCompleteProcessing(players)
    }

    @Test
    @Transactional
    fun `test fetchPlayers integration - with existing job state`() {
        val players = listOf(
            Player("1", "Player 1", "Forward", "1990-01-01", 30, listOf("Country 1"), 180, "Right", "2020-01-01", "Club A", "2023-01-01", 1000000, "Active"),
            Player("2", "Player 2", "Midfielder", "1992-01-01", 28, listOf("Country 2"), 175, "Left", "2020-01-01", "Club B", "2023-01-01", 800000, "Active"),
            Player("3", "Player 3", "Defender", "1995-01-01", 25, listOf("Country 3"), 185, "Left", "2020-01-01", "Club C", "2023-01-01", 700000, "Active")
        )
        `when`(transfermarktClient.getPlayersForClub(clubId)).thenReturn(players)

        val jobState = dbStateAlreadyPresent()
        addPlayerAndProcessedPlayerToDb(players[0], jobState)
        addPlayerAndProcessedPlayerToDb(players[1], jobState)

        fetchClubPlayersJob.fetchAndSavePlayers(clubId)

        verifyCompleteProcessing(players)
    }

    @Test
    @Transactional
    fun `test fetchPlayers integration - interruption and resume before first player`() {
        val players = listOf(
            Player("1", "Player 1", "Forward", "1990-01-01", 30, listOf("Country 1"), 180, "Right", "2020-01-01", "Club A", "2023-01-01", 1000000, "Active"),
            Player("2", "Player 2", "Midfielder", "1992-01-01", 28, listOf("Country 2"), 175, "Left", "2020-01-01", "Club B", "2023-01-01", 800000, "Active"),
            Player("3", "Player 3", "Defender", "1995-01-01", 25, listOf("Country 3"), 185, "Left", "2020-01-01", "Club C", "2023-01-01", 700000, "Active")
        )
        `when`(transfermarktClient.getPlayersForClub(clubId)).thenReturn(players)

        val jobState = dbStateAlreadyPresent()

        simulateJobInterruption(players, jobState, 0)

        verifyPartialProcessing(emptyList())

        fetchClubPlayersJob.fetchAndSavePlayers(clubId)

        verifyCompleteProcessing(players)
    }

    @Test
    @Transactional
    fun `test fetchPlayers integration - interruption and resume after first player`() {
        val players = listOf(
            Player("1", "Player 1", "Forward", "1990-01-01", 30, listOf("Country 1"), 180, "Right", "2020-01-01", "Club A", "2023-01-01", 1000000, "Active"),
            Player("2", "Player 2", "Midfielder", "1992-01-01", 28, listOf("Country 2"), 175, "Left", "2020-01-01", "Club B", "2023-01-01", 800000, "Active"),
            Player("3", "Player 3", "Defender", "1995-01-01", 25, listOf("Country 3"), 185, "Left", "2020-01-01", "Club C", "2023-01-01", 700000, "Active")
        )
        `when`(transfermarktClient.getPlayersForClub(clubId)).thenReturn(players)

        val jobState = dbStateAlreadyPresent()

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

        val jobState = dbStateAlreadyPresent()

        simulateJobInterruption(players, jobState, interruptAfter = players.size)

        verifyPartialProcessing(players)

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

        val jobState = dbStateAlreadyPresent()

        simulateJobInterruption(players, jobState, interruptAfter = 2)

        verifyPartialProcessing(players.subList(0, 2))

        fetchClubPlayersJob.fetchAndSavePlayers(clubId)

        verifyCompleteProcessing(players)
    }

    @Test
    fun `test fetchPlayers integration - error in retrieve player API`() {
        `when`(transfermarktClient.getPlayersForClub(clubId)).thenThrow(WebException("API error"))

        val exception = assertThrows(WebException::class.java) {
            fetchClubPlayersJob.fetchAndSavePlayers(clubId)
        }

        assertEquals("API error", exception.message)

        assertEquals(0, playerRepository.findAll().list().size)
        assertEquals(0, processedPlayerRepository.findAll().list().size)
        assertEquals(0, jobStateRepository.findAll().list().size)
    }

    private fun simulateJobInterruption(players: List<Player>, jobState: JobStateEntity, interruptAfter: Int) {
        try {
            for (i in 0 until interruptAfter) {
                addPlayerAndProcessedPlayerToDb(players[i], jobState)
            }
            if (interruptAfter < players.size) {
                throw RuntimeException("Simulated job interruption")
            }
        } catch (e: RuntimeException) {
            assertEquals("Simulated job interruption", e.message)
        }
    }

    private fun <T> flushAndClear(repository: T) {
        val entityManager = (repository as PanacheRepository<*>).getEntityManager()
        entityManager.flush()
        entityManager.clear()
    }

    private fun verifyPlayers(players: List<Player>) {
        val savedPlayers = playerRepository.findAll().list()
        val savedPlayerIds = savedPlayers.map { it.id }.toSet()

        assertEquals(players.size, savedPlayers.size)
        assertTrue(savedPlayerIds.containsAll(players.map { it.id }))

        val processedPlayers = processedPlayerRepository.findAll().list()
        val processedPlayerIds = processedPlayers.map { it.playerId }.toSet()

        assertEquals(players.size, processedPlayers.size)
        assertTrue(processedPlayerIds.containsAll(players.map { it.id }))
    }

    private fun verifyPartialProcessing(players: List<Player>) {
        flushAndClear(playerRepository)
        flushAndClear(processedPlayerRepository)
        flushAndClear(jobStateRepository)

        verifyPlayers(players)

        val jobStates = jobStateRepository.findAll().list()
        assertEquals(1, jobStates.size)
        assertEquals("IN_PROGRESS", jobStates[0].status)
    }

    private fun verifyCompleteProcessing(players: List<Player>) {
        flushAndClear(playerRepository)
        flushAndClear(processedPlayerRepository)
        flushAndClear(jobStateRepository)

        verifyPlayers(players)

        val jobStates = jobStateRepository.findAll().list()
        assertEquals(1, jobStates.size)
        assertEquals("COMPLETED", jobStates[0].status)
    }




}