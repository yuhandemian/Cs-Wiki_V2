package io.ata.chat.repository

import io.ata.chat.domain.Conversation
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository

interface ConversationRepository : MongoRepository<Conversation, String> {
    fun findByUserIdOrderByUpdatedAtDesc(userId: Long, pageable: Pageable): Page<Conversation>
    fun findByIdAndUserId(id: String, userId: Long): Conversation?
    fun deleteByIdAndUserId(id: String, userId: Long): Long
}
