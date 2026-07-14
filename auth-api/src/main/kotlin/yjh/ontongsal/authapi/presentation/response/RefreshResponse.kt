package yjh.ontongsal.authapi.presentation.response

data class RefreshResponse(
    val accessToken: String,
    val refreshToken: String,
) {
    companion object {
        fun from(accessToken: String, refreshToken: String): RefreshResponse {
            return RefreshResponse(accessToken, refreshToken)
        }
    }
}