package io.playersjob.adapters.dto

import kotlinx.serialization.Serializable

@Serializable
data class TransfermarktPlayer(
    val id: String,
    val name: String,
    val position: String,
    var dateOfBirth: String? = null,
    val age: Int? = null,
    val nationality: List<String>,
    var height: Int? = null,
    var foot: String? = null,
    var joinedOn: String? = null,
    var signedFrom: String? = null,
    var contract: String? = null,
    var marketValue: Int? = null,
    var status: String? = null
)