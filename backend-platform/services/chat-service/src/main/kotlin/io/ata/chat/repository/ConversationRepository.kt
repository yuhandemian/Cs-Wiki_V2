package io.ata.chat.repository

import io.ata.chat.domain.Conversation
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository

// Spring Data MongoDB Repository입니다.
// JPA Repository와 비슷하게 메서드 이름으로 query를 자동 생성합니다.
interface ConversationRepository : MongoRepository<Conversation, String> {
    // 사용자별 대화 목록을 updatedAt 내림차순으로 조회합니다.
    fun findByUserIdOrderByUpdatedAtDesc(userId: Long, pageable: Pageable): Page<Conversation>

    // 단건 조회와 소유권 확인을 동시에 처리합니다.
    fun findByIdAndUserId(id: String, userId: Long): Conversation?

    // 삭제 조건에 userId를 포함해 다른 사용자의 대화를 지우지 못하게 합니다.
    fun deleteByIdAndUserId(id: String, userId: Long): Long
}
