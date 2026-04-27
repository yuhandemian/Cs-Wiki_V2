package io.ata.chat.exception

// 대화가 없거나, 현재 사용자의 대화가 아닐 때 사용합니다.
// id만 존재해도 userId가 다르면 Repository 조회 결과가 null이므로 이 예외가 발생합니다.
class ConversationNotFoundException(id: String) : RuntimeException("대화를 찾을 수 없습니다: $id")
