package yjh.ontongsal.authapi.shared.response

sealed class AppException(
    open val code: Int,
    open val statusCode: Int,
    message: String,
) : RuntimeException(message) {

    class BadRequest(errorCode: ErrorCode, customMessage: String? = null) :
        AppException(errorCode.code, 400, customMessage ?: errorCode.message)

    class Unauthorized(errorCode: ErrorCode, customMessage: String? = null) :
        AppException(errorCode.code, 401, customMessage ?: errorCode.message)

    class Forbidden(errorCode: ErrorCode, customMessage: String? = null) :
        AppException(errorCode.code, 403, customMessage ?: errorCode.message)

    class NotFound(errorCode: ErrorCode, customMessage: String? = null) :
        AppException(errorCode.code, 404, customMessage ?: errorCode.message)

    class Conflict(errorCode: ErrorCode, customMessage: String? = null) :
        AppException(errorCode.code, 409, customMessage ?: errorCode.message)

    class Internal(errorCode: ErrorCode, customMessage: String? = null) :
        AppException(errorCode.code, 500, customMessage ?: errorCode.message)
}

