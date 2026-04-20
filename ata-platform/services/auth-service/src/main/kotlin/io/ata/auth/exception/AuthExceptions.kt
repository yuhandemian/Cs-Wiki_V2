package io.ata.auth.exception

class DuplicateEmailException(email: String) : RuntimeException("이미 사용 중인 이메일입니다: $email")
class InvalidCredentialsException : RuntimeException("이메일 또는 비밀번호가 올바르지 않습니다")
class UserNotFoundException(id: Long) : RuntimeException("사용자를 찾을 수 없습니다: $id")
