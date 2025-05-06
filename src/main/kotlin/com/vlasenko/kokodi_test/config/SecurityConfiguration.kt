package com.vlasenko.kokodi_test.config

import com.vlasenko.kokodi_test.services.UserService
import com.vlasenko.kokodi_test.util.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration

/**
 * Security configuration
 */

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfiguration(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val userService: UserService
) {

    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { it.disable() }
            .cors { cors ->
                cors.configurationSource { request ->
                    val corsConfiguration = CorsConfiguration().apply {
                        setAllowedOriginPatterns(mutableListOf("*"))
                        allowedMethods = mutableListOf("GET", "POST")
                        allowedHeaders = mutableListOf("Authorization", "Content-Type")
                        allowCredentials = true
                    }
                    corsConfiguration
                }
            }
            .authorizeHttpRequests { request ->
                request
                    .requestMatchers("/auth/**").permitAll()
                    .requestMatchers("/games").hasRole("USER")
                    .anyRequest().authenticated()
            }
            .sessionManagement { manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationProvider(): AuthenticationProvider {
        val authProvider = DaoAuthenticationProvider()
        authProvider.setUserDetailsService(userService.getUserDetailsService())
        authProvider.setPasswordEncoder(passwordEncoder())

        return authProvider
    }

    @Bean
    @Throws(Exception::class)
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager {
        return config.authenticationManager
    }
}