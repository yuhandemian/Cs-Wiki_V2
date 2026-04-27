package io.ata.prompt.exception

// prompt-service에서 사용하는 도메인 예외입니다.
// 현재는 별도 GlobalExceptionHandler가 없으므로 Spring 기본 예외 처리로 응답됩니다.

// 요청한 id의 프롬프트가 DB에 없을 때 사용합니다.
class PromptNotFoundException(id: Long) : RuntimeException("프롬프트를 찾을 수 없습니다: $id")

// 수정/삭제 시 작성자가 아닌 사용자가 접근했을 때 사용합니다.
class UnauthorizedException : RuntimeException("해당 프롬프트에 접근 권한이 없습니다")
