package io.playersjob.adapters.persistence

import io.playersjob.adapters.persistence.entities.JpaPlayerEntity
import io.playersjob.core.domain.Player
import io.playersjob.core.ports.PlayerRepository
import io.quarkus.hibernate.reactive.panache.Panache
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory

@ApplicationScoped
class JpaPlayerRepository @Inject constructor(
    private val em: EntityManager
) : PlayerRepository {

    private val logger = LoggerFactory.getLogger(JpaPlayerRepository::class.java)

    @Transactional
    override fun save(player: Player): Uni<Void> {
        logger.debug("Saving player with id -> {} and name -> {}", player.id, player.name)
        return Panache.withTransaction {
            val entity = JpaPlayerEntity()
            entity.id = player.id
            entity.name = player.name
            entity.position = player.position
            entity.dateOfBirth = player.dateOfBirth
            entity.age = player.age
            entity.nationality = player.nationality.toMutableList()
            entity.marketValue = player.marketValue
            entity.height = player.height
            entity.foot = player.foot
            entity.joinedOn = player.joinedOn
            entity.signedFrom = player.signedFrom
            entity.contract = player.contract
            entity.status = player.status

            em.merge(entity)
            Uni.createFrom().voidItem()
        }
    }

    @Transactional
    override fun findLastProcessedPlayer(): Uni<Player?> {
        logger.debug("Retrieve last processed player...")
        return Uni.createFrom().item {
            try {
                val query = em.createQuery(
                    "SELECT p FROM JpaPlayerEntity p ORDER BY p.id DESC",
                    JpaPlayerEntity::class.java
                )
                query.maxResults = 1
                val result = query.resultList.firstOrNull()

                result?.let {
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
            } catch (e: Exception) {
                logger.error("Error executing JPQL query", e)
                null
            }
        }
    }
}