package com.example.wetour.repository

import com.example.wetour.repository.model.ExpenseShare
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface ExpenseShareRepository : JpaRepository<ExpenseShare, UUID> {
    fun findByExpenseId(expenseId: UUID): List<ExpenseShare>
    fun findByTripMemberId(tripMemberId: UUID): List<ExpenseShare>
    fun findByExpenseIdAndTripMemberId(expenseId: UUID, tripMemberId: UUID): ExpenseShare?

    @Query("SELECT es FROM ExpenseShare es WHERE es.expense.id = :expenseId AND es.tripMember.user.id = :userId")
    fun findByExpenseIdAndUserId(@Param("expenseId") expenseId: UUID, @Param("userId") userId: UUID): ExpenseShare?

    @Query("SELECT es FROM ExpenseShare es WHERE es.tripMember.id = :memberId AND es.isSettled = false")
    fun findUnsettledSharesByMemberId(@Param("memberId") memberId: UUID): List<ExpenseShare>

    @Query("SELECT COALESCE(SUM(es.shareAmount), 0) FROM ExpenseShare es WHERE es.tripMember.id = :memberId AND es.isSettled = false")
    fun getTotalUnsettledAmountByMemberId(@Param("memberId") memberId: UUID): Double
}