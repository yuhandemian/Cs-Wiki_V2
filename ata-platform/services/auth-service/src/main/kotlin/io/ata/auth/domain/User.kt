package io.ata.auth.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    val email: String,

    @Column(nullable = true)
    var password: String? = null,

    @Column(nullable = false)
    var name: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var plan: SubscriptionPlan = SubscriptionPlan.FREE,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val provider: AuthProvider = AuthProvider.LOCAL,

    @Column(name = "provider_id")
    val providerId: String? = null,

    @Column(name = "created_at", updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class SubscriptionPlan { FREE, PRO, ENTERPRISE }
enum class AuthProvider { LOCAL, GOOGLE, KAKAO }
