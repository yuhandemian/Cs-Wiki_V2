package io.ata.auth.exception

import io.ata.auth.dto.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
// Controller 바깥으로 던져진 예외를 공통 JSON 응답으로 바꾸는 곳입니다.
// 각 Controller에서 try-catch를 반복하지 않게 해줍니다.
class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateEmailException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    // 이메일 중복은 HTTP 409 Conflict로 응답합니다.
    fun handleDuplicateEmail(e: DuplicateEmailException) = ApiResponse.error(e.message ?: "Conflict")

    @ExceptionHandler(InvalidCredentialsException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    // 로그인 실패, 만료/위조 token, blacklist token 등 인증 실패를 401로 통일합니다.
    fun handleInvalidCredentials(e: InvalidCredentialsException) = ApiResponse.error(e.message ?: "Unauthorized")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    // @Valid 검증 실패를 field -> message 형태로 모아 프론트엔드가 표시하기 쉽게 만듭니다.
    fun handleValidation(e: MethodArgumentNotValidException): ApiResponse<Map<String, String>> {
        val errors = e.bindingResult.allErrors.associate {
            (it as FieldError).field to (it.defaultMessage ?: "Invalid")
        }
        return ApiResponse(false, errors, "입력값을 확인해주세요")
    }
}
