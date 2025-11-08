package com.example.wetour.repository

import com.example.wetour.repository.model.TripInvitation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface TripInvitationRepository : JpaRepository<TripInvitation, UUID> {
    fun findByInviteToken(token: String): TripInvitation?
    fun findByTripId(tripId: UUID): List<TripInvitation>
    fun findByTripIdAndIsActive(tripId: UUID, isActive: Boolean): List<TripInvitation>

    @Query("SELECT ti FROM TripInvitation ti WHERE ti.inviteToken = :token AND ti.isActive = true AND ti.expiresAt > CURRENT_TIMESTAMP")
    fun findActiveByToken(@Param("token") token: String): TripInvitation?
}