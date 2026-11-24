# 플래닝펫 (Planning Pet)

일정을 끝내면 경험치와 포인트를 받아 반려동물을 키우는 일정 관리 데스크톱 앱입니다.
2026-2 소프트웨어시스템설계 11조 팀 프로젝트의 본 저장소입니다.

- 언어와 화면: Java 21, Swing (도메인별 model, view, controller, D-46)
- 상태: 설계 문서와, 그 설계대로 움직이는 Swing 프로토타입이 있습니다. 본 구현은 아직 시작하지 않았습니다.
- [planning-pet](https://github.com/2026-2-software-system-design-11/planning-pet)은 아이디어 확인용으로 한 번에 만든 첫 저장소입니다. 이 저장소의 코드와 문서는 여기서 새로 씁니다.

## 둘러보기

| 폴더 | 내용 |
|---|---|
| [docs/](docs/README.md) | 요구사항, 유스케이스, 화면 설계, 함수 명세와 검사 표, 결정 기록, 페르소나, 유저플로우, 디자인 시스템, QA 결과 |
| [prototype/](prototype/README.md) | 05 화면 설계대로 움직이는 Swing 프로토타입과 그 검사. 홈, 일정관리, 나의 방(상점 칸, 캐릭터 창), 통계, 설정 |

## 실행과 검사

JDK 21이 필요합니다. 자세한 옵션은 [prototype/README.md](prototype/README.md)에 있습니다.

```sh
prototype/run.sh     # 프로토타입 창을 엽니다
prototype/check.sh   # 06 검사 표의 단위 검사와 패키지 규칙(D-46) 검사
prototype/qa.sh      # 유저플로우를 창 없이 돌려 docs/qa/에 보고서를 씁니다
```

## 정할 것과 할 일

- 명세가 아직 정하지 않은 것은 각 문서 끝 "미정" 절에 `Q-번호`로 적고, `question` 라벨 이슈로 올립니다. 정하면 [결정 기록](docs/decisions.md)에 `D-번호`로 남기고 명세, 코드, QA 순서로 반영합니다.
- 정할 것과 고칠 것은 [열린 이슈](https://github.com/2026-2-software-system-design-11/planning-pet-app/issues)에 있습니다.

## 협업 규칙

이슈, 브랜치, 커밋, PR 규칙은 조직 공통 [CONTRIBUTING.md](https://github.com/2026-2-software-system-design-11/.github/blob/main/CONTRIBUTING.md)를 따릅니다.
작업 브랜치는 `develop`에서 따고 `develop`으로 PR을 엽니다. `main`은 `develop`에서 오는 릴리스 PR로만 바뀝니다.
커밋은 줄 수가 아니라 뜻으로 나누고, 커밋마다 빌드와 `prototype/check.sh`가 통과해야 합니다(#207).
