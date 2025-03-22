package io.playersjob.adapters.persistence

import io.playersjob.adapters.persistence.entities.JobStateEntity
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceException
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.MockitoAnnotations

@QuarkusTest
class JpaJobStateRepositoryTest {

    @Inject
    lateinit var jpaJobStateRepository: JpaJobStateRepository

    @Inject
    lateinit var em: EntityManager

    @BeforeEach
    @Transactional
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        em.createQuery("DELETE FROM ProcessedPlayerEntity").executeUpdate()
        em.createQuery("DELETE FROM JobStateEntity").executeUpdate()
    }

    @Test
    @Transactional
    fun `test save job state`() {
        val jobState = jpaJobStateRepository.startNewJob()
        assertNotNull(jobState)
        assertEquals("IN_PROGRESS", jobState.status)
    }

    @Test
    @Transactional
    fun `test complete job state`() {
        val jobState = jpaJobStateRepository.startNewJob()
        jpaJobStateRepository.setJobState(jobState, "COMPLETED")
        val completedJobState = jpaJobStateRepository.findById(jobState.id)
        assertNotNull(completedJobState)
        assertEquals("COMPLETED", completedJobState!!.status)
        assertNotNull(completedJobState.endTime)
    }

    @Test
    @Transactional
    fun `test retrieve last job state`() {
        val jobState = jpaJobStateRepository.startNewJob()
        val lastJobState = jpaJobStateRepository.getLastJobState()
        assertNotNull(lastJobState)
        assertEquals(jobState.id, lastJobState!!.id)
    }

    @Test
    @Transactional
    fun `test save with exception`() {
        val jobState = JobStateEntity(status = "IN_PROGRESS")

        try {
            em.persist(jobState)
            throw PersistenceException("Simulated persistence exception")
        } catch (e: PersistenceException) {
            assertTrue(e.message!!.contains("Simulated persistence exception"))
        }
    }

    @Test
    @Transactional
    fun `test retrieve with exception`() {
        try {
            jpaJobStateRepository.getLastJobState()
            throw PersistenceException("Simulated persistence exception")
        } catch (e: PersistenceException) {
            assertTrue(e.message!!.contains("Simulated persistence exception"))
        }
    }
}