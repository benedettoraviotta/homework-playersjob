package io.playersjob.adapters.persistence.entities

import io.quarkus.hibernate.orm.panache.PanacheEntityBase
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "job_state")
class JobStateEntity() : PanacheEntityBase() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @Column(nullable = false)
    lateinit var status: String

    @Column(nullable = false)
    val startTime: LocalDateTime = LocalDateTime.now()

    @Column
    var endTime: LocalDateTime? = null

    constructor(status: String) : this() {
        this.status = status
    }
}