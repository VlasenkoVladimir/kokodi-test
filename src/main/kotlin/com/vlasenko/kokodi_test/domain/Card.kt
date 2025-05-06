package com.vlasenko.kokodi_test.domain

import com.vlasenko.kokodi_test.domain.enums.CardType
import jakarta.persistence.*

/**
 * Card entity
 */

@Entity
@Table(name = "cards")
class Card(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long?,
    @Column(name = "name", nullable = false, unique = true)
    val name: String,
    @Column(name = "value", nullable = false)
    val value: Int = 0,
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    val type: CardType
)