package com.example.wetour.repository

import com.example.wetour.repository.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface UserRepository : JpaRepository<User, UUID> {
    fun findByDeviceId(deviceId: String): User?
    fun findByEmail(email: String): User?
    fun existsByEmail(email: String): Boolean

    @Query("SELECT u FROM User u WHERE u.deviceId = :deviceId AND u.isAnonymous = true")
    fun findAnonymousByDeviceId(@Param("deviceId") deviceId: String): User?
}