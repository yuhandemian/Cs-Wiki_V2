package io.ata.auth.exception

// 인증 도메인에서 발생하는 비즈니스 예외들입니다.
// RuntimeException을 상속하면 Service에서 throw한 뒤 GlobalExceptionHandler가 HTTP 응답으로 변환합니다.

// 회원가입 시 이미 존재하는 이메일이면 409 Conflict로 변환됩니다.
class DuplicateEmailException(email: String) : RuntimeException("이미 사용 중인 이메일입니다: $email")

// 로그인 실패, token 검증 실패, refresh 실패를 모두 같은 메시지로 묶습니다.
// 보안상 "이메일이 존재하는지" 같은 세부 실패 이유를 외부에 노출하지 않습니다.
class InvalidCredentialsException : RuntimeException("이메일 또는 비밀번호가 올바르지 않습니다")

// 현재 auth 흐름에서는 직접 사용되지 않지만, 사용자 조회 API가 추가될 때 쓸 수 있는 예외입니다.
class UserNotFoundException(id: Long) : RuntimeException("사용자를 찾을 수 없습니다: $id")
