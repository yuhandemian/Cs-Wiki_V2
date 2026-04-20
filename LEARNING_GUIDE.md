# ATA Learning Guide

이 문서는 이 프로젝트를 처음 공부할 때 보는 진입점입니다.
Java/Spring Boot 경험은 있지만 Kotlin은 처음이라는 전제로 작성되었습니다.

## 추천 순서

1. [학습 로드맵](docs/00-learning-roadmap.md)
2. [서비스 스캔 결과](docs/01-service-scan.md)
3. [프로젝트 전체 구조](docs/02-project-overview.md)
4. [Java 개발자를 위한 Kotlin](docs/03-kotlin-for-java-developers.md)
5. [핵심 기능 흐름](docs/05-core-flows.md)
6. [백엔드 아키텍처](docs/04-backend-architecture.md)
7. [DB 및 인프라](docs/06-database-and-infra.md)
8. [프론트엔드 아키텍처](docs/07-frontend-architecture.md)

이 순서로 문서를 먼저 읽으면, 코드 파일의 주석이 훨씬 잘 이어집니다.

## 먼저 읽을 코드

인증 흐름부터 보는 것을 추천합니다.

```text
ata-platform/services/auth-service/src/main/kotlin/io/ata/auth/AuthApplication.kt
ata-platform/services/auth-service/src/main/kotlin/io/ata/auth/controller/AuthController.kt
ata-platform/services/auth-service/src/main/kotlin/io/ata/auth/service/AuthService.kt
ata-platform/services/auth-service/src/main/kotlin/io/ata/auth/service/JwtService.kt
ata-platform/services/auth-service/src/main/kotlin/io/ata/auth/config/SecurityConfig.kt
ata-platform/services/auth-service/src/main/kotlin/io/ata/auth/domain/User.kt
ata-platform/services/auth-service/src/main/kotlin/io/ata/auth/dto/AuthDto.kt
ata-platform/services/api-gateway/src/main/kotlin/io/ata/gateway/filter/JwtAuthFilter.kt
```

읽는 요령:

1. Controller에서 API path를 확인합니다.
2. Service에서 실제 비즈니스 로직을 따라갑니다.
3. Repository에서 DB 접근 방식이 자동 query인지 직접 query인지 봅니다.
4. DTO에서 요청/응답 데이터 모양을 확인합니다.
5. gateway filter에서 인증 정보가 내부 서비스로 어떻게 전달되는지 봅니다.

## 프론트엔드 연결 보기

백엔드 인증 흐름을 본 다음에는 프론트엔드의 API 연결부를 보면 좋습니다.

```text
ata-frontend/src/lib/api.ts
ata-frontend/src/stores/authStore.ts
ata-frontend/src/stores/chatStore.ts
```

여기서 확인할 것:

- access token이 어디에 저장되는가
- Axios가 token을 어떤 헤더에 붙이는가
- 401 응답이 오면 refresh token으로 어떻게 재시도하는가
- gateway가 기대하는 `Authorization: Bearer ...` 형식과 맞는가

## 현재 주석이 추가된 범위

```text
auth-service:
- AuthApplication.kt
- AuthController.kt
- AuthService.kt
- JwtService.kt
- SecurityConfig.kt
- User.kt
- AuthDto.kt
- UserRepository.kt
- AuthExceptions.kt
- GlobalExceptionHandler.kt

api-gateway:
- GatewayApplication.kt
- JwtAuthFilter.kt

frontend:
- src/lib/api.ts
- src/stores/authStore.ts
- src/stores/chatStore.ts
```

## 다음 학습 대상

`prompt-service`까지 주석이 추가되어 있으므로, 인증 흐름 다음에는 프롬프트 CRUD를 보면 됩니다.

추천 순서:

```text
PromptController.kt
PromptService.kt
PromptRepository.kt
Prompt.kt
PromptDto.kt
```

`chat-service`도 주석이 추가되어 있으므로, 프롬프트 CRUD 다음에는 MongoDB 기반 대화 저장 흐름을 보면 됩니다.

추천 순서:

```text
ConversationController.kt
ConversationService.kt
ConversationRepository.kt
Conversation.kt
ChatDto.kt
```

마지막으로 coroutine과 외부 HTTP 호출이 있는 `ai-proxy-service`를 보면 됩니다.

추천 순서:

```text
AiProxyController.kt
AiProxyService.kt
LlmClient.kt
OpenAiClient.kt
AnthropicClient.kt
AiProviderProperties.kt
ChatDto.kt
LlmProvider.kt
```

여기서 집중할 것:

- `suspend` 함수가 일반 AI 응답에 쓰이는 방식
- `Flux`가 streaming 응답에 쓰이는 방식
- `async`와 `coroutineScope`로 여러 provider 요청을 병렬 처리하는 방식
- provider별 외부 API 차이를 client 내부에 숨기는 방식
