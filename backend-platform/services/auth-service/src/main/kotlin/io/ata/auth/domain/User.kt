package io.ata.auth.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "users")
// JPA Entity입니다. Kotlin에서는 JPA plugin(kotlin-jpa)이 no-arg/open 처리를 도와줍니다.
// val은 재할당하지 않는 필드, var는 이후 수정 가능한 필드에 사용했습니다.
class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    // DB auto increment id입니다. 새 Entity를 만들 때는 기본값 0으로 두고 DB가 실제 값을 생성합니다.
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    val email: String,

    @Column(nullable = true)
    // OAuth 가입자는 로컬 비밀번호가 없을 수 있으므로 nullable입니다.
    var password: String? = null,

    @Column(nullable = false)
    var name: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    // enum을 숫자 ordinal이 아니라 문자열로 저장합니다.
    // enum 순서가 바뀌어도 DB 의미가 깨지지 않아 안전합니다.
    var plan: SubscriptionPlan = SubscriptionPlan.FREE,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val provider: AuthProvider = AuthProvider.LOCAL,

    @Column(name = "provider_id")
    // Google/Kakao 같은 외부 provider의 사용자 식별자를 저장할 확장 지점입니다.
    val providerId: String? = null,

    @Column(name = "created_at", updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

// 구독 플랜은 access token claim에도 들어가 gateway 이후 서비스에서 사용할 수 있습니다.
enum class SubscriptionPlan { FREE, PRO, ENTERPRISE }

// LOCAL은 이메일/비밀번호, GOOGLE/KAKAO는 OAuth 로그인을 의미합니다.
enum class AuthProvider { LOCAL, GOOGLE, KAKAO }
