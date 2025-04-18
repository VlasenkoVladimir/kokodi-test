package com.vlasenko.kokodi_test.repository

import com.vlasenko.kokodi_test.domain.User
import org.springframework.data.jpa.repository.JpaRepository

/**
 * User repository
 */

interface UserRepository : JpaRepository<User, Long>