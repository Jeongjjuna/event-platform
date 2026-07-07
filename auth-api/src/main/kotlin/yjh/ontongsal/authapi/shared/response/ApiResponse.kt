package yjh.ontongsal.authapi.shared.response

import java.time.Instant

interface ApiResponse {
    val code: Int
    val message: String
}

data class SuccessResponse<T>(
    override val code: Int = 200,
    override val message: String,
    val data: T?,
) : ApiResponse

data class ErrorResponse(
    override val code: Int,
    override val message: String,            // 요약(개발자용)
    val details: List<ErrorDetail>? = null,  // 상세내역(개발자용)
    val timestamp: Instant = Instant.now(),
) : ApiResponse

data class ErrorDetail(
    val field: String?,
    val reason: String,
)
