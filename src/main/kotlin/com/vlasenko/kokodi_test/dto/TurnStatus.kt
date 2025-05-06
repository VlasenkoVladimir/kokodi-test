package com.vlasenko.kokodi_test.dto

import com.vlasenko.kokodi_test.domain.enums.GameStatus

/**
 * DTO for turn status
 */

data class TurnStatus(
    val id: Long,
    val status: GameStatus,
    val turnPointer: Int,
    val players: MutableList<Long>,
    val scores: MutableMap<Long, Int>,
    val activeUserId: Long,
    val skipCounter: Int
)