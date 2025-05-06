package com.vlasenko.kokodi_test.repository

import com.vlasenko.kokodi_test.domain.Turn
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Turn repository
 */

interface TurnRepository: JpaRepository<Turn, Long>