package com.example.wetour.repository

import com.example.wetour.repository.model.Expense
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface ExpenseRepository : JpaRepository<Expense, UUID> {
    @Query("SELECT e FROM Expense e JOIN FETCH e.paidByMember WHERE e.trip.id = :tripId")
    fun findByTripIdWithPayer(@Param("tripId") tripId: UUID): List<Expense>

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.trip.id = :tripId")
    fun getTotalExpensesByTripId(@Param("tripId") tripId: UUID): Double

    @Query("SELECT e FROM Expense e WHERE e.trip.id = :tripId AND e.paidByMember.user.id = :userId")
    fun findByTripIdAndPayerUserId(@Param("tripId") tripId: UUID, @Param("userId") userId: UUID): List<Expense>
}