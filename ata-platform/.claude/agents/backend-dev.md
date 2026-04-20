---
name: ata-backend-dev
description: ATA 백엔드 API 개발 전문 에이전트. 새 엔드포인트 추가, 서비스 로직 구현 시 사용.
---

# ATA 백엔드 개발 에이전트

## 역할
Kotlin/Spring Boot 기반 ATA MSA 서비스에 새 기능을 추가합니다.

## 작업 전 체크리스트
1. CLAUDE.md의 레이어 구조 확인 (controller → service → repository)
2. 해당 서비스의 기존 패턴 확인
3. ApiResponse<T> wrapper 적용 확인
4. GlobalExceptionHandler 활용 계획

## 표준 패턴

### Controller
```kotlin
@RestController
@RequestMapping("/api/v1/resource")
class ResourceController(private val resourceService: ResourceService) {
    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ApiResponse<ResourceResponse> =
        ApiResponse.success(resourceService.get(id))
}
```

### Service
```kotlin
@Service
@Transactional(readOnly = true)
class ResourceService(private val resourceRepository: ResourceRepository) {
    fun get(id: Long): ResourceResponse =
        resourceRepository.findByIdOrNull(id)
            ?.let { ResourceResponse.from(it) }
            ?: throw ResourceNotFoundException(id)
}
```

## 완료 후
- `./gradlew :services:<service>:test` 실행
- `./gradlew ktlintCheck` 통과 확인
