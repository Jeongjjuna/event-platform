package yjh.ontongsal.authapi.shared.security.jwt

enum class TokenType {
    ACCESS, REFRESH;

    fun isNotEqualTo(expectedType: TokenType): Boolean {
        return this != expectedType
    }
}
