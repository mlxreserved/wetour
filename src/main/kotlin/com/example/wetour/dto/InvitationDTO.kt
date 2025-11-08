package com.example.wetour.dto

import java.time.Instant
import java.util.UUID

data class InvitationResponse(
    val id: UUID,
    val tripId: UUID,
    val inviteToken: String,
    val inviteUrl: String,
    val expiresAt: Instant,
    val createdAt: Instant
)

data class InvitationInfoResponse(
    val tripId: UUID,
    val tripName: String,
    val creatorName: String,
    val temporaryMembers: List<TripMemberResponse>,
    val expiresAt: Instant
)

data class AcceptInvitationRequest(
    val selectedMemberId: UUID
)