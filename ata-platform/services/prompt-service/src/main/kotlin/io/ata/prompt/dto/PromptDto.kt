package io.ata.prompt.dto

import io.ata.prompt.domain.Prompt
import io.ata.prompt.domain.PromptCategory
import io.ata.prompt.domain.Visibility
import jakarta.validation.constraints.NotBlank

data class CreatePromptRequest(
    @field:NotBlank val title: String,
    @field:NotBlank val content: String,
    val description: String? = null,
    val category: PromptCategory = PromptCategory.GENERAL,
    val visibility: Visibility = Visibility.PRIVATE
)

data class UpdatePromptRequest(
    val title: String? = null,
    val content: String? = null,
    val description: String? = null,
    val category: PromptCategory? = null,
    val visibility: Visibility? = null
)

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
