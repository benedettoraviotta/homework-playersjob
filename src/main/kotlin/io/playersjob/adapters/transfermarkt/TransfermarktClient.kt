package io.playersjob.adapters.transfermarkt

import io.playersjob.adapters.transfermarkt.exceptions.NetworkException
import io.playersjob.adapters.transfermarkt.exceptions.ServerException
import io.playersjob.adapters.transfermarkt.exceptions.ValidationException
import io.playersjob.core.domain.Player
import io.playersjob.core.ports.PlayerProvider
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.ProcessingException
import jakarta.ws.rs.WebApplicationException
import org.eclipse.microprofile.rest.client.inject.RestClient

@ApplicationScoped
class TransfermarktClient @Inject constructor(
    @RestClient private val restClient: TransfermarktRestClient
) : PlayerProvider {

    override fun getPlayersForClub(clubId: Int): Uni<List<Player>> {
        return restClient.getPlayers(clubId)
            .onFailure().retry().atMost(3)
            .onFailure().recoverWithUni { throwable ->
                when (throwable) {
                    is ProcessingException -> Uni.createFrom().failure(NetworkException("Network error occurred while fetching players"))
                    is WebApplicationException -> {
                        when (throwable.response.status) {
                            500 -> Uni.createFrom().failure(ServerException("Server error occurred while fetching players"))
                            422 -> {
                                val errorMsg = throwable.response.readEntity(String::class.java)
                                Uni.createFrom().failure(ValidationException("Validation error occurred while fetching players: $errorMsg"))
                            }
                            else -> Uni.createFrom().failure(throwable)
                        }
                    }
                    else -> Uni.createFrom().failure(throwable)
                }
            }
            .map { transfermarktPlayers ->
                transfermarktPlayers.map {
                    Player(
                        it.id, it.name, it.position, it.dateOfBirth, it.age, it.nationality,
                        it.height, it.foot, it.joinedOn, it.signedFrom, it.contract, it.marketValue, it.status
                    )
                }
            }
    }
}