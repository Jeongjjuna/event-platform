package yjh.ontongsal.authapi.shared.response

sealed class DomainException(
    open val code: Int,
    message: String,
) : RuntimeException(message) {

    /**
     * 현재 Aggregate 상태 때문에 행위 불가
     * ex) 탈퇴한 사용자 탈퇴 시도
     * ex) 이미 완료된 주문 취소 시도
     * ex) 만료된 쿠폰 사용 시도
     */
    class InvalidState(
        errorCode: ErrorCode,
        customMessage: String? = null,
    ) : DomainException(
        errorCode.code,
        customMessage ?: errorCode.message
    )

    /**
     * 인증 정보가 유효하지 않아 요청자를 식별할 수 없음
     * ex) 잘못된 access token
     * ex) 만료된 refresh token
     * ex) 저장된 refresh token과 요청 token 불일치
     * ex) 존재하지 않는 세션으로 인증 요청
     */
    class Unauthorized(
        errorCode: ErrorCode,
        customMessage: String? = null,
    ) : DomainException(
        errorCode.code,
        customMessage ?: errorCode.message
    )

    /**
     * 도메인 객체 간 상태 충돌
     * ex) 이미 완료된 주문에 대한 주문 요청
     */
    class Conflict(
        errorCode: ErrorCode,
        customMessage: String? = null,
    ) : DomainException(
        errorCode.code,
        customMessage ?: errorCode.message
    )

    /**
     * 도메인 정책 위반
     * ex) 잔액보다 큰 출금, 비밀번호 정책 위반
     * ex) 이전 비밀번호와 같은 비밀번호로 변경 시도
     */
    class RuleViolation(
        errorCode: ErrorCode,
        customMessage: String? = null,
    ) : DomainException(
        errorCode.code,
        customMessage ?: errorCode.message
    )

    /**
     * NotFound
     * Aggregate 내부에서 필요한 Entity를 찾지 못함
     */
    class NotFound(
        errorCode: ErrorCode,
        customMessage: String? = null,
    ) : DomainException(
        errorCode.code,
        customMessage ?: errorCode.message
    )
}