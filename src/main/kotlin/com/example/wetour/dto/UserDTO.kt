package com.example.wetour.dto

import com.example.wetour.repository.model.User
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.UUID

// Регистрация
data class RegisterRequest(
    @field:NotBlank
    @field:Email
    val email: String,

    @field:NotBlank
    @field:Size(min = 2, max = 50)
    val username: String,

    @field:NotBlank
    @field:Size(min = 6)
    val password: String
)

// Логин
data class LoginRequest(
    @field:NotBlank
    @field:Email
    val email: String,

    @field:NotBlank
    val password: String
)

// Ответ аутентификации
data class AuthResponse(
    val user: UserResponse,
    val token: String
)

// Ответ пользователя
data class UserResponse(
    val id: UUID,
    val email: String?,
    val username: String,
    val isAnonymous: Boolean,
    val deviceId: String?,
    val createdAt: Instant
)

// Extension function для User
fun User.toResponse() = UserResponse(
    id = id!!,
    email = email,
    username = username,
    isAnonymous = isAnonymous,
    deviceId = deviceId,
    createdAt = createdAt!!
)