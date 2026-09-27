# 개발 규칙

## 아키텍처

모든 기능 개발은 다음 계층 구조를 지켜야 합니다.

```
Controller → UseCase → Service (선택) → Repository
```

- **Controller**: 요청 바디가 있으면 `controller/request/`에 DTO를 만듭니다. 컨트롤러는 `usecase.execute()`만 호출하고, usecase의 Out 클래스를 그대로 반환합니다. 응답 DTO는 `controller/response/`에 둡니다.
- **UseCase**: 클래스명은 동사로 시작합니다. 반드시 `execute()` 메서드를 가집니다. 입력 DTO는 `usecase/input/`, 출력 DTO는 `usecase/out/`에 둡니다.
- **Service**: 다른 도메인/모듈의 JPA 엔티티에 접근할 때만 사용합니다.
- **Repository**: `save`와 같은 기본 JPA 메서드도 반드시 repository 인터페이스에 선언합니다.

## 완료 후 처리

개발이 완료되면 `dev` 브랜치로 Pull Request를 생성합니다.

브랜치 생성/커밋 메시지 형식 등 반드시 지켜야 하는 워크플로우 규칙은 [`.claude/hooks/README.md`](../hooks/README.md)를 참고하세요.
