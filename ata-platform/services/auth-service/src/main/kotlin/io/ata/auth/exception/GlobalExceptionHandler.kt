package io.ata.auth.exception

import io.ata.auth.dto.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateEmailException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleDuplicateEmail(e: DuplicateEmailException) = ApiResponse.error(e.message ?: "Conflict")

    @ExceptionHandler(InvalidCredentialsException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleInvalidCredentials(e: InvalidCredentialsException) = ApiResponse.error(e.message ?: "Unauthorized")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidation(e: MethodArgumentNotValidException): ApiResponse<Map<String, String>> {
        val errors = e.bindingResult.allErrors.associate {
            (it as FieldError).field to (it.defaultMessage ?: "Invalid")
        }
        return ApiResponse(false, errors, "입력값을 확인해주세요")
    }
}
