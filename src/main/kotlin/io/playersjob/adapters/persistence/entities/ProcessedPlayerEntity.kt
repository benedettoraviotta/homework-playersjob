package io.playersjob.adapters.persistence.entities

import io.quarkus.hibernate.orm.panache.PanacheEntityBase
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "processed_player")
class ProcessedPlayerEntity() : PanacheEntityBase() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @Column(nullable = false, name = "player_id")
    lateinit var playerId: String

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    lateinit var jobState: JobStateEntity

    @Column(nullable = false, name = "processed_time")
    val processedTime: LocalDateTime = LocalDateTime.now()

    constructor(playerId: String, jobState: JobStateEntity) : this() {
        this.playerId = playerId
        this.jobState = jobState
    }
}