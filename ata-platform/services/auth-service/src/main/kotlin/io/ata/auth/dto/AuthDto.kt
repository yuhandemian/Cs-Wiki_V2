package io.ata.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SignUpRequest(
    @field:Email val email: String,
    @field:NotBlank @field:Size(min = 8) val password: String,
    @field:NotBlank val name: String
)

data class SignInRequest(
    @field:Email val email: String,
    @field:NotBlank val password: String
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long
)

data class RefreshRequest(
    @field:NotBlank val refreshToken: String
)

data class UserResponse(
    val id: Long,
    val email: String,
    val name: String,
    val plan: String
)

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null
) {
    companion object {
        fun <T> success(data: T) = ApiResponse(true, data)
        fun error(message: String) = ApiResponse<Nothing>(false, message = message)
    }
}
