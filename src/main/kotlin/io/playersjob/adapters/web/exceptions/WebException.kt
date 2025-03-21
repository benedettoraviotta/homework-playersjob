package io.playersjob.adapters.web.exceptions

import io.playersjob.core.domain.exceptions.DomainException

open class WebException(
    message: String,
    errorCode: String? = WebErrorCodes.API_REQUEST_FAILED
    , cause: Throwable? = null
) : DomainException(message, errorCode, cause)