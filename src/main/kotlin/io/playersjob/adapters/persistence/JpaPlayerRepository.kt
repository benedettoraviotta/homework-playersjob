package io.playersjob.adapters.persistence

import io.playersjob.adapters.persistence.entities.PlayerEntity
import io.playersjob.adapters.persistence.exceptions.DBException
import io.playersjob.adapters.persistence.exceptions.PersistExeption
import io.playersjob.core.domain.Player
import io.playersjob.core.ports.PlayerRepository
import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.PersistenceException
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory

@ApplicationScoped
class JpaPlayerRepository : PanacheRepository<PlayerEntity>, PlayerRepository {

    private val logger = LoggerFactory.getLogger(JpaPlayerRepository::class.java)

    @Transactional
    override fun save(player: Player) {
        logger.debug("Saving player with id -> {} and name -> {}", player.id, player.name)
        val entity = PlayerEntity().apply {
            id = player.id
            name = player.name
            position = player.position
            dateOfBirth = player.dateOfBirth
            age = player.age
            nationality = player.nationality.toMutableList()
            marketValue = player.marketValue
            height = player.height
            foot = player.foot
            joinedOn = player.joinedOn
            signedFrom = player.signedFrom
            contract = player.contract
            status = player.status
        }

        try {
            persistAndFlush(entity)
        } catch (e: PersistenceException) {
            logger.error("Error executing JPQL query to save player", e)
            throw PersistExeption("Error executing JPQL query to save player: ${e.message}", cause = e)
        } catch (e: Exception) {
            logger.error("Unexpected error while saving player", e)
            throw DBException("Unexpected error while saving player", cause = e)
        }
    }

    @Transactional
    override fun findPlayerById(playerId: String): Player? {
        logger.debug("Search player with id {}", playerId)
        return try {
            val entity = find("id", playerId).firstResult<PlayerEntity>()
            entity?.let {
                Player(
                    id = it.id,
                    name = it.name,
                    position = it.position,
                    dateOfBirth = it.dateOfBirth,
                    age = it.age,
                    nationality = it.nationality,
                    marketValue = it.marketValue,
                    height = it.height,
                    foot = it.foot,
                    joinedOn = it.joinedOn,
                    signedFrom = it.signedFrom,
                    contract = it.contract,
                    status = it.status
                )
            }
        } catch (e: PersistenceException) {
            logger.error("Error executing JPQL query to fetch player by id", e)
            throw PersistExeption("Error executing JPQL query to fetch player by id: ${e.message}", cause = e)
        } catch (e: Exception) {
            logger.error("Unexpected error while fetching player by id", e)
            throw DBException("Unexpected error while fetching player by id: ${e.message}", cause = e)
        }
    }
}