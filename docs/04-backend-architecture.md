# Backend Architecture

백엔드는 `ata-platform` 아래의 Gradle 멀티모듈 프로젝트입니다. 각 서비스는 독립적인 Spring Boot 애플리케이션이고, 외부 요청은 `api-gateway`를 통해 들어오는 구조입니다.

## 모듈 구성

```text
ata-platform/
├── settings.gradle.kts
├── build.gradle.kts
├── docker-compose.yml
└── services/
    ├── api-gateway
    ├── auth-service
    ├── prompt-service
    ├── chat-service
    └── ai-proxy-service
```

`settings.gradle.kts`에서 서비스 모듈을 include하고, 루트 `build.gradle.kts`에서 Kotlin, Spring Boot, dependency management, 테스트 설정을 공통 적용합니다.

## 공통 계층 구조

대부분의 서비스는 아래 구조를 따릅니다.

```text
Application.kt        # Spring Boot 시작점
controller/           # HTTP 요청/응답
service/              # 비즈니스 로직과 트랜잭션
domain/               # Entity, Document, enum
dto/                  # 요청/응답 DTO
repository/           # DB 접근
exception/            # 도메인 예외
resources/application.yml
```

Java Spring Boot 프로젝트와 같은 계층 구조입니다. Kotlin 문법만 다를 뿐 역할은 익숙한 방식 그대로입니다.

## Gateway 중심 인증

`api-gateway`는 단순 라우터가 아니라 보호된 API의 인증 필터 역할도 합니다.

```text
Frontend
  -> Authorization: Bearer <accessToken>
  -> api-gateway JwtAuthFilter
  -> JWT 검증
  -> X-User-Id / X-User-Email / X-User-Plan 추가
  -> downstream service
```

내부 서비스는 access token을 직접 파싱하지 않고 gateway가 넘겨준 `X-User-*` 헤더를 사용합니다. 예를 들어 `prompt-service`는 프롬프트 생성, 수정, 삭제에서 `X-User-Id`를 현재 사용자 ID로 사용합니다.

## auth-service

`auth-service`는 인증의 기준점입니다.

- 사용자 저장: MySQL `ata_auth.users`
- 비밀번호 hash: BCrypt
- access token 발급: userId, email, plan claim 포함
- refresh token 발급: userId만 subject로 포함
- refresh token blacklist: Redis

주요 읽기 순서:

```text
AuthController.kt
AuthService.kt
JwtService.kt
SecurityConfig.kt
User.kt
AuthDto.kt
```

## prompt-service

`prompt-service`는 JPA CRUD 패턴을 공부하기 좋은 서비스입니다.

- 사용자 프롬프트 생성
- 내 프롬프트 목록 조회
- 공개 프롬프트 라이브러리 조회
- 공개 프롬프트 검색
- 프롬프트 수정/삭제
- 좋아요 수 증가
- 사용 횟수 증가

주요 읽기 순서:

```text
PromptController.kt
PromptService.kt
PromptRepository.kt
Prompt.kt
PromptDto.kt
```

특징:

- `Prompt` Entity는 `User` Entity와 JPA 관계를 맺지 않고 `userId` 값만 저장합니다.
- 마이크로서비스 구조에서는 서비스 간 DB 조인보다 ID 기반 연결을 선호합니다.
- 수정/삭제는 DB의 `prompt.userId`와 gateway가 넘긴 `X-User-Id`를 비교해 소유권을 검사합니다.
- 좋아요/사용 횟수는 Entity save 대신 JPQL update query로 바로 증가시킵니다.

## chat-service

`chat-service`는 MongoDB 기반입니다. 대화방 하나가 document이고, 메시지 목록이 그 안에 포함되는 구조입니다.

주요 기능:

- 대화방 생성
- 사용자별 대화 목록 조회
- 대화 상세 조회
- 메시지 추가
- 첫 사용자 메시지 기반 제목 자동 생성
- 대화 제목 수정
- 대화 삭제

주요 읽기 순서:

```text
ConversationController.kt
ConversationService.kt
ConversationRepository.kt
Conversation.kt
ChatDto.kt
```

특징:

- `@Document(collection = "conversations")`로 MongoDB 컬렉션에 저장합니다.
- 메시지는 별도 collection이 아니라 `Conversation.messages` 배열에 embedded document로 저장합니다.
- 조회/수정/삭제는 항상 `conversationId + userId`를 함께 조건으로 사용해 사용자별 데이터를 격리합니다.
- MongoDB document ID는 `String?`으로 두고, 저장 후 응답 변환 시 `id!!`로 반드시 존재한다고 가정합니다.

## ai-proxy-service

`ai-proxy-service`는 여러 LLM provider 호출을 한 인터페이스로 묶습니다.

- OpenAI client
- Anthropic client
- 단일 chat
- streaming chat
- multi-provider chat

주요 읽기 순서:

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

특징:

- Controller와 Service는 내부 표준 DTO인 `ChatMessage`, `ChatResponse`, `ChatChunk`를 사용합니다.
- provider별 API 차이는 `OpenAiClient`, `AnthropicClient` 내부에 숨깁니다.
- 일반 chat은 Kotlin coroutine의 `suspend` 함수로 처리합니다.
- streaming chat은 Reactor `Flux`와 SSE로 처리합니다.
- multi chat은 coroutine `async`를 사용해 여러 provider 요청을 병렬 실행합니다.
- enum과 설정에는 Gemini, Grok, Perplexity, DeepSeek가 있지만 현재 실제 client 매핑은 OpenAI/Anthropic만 되어 있습니다.

이 서비스는 Kotlin coroutine, WebClient, Server-Sent Events가 등장하므로 마지막에 공부하는 것을 추천합니다.

## 트랜잭션 기준

서비스 클래스에는 보통 다음 패턴을 씁니다.

```kotlin
@Transactional(readOnly = true)
class PromptService(...)
```

읽기 전용을 기본으로 두고, DB를 변경하는 메서드만 별도로 `@Transactional`을 붙입니다.

```kotlin
@Transactional
fun create(...)
```

이렇게 하면 의도하지 않은 쓰기를 줄이고, 읽기 작업 최적화 여지를 가질 수 있습니다.
