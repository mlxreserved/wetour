package com.example.wetour.dto

import com.example.wetour.repository.model.ExpenseShare
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class ExpenseShareResponse(
    val id: UUID,
    val tripMember: TripMemberResponse,
    val shareAmount: BigDecimal,
    val isSettled: Boolean,
    val settledAt: Instant?
)

fun ExpenseShare.toResponse() = ExpenseShareResponse(
    id = id!!,
    tripMember = tripMember.toResponse(),
    shareAmount = shareAmount,
    isSettled = isSettled,
    settledAt = settledAt
)