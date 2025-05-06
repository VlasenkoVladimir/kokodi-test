package com.vlasenko.kokodi_test.controllers.api.rest.v1

import com.vlasenko.kokodi_test.dto.JwtAuthenticationResponse
import com.vlasenko.kokodi_test.dto.SignInRequest
import com.vlasenko.kokodi_test.dto.SignUpRequest
import com.vlasenko.kokodi_test.services.AuthenticationService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Endpoints for sign-up/sign-in users
 */

@RestController
@RequestMapping("/auth")
class AuthController(

    private val authenticationService: AuthenticationService
) {

    @PostMapping("/sign-up")
    fun signUp(@RequestBody request: SignUpRequest): JwtAuthenticationResponse = authenticationService.signUp(request)

    @PostMapping("/sign-in")
    fun signIn(@RequestBody request: SignInRequest): JwtAuthenticationResponse = authenticationService.signIn(request)
}