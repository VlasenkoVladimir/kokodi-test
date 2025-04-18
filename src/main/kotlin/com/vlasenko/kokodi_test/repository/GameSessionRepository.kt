package com.vlasenko.kokodi_test.repository

import com.vlasenko.kokodi_test.domain.GameSession
import org.springframework.data.jpa.repository.JpaRepository

/**
 * GameSession repository
 */

interface GameSessionRepository: JpaRepository<GameSession, Long>