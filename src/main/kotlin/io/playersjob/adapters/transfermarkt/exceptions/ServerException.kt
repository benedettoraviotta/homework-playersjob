package io.playersjob.adapters.transfermarkt.exceptions

import io.playersjob.core.domain.exceptions.DomainException

class ServerException(message: String) : DomainException(message, ErrorCodes.SERVER_ERROR)