package com.vlasenko.kokodi_test.domain

import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**
 * User entity
 */

@Entity
@Table(name = "users")
class User() : UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null

    @Column(name = "name", nullable = false, unique = true)
    private var name: String? = null

    @Column(name = "username", nullable = false, unique = true)
    private var username: String? = null

    @Column(name = "password", nullable = false)
    private var password: String? = null

    @Transient
    val role: String = "ROLE_USER"

    override fun getAuthorities() = mutableListOf<GrantedAuthority>(SimpleGrantedAuthority(role))

    override fun getPassword() = this.password

    override fun getUsername() = this.username

    override fun isAccountNonExpired() = true

    override fun isAccountNonLocked() = true

    override fun isCredentialsNonExpired() = true

    override fun isEnabled() = true

    class Builder {
        private var name: String? = null
        private var username: String? = null
        private var password: String? = null

        fun withName(name: String): Builder {
            this.name = name
            return this
        }

        fun withUsername(username: String): Builder {
            this.username = username
            return this
        }

        fun withPassword(password: String): Builder {
            this.password = password
            return this
        }

        fun build(): User {
            val user = User()
            user.name = this.name
            user.username = this.username
            user.password = this.password
            return user
        }
    }
}