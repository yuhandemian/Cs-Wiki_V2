# Core Flows

이 문서는 프로젝트의 핵심 기능 흐름을 요청 단위로 따라갑니다. 먼저 인증 흐름부터 정리하고, 이후 프롬프트, 채팅, AI proxy 흐름을 확장합니다.

## 회원가입

```text
Frontend
  -> POST /auth/sign-up
  -> api-gateway
  -> auth-service AuthController.signUp
  -> AuthService.signUp
  -> UserRepository.existsByEmail
  -> PasswordEncoder.encode
  -> UserRepository.save
  -> JwtService.generateAccessToken
  -> JwtService.generateRefreshToken
  -> TokenResponse
```

동작 설명:

1. 프론트엔드는 이메일, 비밀번호, 이름을 전송합니다.
2. gateway는 `/auth/**` 요청을 JWT 검증 없이 `auth-service`로 넘깁니다.
3. `AuthController.signUp`이 요청 DTO validation을 수행합니다.
4. `AuthService.signUp`이 이메일 중복 여부를 확인합니다.
5. 비밀번호는 BCrypt로 해시한 뒤 저장합니다.
6. 저장된 사용자 ID를 기준으로 access token과 refresh token을 발급합니다.
7. 프론트엔드는 토큰을 저장하고 이후 요청에 access token을 붙입니다.

## 로그인

```text
Frontend
  -> POST /auth/sign-in
  -> api-gateway
  -> auth-service AuthController.signIn
  -> AuthService.signIn
  -> UserRepository.findByEmail
  -> PasswordEncoder.matches
  -> JwtService.generateAccessToken
  -> JwtService.generateRefreshToken
  -> TokenResponse
```

동작 설명:

1. 이메일로 사용자를 조회합니다.
2. 사용자가 없으면 `InvalidCredentialsException`을 던집니다.
3. 비밀번호가 맞지 않아도 같은 예외를 던집니다.
4. 성공하면 새 access token과 refresh token을 발급합니다.

사용자 존재 여부와 비밀번호 실패를 같은 예외로 처리하는 이유는 보안상 "이 이메일이 가입되어 있는지"를 외부에 알려주지 않기 위해서입니다.

## Access Token 기반 API 요청

```text
Frontend
  -> Authorization: Bearer <accessToken>
  -> api-gateway JwtAuthFilter
  -> JWT 검증
  -> X-User-Id / X-User-Email / X-User-Plan 헤더 추가
  -> prompt-service, chat-service, ai-proxy-service
```

동작 설명:

1. 프론트엔드의 Axios request interceptor가 access token을 `Authorization` 헤더에 붙입니다.
2. gateway의 `JwtAuthFilter`가 Bearer token을 추출합니다.
3. JWT signature와 만료 시간을 검증합니다.
4. 성공하면 사용자 정보를 내부 헤더로 변환합니다.
5. 내부 서비스는 이 헤더를 신뢰하고 현재 사용자 ID를 판단합니다.

## Refresh Token

```text
Frontend
  -> POST /auth/refresh
  -> AuthService.refresh
  -> JwtService.validateAndGetUserId
  -> Redis blacklist 확인
  -> UserRepository.findById
  -> 기존 refresh token blacklist 등록
  -> 새 access token, refresh token 발급
```

동작 설명:

1. access token이 만료되면 프론트엔드는 refresh token으로 재발급을 요청합니다.
2. `JwtService`가 refresh token을 검증하고 subject에서 userId를 꺼냅니다.
3. Redis blacklist에 이미 등록된 refresh token이면 실패합니다.
4. 유효한 refresh token이면 기존 refresh token을 blacklist에 등록합니다.
5. 새 access token과 refresh token을 발급합니다.

이 방식은 refresh token rotation입니다. 한 번 사용한 refresh token을 다시 못 쓰게 만들어 탈취 위험을 줄입니다.

## 로그아웃

```text
Frontend
  -> POST /auth/sign-out
  -> AuthService.signOut
  -> Redis blacklist에 refresh token 저장
```

동작 설명:

