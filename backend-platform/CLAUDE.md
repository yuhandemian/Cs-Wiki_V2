# ATA Platform — Claude Code Harness

## 프로젝트 개요
AllThatAI(ATA) 클론 — 통합형 LLM AI 콘솔 플랫폼
- 여러 AI 모델 동시 비교 (GPT, Claude, Gemini, Grok, Perplexity, DeepSeek)
- 프롬프트 라이브러리 관리
- AI 워크스페이스 (문서 업로드 + RAG)
- 커스텀 AI (화이트라벨링)

## MSA 아키텍처

```
[Client] → [api-gateway :8080]
              ├─ /auth/**       → auth-service :8081
              ├─ /api/chat/**   → chat-service :8082
              ├─ /api/ai/**     → ai-proxy-service :8083
              └─ /api/prompts/** → prompt-service :8084

[Infrastructure]
  MySQL  :3306  — users, subscriptions, prompts
  Redis  :6379  — JWT 블랙리스트, 캐시, rate-limit
  MongoDB:27017 — 채팅 히스토리
  Kafka  :9092  — 비동기 이벤트 (usage tracking)
  MinIO  :9000  — 파일 스토리지 (워크스페이스 문서)
```

## 서비스별 책임

| 서비스 | 포트 | 역할 |
|--------|------|------|
| api-gateway | 8080 | JWT 검증 필터, 라우팅, CORS, Rate Limiting |
| auth-service | 8081 | 회원가입/로그인, JWT 발급, OAuth2(Google) |
| ai-proxy-service | 8083 | Multi-LLM API 통합, SSE 스트리밍 |
| chat-service | 8082 | 대화 세션 관리, 히스토리 |
| prompt-service | 8084 | 프롬프트 CRUD, 라이브러리, 공유 |

## 개발 명령어

```bash
# 인프라 실행
docker compose up -d mysql redis mongodb kafka minio

# 전체 빌드
./gradlew build

# 서비스별 실행
./gradlew :services:auth-service:bootRun
./gradlew :services:ai-proxy-service:bootRun
./gradlew :services:chat-service:bootRun
./gradlew :services:prompt-service:bootRun
./gradlew :services:api-gateway:bootRun

# 린트
./gradlew ktlintCheck
./gradlew ktlintFormat

# 테스트
./gradlew test
```

## 코딩 컨벤션

- **언어**: Kotlin (Java 혼용 금지)
- **패키지**: `io.ata.<service>.<layer>` (예: `io.ata.chat.controller`)
- **레이어**: `controller → service → repository` (domain model 별도)
- **응답 형식**: 모든 API 응답은 `ApiResponse<T>` wrapper 사용
- **예외**: `GlobalExceptionHandler` + 커스텀 Exception 클래스
- **DTO**: Request/Response 분리, `data class` 사용
- **DB**: JPA Entity는 `@Entity` + `BaseEntity`(createdAt, updatedAt) 상속

## 환경변수 (로컬 개발)

```bash
# .env.local (gitignore됨)
OPENAI_API_KEY=sk-...
ANTHROPIC_API_KEY=sk-ant-...
GEMINI_API_KEY=AIza...
GROK_API_KEY=xai-...
JWT_SECRET=your-256-bit-secret
MYSQL_PASSWORD=ata_password
```

## AI 서브에이전트 가이드

각 서비스 디렉터리의 `.claude/agents/` 참조:
- `backend-dev.md` — 새 API 엔드포인트 추가 시
- `test-writer.md` — 테스트 코드 작성 시
- `refactor.md` — 리팩터링 작업 시

## 핵심 도메인 규칙

1. **Multi-LLM 동시 호출**: `ai-proxy-service`에서 Kotlin Coroutines `async {}` 병렬 실행
2. **SSE 스트리밍**: Spring WebFlux `Flux<ServerSentEvent>` 사용
3. **Rate Limiting**: Redis + Bucket4j, 플랜별 일일 토큰 한도
4. **프롬프트 공유**: public/private/organization 3단계 visibility
5. **워크스페이스**: MinIO 업로드 → 텍스트 추출 → Redis Vector (pgvector 추후)
