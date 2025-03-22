package io.playersjob.adapters.persistence

import io.playersjob.adapters.persistence.entities.JobStateEntity
import io.playersjob.core.ports.JobStateRepository
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

@ApplicationScoped
class JpaJobStateRepository : JobStateRepository, PanacheRepository<JobStateEntity> {

    private val logger = LoggerFactory.getLogger(JpaJobStateRepository::class.java)

    override fun startNewJob(): JobStateEntity {
        val jobState = JobStateEntity()
        setJobState(jobState, "IN_PROGRESS")
        logger.debug("Create new job with id {}", jobState.id)
        return jobState
    }

    override fun setJobState(jobState: JobStateEntity, jobStatus: String) {
        jobState.status = jobStatus
        jobState.endTime = LocalDateTime.now()
        persistAndFlush(jobState)
        logger.debug("Set job {} to {}", jobState.id, jobStatus)
    }

    override fun getLastJobState(): JobStateEntity? {
        logger.debug("Search last \"IN PROGRESS\" job state")
        return find("status = ?1 ORDER BY startTime DESC", "IN_PROGRESS").firstResult()
    }
}