package io.playersjob.adapters.persistence

import io.playersjob.adapters.persistence.entities.ProcessedPlayerEntity
import io.playersjob.adapters.persistence.entities.JobStateEntity
import io.playersjob.core.ports.ProcessedPlayerRepository
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory

@ApplicationScoped
class JpaProcessedPlayerRepository : ProcessedPlayerRepository, PanacheRepository<ProcessedPlayerEntity> {
    private val logger = LoggerFactory.getLogger(JpaProcessedPlayerRepository::class.java)

    override fun existById(id: String): Boolean {
        logger.debug("Verify if player with id {} was processed", id)
        return find("playerId", id).firstResult() != null
    }

    override fun save(id: String, jobState: JobStateEntity) {
        logger.debug("Saving processed player with id -> {} in job -> {}", id, jobState.id)
        persistAndFlush(ProcessedPlayerEntity(playerId = id, jobState = jobState))
    }

    override fun findByJobState(jobState: JobStateEntity): List<ProcessedPlayerEntity> {
        logger.debug("Search processed player in job state {}", jobState.id)
        return find("jobState", jobState).list()
    }
}