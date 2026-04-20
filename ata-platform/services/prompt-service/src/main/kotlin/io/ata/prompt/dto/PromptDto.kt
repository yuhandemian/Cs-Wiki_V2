package io.ata.prompt.dto

import io.ata.prompt.domain.Prompt
import io.ata.prompt.domain.PromptCategory
import io.ata.prompt.domain.Visibility
import jakarta.validation.constraints.NotBlank

// 프롬프트 생성 요청 DTO입니다.
// title/content는 필수이고, 나머지는 기본값을 둬서 프론트가 생략할 수 있습니다.
data class CreatePromptRequest(
    @field:NotBlank val title: String,
    @field:NotBlank val content: String,
    val description: String? = null,
    val category: PromptCategory = PromptCategory.GENERAL,
    val visibility: Visibility = Visibility.PRIVATE
)

// 수정 요청 DTO입니다.
// 모든 필드를 nullable로 둔 것은 PATCH처럼 "보낸 필드만 수정"하는 효과를 내기 위해서입니다.
data class UpdatePromptRequest(
    val title: String? = null,
    val content: String? = null,
    val description: String? = null,
    val category: PromptCategory? = null,
    val visibility: Visibility? = null
)

// 프론트엔드로 내려주는 응답 DTO입니다.
// Entity를 그대로 노출하지 않고, 필요한 필드와 문자열화된 enum/date만 전달합니다.
data class PromptResponse(
    val id: Long,
    val userId: Long,
    val title: String,
    val content: String,
    val description: String?,
    val category: String,
    val visibility: String,
    val likeCount: Int,
    val useCount: Int,
    val createdAt: String,
    val updatedAt: String
)

// prompt-service 전용 공통 응답 wrapper입니다.
// auth-service에도 같은 모양이 있으며, 실제 프로젝트가 커지면 공통 모듈로 뺄 수 있습니다.
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null
) {
    companion object {
        fun <T> success(data: T) = ApiResponse(true, data)
        fun error(message: String) = ApiResponse<Nothing>(false, message = message)
    }
}

// Kotlin extension function입니다.
// Prompt Entity에 toResponse() 함수를 "추가한 것처럼" 호출할 수 있게 해줍니다.
// Java라면 static mapper method나 별도 Mapper 클래스로 자주 작성합니다.
fun Prompt.toResponse() = PromptResponse(
    id = id,
    userId = userId,
    title = title,
    content = content,
    description = description,
    category = category.name,
    visibility = visibility.name,
    likeCount = likeCount,
    useCount = useCount,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString()
)
