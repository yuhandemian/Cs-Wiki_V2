package io.ata.chat.dto

import io.ata.chat.domain.Conversation
import io.ata.chat.domain.Message
import jakarta.validation.constraints.NotBlank

// 대화방 생성 요청입니다.
// provider는 필수이고, model/title/workspaceId는 선택 또는 기본값을 둡니다.
data class CreateConversationRequest(
    @field:NotBlank val provider: String,
    val model: String = "",
    val title: String = "새 대화",
    val workspaceId: String? = null
)

// 대화에 메시지를 추가할 때 사용하는 요청 DTO입니다.
data class AddMessageRequest(
    @field:NotBlank val role: String,
    @field:NotBlank val content: String,
    val inputTokens: Int = 0,
    val outputTokens: Int = 0
)

// 대화 목록 화면용 가벼운 응답입니다.
// 메시지 전체를 보내지 않고 개수와 최근 수정 시각만 보냅니다.
data class ConversationSummary(
    val id: String,
    val title: String,
    val provider: String,
    val model: String,
    val messageCount: Int,
    val updatedAt: String
)

// 대화 상세 화면용 응답입니다.
// 실제 메시지 목록을 포함합니다.
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

// chat-service 전용 공통 응답 wrapper입니다.
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

// 목록 응답으로 변환하는 extension function입니다.
// id!!는 저장된 document에는 id가 반드시 있다는 가정입니다.
fun Conversation.toSummary() = ConversationSummary(
    id = id!!,
    title = title,
    provider = provider,
    model = model,
    messageCount = messages.size,
    updatedAt = updatedAt.toString()
)

// 상세 응답으로 변환하는 extension function입니다.
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
