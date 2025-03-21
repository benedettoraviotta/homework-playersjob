package io.playersjob.adapters.persistence

import io.playersjob.adapters.persistence.entities.ProcessedPlayerEntity
import io.playersjob.adapters.persistence.entities.JobStateEntity
import io.playersjob.core.ports.ProcessedPlayerRepository
import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional

@ApplicationScoped
class JpaProcessedPlayerRepository : ProcessedPlayerRepository, PanacheRepository<ProcessedPlayerEntity> {
    override fun existById(id: String): Boolean {
        return find("playerId", id).firstResult<ProcessedPlayerEntity>() != null
    }

    @Transactional
    override fun save(id: String, jobState: JobStateEntity) {
        persist(ProcessedPlayerEntity(playerId = id, jobState = jobState))
    }

    @Transactional
    override fun findByJobState(jobState: JobStateEntity): List<ProcessedPlayerEntity> {
        return find("jobState", jobState).list()
    }
}