package com.example.wetour.service

import com.example.wetour.dto.BalanceResponse
import com.example.wetour.dto.DebtResponse
import com.example.wetour.dto.toResponse
import com.example.wetour.repository.ExpenseRepository
import com.example.wetour.repository.ExpenseShareRepository
import com.example.wetour.repository.TripMemberRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.UUID

@Service
class BalanceService(
    private val tripMemberRepository: TripMemberRepository,
    private val expenseRepository: ExpenseRepository,
    private val expenseShareRepository: ExpenseShareRepository
) {

    fun calculateBalances(tripId: UUID): List<BalanceResponse> {
        val members = tripMemberRepository.findByTripId(tripId)
        val balances = mutableListOf<BalanceResponse>()

        members.forEach { member ->
            val totalPaid = calculateTotalPaidByMember(member.id!!)
            val totalOwed = calculateTotalOwedByMember(member.id!!)
            val balance = totalPaid - totalOwed

            balances.add(
                BalanceResponse(
                    member = member.toResponse(),
                    totalPaid = totalPaid,
                    totalDebt = totalOwed,
                    balance = balance
                )
            )
        }

        return balances
    }

    fun calculateSimplifiedDebts(tripId: UUID): List<DebtResponse> {
        val balances = calculateBalances(tripId)
        val creditors = balances.filter { it.balance > BigDecimal.ZERO }.sortedByDescending { it.balance }.toMutableList()
        val debtors = balances.filter { it.balance < BigDecimal.ZERO }.sortedBy { it.balance }.toMutableList()

        val debts = mutableListOf<DebtResponse>()
        var creditorIndex = 0
        var debtorIndex = 0

        while (creditorIndex < creditors.size && debtorIndex < debtors.size) {
            val creditor = creditors[creditorIndex]
            val debtor = debtors[debtorIndex]

            val amount = minOf(creditor.balance, -debtor.balance)

            debts.add(
                DebtResponse(
                    fromMember = debtor.member,
                    toMember = creditor.member,
                    amount = amount
                )
            )

            if (creditor.balance == amount) {
                creditorIndex++
            } else {
                creditors[creditorIndex] = creditor.copy(balance = creditor.balance - amount)
            }

            if (-debtor.balance == amount) {
                debtorIndex++
            } else {
                debtors[debtorIndex] = debtor.copy(balance = debtor.balance + amount)
            }
        }

        return debts
    }

    private fun calculateTotalPaidByMember(memberId: UUID): BigDecimal {
        val expenses = expenseRepository.findByTripIdAndPayerUserId(getTripIdByMember(memberId), getUserIdByMember(memberId))
        return expenses.fold(BigDecimal.ZERO) { acc, expense -> acc + expense.amount }
    }

    private fun calculateTotalOwedByMember(memberId: UUID): BigDecimal {
        val shares = expenseShareRepository.findByTripMemberId(memberId)
        return shares.filter { !it.isSettled }
            .fold(BigDecimal.ZERO) { acc, share -> acc + share.shareAmount }
    }

    private fun getTripIdByMember(memberId: UUID): UUID {
        val member = tripMemberRepository.findById(memberId)
            .orElseThrow { IllegalArgumentException("Member not found") }
        return member.trip.id!!
    }

    private fun getUserIdByMember(memberId: UUID): UUID {
        val member = tripMemberRepository.findById(memberId)
            .orElseThrow { IllegalArgumentException("Member not found") }
        return member.user?.id ?: throw IllegalArgumentException("Member has no user")
    }
}