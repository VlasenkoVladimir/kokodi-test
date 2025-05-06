package com.vlasenko.kokodi_test.services

import com.vlasenko.kokodi_test.domain.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey

/**
 * JWT security service
 */

@Service
class JwtService(

    @Value("\${auth.jwt.secret}")
    private val jwtSigningKey: String,

    @Value("\${auth.jwt.expiration}")
    private val expiration: Long
) {

    /**
     * Get username from token
     */
    fun extractUserName(token: String): String = extractClaim(token, Claims::getSubject)

    /**
     * Generate token
     */
    fun generateToken(userDetails: UserDetails): String {
        val claims = mutableMapOf<String, Any>()

        if (userDetails is User) {
            claims["id"] = userDetails.id as Long
            claims["role"] = userDetails.role
        }

        return generateToken(claims, userDetails)
    }

    /**
     * Validate token
     */
    fun isTokenValid(token: String, userDetails: UserDetails): Boolean {
        val userName = extractUserName(token)

        return (userName == userDetails.username) && !isTokenExpired(token)
    }

    /**
     * Extract data from token
     */
    private inline fun <reified T> extractClaim(token: String, claimsResolver: (Claims) -> T): T =
        claimsResolver.invoke(extractAllClaims(token))

    /**
     * Generate token
     */
    private fun generateToken(extraClaims: Map<String, Any>, userDetails: UserDetails): String =
        Jwts.builder()
            .apply { extraClaims.forEach { claim(it.key, it.value) } }
            .subject(userDetails.username)
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + expiration))
            .signWith(getSigningKey())
            .compact()

    /**
     * Validate expiration date
     */
    private fun isTokenExpired(token: String): Boolean = extractExpiration(token).before(Date())

    /**
     * Extract expiration date from token
     */
    private fun extractExpiration(token: String): Date = extractClaim(token, Claims::getExpiration)

    /**
     * Extract all data from token
     */
    private fun extractAllClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload()
    }

    /**
     * Get key for signing
     */
    private fun getSigningKey(): SecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSigningKey))
}