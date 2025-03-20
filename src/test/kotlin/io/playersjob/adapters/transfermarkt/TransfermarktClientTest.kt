package io.playersjob.adapters.transfermarkt

import io.playersjob.adapters.dto.TransfermarktPlayer
import io.playersjob.adapters.transfermarkt.exceptions.NetworkException
import io.playersjob.adapters.transfermarkt.exceptions.ServerException
import io.playersjob.adapters.transfermarkt.exceptions.ValidationException
import io.smallrye.mutiny.Uni
import jakarta.ws.rs.ProcessingException
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class TransfermarktClientTest {

    @Mock
    @RestClient
    lateinit var restClient: TransfermarktRestClient

    @InjectMocks
    lateinit var transfermarktClient: TransfermarktClient

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `test getPlayersForClub success`() {
        val clubId = 5
        val transfermarktPlayers = listOf(
            TransfermarktPlayer("1", "Player 1", "Forward", "1990-01-01", 30, listOf("Country 1"), 180, "Right", "2020-01-01", "Club A", "2023-01-01", 1000000),
            TransfermarktPlayer("2", "Player 2", "Midfielder", "1992-01-01", 28, listOf("Country 2"), 175, "Left", "2020-01-01", "Club B", "2023-01-01", 800000)
        )

        `when`(restClient.getPlayers(clubId)).thenReturn(Uni.createFrom().item(transfermarktPlayers))

        val players = transfermarktClient.getPlayersForClub(clubId).await().indefinitely()

        verify(restClient).getPlayers(clubId)
        assert(players.size == transfermarktPlayers.size)
    }

    @Test
    fun `test getPlayersForClub network error`() {
        val clubId = 5

        `when`(restClient.getPlayers(clubId)).thenReturn(Uni.createFrom().failure(ProcessingException("Network error")))

        try {
            transfermarktClient.getPlayersForClub(clubId).await().indefinitely()
        } catch (e: NetworkException) {
            assert(e.message == "Network error occurred while fetching players")
        }

        verify(restClient).getPlayers(clubId)
    }

    @Test
    fun `test getPlayersForClub server error`() {
        val clubId = 5
        val response = mock(Response::class.java)

        `when`(response.status).thenReturn(500)
        `when`(restClient.getPlayers(clubId)).thenReturn(Uni.createFrom().failure(WebApplicationException(response)))

        try {
            transfermarktClient.getPlayersForClub(clubId).await().indefinitely()
        } catch (e: ServerException) {
            assert(e.message == "Server error occurred while fetching players")
        }

        verify(restClient).getPlayers(clubId)
    }

    @Test
    fun `test getPlayersForClub validation error`() {
        val clubId = 5
        val response = mock(Response::class.java)
        val errorJson = """
            {
                "detail": [
                    {
                        "loc": ["string", 0],
                        "msg": "string",
                        "type": "string"
                    }
                ]
            }
        """

        `when`(response.status).thenReturn(422)
        `when`(response.readEntity(String::class.java)).thenReturn(errorJson)
        `when`(restClient.getPlayers(clubId)).thenReturn(Uni.createFrom().failure(WebApplicationException(response)))

        try {
            transfermarktClient.getPlayersForClub(clubId).await().indefinitely()
        } catch (e: ValidationException) {
            assert(e.message == "Validation error occurred while fetching players: $errorJson")
        }

        verify(restClient).getPlayers(clubId)
    }
}