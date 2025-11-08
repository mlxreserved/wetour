package com.example.wetour.repository

import com.example.wetour.repository.model.Trip
import com.example.wetour.repository.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface TripRepository : JpaRepository<Trip, UUID> {
    fun findByCreatedBy(user: User): List<Trip>
    fun findByCreatedByAndIsTemporary(user: User, isTemporary: Boolean): List<Trip>
    fun countByCreatedByAndIsTemporary(user: User, isTemporary: Boolean): Int

    @Query("SELECT t FROM Trip t JOIN t.members tm WHERE tm.user.id = :userId AND t.isActive = true")
    fun findByUserId(@Param("userId") userId: UUID): List<Trip>

    @Query("SELECT COUNT(t) FROM Trip t WHERE t.createdBy.id = :userId AND t.isTemporary = true")
    fun countAnonymousTripsByUserId(@Param("userId") userId: UUID): Int
}