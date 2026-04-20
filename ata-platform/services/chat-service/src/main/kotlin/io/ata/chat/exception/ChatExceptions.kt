package io.ata.chat.exception

class ConversationNotFoundException(id: String) : RuntimeException("대화를 찾을 수 없습니다: $id")
