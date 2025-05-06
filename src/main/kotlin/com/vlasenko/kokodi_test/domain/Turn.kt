package com.vlasenko.kokodi_test.domain

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

/**
 * Turn in game history
 */
@Entity
@Table(name = "turns")
data class Turn(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long?,
    @Column(name = "game_session_id", nullable = false)
    val gameSessionId: Long,
    @Column(name = "turn_pointer", nullable = false)
    var turnPointer: Int,
    @Column(name = "skip_counter")
    val skipCounter: Int,
    @Column(name = "active_username", nullable = false)
    val activeUsername: String,
    @Column(name = "actual_card_name")
    val actualCardName: String?,
    @Column(name = "actual_card_type")
    val actualCardType: String?,
    @Column(name = "actual_card_value")
    val actualCardValue: Int?,
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "score_changes")
    val scoreChanges: Map<Long, Int>?
)