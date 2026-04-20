# ATA Project Learning Roadmap

이 문서는 Java 경험은 있지만 Kotlin 경험은 없는 상태에서 이 프로젝트를 공부하기 위한 작업 기획서입니다.
목표는 단순히 실행 방법을 아는 것이 아니라, 각 파일이 왜 존재하고 요청이 어떤 흐름으로 처리되는지 이해할 수 있게 만드는 것입니다.

## 목표

- 프로젝트 전체 구조를 먼저 파악한다.
- Kotlin 코드를 Java/Spring Boot 관점에서 읽을 수 있게 한다.
- 주요 백엔드 파일에 공부용 주석을 추가한다.
- 핵심 기능 흐름을 별도 문서로 정리한다.
- 프론트엔드가 백엔드 API와 연결되는 방식을 파악한다.

## 최종 산출물

```text
docs/
├── 00-learning-roadmap.md
├── 01-service-scan.md
├── 02-project-overview.md
├── 03-kotlin-for-java-developers.md
├── 04-backend-architecture.md
├── 05-core-flows.md
├── 06-database-and-infra.md
└── 07-frontend-architecture.md

LEARNING_GUIDE.md
```

문서 번호는 진행하면서 조정할 수 있습니다. `00-learning-roadmap.md`는 작업 계획, `01-service-scan.md`는 실제 코드 스캔 결과를 누적하는 용도입니다.

## 권장 학습 순서

1. 프로젝트 전체 지도 보기
2. Kotlin 문법을 이 프로젝트 코드 기준으로 익히기
3. `auth-service`에서 회원가입/로그인/JWT 발급 흐름 보기
4. `api-gateway`에서 JWT 검증과 라우팅 흐름 보기
5. `prompt-service`에서 JPA 기반 CRUD 흐름 보기
6. `chat-service`에서 MongoDB 기반 대화 저장 흐름 보기
7. `ai-proxy-service`에서 외부 LLM API 프록시 흐름 보기
8. `ata-frontend`에서 API 호출, 토큰 저장, 화면 연결 흐름 보기

## 작업 순서

1. 현재 코드 전체를 서비스별로 정밀 스캔한다.
2. `docs/` 디렉터리를 생성한다.
3. 전체 구조 문서를 작성한다.
4. Java 개발자를 위한 Kotlin 문서를 작성한다.
5. `auth-service`부터 공부용 주석을 추가한다.
6. 로그인/회원가입 흐름을 문서화한다.
7. `api-gateway`에 공부용 주석을 추가한다.
8. JWT 인증 흐름을 문서화한다.
9. `prompt-service`, `chat-service`, `ai-proxy-service` 순서로 주석 추가와 흐름 문서화를 반복한다.
10. 프론트엔드 API 연결부에 주석을 추가하고 문서화한다.
11. 마지막에 `LEARNING_GUIDE.md`를 작성한다.

## 1차 작업 범위

처음부터 모든 파일에 주석을 추가하면 양이 커져서 학습 밀도가 떨어질 수 있습니다. 1차 범위는 인증과 전체 구조를 잡는 데 집중합니다.

```text
1차 목표:
- docs/00-learning-roadmap.md 작성
- docs/01-service-scan.md 작성
- docs/02-project-overview.md 작성
- docs/03-kotlin-for-java-developers.md 작성
- auth-service 주요 파일 주석 추가
- api-gateway JWT 필터 주석 추가
- docs/05-core-flows.md에 인증 흐름 정리
- LEARNING_GUIDE.md 초안 작성
```

## 주석 작성 원칙

주석은 "코드를 다시 말하는 설명"이 아니라 "왜 이 코드가 필요한지"와 "Spring/Kotlin 관점에서 어떻게 읽어야 하는지"를 설명합니다.

### 상세 주석 대상

- Controller
- Service
- Security/JWT 관련 설정
- Gateway Filter
- 외부 API Client
- 핵심 도메인 Entity

### 중간 주석 대상

- DTO
- Repository
- Exception
- `application.yml`

### 최소 주석 대상

- `Application.kt`
- 단순 enum
- 단순 UI 컴포넌트
- Shadcn/Radix 기반 공통 UI 래퍼

## Java 개발자가 특히 볼 포인트

- Kotlin `data class`는 Java DTO/record와 비슷하다.
- `val`은 재할당 불가, `var`는 재할당 가능이다.
- `String?`처럼 `?`가 붙으면 null이 가능하다.
- `?:`는 Java의 기본값 처리, `?.`는 null-safe 호출에 가깝다.
- `companion object`는 Java의 `static` 멤버와 비슷한 용도로 쓰인다.
- `@field:NotBlank`처럼 annotation target이 붙는 문법은 Kotlin과 Bean Validation을 함께 쓸 때 중요하다.
- 생성자 주입은 `class AuthService(private val repository: UserRepository)`처럼 클래스 선언부에서 바로 표현된다.

## 진행 체크리스트

- [x] 작업 기획 문서화
- [x] `docs/` 디렉터리 생성
- [x] 1단계 서비스별 스캔 시작
- [x] 전체 구조 문서 작성
- [x] Kotlin for Java 문서 작성
- [x] `auth-service` 주석 추가
- [x] 로그인/회원가입 흐름 문서화
- [x] `api-gateway` 주석 추가
- [x] JWT 인증 흐름 문서화
- [x] 나머지 서비스 주석 및 문서화
- [x] 프론트엔드 연결부 주석 및 문서화
- [x] DB 및 인프라 문서 작성
- [x] 프론트엔드 아키텍처 문서 작성
- [x] `LEARNING_GUIDE.md` 작성

부분 진행:

- [x] `prompt-service` 주석 추가
- [x] 프롬프트 CRUD 흐름 문서화
- [x] `chat-service` 주석 및 흐름 문서화
- [x] `ai-proxy-service` 주석 및 흐름 문서화
