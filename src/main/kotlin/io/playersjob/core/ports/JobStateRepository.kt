package io.playersjob.core.ports

import io.playersjob.adapters.persistence.entities.JobStateEntity

interface JobStateRepository {
    fun startNewJob(): JobStateEntity
    fun setJobState(jobState: JobStateEntity, jobStatus: String)
    fun getLastJobState(): JobStateEntity?
}