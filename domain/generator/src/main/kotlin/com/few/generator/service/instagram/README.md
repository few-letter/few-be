# 인스타그램 카드 이미지 생성기

GEN 생성에 대해 인스타그램 카드 이미지 생성 기능을 Kotlin으로 포팅한 라이브러리입니다.

## 📋 개요

이 라이브러리는 뉴스 콘텐츠를 인스타그램 피드에 적합한 카드 이미지로 자동 생성합니다.

### 지원 이미지 타입

**Single News Card** (800 x 950)
   - 개별 뉴스 기사를 카드 형식으로 표현
   - 헤더 (날짜 + 카테고리) + 제목 + 본문 + 로고

## 🎨 주요 기능

- ✅ 카테고리별 색상 및 배경 이미지 자동 적용
- ✅ 하이라이트 텍스트 지원 (특정 키워드 강조)
- ✅ 한글 폰트 자동 로딩 (시스템 폰트 사용)
- ✅ 자동 줄바꿈 및 텍스트 정렬
- ✅ 로고 이미지 자동 배치

## 📦 사용법

### Single News Card 생성

```kotlin
val generator = SingleNewsCardGenerator()

val content = NewsContent(
    headline = "삼성전자, AI 반도체 기술로 글로벌 시장 주도권 확보",
    summary = "삼성전자가 인공지능(AI) 전용 반도체 개발에 성공하며...",
    category = "기술",
    createdAt = LocalDateTime.now(),
    highlightTexts = listOf("삼성전자", "AI 반도체", "성능 향상")
)

val success = generator.generateImage(content, "/path/to/output.png")
```

## 🎯 지원 카테고리

| 카테고리 | 영문 코드    | 색상        |
|----------|--------------|-------------|
| 기술     | technology   | 보라색      |
| 경제     | economy      | 핑크색      |
| 정치     | politics     | 청록색      |
| 사회     | society      | 빨간색      |
| 생활     | life         | 주황색      |
| 기타     | etc          | 보라색      |

## 📂 리소스 파일 구조

```
domain/generator/src/main/resources/images/
├── few_logo.png                    # FEW 로고
├── logo.png                        # 그룹 로고 (썸네일용)
├── technology_summary_bg.png       # 기술 카테고리 헤더 배경 (단일)
├── economy_summary_bg.png          # 경제 카테고리 헤더 배경 (단일)
├── politics_summary_bg.png         # 정치 카테고리 헤더 배경 (단일)
├── society_summary_bg.png          # 사회 카테고리 헤더 배경 (단일)
└── life_summary_bg.png             # 생활 카테고리 헤더 배경 (단일)
```

## 🔧 기술 스택

- **언어**: Kotlin
- **이미지 처리**: Java AWT (BufferedImage, Graphics2D)
- **폰트**: 시스템 한글 폰트 (Apple SD Gothic Neo)
- **이미지 포맷**: PNG

## ⚠️ 주의사항

### Python PIL vs Kotlin AWT 차이점

1. **이모지 렌더링**
   - PIL: 이모지를 비교적 잘 처리
   - AWT: 이모지 렌더링이 제한적 (시스템 폰트에 따라 다름)

2. **텍스트 측정 정확도**
   - PIL: `getbbox()` 매우 정확
   - AWT: `FontMetrics`가 CJK 문자에서 부정확할 수 있음

3. **폰트 로딩**
   - PIL: OTF/TTF 파일 직접 로드 가능
   - AWT: 시스템 폰트 사용 또는 Font.createFont() 필요

### 권장사항

- 한글 텍스트는 잘 처리되지만, 이모지 사용은 최소화하는 것을 권장
- 가능하면 macOS 환경에서 테스트 (한글 폰트 지원이 더 좋음)
- 생성된 이미지는 반드시 육안으로 확인 필요

## 🧪 테스트

```bash
./gradlew :domain:generator:test --tests "com.few.generator.service.instagram.InstagramImageGeneratorTest"
```

테스트가 성공하면 프로젝트 루트에 다음 이미지들이 생성됩니다:
- `test_single_news_card.png`
- `test_기술_card.png`
- `test_경제_card.png`
- `test_정치_card.png`
- `test_사회_card.png`
- `test_생활_card.png`

## 📝 Python 원본 코드와의 차이점

| 기능                | Python PIL          | Kotlin AWT         | 비고                           |
|---------------------|---------------------|--------------------|--------------------------------|
| 이미지 생성         | ✅                  | ✅                 | 동일                           |
| 한글 텍스트         | ✅                  | ✅                 | 동일                           |
| 하이라이트          | ✅                  | ✅                 | 동일                           |
| 이모지              | ✅                  | ⚠️                 | 제한적 지원                    |
| 배경 이미지         | ✅                  | ✅                 | 동일                           |
| 커버 이미지         | ✅                  | ❌                 | 미구현 (추가 가능)             |
| URL 이미지 다운로드 | ✅                  | ❌                 | 미구현 (추가 가능)             |

## 🚀 향후 개선 사항

- [ ] 커버 이미지 지원 (Group Single 타입 - 1080 x 1350)
- [ ] URL에서 이미지 다운로드 기능
- [ ] 이모지 렌더링 개선
- [ ] 커스텀 폰트 로딩 지원
- [ ] 더 많은 레이아웃 옵션

## 📄 라이선스

few-be 프로젝트 라이센스를 따릅니다.
