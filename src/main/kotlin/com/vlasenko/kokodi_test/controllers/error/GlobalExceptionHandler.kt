package com.vlasenko.kokodi_test.controllers.error

import com.vlasenko.kokodi_test.exceptions.GameSessionException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Error controller
 */

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(ex: NoSuchElementException): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.message ?: "Resource not found")

    @ExceptionHandler(IllegalStateException::class)
    fun handleConflict(ex: IllegalStateException): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(ex.message ?: "Invalid operation")

    @ExceptionHandler(GameSessionException::class)
    fun handleGameSession(ex: GameSessionException): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(ex.message ?: "Game session error")

    @ExceptionHandler(Exception::class)
    fun handleGeneral(ex: Exception): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error occurred")
}