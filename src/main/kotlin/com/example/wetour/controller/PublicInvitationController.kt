package com.example.wetour.controller

import com.example.wetour.dto.AcceptInvitationRequest
import com.example.wetour.dto.InvitationInfoResponse
import com.example.wetour.dto.TripMemberResponse
import com.example.wetour.dto.toResponse
import com.example.wetour.service.InvitationService
import com.example.wetour.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/public/invitations")
class PublicInvitationController(
    private val invitationService: InvitationService,
    private val userService: UserService
) {

    @GetMapping("/{token}")
    fun getInvitationInfo(@PathVariable token: String): InvitationInfoResponse {
        return invitationService.getInvitationInfo(token)
    }

    @PostMapping("/{token}/accept")
    fun acceptInvitation(
        @PathVariable token: String,
        @RequestBody request: AcceptInvitationRequest,
        @RequestHeader("Device-Id") deviceId: String
    ): ResponseEntity<TripMemberResponse> {
        val user = userService.getOrCreateUserByDeviceId(deviceId, "New User")
        val member = invitationService.acceptInvitation(token, request, user)
        return ResponseEntity.status(HttpStatus.CREATED).body(member.toResponse())
    }
}