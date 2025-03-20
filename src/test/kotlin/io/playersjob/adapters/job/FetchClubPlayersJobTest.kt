package io.playersjob.adapters.job

import io.playersjob.application.services.PlayerFetcher
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

class FetchClubPlayersJobTest {

    @Mock
    lateinit var playerProvider: PlayerProvider

    @Mock
    lateinit var playerRepository: PlayerRepository

    @Mock
    lateinit var jobStateRepository: JobStateRepository

    @InjectMocks
    lateinit var playerFetcher: PlayerFetcher

    @InjectMocks
    lateinit var fetchClubPlayersJob: FetchClubPlayersJob

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `test fetchPlayers integration`() {
        val clubId = 5
        val players = listOf(
            Player("1", "Player 1", "Forward", "1990-01-01", 30, listOf("Country 1"), 180, "Right", "2020-01-01", "Club A", "2023-01-01", 1000000),
            Player("2", "Player 2", "Midfielder", "1992-01-01", 28, listOf("Country 2"), 175, "Left", "2020-01-01", "Club B", "2023-01-01", 800000)
        )

        `when`(jobStateRepository.getLastJobState()).thenReturn(Uni.createFrom().nullItem())
        `when`(playerProvider.getPlayersForClub(clubId)).thenReturn(Uni.createFrom().item(players))
        `when`(playerRepository.save(any(Player::class.java))).thenReturn(Uni.createFrom().voidItem())
        `when`(jobStateRepository.save(any(JobState::class.java))).thenReturn(Uni.createFrom().voidItem())

        fetchClubPlayersJob.fetchPlayers(clubId)

        verify(jobStateRepository).getLastJobState()
        verify(playerProvider).getPlayersForClub(clubId)
        verify(playerRepository, times(players.size)).save(any(Player::class.java))
        verify(jobStateRepository, times(players.size)).save(any(JobState::class.java))
    }

    @Test
    fun `test fetchPlayers with existing job state`() {
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

        fetchClubPlayersJob.fetchPlayers(clubId)

        verify(jobStateRepository).getLastJobState()
        verify(playerProvider).getPlayersForClub(clubId)
        verify(playerRepository, times(players.size - 1)).save(any(Player::class.java))
        verify(jobStateRepository, times(players.size - 1)).save(any(JobState::class.java))
    }

    @Test
    fun `test fetchPlayers with empty player list`() {
        val clubId = 5
        val players = emptyList<Player>()

        `when`(jobStateRepository.getLastJobState()).thenReturn(Uni.createFrom().nullItem())
        `when`(playerProvider.getPlayersForClub(clubId)).thenReturn(Uni.createFrom().item(players))

        fetchClubPlayersJob.fetchPlayers(clubId)

        verify(jobStateRepository).getLastJobState()
        verify(playerProvider).getPlayersForClub(clubId)
        verify(playerRepository, never()).save(any(Player::class.java))
        verify(jobStateRepository, never()).save(any(JobState::class.java))
    }

    @Test
    fun `test fetchPlayers with network error`() {
        val clubId = 5

        `when`(jobStateRepository.getLastJobState()).thenReturn(Uni.createFrom().nullItem())
        `when`(playerProvider.getPlayersForClub(clubId)).thenReturn(Uni.createFrom().failure(RuntimeException("Network error")))

        try {
            fetchClubPlayersJob.fetchPlayers(clubId)
        } catch (e: Exception) {
            assert(e.message == "Network error")
        }

        verify(jobStateRepository).getLastJobState()
        verify(playerProvider).getPlayersForClub(clubId)
        verify(playerRepository, never()).save(any(Player::class.java))
        verify(jobStateRepository, never()).save(any(JobState::class.java))
    }

    @Test
    fun `test fetchPlayers with save error`() {
        val clubId = 5
        val players = listOf(
            Player("1", "Player 1", "Forward", "1990-01-01", 30, listOf("Country 1"), 180, "Right", "2020-01-01", "Club A", "2023-01-01", 1000000)
        )

        `when`(jobStateRepository.getLastJobState()).thenReturn(Uni.createFrom().nullItem())
        `when`(playerProvider.getPlayersForClub(clubId)).thenReturn(Uni.createFrom().item(players))
        `when`(playerRepository.save(any(Player::class.java))).thenReturn(Uni.createFrom().failure(RuntimeException("Save error")))

        try {
            fetchClubPlayersJob.fetchPlayers(clubId)
        } catch (e: Exception) {
            assert(e.message == "Save error")
        }

        verify(jobStateRepository).getLastJobState()
        verify(playerProvider).getPlayersForClub(clubId)
        verify(playerRepository).save(any(Player::class.java))
        verify(jobStateRepository, never()).save(any(JobState::class.java))
    }
}