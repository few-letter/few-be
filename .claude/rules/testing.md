# 테스트 규칙

- 이 프로젝트에서는 **단위 테스트(Unit Test)만** 작성하고 실행합니다. 통합 테스트(`"integration"` 태그가 붙은 JUnit 5 테스트 등)는 작성하지 않습니다.
- 테스트 스택: **Kotest 5.8.0** (Spring extensions), **MockK 1.13.9**.
- 기존 테스트 코드 위치를 참고하여 동일한 스타일/컨벤션을 따르세요.
    - `domain/generator/src/test/kotlin/com/few/generator/...`
    - `library/email/src/test/kotlin/com/few/email/...`
- 코드를 수정한 경우 대응하는 테스트 코드도 함께 갱신합니다. 대응하는 테스트가 없다면 새로 작성하지 않고 넘어갑니다.
