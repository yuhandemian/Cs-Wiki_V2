package io.ata.prompt.repository

import io.ata.prompt.domain.Prompt
import io.ata.prompt.domain.PromptCategory
import io.ata.prompt.domain.Visibility
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface PromptRepository : JpaRepository<Prompt, Long> {
    fun findByUserId(userId: Long, pageable: Pageable): Page<Prompt>
    fun findByVisibilityAndCategory(visibility: Visibility, category: PromptCategory, pageable: Pageable): Page<Prompt>
    fun findByVisibility(visibility: Visibility, pageable: Pageable): Page<Prompt>

    @Query("SELECT p FROM Prompt p WHERE p.title LIKE %:keyword% AND p.visibility = 'PUBLIC'")
    fun searchPublic(keyword: String, pageable: Pageable): Page<Prompt>

    @Modifying
    @Query("UPDATE Prompt p SET p.likeCount = p.likeCount + 1 WHERE p.id = :id")
    fun incrementLike(id: Long)

    @Modifying
    @Query("UPDATE Prompt p SET p.useCount = p.useCount + 1 WHERE p.id = :id")
    fun incrementUseCount(id: Long)
}
