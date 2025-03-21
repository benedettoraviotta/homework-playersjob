package io.playersjob.adapters.persistence.entities

import io.quarkus.hibernate.orm.panache.PanacheEntityBase
import jakarta.persistence.*

@Entity
@Table(name = "players")
class PlayerEntity : PanacheEntityBase() {
    @Id
    lateinit var id: String

    @Column(nullable = false)
    lateinit var name: String

    @Column(nullable = false)
    lateinit var position: String

    @Column(name = "date_of_birth")
    var dateOfBirth: String? = null

    @Column
    var age: Int? = null

    @ElementCollection
    @CollectionTable(name = "player_nationalities", joinColumns = [JoinColumn(name = "player_id")])
    @Column(name = "nationality")
    var nationality: MutableList<String> = mutableListOf()

    @Column
    var height: Int? = null

    @Column
    var foot: String? = null

    @Column(name = "joined_on")
    var joinedOn: String? = null

    @Column(name = "signed_from")
    var signedFrom: String? = null

    @Column
    var contract: String? = null

    @Column(name = "market_value")
    var marketValue: Int? = null

    @Column
    var status: String? = null
}