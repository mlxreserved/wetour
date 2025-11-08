package com.example.wetour.controller

import com.example.wetour.dto.RegisterRequest
import com.example.wetour.dto.UserResponse
import com.example.wetour.dto.toResponse
import com.example.wetour.service.UserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {

    @GetMapping("/me")
    fun getCurrentUser(@RequestHeader("Device-Id") deviceId: String): UserResponse {
        val user = userService.getUserByDeviceId(deviceId)
        return user.toResponse()
    }

    @PostMapping("/register")
    fun registerUser(
        @RequestHeader("Device-Id") deviceId: String,
        @RequestBody request: RegisterRequest
    ): UserResponse {
        val user = userService.getUserByDeviceId(deviceId)
        val registeredUser = userService.convertToRegisteredUser(user, request.email, request.password)
        return registeredUser.toResponse()
    }
}