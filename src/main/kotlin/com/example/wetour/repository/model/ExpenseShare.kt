package com.example.wetour.repository.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.util.*

@Entity
@Table(name = "expense_shares")
data class ExpenseShare(
    @Id
    @GeneratedValue
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    val expense: Expense,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_member_id", nullable = false)
    val tripMember: TripMember,

    @Column(nullable = false, precision = 12, scale = 2)
    val shareAmount: BigDecimal,

    @Column(name = "custom_share")
    val customShare: Boolean = false,

    @Column(name = "is_settled")
    val isSettled: Boolean = false,

    @Column(name = "settled_at")
    val settledAt: Instant? = null
)