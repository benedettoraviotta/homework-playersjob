package io.playersjob.core.domain.exceptions

open class DomainException(message: String, val errorCode: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)