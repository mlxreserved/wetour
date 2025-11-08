package com.example.wetour.service

import com.example.wetour.dto.CreateTripRequest
import com.example.wetour.dto.TripResponse
import com.example.wetour.dto.UpdateTripRequest
import com.example.wetour.dto.toResponse
import com.example.wetour.repository.ExpenseRepository
import com.example.wetour.repository.TripMemberRepository
import com.example.wetour.repository.TripRepository
import com.example.wetour.repository.model.Trip
import com.example.wetour.repository.model.TripMember
import com.example.wetour.repository.model.User
import com.example.wetour.utils.MemberType
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TripService(
    private val tripRepository: TripRepository,
    private val tripMemberRepository: TripMemberRepository,
    private val expenseRepository: ExpenseRepository,
    private val authService: AuthService
) {

    companion object {
        const val MAX_ANONYMOUS_TRIPS = 3
    }

    @Transactional
    fun createTrip(request: CreateTripRequest, userId: UUID): Trip {
        val user = authService.getUserById(userId)

        // Проверяем лимит для анонимных пользователей
        if (user.isAnonymous) {
            val tripCount = tripRepository.countAnonymousTripsByUserId(user.id!!)
            if (tripCount >= MAX_ANONYMOUS_TRIPS) {
                throw IllegalArgumentException("Maximum $MAX_ANONYMOUS_TRIPS trips allowed for anonymous users. Please register to create more trips.")
            }
        }

        val trip = Trip(
            name = request.name,
            description = request.description,
            currency = request.currency,
            createdBy = user,
            isTemporary = user.isAnonymous
        )

        val savedTrip = tripRepository.save(trip)

        // Автоматически добавляем создателя как участника
        val creatorMember = TripMember(
            trip = savedTrip,
            user = user,
            memberType = if (user.isAnonymous) MemberType.ANONYMOUS
            else MemberType.USER,
            displayName = user.username
        )
        tripMemberRepository.save(creatorMember)

        return savedTrip
    }

    fun getUserTrips(userId: UUID): List<Trip> {
        return tripRepository.findByUserId(userId)
    }

    fun getTrip(tripId: UUID, userId: UUID): Trip {
        val trip = tripRepository.findById(tripId)
            .orElseThrow { IllegalArgumentException("Trip not found") }

        verifyUserHasAccessToTrip(tripId, userId)
        return trip
    }

    fun updateTrip(tripId: UUID, request: UpdateTripRequest, userId: UUID): Trip {
        var trip = getTrip(tripId, userId)

        request.name?.let { trip = trip.copy(name = it) }
        request.description?.let { trip = trip.copy(description = it) }
        request.currency?.let { trip = trip.copy(currency = it) }

        return tripRepository.save(trip)
    }

    @Transactional
    fun archiveTrip(tripId: UUID, userId: UUID) {
        var trip = getTrip(tripId, userId)
        trip = trip.copy(isActive = false)
        tripRepository.save(trip)
    }

    fun getTripWithStats(tripId: UUID, userId: UUID): TripResponse {
        val trip = getTrip(tripId, userId)
        val memberCount = tripMemberRepository.findByTripId(tripId).size
        val totalExpenses = expenseRepository.getTotalExpensesByTripId(tripId)

        return trip.toResponse(memberCount, totalExpenses)
    }

    private fun verifyUserHasAccessToTrip(tripId: UUID, userId: UUID) {
        if (!tripMemberRepository.existsByTripIdAndUserId(tripId, userId)) {
            throw IllegalArgumentException("Access denied to trip")
        }
    }
}