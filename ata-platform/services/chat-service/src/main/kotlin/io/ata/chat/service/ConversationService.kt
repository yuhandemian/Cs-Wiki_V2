package io.ata.chat.service

import io.ata.chat.domain.Conversation
import io.ata.chat.domain.Message
import io.ata.chat.dto.*
import io.ata.chat.exception.ConversationNotFoundException
import io.ata.chat.repository.ConversationRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ConversationService(private val conversationRepository: ConversationRepository) {

    fun create(userId: Long, request: CreateConversationRequest): ConversationDetail {
        val conversation = conversationRepository.save(
            Conversation(
                userId = userId,
                title = request.title,
                provider = request.provider.uppercase(),
                model = request.model,
                workspaceId = request.workspaceId
            )
        )
        return conversation.toDetail()
    }

    fun list(userId: Long, page: Int, size: Int): List<ConversationSummary> {
        val pageable = PageRequest.of(page, size)
        return conversationRepository
            .findByUserIdOrderByUpdatedAtDesc(userId, pageable)
            .content
            .map { it.toSummary() }
    }

    fun get(userId: Long, conversationId: String): ConversationDetail {
        val conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
            ?: throw ConversationNotFoundException(conversationId)
        return conversation.toDetail()
    }

    fun addMessage(userId: Long, conversationId: String, request: AddMessageRequest): ConversationDetail {
        val conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
            ?: throw ConversationNotFoundException(conversationId)

        conversation.messages.add(
            Message(
                role = request.role,
                content = request.content,
                inputTokens = request.inputTokens,
                outputTokens = request.outputTokens
            )
        )
        conversation.updatedAt = LocalDateTime.now()

        // 첫 메시지로 제목 자동 설정
        if (conversation.messages.size == 1 && request.role == "user") {
            val autoTitle = request.content.take(30) + if (request.content.length > 30) "..." else ""
            return conversationRepository.save(conversation.copy(title = autoTitle)).toDetail()
        }

        return conversationRepository.save(conversation).toDetail()
    }

    fun delete(userId: Long, conversationId: String) {
        val deleted = conversationRepository.deleteByIdAndUserId(conversationId, userId)
        if (deleted == 0L) throw ConversationNotFoundException(conversationId)
    }

    fun updateTitle(userId: Long, conversationId: String, title: String): ConversationDetail {
        val conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
            ?: throw ConversationNotFoundException(conversationId)
        return conversationRepository.save(conversation.copy(title = title)).toDetail()
    }
}
