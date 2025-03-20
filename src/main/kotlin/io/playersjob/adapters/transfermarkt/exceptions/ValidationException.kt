package io.playersjob.adapters.transfermarkt.exceptions

import io.playersjob.core.domain.exceptions.DomainException

class ValidationException(message: String) : DomainException(message, ErrorCodes.VALIDATION_ERROR)