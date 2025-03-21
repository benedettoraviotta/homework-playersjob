package io.playersjob

import io.playersjob.application.services.FetchClubPlayersJob
import io.quarkus.runtime.Quarkus
import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.eclipse.microprofile.config.inject.ConfigProperty

@QuarkusMain
class Main : QuarkusApplication {

    @Inject
    lateinit var fetchJobRunner: FetchClubPlayersJob

    @ConfigProperty(name = "fetch.job.club.id", defaultValue = "5")
    lateinit var clubId: Integer

    @Transactional
    override fun run(vararg args: String?): Int {
        fetchJobRunner.fetchAndSavePlayers(clubId.toInt())
        Quarkus.waitForExit()
        return 0
    }
}