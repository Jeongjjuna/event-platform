package yjh.ontongsal.authapi.domain

import yjh.ontongsal.authapi.shared.response.ErrorCode

enum class AuthErrorCode(
    override val code: Int,
    override val message: String,
): ErrorCode {
    // User (1000~1099)
    USER_NOT_FOUND(1000, "사용자를 찾을 수 없습니다"),
    USER_CONFLICT(1001, "사용자가 이미 존재합니다."),
    NOT_MATCH_PASSWORD(1002, "비밀번호가 일치하지 않습니다."),
    LOGIN_FAILED(1003, "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_FORMAT_EMAIL(1004, "유효하지 않은 형식의 이메일 입니다."),
    INVALID_FORMAT_PASSWORD(1005, "유효하지 않은 형식의 비밀번호 입니다."),
    USER_ALREADY_WITHDRAWN(1006, "사용자가 이미 탈퇴되었습니다."),

    // Auth (2000~2099)
    INVALID_TOKEN_TYPE(2000, "토큰 타입이 유효하지 않습니다."),
    NOT_FOUND_SESSION(2001, "세션이 존재하지 않습니다."),
    SESSION_TOKEN_MISMATCH(2001, "인증 정보가 일치하지 않습니다."),

    INTERNAL_SERVER_ERROR(9999, "내부 서버 오류가 발생했습니다."),
}