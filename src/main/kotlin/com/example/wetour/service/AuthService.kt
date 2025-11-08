package com.example.wetour.service

import com.example.wetour.dto.AuthResponse
import com.example.wetour.dto.LoginRequest
import com.example.wetour.dto.RegisterRequest
import com.example.wetour.dto.toResponse
import com.example.wetour.repository.UserRepository
import com.example.wetour.repository.model.User
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @Value("\${app.jwt.secret:mySuperSecretKeyForJWTTokenGeneration2024}")
    private lateinit var jwtSecret: String

    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }

    // 1. Создание анонимной сессии
    fun createAnonymousSession(deviceId: String, username: String): AuthResponse {
        // Проверяем не зарегистрирован ли уже пользователь с этим device_id
        val existingUser = userRepository.findByDeviceId(deviceId)
        if (existingUser != null && !existingUser.isAnonymous) {
            throw IllegalArgumentException("This device is already registered")
        }

        val user = existingUser ?: User(
            username = username,
            deviceId = deviceId,
            isAnonymous = true
        ).let { userRepository.save(it) }

        val token = generateToken(user)
        return AuthResponse(user.toResponse(), token)
    }

    // 2. Регистрация анонимного пользователя
    fun registerAnonymousUser(deviceId: String, request: RegisterRequest): AuthResponse {
        val anonymousUser = userRepository.findByDeviceId(deviceId)
            ?: throw IllegalArgumentException("Anonymous user not found")

        if (!anonymousUser.isAnonymous) {
            throw IllegalArgumentException("User already registered")
        }

        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email already registered")
        }

        // "Апгрейдим" анонимного пользователя
        val upgradedAnonymousUser = anonymousUser.copy(
            email = request.email,
            username = request.username,
            passwordHash = passwordEncoder.encode(request.password),
            isAnonymous = false,
            deviceId = null
        )

        val registeredUser = userRepository.save(upgradedAnonymousUser)
        val token = generateToken(registeredUser)

        return AuthResponse(registeredUser.toResponse(), token)
    }

    // 3. Обычная регистрация нового пользователя
    fun registerNewUser(request: RegisterRequest): AuthResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email already registered")
        }

        val user = User(
            email = request.email,
            username = request.username,
            passwordHash = passwordEncoder.encode(request.password),
            isAnonymous = false
        )

        val savedUser = userRepository.save(user)
        val token = generateToken(savedUser)

        return AuthResponse(savedUser.toResponse(), token)
    }

    // 4. Логин для зарегистрированных пользователей
    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw IllegalArgumentException("User not found")

        if (user.isAnonymous) {
            throw IllegalArgumentException("Please register first")
        }

        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw IllegalArgumentException("Invalid password")
        }

        val token = generateToken(user)
        return AuthResponse(user.toResponse(), token)
    }

    // 5. Валидация JWT токена
    fun validateToken(token: String): UUID? {
        return try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body
            UUID.fromString(claims.subject)
        } catch (e: Exception) {
            null
        }
    }

    // 6. Генерация JWT токена
    private fun generateToken(user: User): String {
        return Jwts.builder()
            .setSubject(user.id.toString())
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000)) // 30 дней
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    // 7. Получение пользователя по ID (для @CurrentUser)
    fun getUserById(userId: UUID): User {
        return userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }
    }
}