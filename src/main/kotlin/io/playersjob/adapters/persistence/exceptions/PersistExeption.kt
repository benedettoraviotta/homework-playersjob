package io.playersjob.adapters.persistence.exceptions

class PersistExeption(message: String, cause: Throwable? = null) : DBException(message, PersistenceErrorCodes.DATABASE_TRANSACTION_ERROR, cause)