package io.playersjob.adapters.persistence

import io.playersjob.adapters.persistence.entities.JobStateEntity
import io.playersjob.core.ports.JobStateRepository
import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.time.LocalDateTime

@ApplicationScoped
class JpaJobStateRepository : JobStateRepository, PanacheRepository<JobStateEntity> {

    @Transactional
    override fun startNewJob(): JobStateEntity {
        val jobState = JobStateEntity(status = "IN_PROGRESS")
        persist(jobState)
        return jobState
    }

    @Transactional
    override fun completeJob(jobState: JobStateEntity) {
        jobState.status = "COMPLETED"
        jobState.endTime = LocalDateTime.now()
        persist(jobState)
    }

    @Transactional
    override fun getLastJobState(): JobStateEntity? {
        return find("status", "IN_PROGRESS").firstResult()
    }
}