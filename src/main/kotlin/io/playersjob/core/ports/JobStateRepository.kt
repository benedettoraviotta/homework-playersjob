package io.playersjob.core.ports

import io.playersjob.adapters.persistence.entities.JobStateEntity

interface JobStateRepository {
    fun startNewJob(): JobStateEntity
    fun completeJob(jobState: JobStateEntity)
    fun getLastJobState(): JobStateEntity?
}