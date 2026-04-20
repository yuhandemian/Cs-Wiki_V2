package io.ata.chat.dto

import io.ata.chat.domain.Conversation
import io.ata.chat.domain.Message
import jakarta.validation.constraints.NotBlank

data class CreateConversationRequest(
    @field:NotBlank val provider: String,
    val model: String = "",
    val title: String = "새 대화",
    val workspaceId: String? = null
)

data class AddMessageRequest(
    @field:NotBlank val role: String,
    @field:NotBlank val content: String,
    val inputTokens: Int = 0,
    val outputTokens: Int = 0
)

data class ConversationSummary(
    val id: String,
    val title: String,
    val provider: String,
    val model: String,
    val messageCount: Int,
    val updatedAt: String
)

data class ConversationDetail(
    val id: String,
    val title: String,
    val provider: String,
    val model: String,
    val messages: List<Message>,
    val workspaceId: String?,
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

fun Conversation.toSummary() = ConversationSummary(
    id = id!!,
    title = title,
    provider = provider,
    model = model,
    messageCount = messages.size,
    updatedAt = updatedAt.toString()
)

fun Conversation.toDetail() = ConversationDetail(
    id = id!!,
    title = title,
    provider = provider,
    model = model,
    messages = messages,
    workspaceId = workspaceId,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString()
)
