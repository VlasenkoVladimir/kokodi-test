package com.vlasenko.kokodi_test.services

import com.vlasenko.kokodi_test.domain.User
import com.vlasenko.kokodi_test.dto.JwtAuthenticationResponse
import com.vlasenko.kokodi_test.dto.SignInRequest
import com.vlasenko.kokodi_test.dto.SignUpRequest
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

/**
 * Authentication service
 */
@Service
class AuthenticationService(
    private val userService: UserService,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager
) {

    /**
     * Register new User
     */
    fun signUp(request: SignUpRequest): JwtAuthenticationResponse {
        val user = User.Builder()
            .withName(request.name)
            .withUsername(request.username)
            .withPassword(passwordEncoder.encode(request.password))
            .build()

        userService.create(user)

        val jwt = jwtService.generateToken(user)

        return JwtAuthenticationResponse(jwt)
    }

    /**
     * Authenticate existed User
     */
    fun signIn(request: SignInRequest): JwtAuthenticationResponse {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.username, request.password)
        )

        return JwtAuthenticationResponse(
            jwtService.generateToken(
                userService.getUserDetailsService().loadUserByUsername(request.username)
            )
        )
    }
}