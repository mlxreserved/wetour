package com.example.wetour.controller

import com.example.wetour.dto.AddTemporaryMemberRequest
import com.example.wetour.dto.AddUserMemberRequest
import com.example.wetour.dto.TripMemberResponse
import com.example.wetour.dto.UpdateMemberRequest
import com.example.wetour.dto.toResponse
import com.example.wetour.service.TripMemberService
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
@RequestMapping("/api/trips/{tripId}/members")
class TripMemberController(
    private val tripMemberService: TripMemberService,
    private val userService: UserService
) {

    @PostMapping("/temporary")
    fun addTemporaryMember(
        @PathVariable tripId: UUID,
        @Valid @RequestBody request: AddTemporaryMemberRequest,
        @RequestHeader("Device-Id") deviceId: String
    ): TripMemberResponse {
        val user = userService.getUserByDeviceId(deviceId)
        return tripMemberService.addTemporaryMember(tripId, request, user).toResponse()
    }

    @PostMapping("/user")
    fun addUserMember(
        @PathVariable tripId: UUID,
        @Valid @RequestBody request: AddUserMemberRequest,
        @RequestHeader("Device-Id") deviceId: String
    ): TripMemberResponse {
        val user = userService.getUserByDeviceId(deviceId)
        return tripMemberService.addUserMember(tripId, request, user).toResponse()
    }

    @GetMapping
    fun getTripMembers(@PathVariable tripId: UUID): List<TripMemberResponse> {
        return tripMemberService.getTripMembers(tripId).map { it.toResponse() }
    }

    @DeleteMapping("/{memberId}")
    fun removeMember(
        @PathVariable tripId: UUID,
        @PathVariable memberId: UUID,
        @RequestHeader("Device-Id") deviceId: String
    ): ResponseEntity<Void> {
        val user = userService.getUserByDeviceId(deviceId)
        tripMemberService.removeMember(tripId, memberId, user)
        return ResponseEntity.noContent().build()
    }

    @PutMapping("/{memberId}")
    fun updateMember(
        @PathVariable tripId: UUID,
        @PathVariable memberId: UUID,
        @Valid @RequestBody request: UpdateMemberRequest,
        @RequestHeader("Device-Id") deviceId: String
    ): TripMemberResponse {
        val user = userService.getUserByDeviceId(deviceId)
        return tripMemberService.updateMember(memberId, request, user).toResponse()
    }
}