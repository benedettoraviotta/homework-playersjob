package io.playersjob.adapters.web.transfermarkt

import io.playersjob.adapters.web.exceptions.NetworkException
import io.playersjob.adapters.web.exceptions.ServerException
import io.playersjob.adapters.web.exceptions.ValidationException
import io.playersjob.core.domain.Player
import io.playersjob.core.ports.PlayerProvider
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.ProcessingException
import jakarta.ws.rs.WebApplicationException
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.slf4j.LoggerFactory

@ApplicationScoped
class TransfermarktClient @Inject constructor(
    @RestClient private val restClient: TransfermarktRestClient
) : PlayerProvider {

    private val logger = LoggerFactory.getLogger(TransfermarktClient::class.java)

    override fun getPlayersForClub(clubId: Int): List<Player> {
        try {
            val transfermarktPlayers = restClient.getPlayers(clubId).players
            return transfermarktPlayers.map {
                Player(
                    id = it.id,
                    name = it.name,
                    position = it.position,
                    dateOfBirth = it.dateOfBirth,
                    age = it.age,
                    nationality = it.nationality,
                    height = it.height,
                    foot = it.foot,
                    joinedOn = it.joinedOn,
                    signedFrom = it.signedFrom,
                    contract = it.contract, it.marketValue, it.status
                )
            }
        } catch (e: ProcessingException) {
            logger.error("Network error", e)
            throw NetworkException("Network error occurred while fetching players")
        } catch (e: WebApplicationException) {
            logger.error("HTTP error", e)
            when (e.response.status) {
                500 -> throw ServerException("Server error occurred while fetching players")
                422 -> {
                    val errorMsg = e.response.readEntity(String::class.java)
                    throw ValidationException("Validation error occurred while fetching players: $errorMsg")
                }
                else -> throw e
            }
        } catch (e: Exception) {
            logger.error("Generic error", e)
            throw e
        }
    }
}