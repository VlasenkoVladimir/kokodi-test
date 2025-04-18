package com.vlasenko.kokodi_test.domain

import jakarta.persistence.*

/**
 * User entity
 */

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long,
    @Column(name = "name", nullable = false, unique = true)
    var name: String,
//    @Column(name = "email", nullable = false)
//    var email: String,
//    @Column(name = "password", nullable = false)
//    var password: String
)