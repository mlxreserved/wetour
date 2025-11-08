package com.example.wetour.dto

import com.example.wetour.repository.model.TripMember
import com.example.wetour.utils.MemberType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.UUID

// Добавление временного участника
data class AddTemporaryMemberRequest(
    @field:NotBlank
    @field:Size(min = 1, max = 100)
    val displayName: String,

    val colorHex: String? = null
)

// Добавление пользовател
data class AddUserMemberRequest(
    @field:NotNull
    val userId: UUID
)

// Обновление участника
data class UpdateMemberRequest(
    @field:Size(min = 1, max = 100)
    val displayName: String? = null,
    val colorHex: String? = null,
    val avatarUrl: String? = null
)

// Ответ участника
data class TripMemberResponse(
    val id: UUID,
    val user: UserResponse?,
    val memberType: MemberType,
    val displayName: String,
    val joinedAt: Instant
)

// Extension function для TripMember
fun TripMember.toResponse() = TripMemberResponse(
    id = id!!,
    user = user?.toResponse(),
    memberType = memberType,
    displayName = displayName,
    joinedAt = joinedAt!!
)