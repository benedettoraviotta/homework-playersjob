package io.playersjob.adapters.persistence.exceptions

import io.playersjob.core.domain.exceptions.DomainException

open class DBException(
    message: String,
    errorCode: String? = PersistenceErrorCodes.DATABASE_ERROR
    , cause: Throwable? = null
) : DomainException(message, errorCode, cause)