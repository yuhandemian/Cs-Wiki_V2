package io.ata.prompt.controller

import io.ata.prompt.dto.*
import io.ata.prompt.service.PromptService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/prompts")
// 프롬프트 CRUD API의 HTTP 입구입니다.
// 인증된 사용자 정보는 request body가 아니라 gateway가 추가한 X-User-Id 헤더에서 받습니다.
class PromptController(private val promptService: PromptService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        // api-gateway의 JwtAuthFilter가 access token의 subject를 X-User-Id로 바꿔 전달합니다.
        // 프론트엔드가 userId를 직접 보내지 않게 해서 조작 가능성을 줄입니다.
        @RequestHeader("X-User-Id") userId: Long,
        @Valid @RequestBody request: CreatePromptRequest
    ): ApiResponse<PromptResponse> =
        ApiResponse.success(promptService.create(userId, request))

    // 내 프롬프트 목록
    @GetMapping("/my")
    fun myPrompts(
        // "내 프롬프트"는 반드시 인증 사용자 기준으로 필터링합니다.
        @RequestHeader("X-User-Id") userId: Long,
        // Spring MVC가 query string의 page/size를 Int로 변환합니다.
        // 값이 없으면 defaultValue가 사용됩니다.
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ApiResponse<List<PromptResponse>> =
        ApiResponse.success(promptService.myPrompts(userId, page, size))

    // 공개 프롬프트 라이브러리 (ATA 핵심 기능 02)
    @GetMapping("/library")
    fun library(
        // category가 없으면 전체 공개 프롬프트를 조회하고, 있으면 해당 카테고리만 조회합니다.
        @RequestParam(required = false) category: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ApiResponse<List<PromptResponse>> =
        ApiResponse.success(promptService.publicLibrary(category, page, size))

    @GetMapping("/search")
    fun search(
        // 공개 라이브러리에서 title 기준으로 검색합니다.
        @RequestParam keyword: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ApiResponse<List<PromptResponse>> =
        ApiResponse.success(promptService.search(keyword, page, size))

    @GetMapping("/{id}")
    // 현재 구현은 id만 알면 단건 조회가 가능합니다.
    // 공개/비공개 권한 정책이 더 엄격해지면 Service에서 visibility와 userId 검사가 필요합니다.
    fun get(@PathVariable id: Long): ApiResponse<PromptResponse> =
        ApiResponse.success(promptService.get(id))

    @PutMapping("/{id}")
    fun update(
        // 수정은 프롬프트 작성자만 가능하므로 userId를 Service까지 전달해 소유권을 검사합니다.
        @RequestHeader("X-User-Id") userId: Long,
        @PathVariable id: Long,
        @RequestBody request: UpdatePromptRequest
    ): ApiResponse<PromptResponse> =
        ApiResponse.success(promptService.update(userId, id, request))

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        // 삭제도 수정과 동일하게 소유권 검사가 필요합니다.
        @RequestHeader("X-User-Id") userId: Long,
        @PathVariable id: Long
    ) = promptService.delete(userId, id)

    @PostMapping("/{id}/like")
    // 좋아요는 인증 사용자 ID를 현재 사용하지 않습니다.
    // 지금 구조에서는 같은 사용자가 여러 번 좋아요를 누르는 것을 막지는 않습니다.
    fun like(@PathVariable id: Long): ApiResponse<Unit> {
        promptService.like(id)
        return ApiResponse.success(Unit)
    }

    // 프롬프트 사용 (useCount 증가 + 내용 반환)
    @PostMapping("/{id}/use")
    // 사용 버튼을 누르면 사용 횟수를 증가시키고, 프론트가 채팅 입력창에 넣을 프롬프트 내용을 반환합니다.
    fun use(@PathVariable id: Long): ApiResponse<PromptResponse> =
        ApiResponse.success(promptService.use(id))
}
