package io.ata.prompt.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "prompts")
class Prompt(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false)
    var title: String,

    @Column(columnDefinition = "TEXT", nullable = false)
    var content: String,

    @Column
    var description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var category: PromptCategory = PromptCategory.GENERAL,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var visibility: Visibility = Visibility.PRIVATE,

    @Column(nullable = false)
    var likeCount: Int = 0,

    @Column(nullable = false)
    var useCount: Int = 0,

    @Column(name = "created_at", updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class PromptCategory {
    GENERAL, CODING, WRITING, ANALYSIS, EDUCATION, MARKETING, DESIGN, CUSTOMER_SERVICE
}

enum class Visibility {
    PRIVATE,       // 나만 보기
    ORGANIZATION,  // 조직 내 공유
    PUBLIC         // 전체 공개 (ATA 라이브러리)
}
