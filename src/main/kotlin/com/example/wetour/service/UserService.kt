package com.example.wetour.service

import com.example.wetour.repository.UserRepository
import com.example.wetour.repository.model.User
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository
) {

    // Основной метод для получения пользователя по ID
    fun getUserById(userId: UUID): User {
        return userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }
    }

    // Для анонимного доступа (используется в AuthService)
    fun getOrCreateAnonymousUser(deviceId: String, username: String): User {
        return userRepository.findByDeviceId(deviceId) ?: createAnonymousUser(deviceId, username)
    }

    // Поиск пользователя по device_id
    fun getUserByDeviceId(deviceId: String): User {
        return userRepository.findByDeviceId(deviceId)
            ?: throw IllegalArgumentException("User not found for device: $deviceId")
    }

    // Поиск пользователя по email
    fun getUserByEmail(email: String): User {
        return userRepository.findByEmail(email)
            ?: throw IllegalArgumentException("User not found")
    }

    // Проверка существования email
    fun existsByEmail(email: String): Boolean {
        return userRepository.existsByEmail(email)
    }

    // Обновление профиля пользователя
    fun updateUserProfile(userId: UUID, username: String?, avatarUrl: String?): User {
        var user = getUserById(userId)

        username?.let { user.copy(username = it) }

        return userRepository.save(user)
    }

    // Создание нового анонимного пользователя
    private fun createAnonymousUser(deviceId: String, username: String): User {
        val user = User(
            username = username,
            deviceId = deviceId,
            isAnonymous = true
        )
        return userRepository.save(user)
    }
}