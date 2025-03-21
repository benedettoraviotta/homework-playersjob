package io.playersjob.core.ports

import io.playersjob.adapters.persistence.entities.JobStateEntity
import io.playersjob.adapters.persistence.entities.ProcessedPlayerEntity

interface ProcessedPlayerRepository {
    fun existById(id: String): Boolean
    fun save(id:String, jobState: JobStateEntity)
    fun findByJobState(jobState: JobStateEntity): List<ProcessedPlayerEntity>
}