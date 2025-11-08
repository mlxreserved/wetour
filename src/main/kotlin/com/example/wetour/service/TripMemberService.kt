package com.example.wetour.service

import com.example.wetour.dto.AddTemporaryMemberRequest
import com.example.wetour.dto.AddUserMemberRequest
import com.example.wetour.dto.UpdateMemberRequest
import com.example.wetour.repository.TripMemberRepository
import com.example.wetour.repository.TripRepository
import com.example.wetour.repository.model.TripMember
import com.example.wetour.repository.model.User
import com.example.wetour.utils.MemberType
import org.springframework.stereotype.Service
import com.example.wetour.repository.model.Trip
import java.util.UUID

@Service
class TripMemberService(
    private val tripRepository: TripRepository,
    private val tripMemberRepository: TripMemberRepository,
    private val authService: AuthService
) {

    fun addTemporaryMember(tripId: UUID, request: AddTemporaryMemberRequest, userId: UUID): TripMember {
        val trip = getTripWithAccess(tripId, userId)

        if (tripMemberRepository.existsByTripIdAndDisplayName(tripId, request.displayName)) {
            throw IllegalArgumentException("Member with this name already exists")
        }

        val member = TripMember(
            trip = trip,
            user = null,
            memberType = MemberType.TEMPORARY,
            displayName = request.displayName,
        )

        return tripMemberRepository.save(member)
    }

    fun addUserMember(tripId: UUID, request: AddUserMemberRequest, userId: UUID): TripMember {
        val trip = getTripWithAccess(tripId, userId)
        val targetUser = authService.getUserById(request.userId)

        if (tripMemberRepository.existsByTripIdAndUserId(tripId, targetUser.id!!)) {
            throw IllegalArgumentException("User is already a member")
        }

        val member = TripMember(
            trip = trip,
            user = targetUser,
            memberType = if (targetUser.isAnonymous) MemberType.ANONYMOUS else MemberType.USER,
            displayName = targetUser.username
        )

        return tripMemberRepository.save(member)
    }

    fun getTripMembers(tripId: UUID): List<TripMember> {
        return tripMemberRepository.findByTripId(tripId)
    }

    fun removeMember(tripId: UUID, memberId: UUID, userId: UUID) {
        val trip = getTripWithAccess(tripId, userId)
        val member = tripMemberRepository.findById(memberId)
            .orElseThrow { IllegalArgumentException("Member not found") }

        if (member.trip.id != trip.id) {
            throw IllegalArgumentException("Member does not belong to this trip")
        }

        // Нельзя удалить создателя
        if (member.user?.id == trip.createdBy.id) {
            throw IllegalArgumentException("Cannot remove trip creator")
        }

        tripMemberRepository.delete(member)
    }

    fun updateMember(memberId: UUID, request: UpdateMemberRequest, userId: UUID): TripMember {
        var member = tripMemberRepository.findById(memberId)
            .orElseThrow { IllegalArgumentException("Member not found") }

        getTripWithAccess(member.trip.id!!, userId)

        request.displayName?.let {
            if (tripMemberRepository.existsByTripIdAndDisplayName(member.trip.id!!, it)) {
                throw IllegalArgumentException("Member with this name already exists")
            }
            member = member.copy(displayName = it)
        }

        return tripMemberRepository.save(member)
    }

    private fun getTripWithAccess(tripId: UUID, userId: UUID): Trip {
        val trip = tripRepository.findById(tripId)
            .orElseThrow { IllegalArgumentException("Trip not found") }

        if (!tripMemberRepository.existsByTripIdAndUserId(tripId, userId)) {
            throw IllegalArgumentException("Access denied to trip")
        }

        return trip
    }
}