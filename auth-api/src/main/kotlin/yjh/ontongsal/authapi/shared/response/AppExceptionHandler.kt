package yjh.ontongsal.authapi.shared.response

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.ConstraintViolationException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import tools.jackson.databind.exc.InvalidFormatException
import tools.jackson.databind.exc.MismatchedInputException


private val log = KotlinLogging.logger {}

@RestControllerAdvice
class AppExceptionHandler {

    /**
     * 5xx(AppException.Internal): 서버 결함이므로 스택트레이스를 포함해 error로 남긴다.
     * 내부 상세가 외부로 노출되지 않도록 응답 message는 일반 문구로 마스킹하고, 원인은 서버 로그에만 기록한다.
     */
    @ExceptionHandler(value = [AppException.Internal::class])
    fun handleInternalAppException(e: AppException.Internal): ResponseEntity<ErrorResponse> {
        log.error(e) { "[ExceptionHandler] : (${e.code}) ${e.message}" }

        val response = ErrorResponse(
            code = e.code,
            message = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
            details = null
        )

        return ResponseEntity
            .status(
                HttpStatus.resolve(e.statusCode)
                    ?: HttpStatus.INTERNAL_SERVER_ERROR
            )
            .body(response)
    }

    /**
     * 4xx: 클라이언트 오류이므로 스택트레이스 없이 warn으로 남긴다.
     * (Internal은 위의 더 구체적인 핸들러가 우선 매칭된다)
     */
    @ExceptionHandler(value = [AppException::class])
    fun handleAppException(e: AppException): ResponseEntity<ErrorResponse> {
        val origin = e.stackTrace.firstOrNull()
        val location = origin?.let {
            "${it.methodName}(${it.fileName}:${it.lineNumber})"
        }

        log.info { "[ExceptionHandler] : (${e.code}) ${e.message} - $location" }

        val response = ErrorResponse(
            code = e.code,
            message = e.message ?: "Unknown error message",
            details = null
        )

        return ResponseEntity
            .status(
                HttpStatus.resolve(e.statusCode)
                    ?: HttpStatus.INTERNAL_SERVER_ERROR
            )
            .body(response)
    }

    // 2. @Valid (@RequestBody)
    @ExceptionHandler(value = [MethodArgumentNotValidException::class])
    fun handleValidationException(
        e: MethodArgumentNotValidException,
    ): ResponseEntity<ErrorResponse> {

        log.info { "[ExceptionHandler] Validation Exception" }

        val errors = e.bindingResult.fieldErrors.map {
            ErrorDetail(
                field = it.field,
                reason = it.defaultMessage ?: "invalid value"
            )
        }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    code = HttpStatus.BAD_REQUEST.value(),
                    message = HttpStatus.BAD_REQUEST.reasonPhrase,
                    details = errors
                )
            )
    }

    // 3. @RequestParam / PathVariable
    @ExceptionHandler(value = [ConstraintViolationException::class])
    fun handleConstraintViolationException(
        e: ConstraintViolationException,
    ): ResponseEntity<ErrorResponse> {

        log.info { "[ExceptionHandler] Constraint Violation" }

        val errors = e.constraintViolations.map {
            ErrorDetail(
                field = it.propertyPath.toString(),
                reason = it.message
            )
        }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    code = HttpStatus.BAD_REQUEST.value(),
                    message = HttpStatus.BAD_REQUEST.reasonPhrase,
                    details = errors.toList()
                )
            )
    }

    // 4. @RequestParam이 required=true 위반
    @ExceptionHandler(value = [MissingServletRequestParameterException::class])
    fun handleMissingParam(
        e: MissingServletRequestParameterException,
    ): ResponseEntity<ErrorResponse> {

        log.info { "[ExceptionHandler] Missing Request Param" }

        val error = ErrorDetail(
            field = e.parameterName,
            reason = "required parameter is missing"
        )

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    code = HttpStatus.BAD_REQUEST.value(),
                    message = HttpStatus.BAD_REQUEST.reasonPhrase,
                    details = listOf(error)
                )
            )
    }

    // 4. HttpMessageNotReadableException (요청 바디 파싱 실패)
    @ExceptionHandler(value = [HttpMessageNotReadableException::class])
    fun handleHttpMessageNotReadableException(
        e: HttpMessageNotReadableException,
    ): ResponseEntity<ErrorResponse> {

        log.info { "[ExceptionHandler] Http Message Not Readable" }

        val reason = when (e.cause) {
            is InvalidFormatException -> "invalid format"
            is MismatchedInputException -> "missing or invalid field"
            else -> "unreadable request body"
        }

        val error = ErrorDetail(
            field = "requestBody",
            reason = reason
        )

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    code = HttpStatus.BAD_REQUEST.value(),
                    message = HttpStatus.BAD_REQUEST.reasonPhrase,
                    details = listOf(error)
                )
            )
    }

    /**
     * UNIQUE 제약조건 위반
     * FK 제약조건 위반
     * NOT NULL 위반
     * length 초과
     * check constraint 위반
     */
    @ExceptionHandler(value = [DataIntegrityViolationException::class])
    fun handleDataIntegrityViolationException(
        e: DataIntegrityViolationException,
    ): ResponseEntity<ErrorResponse> {

        log.error(e) { "[ExceptionHandler] Data Integrity Violation" }

        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(
                ErrorResponse(
                    code = HttpStatus.CONFLICT.value(),
                    message = HttpStatus.CONFLICT.reasonPhrase,
                    details = listOf()
                )
            )
    }

    @ExceptionHandler(value = [AuthorizationDeniedException::class])
    fun handleAuthorizationDeniedException(
        e: AuthorizationDeniedException,
    ): ResponseEntity<ErrorResponse> {

        log.info { "[ExceptionHandler] Authorization Denied : ${e.message}" }

        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(
                ErrorResponse(
                    code = HttpStatus.FORBIDDEN.value(),
                    message = HttpStatus.FORBIDDEN.reasonPhrase,
                    details = null
                )
            )
    }

    @ExceptionHandler(value = [Exception::class])
    fun handleException(e: Exception, request: WebRequest): ResponseEntity<ErrorResponse> {
        log.error(e) { "[ExceptionHandler] Exception : ${e.message}" }

        val response = ErrorResponse(
            code = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            message = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
            details = listOf(
                ErrorDetail(
                    field = null,
                    reason = request.getDescription(false)
                )
            )
        )

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(response)
    }
}
