package io.playersjob.adapters.persistence

import io.playersjob.adapters.persistence.entities.JpaPlayerEntity
import io.playersjob.core.domain.Player
import io.quarkus.test.TestTransaction
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
class JpaPlayerRepositoryTest {

    @Inject
    lateinit var entityManager: EntityManager

    @Inject
    lateinit var playerRepository: JpaPlayerRepository

    @BeforeEach
    @TestTransaction
    fun setUp() {
        entityManager.createQuery("DELETE FROM JpaPlayerEntity").executeUpdate()
    }

    @Test
    @TestTransaction
    fun `save should persist player entity`() {
        val player = Player("1", "Lionel Messi", "Forward", "1987-06-24", nationality = listOf("Argentina"))

        playerRepository.save(player).await().indefinitely()

        val savedPlayer = entityManager.createQuery(
            "SELECT p FROM JpaPlayerEntity p WHERE p.id = :id",
            JpaPlayerEntity::class.java
        )
            .setParameter("id", "1")
            .resultList
            .firstOrNull()

        assertNotNull(savedPlayer)
        assertEquals("Lionel Messi", savedPlayer?.name)
        assertEquals("Forward", savedPlayer?.position)
    }

    @Test
    @TestTransaction
    fun `findLastProcessedPlayer should return last player when exists`() {
        val player1 = JpaPlayerEntity().apply {
            id = "1"
            name = "Lionel Messi"
            position = "Forward"
        }
        val player2 = JpaPlayerEntity().apply {
            id = "2"
            name = "Cristiano Ronaldo"
            position = "Forward"
        }

        entityManager.persist(player1)
        entityManager.persist(player2)

        val lastPlayer = playerRepository.findLastProcessedPlayer().await().indefinitely()

        assertNotNull(lastPlayer)
        assertEquals("2", lastPlayer?.id)
        assertEquals("Cristiano Ronaldo", lastPlayer?.name)
    }

    @Test
    @TestTransaction
    fun `findLastProcessedPlayer should return null if no players exist`() {
        val lastPlayer = playerRepository.findLastProcessedPlayer().await().indefinitely()

        assertNull(lastPlayer)
    }

    @Test
    @TestTransaction
    fun `findLastProcessedPlayer should handle exception gracefully`() {
        entityManager.createQuery("DELETE FROM JpaPlayerEntity").executeUpdate()
        entityManager.close()

        val lastPlayer = playerRepository.findLastProcessedPlayer().await().indefinitely()

        assertNull(lastPlayer)
    }
}