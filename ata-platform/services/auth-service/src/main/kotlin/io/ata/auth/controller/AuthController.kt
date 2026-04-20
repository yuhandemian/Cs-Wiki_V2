package io.ata.auth.controller

import io.ata.auth.dto.*
import io.ata.auth.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    fun signUp(@Valid @RequestBody request: SignUpRequest): ApiResponse<TokenResponse> =
        ApiResponse.success(authService.signUp(request))

    @PostMapping("/sign-in")
    fun signIn(@Valid @RequestBody request: SignInRequest): ApiResponse<TokenResponse> =
        ApiResponse.success(authService.signIn(request))

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: RefreshRequest): ApiResponse<TokenResponse> =
        ApiResponse.success(authService.refresh(request))

    @PostMapping("/sign-out")
    fun signOut(@Valid @RequestBody request: RefreshRequest): ApiResponse<Unit> {
        authService.signOut(request.refreshToken)
        return ApiResponse.success(Unit)
    }
}
