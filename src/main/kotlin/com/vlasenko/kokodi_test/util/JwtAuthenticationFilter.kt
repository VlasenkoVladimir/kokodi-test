package com.vlasenko.kokodi_test.util

import com.vlasenko.kokodi_test.services.JwtService
import com.vlasenko.kokodi_test.services.UserService
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

/**
 * JWT security filter
 */

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userService: UserService
) : OncePerRequestFilter() {
    private val logger: Logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {

        val token = extractToken(request)

        if (token != null) {
            try {

                val username = jwtService.extractUserName(token)
                val user = userService.getByUsername(username)


                if (jwtService.isTokenValid(token, user)) {

                    val authentication = UsernamePasswordAuthenticationToken(
                        user, null, user.authorities
                    )

                    SecurityContextHolder.getContext().authentication = authentication

                    logger.info("Authenticated user: $username")
                }
            } catch (e: Exception) {

                logger.error("Authentication failed for token: $token", e)
            }
        }

        chain.doFilter(request, response)
    }

    private fun extractToken(request: HttpServletRequest): String? {
        val authorizationHeader = request.getHeader("Authorization")

        return if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            authorizationHeader.substring(7)
        } else null
    }
}