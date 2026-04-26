# Project Overview

이 프로젝트는 여러 AI provider를 한 화면에서 사용하고, 대화와 프롬프트를 관리하는 플랫폼 형태의 애플리케이션입니다.
구조는 프론트엔드와 백엔드가 분리되어 있고, 백엔드는 여러 Spring Boot 서비스로 나뉘어 있습니다.

## 큰 그림

```text
Browser
  |
  | HTTP
  v
frontend-app (Next.js)
  |
  | Axios, Authorization: Bearer <accessToken>
  v
api-gateway (Spring Cloud Gateway, port 8080)
  |
  | route + JWT 검증 후 X-User-* 헤더 전달
  v
각 백엔드 서비스
```

프론트엔드는 항상 `api-gateway`를 바라보는 구조입니다. gateway는 URL path를 기준으로 요청을 알맞은 서비스로 넘깁니다.

## 서비스 구성

```text
services/
├── api-gateway          # 단일 진입점, 라우팅, JWT 검증
├── auth-service         # 회원가입, 로그인, JWT 발급, refresh/logout
├── prompt-service       # 프롬프트 CRUD, 공개 라이브러리, 검색
├── chat-service         # 대화방과 메시지 저장/조회
└── ai-proxy-service     # OpenAI/Anthropic 등 LLM provider 호출
```

## 요청 라우팅

`api-gateway`의 `application.yml` 기준 라우팅은 다음과 같습니다.

```text
/auth/**          -> auth-service:8081
/api/chat/**      -> chat-service:8082
/api/ai/**        -> ai-proxy-service:8083
/api/prompts/**   -> prompt-service:8084
```

`/auth/**`는 로그인 전에도 접근해야 하므로 JWT 필터가 붙지 않습니다. 나머지 주요 API는 gateway에서 JWT를 검증한 뒤 내부 서비스로 전달합니다.

## 인증 흐름

1. 사용자가 프론트엔드에서 회원가입 또는 로그인을 합니다.
2. `auth-service`가 access token과 refresh token을 발급합니다.
3. 프론트엔드는 두 토큰을 `localStorage`에 저장합니다.
4. 이후 API 요청마다 access token을 `Authorization: Bearer ...` 헤더에 넣습니다.
5. `api-gateway`의 `JwtAuthFilter`가 토큰을 검증합니다.
6. 검증에 성공하면 gateway가 `X-User-Id`, `X-User-Email`, `X-User-Plan` 헤더를 추가합니다.
7. `prompt-service`, `chat-service` 같은 내부 서비스는 이 헤더를 보고 현재 사용자를 판단합니다.

## 데이터 저장소

```text
MySQL     - auth-service, prompt-service
Redis     - refresh token blacklist, gateway rate limiter
MongoDB   - chat-service
Kafka     - docker-compose에는 있으나 현재 직접 사용 코드는 미확인
MinIO     - docker-compose에는 있으나 현재 직접 사용 코드는 미확인
```

## 처음 읽을 파일

백엔드를 먼저 이해하려면 아래 순서를 추천합니다.

```text
1. backend root `settings.gradle.kts`
2. backend root `build.gradle.kts`
3. `services/auth-service/.../AuthController.kt`
4. `services/auth-service/.../AuthService.kt`
5. `services/auth-service/.../JwtService.kt`
6. `services/api-gateway/.../JwtAuthFilter.kt`
7. `services/api-gateway/src/main/resources/application.yml`
```

프론트엔드 연결을 보려면 다음 파일을 보면 됩니다.

```text
1. `src/lib/api.ts`
2. `src/stores/authStore.ts`
3. `src/app/(auth)/sign-in/page.tsx`
4. `src/app/(dashboard)/chat/page.tsx`
5. `src/app/(dashboard)/prompts/page.tsx`
```

## Java/Spring 경험으로 읽는 방법

이 코드는 Java Spring Boot 프로젝트와 구조가 거의 같습니다.

- `Controller`: HTTP 요청을 받습니다.
- `Service`: 비즈니스 로직을 처리합니다.
- `Repository`: DB 접근을 담당합니다.
- `DTO`: 요청/응답 객체입니다.
- `Entity` 또는 `Document`: DB에 저장되는 도메인 모델입니다.
- `application.yml`: 포트, DB, Redis, JWT secret 같은 환경 설정입니다.

Kotlin 문법이 낯설어도 Spring의 계층 구조는 익숙한 방식 그대로입니다. 먼저 "이 클래스가 어느 계층인가"를 보고, 그 다음 Kotlin 문법을 하나씩 대응시키면 됩니다.
