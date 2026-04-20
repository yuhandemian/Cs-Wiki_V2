# Database and Infra

이 프로젝트의 로컬 인프라는 `ata-platform/docker-compose.yml`에 정의되어 있습니다.

## 구성 요소

```text
MySQL     - auth-service, prompt-service
Redis     - refresh token blacklist, gateway rate limiter
MongoDB   - chat-service
Zookeeper - Kafka 실행용
Kafka     - 이벤트 기반 확장용으로 준비된 구성
MinIO     - 파일/객체 저장소 확장용으로 준비된 구성
```

## MySQL

MySQL은 정형 데이터 저장에 사용됩니다.

현재 연결된 서비스:

```text
auth-service   -> ata_auth
prompt-service -> ata_prompt
```

초기화 SQL:

```text
ata-platform/infra/mysql/init/01_create_databases.sql
ata-platform/infra/mysql/init/02_auth_schema.sql
ata-platform/infra/mysql/init/03_prompt_schema.sql
```

### auth-service

`auth-service`는 `ata_auth` 데이터베이스를 사용합니다.

주요 테이블:

```text
users
```

저장 내용:

- 이메일
- BCrypt hash 비밀번호
- 사용자 이름
- 구독 플랜
- 로그인 provider
- 외부 provider ID
- 생성/수정 시각

`auth-service`의 JPA 설정은 `ddl-auto: update`입니다. 즉, Entity 변경을 Hibernate가 DB schema에 반영하려고 시도합니다.

### prompt-service

`prompt-service`는 `ata_prompt` 데이터베이스를 사용합니다.

주요 테이블:

```text
prompts
```

저장 내용:

- 작성자 userId
- 제목
- 본문
- 설명
- 카테고리
- 공개 범위
- 좋아요 수
- 사용 횟수
- 생성/수정 시각

`prompt-service`의 JPA 설정은 `ddl-auto: validate`입니다. 즉, Hibernate가 schema를 만들거나 수정하지 않고, Entity와 DB schema가 맞는지만 검증합니다.

이 차이가 중요합니다.

```text
auth-service:   ddl-auto update   -> 개발 중 schema 자동 변경 허용
prompt-service: ddl-auto validate -> SQL로 만든 schema와 Entity 일치 여부만 확인
```

## Redis

Redis는 두 용도로 등장합니다.

### Refresh Token Blacklist

`auth-service`는 로그아웃하거나 refresh token rotation이 일어날 때 refresh token을 Redis blacklist에 저장합니다.

```text
blacklist:refresh:<refreshToken> -> "1"
```

이 key가 있으면 해당 refresh token은 더 이상 사용할 수 없습니다.

### Gateway Rate Limiter

`api-gateway`의 `application.yml`에는 `RequestRateLimiter` 필터가 있습니다.

```text
/auth/**    -> replenishRate 20, burstCapacity 40
/api/ai/**  -> replenishRate 10, burstCapacity 20
```

즉, 로그인/회원가입과 AI 요청에 대한 요청 제한을 Redis 기반으로 둘 수 있습니다.

## MongoDB

MongoDB는 `chat-service`에서 사용합니다.

데이터베이스:

```text
ata_chat
```

컬렉션:

```text
conversations
```

Document 구조:

```text
Conversation
├── id
├── userId
├── title
├── provider
├── model
├── messages[]
├── workspaceId
├── createdAt
└── updatedAt
```

메시지는 별도 collection으로 나뉘지 않고 `messages` 배열에 embedded document로 저장됩니다.

장점:

- 대화 상세 조회 시 한 번에 메시지 목록까지 가져올 수 있습니다.
- 초기 구현이 단순합니다.

주의점:

- 대화 하나의 메시지가 매우 많아지면 MongoDB document 크기 제한을 고려해야 합니다.
- 긴 대화를 많이 저장하는 서비스라면 메시지를 별도 collection으로 분리하는 설계도 검토할 수 있습니다.

## Kafka

`docker-compose.yml`에는 Zookeeper와 Kafka가 포함되어 있습니다.

현재 1차 코드 스캔 기준으로 Kafka를 직접 사용하는 Kotlin 코드는 확인되지 않았습니다.

추후 사용할 수 있는 방향:

- 채팅 완료 이벤트 발행
- 사용량 집계 이벤트 발행
- 알림/로그/분석 파이프라인
- AI 요청 이력 비동기 처리

## MinIO

MinIO는 S3 호환 객체 저장소입니다.

현재 1차 코드 스캔 기준으로 MinIO를 직접 사용하는 Kotlin 코드는 확인되지 않았습니다.

추후 사용할 수 있는 방향:

- 첨부 파일 저장
- 이미지 생성 결과 저장
- 대용량 문서 업로드
- 워크스페이스 파일 관리

## 포트 정리

```text
api-gateway       8080
auth-service      8081
chat-service      8082
ai-proxy-service  8083
prompt-service    8084

MySQL             3306
Redis             6379
MongoDB           27017
Kafka             9092
Zookeeper         2181
MinIO API         9000
MinIO Console     9001
```

## 로컬 실행 순서

개념적으로는 아래 순서입니다.

```text
1. docker-compose로 인프라 실행
2. auth-service 실행
3. chat-service 실행
4. ai-proxy-service 실행
5. prompt-service 실행
6. api-gateway 실행
7. ata-frontend 실행
```

프론트엔드는 gateway 주소인 `http://localhost:8080`을 바라봅니다.
