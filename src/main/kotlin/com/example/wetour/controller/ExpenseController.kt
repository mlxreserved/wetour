package com.example.wetour.controller

import com.example.wetour.dto.CreateExpenseRequest
import com.example.wetour.dto.ExpenseResponse
import com.example.wetour.dto.UpdateExpenseRequest
import com.example.wetour.dto.toResponse
import com.example.wetour.repository.ExpenseShareRepository
import com.example.wetour.service.ExpenseService
import com.example.wetour.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/trips/{tripId}/expenses")
class ExpenseController(
    private val expenseService: ExpenseService,
    private val userService: UserService,
    private val expenseShareRepository: ExpenseShareRepository
) {

    @PostMapping
    fun createExpense(
        @PathVariable tripId: UUID,
        @Valid @RequestBody request: CreateExpenseRequest,
        @RequestHeader("Device-Id") deviceId: String
    ): ExpenseResponse {
        val user = userService.getUserByDeviceId(deviceId)
        val expense = expenseService.createExpense(tripId, request, user)
        val shares = expenseShareRepository.findByExpenseId(expense.id!!).map { it.toResponse() }
        return expense.toResponse(shares)
    }

    @GetMapping
    fun getTripExpenses(@PathVariable tripId: UUID): List<ExpenseResponse> {
        val expenses = expenseService.getTripExpenses(tripId)
        return expenses.map { expense ->
            val shares = expenseShareRepository.findByExpenseId(expense.id!!).map { it.toResponse() }
            expense.toResponse(shares)
        }
    }

    @GetMapping("/{expenseId}")
    fun getExpense(
        @PathVariable tripId: UUID,
        @PathVariable expenseId: UUID
    ): ExpenseResponse {
        val expense = expenseService.getExpense(expenseId)
        val shares = expenseShareRepository.findByExpenseId(expenseId).map { it.toResponse() }
        return expense.toResponse(shares)
    }

    @PutMapping("/{expenseId}")
    fun updateExpense(
        @PathVariable tripId: UUID,
        @PathVariable expenseId: UUID,
        @Valid @RequestBody request: UpdateExpenseRequest,
        @RequestHeader("Device-Id") deviceId: String
    ): ExpenseResponse {
        val user = userService.getUserByDeviceId(deviceId)
        val expense = expenseService.updateExpense(expenseId, request, user)
        val shares = expenseShareRepository.findByExpenseId(expenseId).map { it.toResponse() }
        return expense.toResponse(shares)
    }

    @DeleteMapping("/{expenseId}")
    fun deleteExpense(
        @PathVariable tripId: UUID,
        @PathVariable expenseId: UUID,
        @RequestHeader("Device-Id") deviceId: String
    ): ResponseEntity<Void> {
        val user = userService.getUserByDeviceId(deviceId)
        expenseService.deleteExpense(expenseId, user)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{expenseId}/settle")
    fun settleExpense(
        @PathVariable tripId: UUID,
        @PathVariable expenseId: UUID,
        @RequestHeader("Device-Id") deviceId: String
    ): ResponseEntity<Void> {
        val user = userService.getUserByDeviceId(deviceId)
        expenseService.settleExpenseForUser(expenseId, user)
        return ResponseEntity.noContent().build()
    }
}