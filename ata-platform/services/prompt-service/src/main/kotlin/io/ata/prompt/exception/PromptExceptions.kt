package io.ata.prompt.exception

class PromptNotFoundException(id: Long) : RuntimeException("프롬프트를 찾을 수 없습니다: $id")
class UnauthorizedException : RuntimeException("해당 프롬프트에 접근 권한이 없습니다")
