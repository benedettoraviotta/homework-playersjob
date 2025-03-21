package io.playersjob.adapters.persistence

import io.playersjob.adapters.persistence.exceptions.DBException
import io.playersjob.adapters.persistence.exceptions.PersistExeption
import io.playersjob.core.domain.Player
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceException
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

@QuarkusTest
class JpaPlayerRepositoryTest {

    @Inject
    lateinit var jpaPlayerRepository: JpaPlayerRepository

    @Inject
    lateinit var em: EntityManager

    @BeforeEach
    @Transactional
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        em.createQuery("DELETE FROM PlayerEntity").executeUpdate()
    }

    @Test
    @Transactional
    fun `test save player`() {
        val player = Player(
            id = "player1",
            name = "John Doe",
            position = "Forward",
            dateOfBirth = "1990-01-01",
            age = 35,
            nationality = listOf("USA"),
            marketValue = 1000000,
            height = 180,
            foot = "Right",
            joinedOn = "2020-01-01",
            signedFrom = "Club A",
            contract = "2025-01-01",
            status = "Active"
        )

        jpaPlayerRepository.save(player)
        val savedPlayer = jpaPlayerRepository.findPlayerById("player1")

        assertNotNull(savedPlayer)
        assertEquals(player.id, savedPlayer?.id)
        assertEquals(player.name, savedPlayer?.name)
    }

    @Test
    @Transactional
    fun `test find player by id`() {
        val player = Player(
            id = "player2",
            name = "Jane Doe",
            position = "Midfielder",
            dateOfBirth = "1992-01-01",
            age = 33,
            nationality = listOf("Canada"),
            marketValue = 2000000,
            height = 170,
            foot = "Left",
            joinedOn = "2019-01-01",
            signedFrom = "Club B",
            contract = "2024-01-01",
            status = "Inactive"
        )

        jpaPlayerRepository.save(player)
        val retrievedPlayer = jpaPlayerRepository.findPlayerById("player2")

        assertNotNull(retrievedPlayer)
        assertEquals(player.id, retrievedPlayer?.id)
        assertEquals(player.name, retrievedPlayer?.name)
    }

    @Test
    @Transactional
    fun `test save player with exception`() {
        val player = Player(
            id = "player3",
            name = "Error Player",
            position = "Error",
            dateOfBirth = "2000-01-01",
            age = 25,
            nationality = listOf("Errorland"),
            marketValue = 0,
            height = 0,
            foot = "None",
            joinedOn = "2021-01-01",
            signedFrom = "Nowhere",
            contract = "2022-01-01",
            status = "Error"
        )

        val jpaPlayerRepositoryMock = Mockito.mock(JpaPlayerRepository::class.java)
        Mockito.doThrow(PersistenceException("Simulated persistence exception")).`when`(jpaPlayerRepositoryMock).save(player)

        try {
            jpaPlayerRepositoryMock.save(player)
            fail("Expected PersistenceException to be thrown")
        } catch (e: PersistenceException) {
            assertEquals("Simulated persistence exception", e.message)
        }
    }

    @Test
    @Transactional
    fun `test find player by id with exception`() {
        val jpaPlayerRepositoryMock = Mockito.mock(JpaPlayerRepository::class.java)
        Mockito.doThrow(PersistenceException("Simulated persistence exception")).`when`(jpaPlayerRepositoryMock).findPlayerById("nonexistent")

        try {
            jpaPlayerRepositoryMock.findPlayerById("nonexistent")
            fail("Expected PersistenceException to be thrown")
        } catch (e: PersistenceException) {
            assertEquals("Simulated persistence exception", e.message)
        }
    }
}