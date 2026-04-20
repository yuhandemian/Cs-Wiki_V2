package io.ata.prompt.service

import io.ata.prompt.domain.Prompt
import io.ata.prompt.domain.PromptCategory
import io.ata.prompt.domain.Visibility
import io.ata.prompt.dto.*
import io.ata.prompt.exception.PromptNotFoundException
import io.ata.prompt.exception.UnauthorizedException
import io.ata.prompt.repository.PromptRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class PromptService(private val promptRepository: PromptRepository) {

    @Transactional
    fun create(userId: Long, request: CreatePromptRequest): PromptResponse =
        promptRepository.save(
            Prompt(
                userId = userId,
                title = request.title,
                content = request.content,
                description = request.description,
                category = request.category,
                visibility = request.visibility
            )
        ).toResponse()

    fun myPrompts(userId: Long, page: Int, size: Int): List<PromptResponse> {
        val pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending())
        return promptRepository.findByUserId(userId, pageable).content.map { it.toResponse() }
    }

    fun publicLibrary(category: String?, page: Int, size: Int): List<PromptResponse> {
        val pageable = PageRequest.of(page, size, Sort.by("likeCount").descending())
        return if (category != null) {
            val cat = PromptCategory.valueOf(category.uppercase())
            promptRepository.findByVisibilityAndCategory(Visibility.PUBLIC, cat, pageable)
        } else {
            promptRepository.findByVisibility(Visibility.PUBLIC, pageable)
        }.content.map { it.toResponse() }
    }

    fun search(keyword: String, page: Int, size: Int): List<PromptResponse> {
        val pageable = PageRequest.of(page, size)
        return promptRepository.searchPublic(keyword, pageable).content.map { it.toResponse() }
    }

    fun get(id: Long): PromptResponse =
        promptRepository.findById(id).orElseThrow { PromptNotFoundException(id) }.toResponse()

    @Transactional
    fun update(userId: Long, id: Long, request: UpdatePromptRequest): PromptResponse {
        val prompt = promptRepository.findById(id).orElseThrow { PromptNotFoundException(id) }
        if (prompt.userId != userId) throw UnauthorizedException()

        request.title?.let { prompt.title = it }
        request.content?.let { prompt.content = it }
        request.description?.let { prompt.description = it }
        request.category?.let { prompt.category = it }
        request.visibility?.let { prompt.visibility = it }
        prompt.updatedAt = LocalDateTime.now()

        return promptRepository.save(prompt).toResponse()
    }

    @Transactional
    fun delete(userId: Long, id: Long) {
        val prompt = promptRepository.findById(id).orElseThrow { PromptNotFoundException(id) }
        if (prompt.userId != userId) throw UnauthorizedException()
        promptRepository.delete(prompt)
    }

    @Transactional
    fun like(id: Long) {
        if (!promptRepository.existsById(id)) throw PromptNotFoundException(id)
        promptRepository.incrementLike(id)
    }

    @Transactional
    fun use(id: Long): PromptResponse {
        val prompt = promptRepository.findById(id).orElseThrow { PromptNotFoundException(id) }
        promptRepository.incrementUseCount(id)
        return prompt.toResponse()
    }
}
