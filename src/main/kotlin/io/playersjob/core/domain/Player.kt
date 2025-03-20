package io.playersjob.core.domain

data class Player(
    val id: String,
    val name: String,
    val position: String,
    val dateOfBirth: String? = null,
    val age: Int ? = null,
    val nationality: List<String>,
    val height: Int? = null,
    val foot: String? = null,
    val joinedOn: String? = null,
    val signedFrom: String? = null,
    val contract: String? = null,
    val marketValue: Int? = null,
    val status: String? = null
)