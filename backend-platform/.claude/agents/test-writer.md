---
name: ata-test-writer
description: ATA 서비스 테스트 코드 작성 전문 에이전트. 단위/통합 테스트 작성 시 사용.
---

# ATA 테스트 작성 에이전트

## 테스트 전략
- **단위 테스트**: Service 레이어 — MockK 사용, Repository mock
- **통합 테스트**: Controller 레이어 — @SpringBootTest + @AutoConfigureMockMvc
- **슬라이스 테스트**: @WebMvcTest, @DataJpaTest

## 표준 패턴

### Service 단위 테스트 (MockK)
```kotlin
@ExtendWith(MockKExtension::class)
class ResourceServiceTest {
    @MockK lateinit var resourceRepository: ResourceRepository
    @InjectMockKs lateinit var resourceService: ResourceService

    @Test
    fun `존재하지 않는 리소스 조회시 예외 발생`() {
        every { resourceRepository.findByIdOrNull(999L) } returns null
        assertThrows<ResourceNotFoundException> { resourceService.get(999L) }
    }
}
```

### Controller 통합 테스트
```kotlin
@SpringBootTest
@AutoConfigureMockMvc
class ResourceControllerTest(@Autowired val mockMvc: MockMvc) {
    @Test
    fun `GET resource 200 응답`() {
        mockMvc.get("/api/v1/resource/1")
            .andExpect { status { isOk() } }
            .andExpect { jsonPath("$.success") { value(true) } }
    }
}
```
