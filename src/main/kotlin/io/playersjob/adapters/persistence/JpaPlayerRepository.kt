package io.playersjob.adapters.persistence

import io.playersjob.adapters.persistence.entities.PlayerEntity
import io.playersjob.core.domain.Player
import io.playersjob.core.ports.PlayerRepository
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory

@ApplicationScoped
class JpaPlayerRepository : PanacheRepository<PlayerEntity>, PlayerRepository {

    private val logger = LoggerFactory.getLogger(JpaPlayerRepository::class.java)

    @Transactional
    override fun save(player: Player) {
        logger.debug("Saving player with id -> {} and name -> {}", player.id, player.name)
        persist(toJpaEntity(player))
    }

    @Transactional
    override fun findPlayerById(playerId: String): Player? {
        logger.debug("Search player with id {}", playerId)
            val entity = find("id", playerId).firstResult()
            return entity?.let {toCoreDomain(entity)}
    }

    private fun toJpaEntity(player: Player): PlayerEntity {
        return PlayerEntity().apply {
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
    }
    
    private fun toCoreDomain(playerEntity: PlayerEntity): Player {
        return Player(
            id = playerEntity.id,
            name = playerEntity.name,
            position = playerEntity.position,
            dateOfBirth = playerEntity.dateOfBirth,
            age = playerEntity.age,
            nationality = playerEntity.nationality,
            marketValue = playerEntity.marketValue,
            height = playerEntity.height,
            foot = playerEntity.foot,
            joinedOn = playerEntity.joinedOn,
            signedFrom = playerEntity.signedFrom,
            contract = playerEntity.contract,
            status = playerEntity.status
        )
    }
}