1. 프론트엔드가 저장 중인 refresh token을 서버로 보냅니다.
2. 서버는 refresh token을 Redis blacklist에 등록합니다.
3. 이후 같은 refresh token으로 재발급을 시도하면 실패합니다.
4. 프론트엔드는 localStorage의 token을 삭제합니다.

## 다음에 추가할 흐름

백엔드 핵심 흐름 1차 문서화는 인증, 프롬프트, 대화, AI proxy까지 완료되었습니다.

## 프롬프트 생성

```text
Frontend
  -> POST /api/prompts
  -> api-gateway JwtAuthFilter
  -> X-User-Id 헤더 추가
  -> PromptController.create
  -> PromptService.create
  -> PromptRepository.save
  -> Prompt.toResponse
```

동작 설명:

1. 프론트엔드가 제목, 본문, 설명, 카테고리, 공개 범위를 보냅니다.
2. gateway가 access token을 검증하고 `X-User-Id` 헤더를 추가합니다.
3. `PromptController`는 이 헤더를 현재 사용자 ID로 받습니다.
4. `PromptService`는 사용자 ID와 요청 데이터를 합쳐 `Prompt` Entity를 만듭니다.
5. JPA가 MySQL `prompts` 테이블에 저장합니다.
6. Entity는 `PromptResponse` DTO로 변환되어 응답됩니다.

프론트엔드가 `userId`를 보내지 않는 점이 중요합니다. 사용자는 브라우저 요청을 조작할 수 있으므로, 작성자 ID는 gateway가 검증한 token에서 가져옵니다.

## 내 프롬프트 목록

```text
Frontend
  -> GET /api/prompts/my?page=0&size=20
  -> JwtAuthFilter
  -> PromptController.myPrompts
  -> PromptService.myPrompts
  -> PromptRepository.findByUserId
```

동작 설명:

1. 현재 사용자 ID는 `X-User-Id`에서 받습니다.
2. `PageRequest`로 page, size, 정렬 조건을 만듭니다.
3. `updatedAt` 내림차순으로 현재 사용자의 프롬프트를 조회합니다.
4. `Page<Prompt>`의 `content`만 DTO 목록으로 변환합니다.

## 공개 프롬프트 라이브러리

```text
Frontend
  -> GET /api/prompts/library
  -> PromptController.library
  -> PromptService.publicLibrary
  -> PromptRepository.findByVisibility
```

카테고리 필터가 있을 때:

```text
GET /api/prompts/library?category=CODING
  -> PromptCategory.valueOf("CODING")
  -> PromptRepository.findByVisibilityAndCategory
```

동작 설명:

1. `visibility = PUBLIC`인 프롬프트만 조회합니다.
2. 카테고리가 있으면 해당 카테고리만 필터링합니다.
3. 좋아요 수가 높은 순서로 정렬합니다.

## 프롬프트 검색

```text
Frontend
  -> GET /api/prompts/search?keyword=...
  -> PromptController.search
  -> PromptService.search
  -> PromptRepository.searchPublic
```

동작 설명:

1. 공개 프롬프트만 검색합니다.
2. 현재 구현은 title에 대해서만 `LIKE %keyword%` 검색을 수행합니다.
3. MySQL schema에는 title fulltext index가 있지만, 현재 Repository query는 JPQL LIKE를 사용합니다.

## 프롬프트 수정

```text
Frontend
  -> PUT /api/prompts/{id}
  -> JwtAuthFilter
  -> PromptController.update
  -> PromptService.update
  -> PromptRepository.findById
  -> 소유권 검사
  -> 변경 필드만 반영
  -> PromptRepository.save
```

동작 설명:

1. 수정할 프롬프트를 ID로 조회합니다.
2. DB의 `prompt.userId`와 gateway가 넘긴 `X-User-Id`를 비교합니다.
3. 작성자가 아니면 `UnauthorizedException`을 던집니다.
4. `UpdatePromptRequest`의 nullable 필드 중 값이 있는 것만 변경합니다.
5. `updatedAt`을 현재 시각으로 갱신합니다.

## 프롬프트 삭제

