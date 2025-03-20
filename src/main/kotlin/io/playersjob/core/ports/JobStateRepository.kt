package io.playersjob.core.ports

import io.playersjob.core.domain.JobState
import io.smallrye.mutiny.Uni

interface JobStateRepository {
    fun save(jobState: JobState): Uni<Void>
    fun getLastJobState(): Uni<JobState?>
}
