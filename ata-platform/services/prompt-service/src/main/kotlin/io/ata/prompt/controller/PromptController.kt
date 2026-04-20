package io.ata.prompt.controller

import io.ata.prompt.dto.*
import io.ata.prompt.service.PromptService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/prompts")
class PromptController(private val promptService: PromptService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @RequestHeader("X-User-Id") userId: Long,
        @Valid @RequestBody request: CreatePromptRequest
    ): ApiResponse<PromptResponse> =
        ApiResponse.success(promptService.create(userId, request))

    // 내 프롬프트 목록
    @GetMapping("/my")
    fun myPrompts(
        @RequestHeader("X-User-Id") userId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ApiResponse<List<PromptResponse>> =
        ApiResponse.success(promptService.myPrompts(userId, page, size))

    // 공개 프롬프트 라이브러리 (ATA 핵심 기능 02)
    @GetMapping("/library")
    fun library(
        @RequestParam(required = false) category: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ApiResponse<List<PromptResponse>> =
        ApiResponse.success(promptService.publicLibrary(category, page, size))

    @GetMapping("/search")
    fun search(
        @RequestParam keyword: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ApiResponse<List<PromptResponse>> =
        ApiResponse.success(promptService.search(keyword, page, size))

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ApiResponse<PromptResponse> =
        ApiResponse.success(promptService.get(id))

    @PutMapping("/{id}")
    fun update(
        @RequestHeader("X-User-Id") userId: Long,
        @PathVariable id: Long,
        @RequestBody request: UpdatePromptRequest
    ): ApiResponse<PromptResponse> =
        ApiResponse.success(promptService.update(userId, id, request))

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @RequestHeader("X-User-Id") userId: Long,
        @PathVariable id: Long
    ) = promptService.delete(userId, id)

    @PostMapping("/{id}/like")
    fun like(@PathVariable id: Long): ApiResponse<Unit> {
        promptService.like(id)
        return ApiResponse.success(Unit)
    }

    // 프롬프트 사용 (useCount 증가 + 내용 반환)
    @PostMapping("/{id}/use")
    fun use(@PathVariable id: Long): ApiResponse<PromptResponse> =
        ApiResponse.success(promptService.use(id))
}
