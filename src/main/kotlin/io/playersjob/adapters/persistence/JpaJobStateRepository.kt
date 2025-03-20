package io.playersjob.adapters.persistence

import io.playersjob.adapters.persistence.entities.JpaJobStateEntity
import io.playersjob.core.domain.JobState
import io.playersjob.core.ports.JobStateRepository
import io.quarkus.hibernate.reactive.panache.Panache
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import org.slf4j.LoggerFactory

@ApplicationScoped
class JpaJobStateRepository @Inject constructor(
    private val em: EntityManager
) : JobStateRepository {

    private val logger = LoggerFactory.getLogger(JpaJobStateRepository::class.java)

    override fun save(jobState: JobState): Uni<Void> {
        logger.debug("Saving new job state! Last processed player id -> {}", jobState.lastProcessedPlayerId)
        return Panache.withTransaction {
            val entity = JpaJobStateEntity()
            entity.lastProcessedPlayerId = jobState.lastProcessedPlayerId

            em.merge(entity)
            Uni.createFrom().voidItem()
        }
    }

    override fun getLastJobState(): Uni<JobState?> {
        logger.debug("Retrieve last processed player...")
        return Uni.createFrom().item {
            try {
                val query = em.createQuery(
                    "SELECT j FROM JpaJobStateEntity j ORDER BY j.id DESC",
                    JpaJobStateEntity::class.java
                )
                query.maxResults = 1
                val result = query.resultList.first()
                JobState(result.lastProcessedPlayerId)

            } catch (e: Exception) {
                logger.error("Error executing JPQL query", e)
                null
            }
        }
    }
}