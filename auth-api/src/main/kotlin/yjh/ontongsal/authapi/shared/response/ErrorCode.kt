package yjh.ontongsal.authapi.shared.response

enum class ErrorCode(
    val code: Int,
    val message: String,
) {
    // User (1000~1099)
    USER_NOT_FOUND(1000, "사용자를 찾을 수 없습니다"),
    USER_CONFLICT(1001, "사용자가 이미 존재합니다."),
    INVALID_PASSWORD(1002, "비밀번호가 올바르지 않습니다."),
    LOGIN_FAILED(1003, "이메일 또는 비밀번호가 올바르지 않습니다."),
}