```text
Frontend
  -> DELETE /api/prompts/{id}
  -> PromptService.delete
  -> PromptRepository.findById
  -> 소유권 검사
  -> PromptRepository.delete
```

수정과 같은 소유권 검사를 거친 뒤 삭제합니다. 성공하면 컨트롤러는 `204 No Content`를 반환합니다.

## 좋아요와 사용 횟수

좋아요:

```text
POST /api/prompts/{id}/like
  -> existsById
  -> incrementLike
```

사용:

```text
POST /api/prompts/{id}/use
  -> findById
  -> incrementUseCount
  -> PromptResponse 반환
```

동작 설명:

1. 좋아요와 사용 횟수는 `@Modifying @Query`를 이용해 DB UPDATE로 바로 증가시킵니다.
2. Entity를 읽어서 count를 증가시키고 save하는 것보다 쿼리가 단순합니다.
3. 현재 좋아요는 사용자별 중복 방지 로직이 없습니다.
4. 현재 `use` 응답의 `useCount`는 증가 전 Entity 값을 기반으로 반환될 수 있습니다. 최신 count가 꼭 필요하면 증가 후 다시 조회해야 합니다.

## 대화방 생성

```text
Frontend
  -> POST /api/chat/conversations
  -> api-gateway JwtAuthFilter
  -> X-User-Id 헤더 추가
  -> ConversationController.create
  -> ConversationService.create
  -> ConversationRepository.save
  -> Conversation.toDetail
```

동작 설명:

1. 프론트엔드가 provider, model, title, workspaceId를 보냅니다.
2. gateway가 access token에서 사용자 ID를 검증해 `X-User-Id`로 넘깁니다.
3. `ConversationService`는 사용자 ID와 요청값으로 MongoDB document를 생성합니다.
4. 새 대화는 처음에는 메시지 목록이 비어 있습니다.

## 대화 목록 조회

```text
Frontend
  -> GET /api/chat/conversations?page=0&size=20
  -> ConversationController.list
  -> ConversationService.list
  -> ConversationRepository.findByUserIdOrderByUpdatedAtDesc
```

동작 설명:

1. 현재 사용자 ID에 해당하는 대화만 조회합니다.
2. `updatedAt` 내림차순으로 정렬합니다.
3. 목록 화면에서는 메시지 전체가 아니라 `ConversationSummary`만 반환합니다.
4. `messageCount`는 `messages.size`로 계산합니다.

## 대화 상세 조회

```text
Frontend
  -> GET /api/chat/conversations/{id}
  -> ConversationRepository.findByIdAndUserId
  -> Conversation.toDetail
```

동작 설명:

1. MongoDB document ID와 사용자 ID를 함께 조건으로 조회합니다.
2. 다른 사용자의 대화 ID를 알아도 `userId`가 다르면 조회되지 않습니다.
3. 상세 응답에는 메시지 목록이 포함됩니다.

## 메시지 추가

```text
Frontend
  -> POST /api/chat/conversations/{id}/messages
  -> ConversationService.addMessage
  -> ConversationRepository.findByIdAndUserId
  -> conversation.messages.add
  -> updatedAt 갱신
  -> ConversationRepository.save
```

동작 설명:

1. 먼저 현재 사용자의 대화인지 확인합니다.
2. `Message` embedded document를 `Conversation.messages` 배열에 추가합니다.
3. 메시지가 추가되었으므로 `updatedAt`을 현재 시각으로 바꿉니다.
4. MongoDB에 Conversation document 전체를 다시 저장합니다.

첫 메시지가 사용자 메시지이면 제목을 자동 생성합니다.

```text
첫 user message content 앞 30자
  -> conversation.copy(title = autoTitle)
  -> save
```

`copy`는 Kotlin data class 기능입니다. 기존 객체의 값을 대부분 유지하고 일부 필드만 바꾼 새 객체를 만듭니다.

## 대화 제목 수정

```text
PATCH /api/chat/conversations/{id}/title
  -> findByIdAndUserId
  -> conversation.copy(title = title)
  -> save
```

현재 구현은 제목만 바꾸고 `updatedAt`은 갱신하지 않습니다. 제목 변경도 목록 최신순에 반영하려면 `updatedAt` 갱신을 추가하면 됩니다.

