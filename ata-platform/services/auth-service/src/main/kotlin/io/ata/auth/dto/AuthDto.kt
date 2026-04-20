package io.ata.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

// Controller가 받는 요청 DTO입니다.
// data class는 Java record/DTO처럼 생성자, getter, equals/hashCode/toString을 자동 생성합니다.
data class SignUpRequest(
    // Kotlin에서는 Bean Validation annotation을 필드에 붙이기 위해 @field: 를 사용합니다.
    @field:Email val email: String,
    @field:NotBlank @field:Size(min = 8) val password: String,
    @field:NotBlank val name: String
)

// 로그인은 이메일과 비밀번호만 받습니다.
data class SignInRequest(
    @field:Email val email: String,
    @field:NotBlank val password: String
)

// 인증 성공 시 프론트엔드에 내려주는 token 묶음입니다.
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    // 기본값이 있는 Kotlin 프로퍼티입니다. 생성 시 생략하면 "Bearer"가 들어갑니다.
    val tokenType: String = "Bearer",
    // 초 단위 access token 만료 시간입니다.
    val expiresIn: Long
)

// refresh와 sign-out은 refresh token 하나만 있으면 처리할 수 있습니다.
data class RefreshRequest(
    @field:NotBlank val refreshToken: String
)

// 현재 코드에서는 아직 직접 쓰이지 않지만, 사용자 프로필 응답을 위한 DTO로 보입니다.
data class UserResponse(
    val id: Long,
    val email: String,
    val name: String,
    val plan: String
)

// 서비스 전반에서 쓰는 공통 응답 wrapper입니다.
// success/data/message 모양을 통일하면 프론트엔드에서 에러와 성공 처리가 단순해집니다.
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null
) {
    // companion object는 Java static factory method와 비슷합니다.
    companion object {
        fun <T> success(data: T) = ApiResponse(true, data)
        fun error(message: String) = ApiResponse<Nothing>(false, message = message)
    }
}
