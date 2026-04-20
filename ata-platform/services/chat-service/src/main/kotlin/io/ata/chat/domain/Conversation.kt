package io.ata.chat.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "conversations")
data class Conversation(
    @Id val id: String? = null,
    val userId: Long,
    val title: String,
    val provider: String,        // LLM 제공자 (OPENAI, ANTHROPIC 등)
    val model: String,
    val messages: MutableList<Message> = mutableListOf(),
    val workspaceId: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

data class Message(
    val role: String,            // user | assistant
    val content: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val inputTokens: Int = 0,
    val outputTokens: Int = 0
)
