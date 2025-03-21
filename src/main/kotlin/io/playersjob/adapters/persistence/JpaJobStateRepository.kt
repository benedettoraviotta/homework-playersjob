package io.playersjob.adapters.persistence

import io.playersjob.adapters.persistence.entities.JobStateEntity
import io.playersjob.core.ports.JobStateRepository
import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

@ApplicationScoped
class JpaJobStateRepository : JobStateRepository, PanacheRepository<JobStateEntity> {

    private val logger = LoggerFactory.getLogger(JpaJobStateRepository::class.java)

    @Transactional
    override fun startNewJob(): JobStateEntity {
        val jobState = JobStateEntity(status = "IN_PROGRESS")
        persist(jobState)
        logger.debug("Create new job with id {}", jobState.id)
        return jobState
    }

    @Transactional
    override fun completeJob(jobState: JobStateEntity) {
        logger.debug("Set job {} to COMPLETED", jobState.id)
        jobState.status = "COMPLETED"
        jobState.endTime = LocalDateTime.now()
        persist(jobState)
    }

    @Transactional
    override fun getLastJobState(): JobStateEntity? {
        logger.debug("Search last \"IN PROGRESS\" job state")
        return find("status", "IN_PROGRESS").firstResult()
    }
}