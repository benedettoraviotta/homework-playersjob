package io.playersjob.adapters.persistence.entities

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase
import jakarta.persistence.*

@Entity
@Table(name = "job_state")
class JpaJobStateEntity : PanacheEntityBase() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    var lastProcessedPlayerId: String = "0"
}