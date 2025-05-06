package com.vlasenko.kokodi_test.dto

/**
 * DTO to register new User
 */

class SignUpRequest(
    val name: String,
    val username: String,
    val password: String
)