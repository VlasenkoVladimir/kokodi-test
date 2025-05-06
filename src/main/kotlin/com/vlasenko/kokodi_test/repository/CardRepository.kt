package com.vlasenko.kokodi_test.repository

import com.vlasenko.kokodi_test.domain.Card
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Card repository
 */

interface CardRepository: JpaRepository<Card, Long>