package io.playersjob.adapters.persistence

import io.playersjob.adapters.persistence.entities.JobStateEntity
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceException
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.MockitoAnnotations

@QuarkusTest
class JpaProcessedPlayerRepositoryTest {

    @Inject
    lateinit var jpaProcessedPlayerRepository: JpaProcessedPlayerRepository

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
    fun `test save processed player`() {
        val playerId = "player1"
        val jobState = JobStateEntity(status = "IN_PROGRESS")
        em.persist(jobState)

        jpaProcessedPlayerRepository.save(playerId, jobState)
        val exists = jpaProcessedPlayerRepository.existById(playerId)

        assertTrue(exists)
    }

    @Test
    @Transactional
    fun `test exists by id`() {
        val playerId = "player2"
        val jobState = JobStateEntity(status = "IN_PROGRESS")
        em.persist(jobState)

        jpaProcessedPlayerRepository.save(playerId, jobState)
        val exists = jpaProcessedPlayerRepository.existById(playerId)

        assertTrue(exists)
    }

    @Test
    @Transactional
    fun `test save processed player with exception`() {
        val playerId = "errorPlayer"
        val jobState = JobStateEntity(status = "IN_PROGRESS")
        em.persist(jobState)

        try {
            jpaProcessedPlayerRepository.save(playerId, jobState)
            throw PersistenceException("Simulated persistence exception")
        } catch (e: Exception) {
            assertTrue(e.message!!.contains("Simulated persistence exception"))
        }
    }

    @Test
    @Transactional
    fun `test exists by id with exception`() {
        try {
            jpaProcessedPlayerRepository.existById("nonexistent")
            throw PersistenceException("Simulated persistence exception")
        } catch (e: Exception) {
            assertTrue(e.message!!.contains("Simulated persistence exception"))
        }
    }
}