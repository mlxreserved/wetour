package com.example.wetour.service

import com.example.wetour.dto.AcceptInvitationRequest
import com.example.wetour.dto.InvitationInfoResponse
import com.example.wetour.dto.InvitationResponse
import com.example.wetour.dto.toResponse
import com.example.wetour.repository.TripInvitationRepository
import com.example.wetour.repository.TripMemberRepository
import com.example.wetour.repository.TripRepository
import com.example.wetour.repository.model.TripInvitation
import com.example.wetour.repository.model.TripMember
import com.example.wetour.repository.model.User
import com.example.wetour.utils.MemberType
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.UUID

@Service
class InvitationService(
    private val tripInvitationRepository: TripInvitationRepository,
    private val tripRepository: TripRepository,
    private val tripMemberRepository: TripMemberRepository
) {

    @Value("\${app.base-url:http://localhost:8080}")
    private lateinit var baseUrl: String

    fun createInvitation(tripId: UUID, user: User): InvitationResponse {
        val trip = tripRepository.findById(tripId)
            .orElseThrow { IllegalArgumentException("Trip not found") }

        if (trip.createdBy.id != user.id) {
            throw IllegalArgumentException("Only trip creator can create invitations")
        }

        val invitation = TripInvitation(
            trip = trip,
            createdBy = user,
            inviteToken = UUID.randomUUID().toString(),
            expiresAt = Instant.now().plus(Duration.ofDays(7))
        )

        val savedInvitation = tripInvitationRepository.save(invitation)

        return InvitationResponse(
            id = savedInvitation.id!!,
            tripId = tripId,
            inviteToken = savedInvitation.inviteToken,
            inviteUrl = "$baseUrl/join/${savedInvitation.inviteToken}",
            expiresAt = savedInvitation.expiresAt,
            createdAt = savedInvitation.createdAt!!
        )
    }

    fun getTripInvitations(tripId: UUID): List<InvitationResponse> {
        return tripInvitationRepository.findByTripIdAndIsActive(tripId, true).map { invitation ->
            InvitationResponse(
                id = invitation.id!!,
                tripId = tripId,
                inviteToken = invitation.inviteToken,
                inviteUrl = "$baseUrl/join/${invitation.inviteToken}",
                expiresAt = invitation.expiresAt,
                createdAt = invitation.createdAt!!
            )
        }
    }

    fun revokeInvitation(invitationId: UUID, user: User) {
        val invitation = tripInvitationRepository.findById(invitationId)
            .orElseThrow { IllegalArgumentException("Invitation not found") }

        if (invitation.createdBy.id != user.id) {
            throw IllegalArgumentException("Only invitation creator can revoke it")
        }

        val changedInvitation = invitation.copy(isActive = false)
        tripInvitationRepository.save(changedInvitation)
    }

    @Transactional
    fun acceptInvitation(token: String, request: AcceptInvitationRequest, user: User): TripMember {
        val invitation = tripInvitationRepository.findActiveByToken(token)
            ?: throw IllegalArgumentException("Invitation not found or expired")

        val selectedMember = tripMemberRepository.findById(request.selectedMemberId)
            .orElseThrow { IllegalArgumentException("Selected member not found") }

        if (selectedMember.trip.id != invitation.trip.id) {
            throw IllegalArgumentException("Selected member does not belong to this trip")
        }

        if (selectedMember.memberType != MemberType.TEMPORARY) {
            throw IllegalArgumentException("Can only claim temporary member spots")
        }

        // "Занимаем" место временного участника
        val changedSelectedMember = selectedMember.copy(
            user = user,
            memberType = MemberType.USER,
        )

        tripInvitationRepository.save(invitation)

        return tripMemberRepository.save(changedSelectedMember)
    }

    fun getInvitationInfo(token: String): InvitationInfoResponse {
        val invitation = tripInvitationRepository.findActiveByToken(token)
            ?: throw IllegalArgumentException("Invitation not found or expired")

        val temporaryMembers = tripMemberRepository.findTemporaryMembersByTripId(invitation.trip.id!!)

        return InvitationInfoResponse(
            tripId = invitation.trip.id!!,
            tripName = invitation.trip.name,
            creatorName = invitation.createdBy.username,
            temporaryMembers = temporaryMembers.map { it.toResponse() },
            expiresAt = invitation.expiresAt
        )
    }
}