# Frontend Architecture

`ata-frontend`는 Next.js App Router 기반 프론트엔드입니다.

## 주요 스택

```text
Next.js
React
TypeScript
Tailwind CSS
Zustand
TanStack React Query
Axios
Radix UI 기반 컴포넌트
```

## 디렉터리 구조

```text
ata-frontend/src/
├── app/
│   ├── (auth)/
│   ├── (dashboard)/
│   ├── globals.css
│   ├── layout.tsx
│   └── page.tsx
├── components/
│   ├── chat/
│   ├── layout/
│   ├── prompts/
│   └── ui/
├── lib/
├── stores/
└── types/
```

## App Router 구조

```text
src/app/(auth)
├── layout.tsx
├── sign-in/page.tsx
└── sign-up/page.tsx

src/app/(dashboard)
├── layout.tsx
├── chat/page.tsx
└── prompts/page.tsx
```

`(auth)`와 `(dashboard)`는 route group입니다. URL에는 괄호 이름이 들어가지 않지만, 레이아웃을 분리하는 데 사용됩니다.

## API 호출 구조

핵심 파일:

```text
src/lib/api.ts
```

역할:

- Axios 인스턴스 생성
- API base URL 설정
- access token 자동 첨부
- 401 응답 시 refresh token으로 재발급 시도
- auth/chat/ai/prompt API 함수 묶음 제공

흐름:

```text
page/component
  -> authApi, promptApi, chatApi, aiApi
  -> Axios instance
  -> api-gateway
  -> backend service
```

기본 gateway 주소:

```text
http://localhost:8080
```

환경 변수 `NEXT_PUBLIC_API_URL`이 있으면 그 값을 사용합니다.

## 인증 상태

핵심 파일:

```text
src/stores/authStore.ts
```

역할:

- access token 저장
- refresh token 저장
- 사용자 정보 저장
- 로그아웃 시 token 제거
- 인증 여부 판단

현재 구조에서는 token이 두 군데에 저장됩니다.

```text
Zustand persist state
localStorage accessToken/refreshToken
```

`api.ts`의 Axios interceptor가 `localStorage`에서 token을 직접 읽기 때문에, 로그인 직후 `setTokens`에서 localStorage에도 즉시 저장합니다.

## 채팅 상태

핵심 파일:

```text
src/stores/chatStore.ts
```

역할:

- 선택된 AI provider 목록 관리
- provider별 메시지 목록 관리
- provider별 loading/error 상태 관리
- streaming 중인 임시 텍스트 관리
- 채팅 입력창 값 관리

구조:

```text
selectedProviders: LlmProvider[]
panels: Record<LlmProvider, PanelState>
inputMessage: string
conversationId: string | null
```

provider마다 독립 패널 상태를 가지기 때문에, GPT와 Claude를 동시에 선택해도 각자의 메시지, 로딩, 에러 상태를 따로 보여줄 수 있습니다.

## 프롬프트 화면

핵심 파일:

```text
src/app/(dashboard)/prompts/page.tsx
src/components/prompts/PromptCard.tsx
src/components/prompts/CreatePromptDialog.tsx
```

역할:

- 공개 라이브러리 조회
- 내 프롬프트 조회
- 카테고리 필터
- 검색
- 새 프롬프트 생성
- 좋아요
- 프롬프트 사용

React Query 사용:

```text
queryKey = ["prompts", tab, category, search]
```

프롬프트 생성이나 좋아요 이후에는 `invalidateQueries`로 목록을 다시 가져옵니다.

## 채팅 화면

핵심 파일:

```text
src/app/(dashboard)/chat/page.tsx
src/components/chat/ChatInput.tsx
src/components/chat/AiPanel.tsx
src/components/chat/ProviderSelector.tsx
src/components/chat/ChatMessage.tsx
```

역할:

- 여러 AI provider 선택
- 같은 질문을 선택된 provider에 전송
- provider별 답변 패널 표시
- provider별 loading/error 상태 표시

현재 `ChatInput`은 `aiApi.chat`을 provider별로 동시에 호출합니다.

```text
selectedProviders.map(provider -> aiApi.chat(...))
Promise.allSettled(...)
```

백엔드에도 `/api/ai/chat/multi`가 있지만, 현재 프론트 채팅 입력은 provider별 단일 chat을 병렬 호출하는 방식입니다.

## 타입 정의

핵심 파일:

```text
src/types/index.ts
```

역할:

- provider 타입
- chat message/response 타입
- conversation 타입
- prompt 타입
- token response 타입
- API 공통 응답 타입

백엔드 DTO와 프론트 타입이 맞아야 API 호출 결과를 안정적으로 다룰 수 있습니다.

## 읽는 순서

프론트엔드를 처음 볼 때는 아래 순서를 추천합니다.

```text
1. src/lib/api.ts
2. src/stores/authStore.ts
3. src/types/index.ts
4. src/app/(auth)/sign-in/page.tsx
5. src/app/(dashboard)/layout.tsx
6. src/app/(dashboard)/prompts/page.tsx
7. src/app/(dashboard)/chat/page.tsx
8. src/stores/chatStore.ts
```

먼저 API 연결과 token 흐름을 보고, 그 다음 화면별 컴포넌트를 보면 구조가 훨씬 덜 낯섭니다.
