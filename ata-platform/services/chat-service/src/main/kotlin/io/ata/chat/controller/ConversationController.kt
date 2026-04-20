package io.ata.chat.controller

import io.ata.chat.dto.*
import io.ata.chat.service.ConversationService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/chat/conversations")
class ConversationController(private val conversationService: ConversationService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @RequestHeader("X-User-Id") userId: Long,
        @Valid @RequestBody request: CreateConversationRequest
    ): ApiResponse<ConversationDetail> =
        ApiResponse.success(conversationService.create(userId, request))

    @GetMapping
    fun list(
        @RequestHeader("X-User-Id") userId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ApiResponse<List<ConversationSummary>> =
        ApiResponse.success(conversationService.list(userId, page, size))

    @GetMapping("/{id}")
    fun get(
        @RequestHeader("X-User-Id") userId: Long,
        @PathVariable id: String
    ): ApiResponse<ConversationDetail> =
        ApiResponse.success(conversationService.get(userId, id))

    @PostMapping("/{id}/messages")
    fun addMessage(
        @RequestHeader("X-User-Id") userId: Long,
        @PathVariable id: String,
        @Valid @RequestBody request: AddMessageRequest
    ): ApiResponse<ConversationDetail> =
        ApiResponse.success(conversationService.addMessage(userId, id, request))

    @PatchMapping("/{id}/title")
    fun updateTitle(
        @RequestHeader("X-User-Id") userId: Long,
        @PathVariable id: String,
        @RequestBody body: Map<String, String>
    ): ApiResponse<ConversationDetail> =
        ApiResponse.success(conversationService.updateTitle(userId, id, body["title"] ?: ""))

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @RequestHeader("X-User-Id") userId: Long,
        @PathVariable id: String
    ) = conversationService.delete(userId, id)
}
