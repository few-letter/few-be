package web.handler.exception

class BadRequestException(
    message: String,
) : RuntimeException(message)