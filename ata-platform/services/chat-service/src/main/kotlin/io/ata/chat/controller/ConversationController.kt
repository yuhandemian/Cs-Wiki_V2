package io.ata.chat.controller

import io.ata.chat.dto.*
import io.ata.chat.service.ConversationService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/chat/conversations")
// 대화방과 메시지 저장 API의 HTTP 입구입니다.
// 모든 API는 gateway가 넣어준 X-User-Id 헤더로 현재 사용자를 판단합니다.
class ConversationController(private val conversationService: ConversationService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        // 프론트엔드가 userId를 보내지 않고, 검증된 JWT에서 나온 userId만 신뢰합니다.
        @RequestHeader("X-User-Id") userId: Long,
        @Valid @RequestBody request: CreateConversationRequest
    ): ApiResponse<ConversationDetail> =
        ApiResponse.success(conversationService.create(userId, request))

    @GetMapping
    fun list(
        // 사용자별 대화 목록입니다. 다른 사용자의 대화가 섞이지 않도록 userId로 필터링합니다.
        @RequestHeader("X-User-Id") userId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ApiResponse<List<ConversationSummary>> =
        ApiResponse.success(conversationService.list(userId, page, size))

    @GetMapping("/{id}")
    fun get(
        // id만으로 조회하지 않고 id + userId로 조회해 소유권 검사를 함께 처리합니다.
        @RequestHeader("X-User-Id") userId: Long,
        @PathVariable id: String
    ): ApiResponse<ConversationDetail> =
        ApiResponse.success(conversationService.get(userId, id))

    @PostMapping("/{id}/messages")
    fun addMessage(
        // 메시지 추가도 해당 대화의 소유자인지 확인한 뒤 수행합니다.
        @RequestHeader("X-User-Id") userId: Long,
        @PathVariable id: String,
        @Valid @RequestBody request: AddMessageRequest
    ): ApiResponse<ConversationDetail> =
        ApiResponse.success(conversationService.addMessage(userId, id, request))

    @PatchMapping("/{id}/title")
    fun updateTitle(
        @RequestHeader("X-User-Id") userId: Long,
        @PathVariable id: String,
        // 간단한 title 변경이라 별도 DTO 대신 Map으로 받습니다.
        // 규모가 커지면 UpdateConversationTitleRequest DTO로 분리하는 편이 더 명확합니다.
        @RequestBody body: Map<String, String>
    ): ApiResponse<ConversationDetail> =
        ApiResponse.success(conversationService.updateTitle(userId, id, body["title"] ?: ""))

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        // deleteByIdAndUserId를 사용해 삭제와 소유권 확인을 한 번에 처리합니다.
        @RequestHeader("X-User-Id") userId: Long,
        @PathVariable id: String
    ) = conversationService.delete(userId, id)
}
