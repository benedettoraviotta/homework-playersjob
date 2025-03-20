package io.playersjob.adapters.transfermarkt.exceptions

import io.playersjob.core.domain.exceptions.DomainException

class NetworkException(message: String) : DomainException(message, ErrorCodes.NETWORK_ERROR)