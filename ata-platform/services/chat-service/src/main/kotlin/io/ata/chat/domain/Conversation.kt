package io.ata.chat.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "conversations")
// MongoDB document입니다. MySQL의 @Entity와 역할은 비슷하지만,
// 관계형 테이블 row가 아니라 conversations 컬렉션의 JSON-like 문서 하나로 저장됩니다.
data class Conversation(
    // MongoDB ObjectId가 문자열로 매핑됩니다. 새 문서 저장 전에는 null일 수 있습니다.
    @Id val id: String? = null,

    // gateway가 검증한 사용자 ID입니다. 사용자별 대화 격리에 사용합니다.
    val userId: Long,
    val title: String,
    val provider: String,        // LLM 제공자 (OPENAI, ANTHROPIC 등)
    val model: String,

    // 메시지 목록을 별도 collection으로 빼지 않고 Conversation document 안에 함께 저장합니다.
    // 대화 상세 조회가 한 번에 끝나는 장점이 있지만, 메시지가 매우 많아지면 document 크기를 고려해야 합니다.
    val messages: MutableList<Message> = mutableListOf(),

    // 추후 워크스페이스/프로젝트 단위로 대화를 묶기 위한 확장 필드입니다.
    val workspaceId: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

// Conversation 안에 포함되는 embedded document입니다.
// MongoDB에는 messages 배열의 각 원소로 저장됩니다.
data class Message(
    val role: String,            // user | assistant
    val content: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    // AI provider 사용량 추적이나 과금 계산을 위한 필드입니다.
    val inputTokens: Int = 0,
    val outputTokens: Int = 0
)
