package io.playersjob.core.domain.exceptions

open class DomainException(message: String, val errorCode: String, cause: Throwable? = null) : RuntimeException(message, cause)