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
// MongoDB 기반 대화 비즈니스 로직입니다.
// JPA 서비스와 달리 @Transactional을 사용하지 않습니다. 단일 document 저장 중심이라 구조가 단순합니다.
class ConversationService(private val conversationRepository: ConversationRepository) {

    fun create(userId: Long, request: CreateConversationRequest): ConversationDetail {
        // 대화방을 먼저 만들고, 메시지는 이후 addMessage API로 누적합니다.
        // provider는 대소문자 혼선을 줄이기 위해 uppercase로 저장합니다.
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
        // 최신 대화가 먼저 오도록 Repository 메서드 이름에 OrderByUpdatedAtDesc를 사용했습니다.
        val pageable = PageRequest.of(page, size)
        return conversationRepository
            .findByUserIdOrderByUpdatedAtDesc(userId, pageable)
            .content
            .map { it.toSummary() }
    }

    fun get(userId: Long, conversationId: String): ConversationDetail {
        // MongoDB id와 userId를 함께 조건으로 걸어 "내 대화"만 조회합니다.
        val conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
            ?: throw ConversationNotFoundException(conversationId)
        return conversation.toDetail()
    }

    fun addMessage(userId: Long, conversationId: String, request: AddMessageRequest): ConversationDetail {
        val conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
            ?: throw ConversationNotFoundException(conversationId)

        // Conversation document 안의 messages 배열에 embedded document를 추가합니다.
        // MongoDB는 이런 "부모 문서 + 하위 배열" 구조가 자연스럽습니다.
        conversation.messages.add(
            Message(
                role = request.role,
                content = request.content,
                inputTokens = request.inputTokens,
                outputTokens = request.outputTokens
            )
        )
        // 메시지가 추가되었으므로 목록 정렬 기준인 updatedAt을 갱신합니다.
        conversation.updatedAt = LocalDateTime.now()

        // 첫 메시지로 제목 자동 설정
        // 첫 사용자 메시지 앞 30자를 대화 제목으로 사용합니다.
        // copy는 data class가 제공하는 얕은 복사 함수입니다. title만 바꾼 새 객체를 만듭니다.
        if (conversation.messages.size == 1 && request.role == "user") {
            val autoTitle = request.content.take(30) + if (request.content.length > 30) "..." else ""
            return conversationRepository.save(conversation.copy(title = autoTitle)).toDetail()
        }

        return conversationRepository.save(conversation).toDetail()
    }

    fun delete(userId: Long, conversationId: String) {
        // 삭제 조건에 userId를 포함해 다른 사용자의 대화를 삭제하지 못하게 합니다.
        val deleted = conversationRepository.deleteByIdAndUserId(conversationId, userId)
        if (deleted == 0L) throw ConversationNotFoundException(conversationId)
    }

    fun updateTitle(userId: Long, conversationId: String, title: String): ConversationDetail {
        val conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
            ?: throw ConversationNotFoundException(conversationId)
        // data class copy로 title만 바꾼 새 Conversation을 저장합니다.
        // updatedAt은 여기서 갱신하지 않으므로, 제목 변경을 목록 최신순에 반영하려면 추가 갱신이 필요합니다.
        return conversationRepository.save(conversation.copy(title = title)).toDetail()
    }
}
