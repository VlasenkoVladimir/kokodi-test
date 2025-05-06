package com.vlasenko.kokodi_test.services

import com.vlasenko.kokodi_test.domain.User
import com.vlasenko.kokodi_test.repository.UserRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

/**
 * User service
 */

@Service
class UserService(
    private val userRepository: UserRepository
) {
    private val logger: Logger = LoggerFactory.getLogger(UserService::class.java)

    /**
     * Save User
     */
    fun save(user: User): User {
        logger.info("Saving user with username: ${user.getUsername()}")

        return userRepository.save(user)
    }

    /**
     * Create User
     */
    fun create(user: User): User {
        logger.info("Attempting to create user with username: ${user.getUsername()}")

        if (userRepository.existsByUsername(user.getUsername())) {
            logger.error("User with username ${user.getUsername()} already exists")
            throw RuntimeException("User already existed")
        }

        val savedUser = save(user)
        logger.info("User with username ${user.getUsername()} successfully created")

        return savedUser
    }

    /**
     * Get User by username
     */
    fun getByUsername(username: String): User {
        logger.info("Fetching user with username: $username")

        return userRepository.findByUsername(username)
            .orElseThrow {
                logger.error("User with username $username not found")
                UsernameNotFoundException("User not found")
            }
    }

    /**
     * Get UserDetailsService for Spring Security
     */
    fun getUserDetailsService(): UserDetailsService =
        UserDetailsService { username: String -> getByUsername(username) }

    /**
     * Get Current User from Spring Security context
     */
    fun getCurrentUserFromContext(): User {
        val user = SecurityContextHolder.getContext().authentication?.principal as? User
            ?: throw IllegalStateException("No authenticated user found in security context")

        logger.info("Fetching current user with username: ${user.getUsername()}")

        return user
    }
}