package io.playersjob.adapters.web.exceptions

class ServerException(message: String, cause: Throwable? = null) : WebException(message,
    WebErrorCodes.API_SERVER_ERROR, cause)