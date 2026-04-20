# Service Scan

이 문서는 학습 문서화 1단계의 결과입니다. 현재 프로젝트를 서비스별로 훑고, 각 모듈의 책임과 읽어야 할 핵심 파일을 정리합니다.

## 루트 구조

```text
Cs-Wiki_V2/
├── ata-frontend/
├── ata-platform/
├── ATA 서비스소개서-Light-1021.pdf
└── 트리플오스 회사 소개서.pdf
```

- `ata-frontend`: Next.js 기반 프론트엔드입니다.
- `ata-platform`: Kotlin/Spring Boot 기반 멀티모듈 백엔드입니다.
- PDF 파일 2개는 프로젝트 참고 자료 또는 서비스 소개 자료로 보입니다.

## 백엔드 플랫폼

`ata-platform`은 Gradle Kotlin DSL을 사용하는 멀티모듈 프로젝트입니다.

```text
ata-platform/services/
├── api-gateway
├── auth-service
├── ai-proxy-service
├── chat-service
└── prompt-service
```

공통 설정은 `ata-platform/build.gradle.kts`에서 관리합니다.

- Kotlin JVM: `1.9.25`
- Spring Boot: `3.3.5`
- Spring Cloud: `2023.0.3`
- JVM target: `17`

## auth-service

### 역할

회원가입, 로그인, JWT 발급, refresh token 처리, 로그아웃 처리를 담당합니다.

### 주요 파일

```text
auth-service/src/main/kotlin/io/ata/auth/
├── AuthApplication.kt
├── config/SecurityConfig.kt
├── controller/AuthController.kt
├── domain/User.kt
├── dto/AuthDto.kt
├── exception/AuthExceptions.kt
├── exception/GlobalExceptionHandler.kt
├── repository/UserRepository.kt
├── service/AuthService.kt
└── service/JwtService.kt
```

### 핵심 흐름

- `AuthController`가 `/auth/sign-up`, `/auth/sign-in`, `/auth/refresh`, `/auth/sign-out` 요청을 받습니다.
- `AuthService`가 실제 회원가입/로그인/refresh/logout 비즈니스 로직을 처리합니다.
- `UserRepository`는 이메일 조회와 중복 체크를 담당합니다.
- `JwtService`는 access token과 refresh token을 생성하고 검증합니다.
- `SecurityConfig`는 auth-service 자체의 Spring Security 설정을 담당합니다.
- `GlobalExceptionHandler`는 인증 관련 예외와 validation 예외를 API 응답 형태로 바꿉니다.

### 학습 포인트

- Kotlin 생성자 주입
- JPA Entity를 Kotlin class로 작성하는 방식
- Bean Validation annotation의 `@field:` 사용
- Spring Security 설정 DSL
- JWT 생성과 검증
- Redis를 이용한 refresh token blacklist 처리

## api-gateway

### 역할

외부 요청을 각 백엔드 서비스로 라우팅하고, 보호된 API 요청의 JWT를 검증합니다.

### 주요 파일

```text
api-gateway/src/main/kotlin/io/ata/gateway/
├── GatewayApplication.kt
└── filter/JwtAuthFilter.kt
```

### 핵심 흐름

- `JwtAuthFilter`가 `Authorization: Bearer ...` 헤더에서 토큰을 꺼냅니다.
- JWT 서명과 만료 여부를 검증합니다.
- 검증에 성공하면 downstream service가 사용할 수 있도록 `X-User-Id`, `X-User-Email`, `X-User-Plan` 헤더를 추가합니다.
- 검증에 실패하면 `401 Unauthorized` JSON 응답을 반환합니다.

### 학습 포인트

- Spring Cloud Gateway의 `AbstractGatewayFilterFactory`
- Servlet 방식이 아닌 Reactive Gateway 필터 흐름
- gateway에서 인증 정보를 내부 헤더로 전달하는 방식

## prompt-service

### 역할

사용자 프롬프트를 생성, 조회, 수정, 삭제하고 공개 프롬프트 라이브러리, 검색, 좋아요, 사용 횟수 증가 기능을 제공합니다.

### 주요 파일

```text
prompt-service/src/main/kotlin/io/ata/prompt/
├── PromptApplication.kt
├── controller/PromptController.kt
├── domain/Prompt.kt
├── dto/PromptDto.kt
├── exception/PromptExceptions.kt
├── repository/PromptRepository.kt
└── service/PromptService.kt
```

### 핵심 흐름

- `PromptController`가 `/api/prompts` 하위 API를 제공합니다.
- 사용자의 ID는 gateway가 넣어준 `X-User-Id` 헤더에서 받습니다.
- `PromptService`가 프롬프트 생성, 내 프롬프트 조회, 공개 라이브러리 조회, 검색, 수정, 삭제, 좋아요, 사용 횟수 증가를 처리합니다.
- `PromptRepository`는 JPA Repository이며 검색용 JPQL query와 count 증가용 modifying query를 포함합니다.

### 학습 포인트

