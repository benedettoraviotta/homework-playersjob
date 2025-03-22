package io.playersjob.adapters.persistence.entities

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "job_state")
class JobStateEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @Column(nullable = false)
    lateinit var status: String

    @Column(nullable = false, name = "start_time")
    val startTime: LocalDateTime = LocalDateTime.now()

    @Column(name = "end_time")
    var endTime: LocalDateTime? = null

    constructor(status: String) : this() {
        this.status = status
    }
}