package com.vlasenko.kokodi_test.domain

import com.vlasenko.kokodi_test.domain.enums.GameStatus
import jakarta.persistence.*
import org.springframework.context.annotation.Scope

/**
 * Game session entity
 */

@Entity
@Table(name = "games")
//@Scope("prototype")
class GameSession(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long,

    // awaitingForPlayers or inProgress or Finished
    @Column(name = "status", nullable = false)
    val status: GameStatus,

    // Every turn each player open a top card
    @Column(name = "deck", nullable = false)
    val deck: List<Card> = ArrayList(),

    // History of turns
    @Column(name = "turns", nullable = false)
    val turns: List<Card> = ArrayList(),

    // userId: Long , queue of turns
    @Column(name = "players", nullable = false)
    val players: List<Long> = ArrayList(),

    // userId: Long, scores: Int
    @Column(name = "scores", nullable = false)
    var scores: Map<Long, Int>,

    // userId witch turn is now
    @Column(name = "active_user", nullable = false)
    var activeUserId: Long = 0,

    // How many turns will be skipped
    @Column(name = "skip_counter", nullable = false)
    var skipCounter: Int = 0
) {

    fun nextTurn() {
        TODO("1st turn at start and finish turn when first player reached 30 scores.")
    }

    fun closeAndSave() {
        TODO("After game ended save entity and close it.")
    }
}