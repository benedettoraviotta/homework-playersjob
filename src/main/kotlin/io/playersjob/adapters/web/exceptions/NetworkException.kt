package io.playersjob.adapters.web.exceptions

class NetworkException(message: String, cause: Throwable? = null) : WebException(message,
    WebErrorCodes.API_NETWORK_ERROR, cause)