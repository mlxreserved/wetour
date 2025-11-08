package com.example.wetour.dto

import com.example.wetour.repository.model.Expense
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

// Создание расхода
data class CreateExpenseRequest(
    @field:NotNull
    val paidByMemberId: UUID,

    @field:NotNull
    @field:DecimalMin("0.01")
    val amount: BigDecimal,

    @field:NotBlank
    @field:Size(max = 500)
    val description: String,

    val category: String = "other",
    val receiptImageUrl: String? = null,
    val expenseDate: LocalDate? = null
)

// Обновление расхода
data class UpdateExpenseRequest(
    @field:DecimalMin("0.01")
    val amount: BigDecimal? = null,

    @field:Size(max = 500)
    val description: String? = null,

    val category: String? = null,
    val receiptImageUrl: String? = null
)

// Ответ расхода
data class ExpenseResponse(
    val id: UUID,
    val amount: BigDecimal,
    val description: String,
    val category: String,
    val paidBy: TripMemberResponse,
    val expenseDate: LocalDate,
    val shares: List<ExpenseShareResponse>,
    val createdAt: Instant
)

// Extension function для Expense
fun Expense.toResponse(shares: List<ExpenseShareResponse> = emptyList()) = ExpenseResponse(
    id = id!!,
    amount = amount,
    description = description,
    category = category,
    paidBy = paidByMember.toResponse(),
    expenseDate = expenseDate,
    shares = shares,
    createdAt = createdAt!!
)