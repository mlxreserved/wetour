package com.example.wetour.repository

import com.example.wetour.repository.model.Trip
import com.example.wetour.repository.model.TripMember
import com.example.wetour.repository.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface TripMemberRepository : JpaRepository<TripMember, UUID> {
    fun findByTripId(tripId: UUID): List<TripMember>
    fun findByTripIdAndUserId(tripId: UUID, userId: UUID): TripMember?
    fun existsByTripIdAndUserId(tripId: UUID, userId: UUID): Boolean
    fun existsByTripIdAndDisplayName(tripId: UUID, displayName: String): Boolean

    @Query("SELECT tm FROM TripMember tm WHERE tm.trip.id = :tripId AND tm.user IS NULL")
    fun findTemporaryMembersByTripId(@Param("tripId") tripId: UUID): List<TripMember>

    @Query("SELECT tm FROM TripMember tm WHERE tm.trip.id = :tripId AND tm.user.id = :userId")
    fun findByTripIdAndUserIdWithUser(@Param("tripId") tripId: UUID, @Param("userId") userId: UUID): TripMember?
}