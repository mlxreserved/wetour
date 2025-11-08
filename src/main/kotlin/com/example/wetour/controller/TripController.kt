package com.example.wetour.controller

import com.example.wetour.dto.CreateTripRequest
import com.example.wetour.dto.TripResponse
import com.example.wetour.dto.UpdateTripRequest
import com.example.wetour.dto.toResponse
import com.example.wetour.service.TripService
import com.example.wetour.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/trips")
class TripController(
    private val tripService: TripService,
    private val userService: UserService
) {

    @PostMapping
    fun createTrip(
        @Valid @RequestBody request: CreateTripRequest,
        @RequestHeader("Device-Id") deviceId: String
    ): ResponseEntity<TripResponse> {
        val user = userService.getOrCreateUserByDeviceId(deviceId, "Traveler")
        val trip = tripService.createTrip(request, user)
        return ResponseEntity.status(HttpStatus.CREATED).body(trip.toResponse())
    }

    @GetMapping
    fun getUserTrips(@RequestHeader("Device-Id") deviceId: String): List<TripResponse> {
        val user = userService.getUserByDeviceId(deviceId)
        return tripService.getUserTrips(user).map { it.toResponse() }
    }

    @GetMapping("/{tripId}")
    fun getTrip(
        @PathVariable tripId: UUID,
        @RequestHeader("Device-Id") deviceId: String
    ): TripResponse {
        val user = userService.getUserByDeviceId(deviceId)
        return tripService.getTripWithStats(tripId, user)
    }

    @PutMapping("/{tripId}")
    fun updateTrip(
        @PathVariable tripId: UUID,
        @Valid @RequestBody request: UpdateTripRequest,
        @RequestHeader("Device-Id") deviceId: String
    ): TripResponse {
        val user = userService.getUserByDeviceId(deviceId)
        return tripService.updateTrip(tripId, request, user).toResponse()
    }

    @DeleteMapping("/{tripId}")
    fun archiveTrip(
        @PathVariable tripId: UUID,
        @RequestHeader("Device-Id") deviceId: String
    ): ResponseEntity<Void> {
        val user = userService.getUserByDeviceId(deviceId)
        tripService.archiveTrip(tripId, user)
        return ResponseEntity.noContent().build()
    }
}