## 대화 삭제

```text
DELETE /api/chat/conversations/{id}
  -> ConversationRepository.deleteByIdAndUserId
```

삭제 조건에 `userId`가 포함되어 있습니다. 삭제 결과가 0건이면 대화가 없거나 현재 사용자의 대화가 아니라는 뜻이므로 `ConversationNotFoundException`을 던집니다.

## 단일 AI Chat

```text
Frontend
  -> POST /api/ai/chat
  -> api-gateway JwtAuthFilter
  -> AiProxyController.chat
  -> AiProxyService.chat
  -> LlmProvider.valueOf
  -> getClient(provider)
  -> OpenAiClient.chat 또는 AnthropicClient.chat
  -> ChatResponse
```

동작 설명:

1. 프론트엔드는 provider, messages, model, temperature, maxTokens를 보냅니다.
2. gateway가 JWT를 검증한 뒤 `ai-proxy-service`로 라우팅합니다.
3. `AiProxyService`는 provider 문자열을 `LlmProvider` enum으로 변환합니다.
4. enum에 맞는 client를 선택합니다.
5. provider별 client가 외부 LLM API 형식에 맞춰 HTTP 요청을 보냅니다.
6. 외부 응답을 내부 표준 `ChatResponse`로 변환합니다.

현재 client가 실제 연결된 provider는 OpenAI와 Anthropic입니다. 나머지 provider는 enum과 설정은 있지만 `AiProxyService.getClient`에서 아직 지원하지 않습니다.

## OpenAI 호출

```text
OpenAiClient.chat
  -> POST /chat/completions
  -> Authorization: Bearer <apiKey>
  -> choices[0].message.content 추출
  -> usage.prompt_tokens / usage.completion_tokens 추출
```

OpenAI는 system/user/assistant 메시지를 모두 `messages` 배열 안에 넣습니다.

## Anthropic 호출

```text
AnthropicClient.chat
  -> splitSystemMessage
  -> POST /messages
  -> x-api-key, anthropic-version header
  -> content[0].text 추출
  -> usage.input_tokens / usage.output_tokens 추출
```

Anthropic은 system 메시지를 `messages` 배열이 아니라 별도 `system` 필드로 받습니다. 그래서 `splitSystemMessage`가 내부 표준 메시지 목록에서 system 메시지를 분리합니다.

## Streaming AI Chat

```text
Frontend
  -> POST /api/ai/chat/stream
  -> AiProxyController.chatStream
  -> AiProxyService.chatStream
  -> client.chatStream
  -> Flux<String>
  -> ChatChunk
  -> ServerSentEvent
```

동작 설명:

1. provider별 client가 외부 API의 streaming 응답을 `Flux<String>`으로 반환합니다.
2. `AiProxyService`는 각 문자열 조각을 `ChatChunk`로 감쌉니다.
3. 정상 종료 시 `done = true`인 chunk를 마지막에 추가합니다.
4. 에러가 발생하면 `error` 필드가 있는 chunk를 반환합니다.
5. Controller는 `ChatChunk`를 SSE 이벤트로 변환해 프론트엔드에 보냅니다.

## Multi Provider Chat

```text
Frontend
  -> POST /api/ai/chat/multi
  -> AiProxyController.multiChat
  -> AiProxyService.multiChat
  -> coroutineScope
  -> providers.map { async { client.chat(...) } }
  -> deferred.await
  -> MultiChatResponse
```

동작 설명:

1. 같은 messages를 여러 provider에 동시에 보냅니다.
2. provider별 호출은 `async`로 병렬 시작됩니다.
3. 각 호출은 `runCatching`으로 감싸져 한 provider 실패가 전체 실패로 번지지 않습니다.
4. 결과는 provider 이름을 key로 하는 map에 담깁니다.
5. 각 provider 결과는 `ChatResponseOrError`로 성공/실패를 개별 표현합니다.

이 구조 덕분에 예를 들어 OpenAI는 성공하고 Anthropic은 실패해도, 프론트엔드는 성공한 provider 답변을 보여줄 수 있습니다.
