package com.example.wetour.controller

import com.example.wetour.dto.InvitationResponse
import com.example.wetour.service.InvitationService
import com.example.wetour.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/trips/{tripId}/invitations")
class InvitationController(
    private val invitationService: InvitationService,
    private val userService: UserService
) {

    @PostMapping
    fun createInvitation(
        @PathVariable tripId: UUID,
        @RequestHeader("Device-Id") deviceId: String
    ): InvitationResponse {
        val user = userService.getUserByDeviceId(deviceId)
        return invitationService.createInvitation(tripId, user)
    }

    @GetMapping
    fun getTripInvitations(@PathVariable tripId: UUID): List<InvitationResponse> {
        return invitationService.getTripInvitations(tripId)
    }

    @DeleteMapping("/{invitationId}")
    fun revokeInvitation(
        @PathVariable tripId: UUID,
        @PathVariable invitationId: UUID,
        @RequestHeader("Device-Id") deviceId: String
    ): ResponseEntity<Void> {
        val user = userService.getUserByDeviceId(deviceId)
        invitationService.revokeInvitation(invitationId, user)
        return ResponseEntity.noContent().build()
    }
}