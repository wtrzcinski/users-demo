package com.codete.user.core.service.exception

sealed class ServiceException(message: String) : RuntimeException(message)

class NotFoundException(message: String) : ServiceException(message)

class ValidationException(message: String) : ServiceException(message)

class DuplicateException(message: String) : ServiceException(message)