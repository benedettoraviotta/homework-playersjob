package io.playersjob.application.services

import io.playersjob.adapters.persistence.JpaJobStateRepository
import io.playersjob.adapters.persistence.JpaPlayerRepository
import io.playersjob.adapters.persistence.JpaProcessedPlayerRepository
import io.playersjob.adapters.persistence.entities.JobStateEntity
import io.playersjob.adapters.persistence.entities.PlayerEntity
import io.playersjob.adapters.persistence.entities.ProcessedPlayerEntity
import io.playersjob.adapters.web.exceptions.WebException
import io.playersjob.adapters.web.transfermarkt.TransfermarktClient
import io.playersjob.core.domain.Player
import io.quarkus.test.InjectMock
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Assertions.*
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
    fun `test fetchPlayers integration - empty player list from API`() {
        val clubId = 5

        `when`(transfermarktClient.getPlayersForClub(clubId)).thenReturn(emptyList())

        fetchClubPlayersJob.fetchAndSavePlayers(clubId)

        assertNull(playerRepository.findPlayerById("1"))
        assertEquals(0, processedPlayerRepository.findAll().list<ProcessedPlayerEntity>().size)
        assertEquals(0L, em.createQuery("SELECT count(p) FROM PlayerEntity p").singleResult)

        val jobStates = jobStateRepository.findAll().list<JobStateEntity>()
        assertEquals(1, jobStates.size)
        assertEquals("COMPLETED", jobStates[0].status)
    }

    @Test
    @Transactional
    fun `test fetchPlayers integration - API throws exception`() {
        val clubId = 5

        `when`(transfermarktClient.getPlayersForClub(clubId)).thenThrow(WebException("API error"))

        val exception = assertThrows(WebException::class.java) {
            fetchClubPlayersJob.fetchAndSavePlayers(clubId)
        }

        assertEquals("API error", exception.message)
        assertNull(playerRepository.findPlayerById("1"))
        assertEquals(0, processedPlayerRepository.findAll().list<ProcessedPlayerEntity>().size)
        assertEquals(0L, em.createQuery("SELECT count(p) FROM PlayerEntity p").singleResult)

        val jobStates = jobStateRepository.findAll().list<JobStateEntity>()
        assertEquals(0, jobStates.size)
    }
}