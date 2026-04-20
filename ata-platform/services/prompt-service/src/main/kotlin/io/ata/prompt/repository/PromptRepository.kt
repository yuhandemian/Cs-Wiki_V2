package io.ata.prompt.repository

import io.ata.prompt.domain.Prompt
import io.ata.prompt.domain.PromptCategory
import io.ata.prompt.domain.Visibility
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

// Spring Data JPA Repository입니다.
// 메서드 이름 기반 query와 직접 작성한 JPQL query를 함께 사용합니다.
interface PromptRepository : JpaRepository<Prompt, Long> {
    // SELECT p FROM Prompt p WHERE p.userId = :userId
    fun findByUserId(userId: Long, pageable: Pageable): Page<Prompt>

    // 공개 라이브러리에서 category filter가 있을 때 사용합니다.
    fun findByVisibilityAndCategory(visibility: Visibility, category: PromptCategory, pageable: Pageable): Page<Prompt>

    // 공개 라이브러리 전체 조회에 사용합니다.
    fun findByVisibility(visibility: Visibility, pageable: Pageable): Page<Prompt>

    // JPQL은 테이블명(prompts)이 아니라 Entity 이름(Prompt)과 필드명(title, visibility)을 사용합니다.
    // 현재는 title LIKE 검색이며 PUBLIC 프롬프트만 검색합니다.
    @Query("SELECT p FROM Prompt p WHERE p.title LIKE %:keyword% AND p.visibility = 'PUBLIC'")
    fun searchPublic(keyword: String, pageable: Pageable): Page<Prompt>

    @Modifying
    // @Modifying은 SELECT가 아닌 UPDATE/DELETE JPQL임을 Spring Data에 알려줍니다.
    // Service 메서드에 @Transactional이 있어야 실제 DB 변경이 안전하게 반영됩니다.
    @Query("UPDATE Prompt p SET p.likeCount = p.likeCount + 1 WHERE p.id = :id")
    fun incrementLike(id: Long)

    @Modifying
    // 프롬프트를 채팅 입력으로 사용할 때 useCount를 1 증가시킵니다.
    @Query("UPDATE Prompt p SET p.useCount = p.useCount + 1 WHERE p.id = :id")
    fun incrementUseCount(id: Long)
}
