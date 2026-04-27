USE ata_prompt;

CREATE TABLE IF NOT EXISTS prompts (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    title       VARCHAR(200) NOT NULL,
    content     TEXT NOT NULL,
    description VARCHAR(500),
    category    ENUM('GENERAL','CODING','WRITING','ANALYSIS','EDUCATION','MARKETING','DESIGN','CUSTOMER_SERVICE') NOT NULL DEFAULT 'GENERAL',
    visibility  ENUM('PRIVATE','ORGANIZATION','PUBLIC') NOT NULL DEFAULT 'PRIVATE',
    like_count  INT NOT NULL DEFAULT 0,
    use_count   INT NOT NULL DEFAULT 0,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_visibility_category (visibility, category),
    INDEX idx_like_count (like_count DESC),
    FULLTEXT INDEX ft_title (title)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ATA 기본 공개 프롬프트 라이브러리 시드 데이터
INSERT INTO prompts (user_id, title, content, description, category, visibility) VALUES
(1, 'IT 서비스 기획서 작성', '당신은 IT 서비스 기획 전문가입니다. 다음 서비스 아이디어에 대한 상세한 기획서를 작성해주세요:\n\n[서비스 아이디어]: {input}\n\n기획서 형식:\n1. 서비스 개요\n2. 목표 사용자\n3. 핵심 기능\n4. 수익 모델\n5. 경쟁사 분석', 'IT 서비스 기획서 자동 작성 프롬프트', 'GENERAL', 'PUBLIC'),
(1, '백엔드 API 코드 리뷰', '당신은 10년 경력의 시니어 백엔드 개발자입니다. 아래 코드를 리뷰해주세요:\n\n```\n{code}\n```\n\n다음 관점에서 검토해주세요:\n- 보안 취약점\n- 성능 최적화\n- 코드 가독성\n- 테스트 커버리지\n- 모범 사례 준수', '백엔드 코드 리뷰 전문 프롬프트', 'CODING', 'PUBLIC'),
(1, '마케팅 카피라이팅', '당신은 창의적인 마케팅 카피라이터입니다. 다음 제품/서비스를 위한 매력적인 마케팅 문구를 작성해주세요:\n\n제품명: {product_name}\n대상 고객: {target_audience}\n핵심 가치: {key_value}\n\n다음을 작성해주세요:\n1. 헤드라인 (3가지 버전)\n2. 서브카피\n3. CTA 문구', '마케팅 카피 자동 생성 프롬프트', 'MARKETING', 'PUBLIC');
