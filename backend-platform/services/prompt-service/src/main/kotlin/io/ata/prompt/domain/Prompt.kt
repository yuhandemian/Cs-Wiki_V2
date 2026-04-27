package io.ata.prompt.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "prompts")
// 프롬프트 라이브러리의 핵심 Entity입니다.
// auth-service의 User와 직접 JPA 관계를 맺지 않고 userId 값만 저장합니다.
// 마이크로서비스 구조에서는 서비스 간 DB 조인을 피하고 ID로 느슨하게 연결하는 방식을 자주 씁니다.
class Prompt(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    // MySQL AUTO_INCREMENT primary key입니다.
    val id: Long = 0,

    @Column(nullable = false)
    // 작성자 ID입니다. gateway가 검증한 X-User-Id 값을 저장합니다.
    val userId: Long,

    @Column(nullable = false)
    var title: String,

    @Column(columnDefinition = "TEXT", nullable = false)
    // 실제 프롬프트 본문입니다. 길이가 길 수 있으므로 TEXT 컬럼을 사용합니다.
    var content: String,

    @Column
    // 카드 목록 등에서 보여줄 짧은 설명입니다. 없으면 content 일부를 대신 보여줄 수 있습니다.
    var description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    // enum을 문자열로 저장해 DB 가독성과 enum 순서 변경 안정성을 확보합니다.
    var category: PromptCategory = PromptCategory.GENERAL,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    // 접근 범위입니다. 공개 라이브러리는 PUBLIC 프롬프트만 조회합니다.
    var visibility: Visibility = Visibility.PRIVATE,

    @Column(nullable = false)
    // 공개 라이브러리 정렬 기준으로 사용됩니다.
    var likeCount: Int = 0,

    @Column(nullable = false)
    // 프롬프트가 채팅 입력에 사용된 횟수입니다.
    var useCount: Int = 0,

    @Column(name = "created_at", updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

// 프론트엔드 필터와 공개 라이브러리 분류에 쓰이는 카테고리입니다.
enum class PromptCategory {
    GENERAL, CODING, WRITING, ANALYSIS, EDUCATION, MARKETING, DESIGN, CUSTOMER_SERVICE
}

// 공개 범위입니다. 현재 코드에서는 PUBLIC만 공개 라이브러리에 노출됩니다.
enum class Visibility {
    PRIVATE,       // 나만 보기
    ORGANIZATION,  // 조직 내 공유
    PUBLIC         // 전체 공개 (ATA 라이브러리)
}
