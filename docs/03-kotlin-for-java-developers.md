# Kotlin for Java Developers

이 문서는 이 프로젝트를 읽는 데 필요한 Kotlin 문법만 먼저 설명합니다. Kotlin 전체 문법을 다루기보다, 현재 코드에서 바로 만나는 표현을 Java/Spring Boot 관점으로 번역합니다.

## val과 var

```kotlin
val id: Long = 0
var name: String
```

- `val`: 한 번 초기화하면 재할당할 수 없습니다. Java의 `final` 변수에 가깝습니다.
- `var`: 재할당할 수 있습니다.

Entity에서 `id`, `email`, `createdAt`처럼 바뀌면 안 되는 값은 `val`, `name`, `password`, `updatedAt`처럼 수정 가능한 값은 `var`로 둡니다.

## 생성자 선언

```kotlin
class AuthService(
    private val userRepository: UserRepository,
    private val jwtService: JwtService
)
```

Java로 쓰면 대략 아래와 같습니다.

```java
class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;

    AuthService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }
}
```

Spring은 이 생성자를 보고 자동으로 Bean을 주입합니다.

## nullable 타입

```kotlin
var password: String? = null
```

`String?`는 null이 가능하다는 뜻입니다. `String`은 기본적으로 null을 허용하지 않습니다.

Java에서는 모든 참조 타입이 null일 수 있지만, Kotlin은 타입에 null 가능성을 표시합니다.

## Elvis operator

```kotlin
val user = userRepository.findByEmail(request.email)
    ?: throw InvalidCredentialsException()
```

`?:`는 왼쪽 값이 null이면 오른쪽 값을 사용합니다. 여기서는 사용자를 못 찾으면 예외를 던집니다.

Java로 쓰면 대략 다음과 같습니다.

```java
User user = userRepository.findByEmail(request.getEmail());
if (user == null) {
    throw new InvalidCredentialsException();
}
```

## data class

```kotlin
data class SignInRequest(
    val email: String,
    val password: String
)
```

`data class`는 DTO에 자주 씁니다. Java record와 비슷하게 생성자, getter, `equals`, `hashCode`, `toString`을 자동 생성합니다.

## companion object

```kotlin
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
```

`companion object`는 Java의 `static` factory method와 비슷한 역할을 합니다.

Java 감각으로 보면 `ApiResponse.success(data)`와 `ApiResponse.error(message)` 같은 정적 메서드를 만든 것입니다.

## annotation target: @field

```kotlin
data class SignUpRequest(
    @field:Email val email: String,
    @field:NotBlank @field:Size(min = 8) val password: String
)
```

Kotlin 프로퍼티는 내부적으로 field, getter, constructor parameter 등 여러 위치로 나뉩니다.
Bean Validation은 실제 필드에 annotation이 붙어야 안정적으로 동작하므로 `@field:`를 사용합니다.

## expression body

```kotlin
fun signIn(request: SignInRequest): ApiResponse<TokenResponse> =
    ApiResponse.success(authService.signIn(request))
```

함수 본문이 한 줄일 때 `{ return ... }` 대신 `=`로 바로 반환값을 표현할 수 있습니다.

Java로 보면 아래와 같습니다.

```java
ApiResponse<TokenResponse> signIn(SignInRequest request) {
    return ApiResponse.success(authService.signIn(request));
}
```

## runCatching

```kotlin
val userId = runCatching { jwtService.validateAndGetUserId(request.refreshToken) }
    .getOrElse { throw InvalidCredentialsException() }
```

`runCatching`은 예외가 날 수 있는 코드를 `Result`로 감쌉니다. 여기서는 JWT 검증 실패를 인증 실패 예외로 바꾸는 용도입니다.

Java의 `try-catch`와 비슷합니다.

## lazy

```kotlin
private val signingKey by lazy {
    Keys.hmacShaKeyFor(secret.toByteArray())
}
```

`by lazy`는 처음 접근할 때 값을 계산하고 이후에는 캐시합니다. JWT signing key는 매 요청마다 새로 만들 필요가 없기 때문에 lazy로 한 번만 만듭니다.

## Unit

```kotlin
fun signOut(...): ApiResponse<Unit>
```

`Unit`은 Java의 `void`와 비슷하지만, 타입으로 사용할 수 있습니다. 응답 데이터가 따로 없다는 뜻입니다.

## suspend

```kotlin
suspend fun chat(request: SingleChatRequest): ChatResponse
```

`suspend`는 Kotlin coroutine에서 쓰는 비동기 함수 표시입니다. Java의 `CompletableFuture`나 reactive 흐름처럼, 기다릴 수 있는 비동기 작업을 표현합니다.
이 프로젝트에서는 `ai-proxy-service`가 외부 AI API를 호출할 때 사용합니다.
