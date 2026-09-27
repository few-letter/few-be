# Hooks — 절대 어기면 안되는 규칙

이 디렉토리의 스크립트는 `.claude/settings.json`의 `PreToolUse` 훅으로 등록되어 있으며, 아래 규칙을 위반하는 Bash 명령을 **자동으로 차단**합니다. `.claude/rules/`가 문서로만 존재하는 가이드라인인 것과 달리, 이 디렉토리의 규칙은 실제로 강제됩니다.

## 1. 이슈 우선 생성 + `dev` 기반 브랜치 (`validate-branch-name.sh`)

- 개발 시작 전 GitHub 이슈를 먼저 생성해야 합니다.
- 새 브랜치는 반드시 `dev` 브랜치로부터 생성해야 합니다.
- 브랜치명 형식: `{fix|feat|refactor|config}/#{이슈번호}` (예: `fix/#1234`, `feat/#1234`)

`git checkout -b`, `git switch -c`, `git branch <name>` 명령을 가로채 위 세 가지를 검사합니다. GitHub 이슈 존재 여부는 `gh` CLI가 설치되어 있고 인증된 경우에만 검사합니다(fail-open) — 네트워크/인증 문제로 개발 자체가 막히지 않도록 하기 위함입니다.

## 2. 커밋 메시지 접두사 (`validate-commit-message.sh`)

- 커밋 메시지는 `feat:`, `fix:`, `refactor:`, `config:`, `test:` 중 하나로 시작해야 합니다. (`.claude/settings.json`의 `commitMessageFormat`과 동일한 컨벤션이며, `[Feat/#이슈번호]`와 같은 대괄호 형식은 허용하지 않습니다.)

`-m`/`--message` 플래그로 전달된 커밋 메시지만 검사합니다. 에디터로 작성하는 커밋(예: `git commit` 단독 실행)은 검사 대상이 아닙니다.

## 알려진 한계

- Bash 도구를 통해 실행되는 git 명령만 검사합니다. IDE, GUI 클라이언트, MCP 도구(`create_or_update_file` 등)를 통한 변경은 검사 대상이 아닙니다.
- 셸 명령 문자열을 정규식으로 파싱하므로, 매우 특이한 명령 형태(멀티라인 heredoc 등)는 정확히 검사되지 않을 수 있습니다.
- GitHub 이슈 존재 여부 검사는 `gh` CLI 상태에 따라 생략될 수 있습니다(위 참고).
