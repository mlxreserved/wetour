package com.example.wetour.dto

import com.example.wetour.repository.model.Trip
import com.example.wetour.utils.Currency
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.UUID

// Создание поездки
data class CreateTripRequest(
    @field:NotBlank
    @field:Size(min = 1, max = 200)
    val name: String,

    val description: String? = null,
    val currency: Currency = Currency.RUB
)

// Обновление поездки
data class UpdateTripRequest(
    @field:Size(min = 1, max = 200)
    val name: String? = null,
    val description: String? = null,
    val currency: Currency? = null
)

// Ответ поездки
data class TripResponse(
    val id: UUID,
    val name: String,
    val description: String?,
    val currency: Currency,
    val createdBy: UserResponse,
    val isTemporary: Boolean,
    val memberCount: Int,
    val totalExpenses: Double,
    val createdAt: Instant,
    val isActive: Boolean
)

// Результат создания анонимной поездки
data class TripCreationResult(
    val trip: TripResponse,
    val remainingTrips: Int,
    val message: String? = null
)

// Extension function для Trip
fun Trip.toResponse(memberCount: Int = 0, totalExpenses: Double = 0.0) = TripResponse(
    id = id!!,
    name = name,
    description = description,
    currency = currency,
    createdBy = createdBy.toResponse(),
    isTemporary = isTemporary,
    memberCount = memberCount,
    totalExpenses = totalExpenses,
    createdAt = createdAt!!,
    isActive = isActive
)