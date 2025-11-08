package com.example.wetour.dto

import java.math.BigDecimal


data class BalanceResponse(
    val member: TripMemberResponse,
    val totalPaid: BigDecimal,
    val totalDebt: BigDecimal,
    val balance: BigDecimal
)

data class DebtResponse(
    val fromMember: TripMemberResponse,
    val toMember: TripMemberResponse,
    val amount: BigDecimal
)