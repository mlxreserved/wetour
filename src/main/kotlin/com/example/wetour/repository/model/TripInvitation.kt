package com.example.wetour.repository.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import java.util.*

@Entity
@Table(name = "trip_invitations")
data class TripInvitation(
    @Id
    @GeneratedValue
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    val trip: Trip,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    val createdBy: User,

    @Column(name = "invite_token", nullable = false, unique = true, length = 100)
    val inviteToken: String,

    @Column(name = "expires_at", nullable = false)
    val expiresAt: Instant,

    @CreationTimestamp
    @Column(name = "created_at")
    val createdAt: Instant? = null,

    @Column(name = "is_active")
    val isActive: Boolean = true
)