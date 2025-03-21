package io.playersjob.adapters.web.exceptions

class ValidationException(message: String, cause: Throwable? = null) : WebException(message,
    WebErrorCodes.API_VALIDATION_FAILED, cause)