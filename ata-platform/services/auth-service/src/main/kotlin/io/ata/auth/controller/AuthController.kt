package io.ata.auth.controller

import io.ata.auth.dto.*
import io.ata.auth.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
// 인증 API의 HTTP 입구입니다.
// Controller는 요청/응답 모양을 담당하고, 실제 회원가입/로그인 정책은 AuthService에 위임합니다.
class AuthController(private val authService: AuthService) {

    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    // @Valid가 SignUpRequest의 Bean Validation annotation을 검사합니다.
    // 성공하면 TokenResponse를 공통 ApiResponse 형태로 감싸서 프론트엔드에 돌려줍니다.
    fun signUp(@Valid @RequestBody request: SignUpRequest): ApiResponse<TokenResponse> =
        ApiResponse.success(authService.signUp(request))

    @PostMapping("/sign-in")
    // Kotlin의 expression body 문법입니다.
    // Java의 { return ApiResponse.success(...); }와 같은 의미입니다.
    fun signIn(@Valid @RequestBody request: SignInRequest): ApiResponse<TokenResponse> =
        ApiResponse.success(authService.signIn(request))

    @PostMapping("/refresh")
    // access token이 만료되었을 때 refresh token으로 새 token 쌍을 발급받는 API입니다.
    fun refresh(@Valid @RequestBody request: RefreshRequest): ApiResponse<TokenResponse> =
        ApiResponse.success(authService.refresh(request))

    @PostMapping("/sign-out")
    // 서버 쪽 로그아웃 처리는 refresh token을 더 이상 사용할 수 없게 만드는 것입니다.
    // access token은 stateless JWT라서 서버에 저장되어 있지 않으므로 여기서는 refresh token만 blacklist에 넣습니다.
    fun signOut(@Valid @RequestBody request: RefreshRequest): ApiResponse<Unit> {
        authService.signOut(request.refreshToken)
        return ApiResponse.success(Unit)
    }
}