- REST CRUD 패턴
- `@RequestHeader`로 인증 사용자 ID를 받는 방식
- JPA Repository method query
- `@Query`, `@Modifying` 사용
- enum을 Entity 필드로 저장하는 방식

## chat-service

### 역할

사용자별 대화방과 메시지 목록을 MongoDB에 저장하고 조회합니다.

### 주요 파일

```text
chat-service/src/main/kotlin/io/ata/chat/
├── ChatApplication.kt
├── controller/ConversationController.kt
├── domain/Conversation.kt
├── dto/ChatDto.kt
├── exception/ChatExceptions.kt
├── repository/ConversationRepository.kt
└── service/ConversationService.kt
```

### 핵심 흐름

- `ConversationController`가 `/api/chat/conversations` 하위 API를 제공합니다.
- gateway가 전달한 `X-User-Id` 헤더를 기준으로 대화 데이터를 사용자별로 분리합니다.
- `ConversationService`가 대화 생성, 목록 조회, 상세 조회, 메시지 추가, 제목 변경, 삭제를 처리합니다.
- `ConversationRepository`는 MongoDB Repository입니다.

### 학습 포인트

- Spring Data MongoDB
- Mongo document와 embedded message 구조
- 사용자별 데이터 접근 제한
- 페이지네이션

## ai-proxy-service

### 역할

프론트엔드 또는 다른 서비스의 AI 요청을 받아 여러 LLM provider의 API 형식으로 변환하고 호출합니다.

### 주요 파일

```text
ai-proxy-service/src/main/kotlin/io/ata/aiproxy/
├── AiProxyApplication.kt
├── client/AnthropicClient.kt
├── client/LlmClient.kt
├── client/OpenAiClient.kt
├── config/AiProviderProperties.kt
├── controller/AiProxyController.kt
├── domain/LlmProvider.kt
├── dto/ChatDto.kt
└── service/AiProxyService.kt
```

### 핵심 흐름

- `AiProxyController`가 `/api/ai/chat`, `/api/ai/chat/stream`, `/api/ai/chat/multi` 요청을 받습니다.
- `AiProxyService`가 provider 이름을 enum으로 변환하고 알맞은 client를 선택합니다.
- `OpenAiClient`, `AnthropicClient`가 각 provider의 HTTP API를 호출합니다.
- multi chat은 여러 provider 요청을 coroutine으로 병렬 실행합니다.
- stream chat은 Server-Sent Events 형식으로 chunk를 내려줍니다.

### 학습 포인트

- Kotlin coroutine의 `suspend`, `async`, `coroutineScope`
- WebClient 기반 외부 HTTP 호출
- Server-Sent Events
- provider별 API 형식 차이를 client로 분리하는 구조
- 설정값을 `@ConfigurationProperties`로 묶는 방식

## 인프라

`ata-platform/docker-compose.yml`은 로컬 개발용 인프라를 정의합니다.

```text
mysql
redis
mongodb
zookeeper
kafka
minio
```

현재 스캔 기준으로 명확히 연결된 저장소는 다음과 같습니다.

- MySQL: `auth-service`, `prompt-service`
- Redis: `auth-service`, `api-gateway`
- MongoDB: `chat-service`

Kafka와 MinIO는 docker-compose에는 있지만, 현재 1차 스캔에서 직접 사용하는 Kotlin 코드는 아직 확인되지 않았습니다. 이후 전체 스캔에서 실제 사용 여부를 다시 확인합니다.

## 프론트엔드

`ata-frontend`는 Next.js App Router 기반입니다.

```text
ata-frontend/src/
├── app/
├── components/
├── lib/
├── stores/
└── types/
```

### 주요 파일

```text
src/lib/api.ts
src/stores/authStore.ts
src/stores/chatStore.ts
src/types/index.ts
src/app/(auth)/sign-in/page.tsx
src/app/(auth)/sign-up/page.tsx
src/app/(dashboard)/chat/page.tsx
src/app/(dashboard)/prompts/page.tsx
```

### 핵심 흐름

- `src/lib/api.ts`가 Axios 인스턴스를 만들고 모든 백엔드 API 호출을 감쌉니다.
- request interceptor가 `localStorage.accessToken`을 `Authorization` 헤더에 붙입니다.
- response interceptor가 `401` 응답을 받으면 refresh token으로 access token 재발급을 시도합니다.
- `authStore`는 access token과 refresh token 저장/삭제를 담당합니다.
- `Prompt` 관련 화면은 React Query로 목록 캐시와 mutation 후 invalidation을 처리합니다.

### 학습 포인트

- Next.js App Router 구조
- client component와 store 사용
- Axios interceptor
- JWT token 저장 위치
- React Query query key와 invalidate

## 다음 단계

다음 작업은 `docs/02-project-overview.md`를 작성하는 것입니다. 이 문서는 위 스캔 내용을 더 읽기 쉬운 전체 구조 설명으로 바꾸고, "처음 프로젝트를 열었을 때 무엇부터 보면 되는지"를 안내합니다.
