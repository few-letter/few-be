-- =====================================================================
-- RawContents JPA 엔티티 제거 마이그레이션
-- 목적: gen 테이블에 url, thumbnail_image_url, media_type 컬럼 추가 및
--       provisioning_contents 테이블에서 raw_contents_id 연관관계 제거
-- 실행 순서: 애플리케이션 배포 전 반드시 실행
-- =====================================================================

-- Step 1: gen 테이블에 신규 컬럼 추가 (nullable로 먼저 추가)
ALTER TABLE gen ADD COLUMN url VARCHAR(2048) NULL;
ALTER TABLE gen ADD COLUMN thumbnail_image_url VARCHAR(2048) NULL;
ALTER TABLE gen ADD COLUMN media_type INT NULL;

-- Step 2: raw_contents 데이터를 gen 테이블로 이관
--         gen -> provisioning_contents -> raw_contents 연결 경로 활용
UPDATE gen g
JOIN provisioning_contents pc ON g.provisioning_contents_id = pc.id
JOIN raw_contents rc ON pc.raw_contents_id = rc.id
SET g.url = rc.url,
    g.thumbnail_image_url = rc.thumbnail_image_url,
    g.media_type = rc.media_type;

-- Step 3: 데이터 이관 확인 (실행 후 결과 검토)
-- SELECT COUNT(*) FROM gen WHERE url IS NULL;
-- SELECT COUNT(*) FROM gen WHERE media_type IS NULL;

-- Step 4: url, media_type 컬럼 NOT NULL 제약 적용
ALTER TABLE gen MODIFY COLUMN url VARCHAR(2048) NOT NULL;
ALTER TABLE gen MODIFY COLUMN media_type INT NOT NULL;

-- Step 5: provisioning_contents 테이블에서 raw_contents_id 인덱스 삭제
DROP INDEX idx_provisioning_contents_1 ON provisioning_contents;

-- Step 6: provisioning_contents 테이블에서 raw_contents_id 컬럼 삭제
ALTER TABLE provisioning_contents DROP COLUMN raw_contents_id;

-- =====================================================================
-- 참고: raw_contents 테이블 자체는 유지됩니다.
--       이력 데이터 보존을 위해 별도로 정리 여부를 결정하세요.
-- =====================================================================
