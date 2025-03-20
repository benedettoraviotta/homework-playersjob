package io.playersjob.adapters.persistence.entities

import jakarta.persistence.*
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase

@Entity
@Table(name = "players")
class JpaPlayerEntity : PanacheEntityBase() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var _id: Long = 0
    lateinit var id: String
    lateinit var name: String
    lateinit var position: String
    var dateOfBirth: String? = null
    var age: Int? = null
    @ElementCollection
    lateinit var nationality: MutableList<String>
    var height: Int? = null
    var foot: String? = null
    var joinedOn: String? = null
    var signedFrom: String? = null
    var contract: String? = null
    var marketValue: Int? = null
    var status: String? = null
}