package com.vlasenko.kokodi_test.domain

import com.vlasenko.kokodi_test.domain.enums.GameStatus
import com.vlasenko.kokodi_test.dto.GameSessionStatus
import com.vlasenko.kokodi_test.dto.TurnStatus
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

/**
 * Game session entity
 */

@Entity
@Table(name = "sessions")
class GameSession(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long?,

    @Column(name = "status", nullable = false)
    var status: GameStatus,

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "session_deck",
        joinColumns = [JoinColumn(name = "session_id")],
        indexes = [Index(columnList = "list_index")]
    )
    @OrderColumn(name = "list_index")
    var deck: MutableList<Card>,

    @Column(name = "turn_pointer", nullable = false)
    var turnPointer: Int = 0,

    @Column(name = "players", nullable = false)
    var players: MutableList<Long>,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "scores", nullable = false)
    var scores: MutableMap<Long, Int>,

    @Column(name = "active_user_id", nullable = false)
    var activeUserId: Long = 0L,

    @Column(name = "skip_counter", nullable = false)
    var skipCounter: Int = 0
) {
    fun toTurnStatus(): TurnStatus = TurnStatus(
        id = this.id!!,
        status = this.status,
        turnPointer = this.turnPointer,
        players = this.players,
        scores = this.scores,
        activeUserId = this.activeUserId,
        skipCounter = this.skipCounter
    )

    fun toGameSessionStatus(): GameSessionStatus = GameSessionStatus(
        id = this.id!!,
        status = this.status,
        deck = this.deck,
        turnPointer = this.turnPointer,
        players = this.players,
        scores = this.scores,
        activeUserId = this.activeUserId,
        skipCounter = this.skipCounter
    )
}