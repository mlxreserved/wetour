package com.example.wetour.service

import com.example.wetour.dto.CreateExpenseRequest
import com.example.wetour.dto.UpdateExpenseRequest
import com.example.wetour.repository.ExpenseRepository
import com.example.wetour.repository.ExpenseShareRepository
import com.example.wetour.repository.TripMemberRepository
import com.example.wetour.repository.TripRepository
import com.example.wetour.repository.model.Expense
import com.example.wetour.repository.model.ExpenseShare
import com.example.wetour.repository.model.User
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ExpenseService(
    private val expenseRepository: ExpenseRepository,
    private val expenseShareRepository: ExpenseShareRepository,
    private val tripMemberRepository: TripMemberRepository,
    private val tripRepository: TripRepository
) {

    @Transactional
    fun createExpense(tripId: UUID, request: CreateExpenseRequest, user: User): Expense {
        val trip = tripRepository.findById(tripId)
            .orElseThrow { IllegalArgumentException("Trip not found") }

        verifyUserHasAccessToTrip(tripId, user)

        val paidByMember = tripMemberRepository.findById(request.paidByMemberId)
            .orElseThrow { IllegalArgumentException("Payer member not found") }

        if (paidByMember.trip.id != trip.id) {
            throw IllegalArgumentException("Payer does not belong to this trip")
        }

        val expense = Expense(
            trip = trip,
            paidByMember = paidByMember,
            amount = request.amount,
            description = request.description,
            category = request.category,
            expenseDate = request.expenseDate ?: java.time.LocalDate.now()
        )

        val savedExpense = expenseRepository.save(expense)

        // Создаем доли для всех участников поездки
        createExpenseShares(savedExpense, tripId)

        return savedExpense
    }

    private fun createExpenseShares(expense: Expense, tripId: UUID) {
        val members = tripMemberRepository.findByTripId(tripId)
        val shareAmount = expense.amount.divide(members.size.toBigDecimal(), 2, java.math.RoundingMode.HALF_UP)

        members.forEach { member ->
            val expenseShare = ExpenseShare(
                expense = expense,
                tripMember = member,
                shareAmount = shareAmount,
                customShare = false
            )
            expenseShareRepository.save(expenseShare)
        }
    }

    fun getTripExpenses(tripId: UUID): List<Expense> {
        return expenseRepository.findByTripIdWithPayer(tripId)
    }

    fun getExpense(expenseId: UUID): Expense {
        return expenseRepository.findById(expenseId)
            .orElseThrow { IllegalArgumentException("Expense not found") }
    }

    fun updateExpense(expenseId: UUID, request: UpdateExpenseRequest, user: User): Expense {
        var expense = getExpense(expenseId)
        verifyUserHasAccessToTrip(expense.trip.id!!, user)

        request.amount?.let { expense = expense.copy(amount = it) }
        request.description?.let { expense = expense.copy(description = it) }
        request.category?.let { expense = expense.copy(category = it) }

        return expenseRepository.save(expense)
    }

    @Transactional
    fun deleteExpense(expenseId: UUID, user: User) {
        val expense = getExpense(expenseId)
        verifyUserHasAccessToTrip(expense.trip.id!!, user)
        expenseRepository.delete(expense)
    }

    fun settleExpenseForUser(expenseId: UUID, user: User) {
        val expense = getExpense(expenseId)
        verifyUserHasAccessToTrip(expense.trip.id!!, user)

        val userMember = tripMemberRepository.findByTripIdAndUserId(expense.trip.id!!, user.id!!)
            ?: throw IllegalArgumentException("User is not a member of this trip")

        val expenseShare = expenseShareRepository.findByExpenseIdAndTripMemberId(expenseId, userMember.id!!)
            ?: throw IllegalArgumentException("Expense share not found")

        val changedExpenseShare = expenseShare.copy(
            isSettled = true,
            settledAt = java.time.Instant.now()
        )

        expenseShareRepository.save(changedExpenseShare)
    }

    private fun verifyUserHasAccessToTrip(tripId: UUID, user: User) {
        if (!tripMemberRepository.existsByTripIdAndUserId(tripId, user.id!!)) {
            throw IllegalArgumentException("Access denied to trip")
        }
    }
}