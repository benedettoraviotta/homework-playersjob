package io.playersjob.application.services

import org.slf4j.LoggerFactory
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty

@ApplicationScoped
class InsertInterruptionSimulator {

    @Inject
    @ConfigProperty(name = "force.insert.interrupt", defaultValue = "no")
    lateinit var forceInsertInterrupt: String

    private val logger = LoggerFactory.getLogger(InsertInterruptionSimulator::class.java)

    fun checkForInterruption(processedCount: Int, interruptAfter: Int = 10) {
        if (forceInsertInterrupt.equals("yes", true) && processedCount == interruptAfter) {
            logger.info("FORCE_INSERT_INTERRUPT - Simulating interruption after {} inserts", interruptAfter)
            throw RuntimeException("FORCE_INSERT_INTERRUPT - Simulated interruption after $interruptAfter inserts.")
        }
    }
}