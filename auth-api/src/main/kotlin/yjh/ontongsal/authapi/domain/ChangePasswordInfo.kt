package yjh.ontongsal.authapi.domain

data class ChangePasswordInfo(
    val currentPassword: String,
    val newPassword: String,
)