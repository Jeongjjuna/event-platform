package yjh.ontongsal.authapi.presentation

import jakarta.validation.constraints.NotNull

data class RefreshRequest(
    @field:NotNull(message = "리프레시 토큰을 입력해주세요.")
    val refreshToken: String,
)
