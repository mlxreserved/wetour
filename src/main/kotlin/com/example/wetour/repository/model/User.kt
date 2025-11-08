package com.example.wetour.repository.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue
    val id: UUID? = null,

    @Column(unique = true)
    val email: String? = null,

    @Column(nullable = false, length = 100)
    val username: String,

    @Column(name = "password_hash")
    val passwordHash: String? = null,

    @Column(name = "is_anonymous", nullable = false)
    val isAnonymous: Boolean = false,

    @Column(name = "device_id", unique = true)
    val deviceId: String? = null,

    @CreationTimestamp
    @Column(name = "created_at")
    val createdAt: Instant? = null,

    @UpdateTimestamp
    @Column(name = "updated_at")
    val updatedAt: Instant? = null
)