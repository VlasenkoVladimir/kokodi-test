package com.vlasenko.kokodi_test.dto

import com.vlasenko.kokodi_test.domain.Card
import com.vlasenko.kokodi_test.domain.enums.GameStatus

/**
 * DTO for GameSession
 */

data class GameSessionStatus(
    val id: Long,
    val status: GameStatus,
    val deck: MutableList<Card>,
    val turnPointer: Int,
    val players: MutableList<Long>,
    val scores: MutableMap<Long, Int>,
    val activeUserId: Long,
    val skipCounter: Int
)