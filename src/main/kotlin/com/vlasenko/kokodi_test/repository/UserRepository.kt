package com.vlasenko.kokodi_test.repository

import com.vlasenko.kokodi_test.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

/**
 * User repository
 */

interface UserRepository : JpaRepository<User, Long> {

    fun findByUsername(username: String): Optional<User>

    fun existsByUsername(username: String?): Boolean
}