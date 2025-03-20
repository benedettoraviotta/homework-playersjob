package io.playersjob.adapters.persistence

import io.playersjob.adapters.persistence.entities.JpaJobStateEntity
import io.playersjob.core.domain.JobState
import io.quarkus.test.TestTransaction
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
class JpaJobStateRepositoryTest {

    @Inject
    lateinit var entityManager: EntityManager

    @Inject
    lateinit var jobStateRepository: JpaJobStateRepository

    @BeforeEach
    @TestTransaction
    fun setUp() {
        entityManager.createQuery("DELETE FROM JpaJobStateEntity").executeUpdate()
    }

    @Test
    @TestTransaction
    fun `test save job state`() {
        val jobState = JobState(lastProcessedPlayerId = "123")

        jobStateRepository.save(jobState).await().indefinitely()

        val savedJobState = entityManager.createQuery(
            "SELECT j FROM JpaJobStateEntity j WHERE j.lastProcessedPlayerId = :lastProcessedPlayerId",
            JpaJobStateEntity::class.java
        )
            .setParameter("lastProcessedPlayerId", "123")
            .resultList
            .firstOrNull()

        assertNotNull(savedJobState)
        assertEquals("123", savedJobState?.lastProcessedPlayerId)
    }

    @Test
    @TestTransaction
    fun `test get default job state when no data is present`() {
        val lastState = jobStateRepository.getLastJobState().await().indefinitely()

        assertNull(lastState)
    }

    @Test
    @TestTransaction
    fun `test get last job state when multiple job states present`() {
        val jobState1 = JpaJobStateEntity().apply { lastProcessedPlayerId = "1" }
        val jobState2 = JpaJobStateEntity().apply { lastProcessedPlayerId = "2" }
        val jobState3 = JpaJobStateEntity().apply { lastProcessedPlayerId = "3" }

        entityManager.persist(jobState1)
        entityManager.persist(jobState2)
        entityManager.persist(jobState3)

        val lastState = jobStateRepository.getLastJobState().await().indefinitely()

        assertNotNull(lastState)
        assertEquals("3", lastState?.lastProcessedPlayerId)
    }
}