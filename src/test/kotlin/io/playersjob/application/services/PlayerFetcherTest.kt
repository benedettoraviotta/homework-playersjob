package io.playersjob.application.services

import io.playersjob.core.domain.JobState
import io.playersjob.core.domain.Player
import io.playersjob.core.ports.JobStateRepository
import io.playersjob.core.ports.PlayerProvider
import io.playersjob.core.ports.PlayerRepository
import io.smallrye.mutiny.Uni
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class PlayerFetcherTest {

    @Mock
    lateinit var playerProvider: PlayerProvider

    @Mock
    lateinit var playerRepository: PlayerRepository

    @Mock
    lateinit var jobStateRepository: JobStateRepository

    @InjectMocks
    lateinit var playerFetcher: PlayerFetcher

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `test fetchAndSavePlayers with no previous job state`() {
        val clubId = 5
        val players = listOf(
            Player("1", "Player 1", "Forward", "1990-01-01", 30, listOf("Country 1"), 180, "Right", "2020-01-01", "Club A", "2023-01-01", 1000000),
            Player("2", "Player 2", "Midfielder", "1992-01-01", 28, listOf("Country 2"), 175, "Left", "2020-01-01", "Club B", "2023-01-01", 800000)
        )

        `when`(jobStateRepository.getLastJobState()).thenReturn(Uni.createFrom().nullItem())
        `when`(playerProvider.getPlayersForClub(clubId)).thenReturn(Uni.createFrom().item(players))
        `when`(playerRepository.save(any(Player::class.java))).thenReturn(Uni.createFrom().voidItem())
        `when`(jobStateRepository.save(any(JobState::class.java))).thenReturn(Uni.createFrom().voidItem())

        playerFetcher.fetchAndSavePlayers(clubId).await().indefinitely()

        verify(jobStateRepository).getLastJobState()
        verify(playerProvider).getPlayersForClub(clubId)
        verify(playerRepository, times(players.size)).save(any(Player::class.java))
        verify(jobStateRepository, times(players.size)).save(any(JobState::class.java))
    }

    @Test
    fun `test fetchAndSavePlayers with previous job state`() {
        val clubId = 5
        val lastJobState = JobState("1")
        val players = listOf(
            Player("1", "Player 1", "Forward", "1990-01-01", 30, listOf("Country 1"), 180, "Right", "2020-01-01", "Club A", "2023-01-01", 1000000),
            Player("2", "Player 2", "Midfielder", "1992-01-01", 28, listOf("Country 2"), 175, "Left", "2020-01-01", "Club B", "2023-01-01", 800000)
        )

        `when`(jobStateRepository.getLastJobState()).thenReturn(Uni.createFrom().item(lastJobState))
        `when`(playerProvider.getPlayersForClub(clubId)).thenReturn(Uni.createFrom().item(players))
        `when`(playerRepository.save(any(Player::class.java))).thenReturn(Uni.createFrom().voidItem())
        `when`(jobStateRepository.save(any(JobState::class.java))).thenReturn(Uni.createFrom().voidItem())

        playerFetcher.fetchAndSavePlayers(clubId).await().indefinitely()

        verify(jobStateRepository).getLastJobState()
        verify(playerProvider).getPlayersForClub(clubId)
        verify(playerRepository, times(players.size - 1)).save(any(Player::class.java))
        verify(jobStateRepository, times(players.size - 1)).save(any(JobState::class.java))
    }
}