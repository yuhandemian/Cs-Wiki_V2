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
// 기본은 읽기 전용 트랜잭션입니다.
// 생성/수정/삭제/카운트 증가는 메서드마다 @Transactional로 write transaction을 엽니다.
@Transactional(readOnly = true)
class PromptService(private val promptRepository: PromptRepository) {

    @Transactional
    fun create(userId: Long, request: CreatePromptRequest): PromptResponse =
        // Controller가 전달한 userId를 owner로 저장합니다.
        // 프론트엔드 입력값이 아니라 gateway 검증 결과를 사용한다는 점이 중요합니다.
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
        // PageRequest는 page 번호가 0부터 시작합니다.
        // 최신 수정 순으로 정렬해서 사용자가 최근 편집한 프롬프트를 먼저 보게 합니다.
        val pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending())
        // Page<Prompt>에서 실제 데이터 목록은 content에 들어있습니다.
        return promptRepository.findByUserId(userId, pageable).content.map { it.toResponse() }
    }

    fun publicLibrary(category: String?, page: Int, size: Int): List<PromptResponse> {
        // 공개 라이브러리는 좋아요가 많은 순으로 보여줍니다.
        val pageable = PageRequest.of(page, size, Sort.by("likeCount").descending())
        return if (category != null) {
            // 프론트엔드에서 온 문자열을 enum으로 변환합니다.
            // 잘못된 문자열이면 IllegalArgumentException이 발생하므로, 추후 400 응답 처리를 추가할 수 있습니다.
            val cat = PromptCategory.valueOf(category.uppercase())
            promptRepository.findByVisibilityAndCategory(Visibility.PUBLIC, cat, pageable)
        } else {
            promptRepository.findByVisibility(Visibility.PUBLIC, pageable)
        }.content.map { it.toResponse() }
    }

    fun search(keyword: String, page: Int, size: Int): List<PromptResponse> {
        val pageable = PageRequest.of(page, size)
        // 현재 검색은 공개 프롬프트의 title에 대해서만 수행합니다.
        // content까지 검색하려면 Repository query를 확장해야 합니다.
        return promptRepository.searchPublic(keyword, pageable).content.map { it.toResponse() }
    }

    fun get(id: Long): PromptResponse =
        // Optional.orElseThrow는 Java Optional과 같은 패턴입니다.
        promptRepository.findById(id).orElseThrow { PromptNotFoundException(id) }.toResponse()

    @Transactional
    fun update(userId: Long, id: Long, request: UpdatePromptRequest): PromptResponse {
        val prompt = promptRepository.findById(id).orElseThrow { PromptNotFoundException(id) }

        // 프롬프트 작성자만 수정할 수 있습니다.
        // gateway가 넣은 userId와 DB의 owner userId를 비교합니다.
        if (prompt.userId != userId) throw UnauthorizedException()

        // let은 값이 null이 아닐 때만 블록을 실행합니다.
        // Java의 if (request.title != null) prompt.setTitle(request.title) 패턴과 같습니다.
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
        // 삭제도 수정과 같은 소유권 검사를 거칩니다.
        if (prompt.userId != userId) throw UnauthorizedException()
        promptRepository.delete(prompt)
    }

    @Transactional
    fun like(id: Long) {
        // UPDATE query만 실행하면 id가 없어도 조용히 0건 수정될 수 있으므로 먼저 존재 여부를 확인합니다.
        if (!promptRepository.existsById(id)) throw PromptNotFoundException(id)
        // 카운트 증가는 Entity를 읽고 저장하는 대신 DB UPDATE query로 바로 처리합니다.
        promptRepository.incrementLike(id)
    }

    @Transactional
    fun use(id: Long): PromptResponse {
        val prompt = promptRepository.findById(id).orElseThrow { PromptNotFoundException(id) }
        // 사용 횟수는 증가시키지만, 반환값은 이미 읽어온 prompt를 DTO로 바꿉니다.
        // 따라서 응답의 useCount는 증가 전 값일 수 있습니다. 최신 count가 필요하면 다시 조회해야 합니다.
        promptRepository.incrementUseCount(id)
        return prompt.toResponse()
    }
}
