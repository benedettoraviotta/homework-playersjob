package io.playersjob.adapters.web.transfermarkt

import io.playersjob.adapters.dto.TransfermarktPlayer
import io.playersjob.adapters.dto.TransfermarktResponse
import io.playersjob.adapters.web.exceptions.NetworkException
import io.playersjob.adapters.web.exceptions.ServerException
import io.playersjob.adapters.web.exceptions.ValidationException
import jakarta.ws.rs.ProcessingException
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
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
        val transfermarktResponse = TransfermarktResponse(
            updatedAt = "2025-03-20:10:20:0000",
            id = "id_1",
            players = transfermarktPlayers
        )

        `when`(restClient.getPlayers(clubId)).thenReturn(transfermarktResponse)

        val players = transfermarktClient.getPlayersForClub(clubId)

        verify(restClient).getPlayers(clubId)
        assertEquals(transfermarktPlayers.size, players.size)
    }

    @Test
    fun `test getPlayersForClub returns empty list`() {
        val clubId = 5
        val transfermarktResponse = TransfermarktResponse(
            updatedAt = "2025-03-20:10:20:0000",
            id = "id_1",
            players = emptyList()
        )
        `when`(restClient.getPlayers(clubId)).thenReturn(transfermarktResponse)

        val players = transfermarktClient.getPlayersForClub(clubId)

        verify(restClient).getPlayers(clubId)
        assertEquals(0, players.size)
    }

    @Test
    fun `test getPlayersForClub network error`() {
        val clubId = 5
        `when`(restClient.getPlayers(clubId)).thenThrow(ProcessingException("Network error"))

        val exception = assertThrows(NetworkException::class.java) {
            transfermarktClient.getPlayersForClub(clubId)
        }

        verify(restClient).getPlayers(clubId)
        assertEquals("Network error occurred while fetching players", exception.message)
    }

    @Test
    fun `test getPlayersForClub server error`() {
        val clubId = 5

        val response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("").build()
        val exception = WebApplicationException(response)

        `when`(restClient.getPlayers(clubId)).thenThrow(exception)

        val thrownException = assertThrows(ServerException::class.java) {
            transfermarktClient.getPlayersForClub(clubId)
        }

        verify(restClient).getPlayers(clubId)
        assertEquals("Server error occurred while fetching players", thrownException.message)
    }


    @Test
    fun `test getPlayersForClub validation error`() {
        val clubId = 5
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

        val response = Response.status(422).entity(errorJson).build()
        val exception = WebApplicationException(response)

        `when`(restClient.getPlayers(clubId)).thenThrow(exception)

        val result = assertThrows(ValidationException::class.java) {
            transfermarktClient.getPlayersForClub(clubId)
        }

        verify(restClient).getPlayers(clubId)
        assertEquals("Validation error occurred while fetching players: $errorJson", result.message)
    }
}