# 설계 문서

교수님이 체크포인트마다 요구한 문서를 순서대로 쓴다. 팀 과제 양식은 CP1만 받았으므로, 다음 문서 목록은 개인과제 CP3 양식(`ssd_ip_checkpoint3_report_format.doc`)과 CP4 제출물 구성을 따른다.

## 폴더

```
docs/
├── README.md        이 파일. 목차와 폴더 규칙
├── decisions.md     결정 기록(D-번호). 모든 폴더의 문서가 가리킨다
├── spec/            제출 양식의 문서. 파일 이름 앞 번호가 양식 순서다
│   └── diagrams/    spec 문서의 그림 원본(.puml)과 그림(.png)
│       ├── usecase/ 03의 유스케이스 다이어그램
│       └── ui/      05의 화면 그림. 공통 부품은 ui-common.iuml
├── ux/              사용자와 화면 경험. 페르소나, 유저플로우, 디자인 시스템
├── research/        결정의 근거가 된 조사와 측정
└── qa/              프로토타입 QA 결과와 수동 확인 목록
    └── blind/       블라인드 예측 검사. 설명은 README
        ├── round1/  1차 실제 동작, 판정, 잠근 예측과 결과
        └── round2/  2차 실제 동작, 판정, 잠근 예측과 결과, 05 대조, 10원칙 평가
```

- 양식에 있는 절은 `spec/`에 `번호-이름.md`로 둔다. 04 시퀀스 다이어그램, 07 클래스 다이어그램도 여기에 둔다.
- 양식에 없는 문서는 성격에 따라 `ux/`, `research/`, `qa/` 중 한 곳에 둔다. `docs/` 바로 아래에는 README와 결정 기록만 둔다.
- 그림은 그 그림을 쓰는 문서와 같은 폴더의 `diagrams/` 아래 종류별 폴더에 원본과 함께 둔다.
- 블라인드 검사를 다시 돌리면 `qa/blind/`에 `round3/`을 만든다.
- `qa/prototype-flows.md`와 `qa/rules.md`는 `prototype/qa.sh`가 만든다.

## 목차

| 순서 | 문서 | 양식의 절 |
|---|---|---|
| 01 | [개요와 요구사항](spec/01-requirements.md) | CHARACTERISTICS, REQUIREMENT LISTS |
| 02 | [유스케이스](spec/02-usecases.md) | USECASES |
| 03 | [유스케이스 다이어그램](spec/03-usecase-diagram.md) | USECASE DIAGRAM |
| 04 | 시퀀스 다이어그램 | SEQUENCE DIAGRAM |
| 05 | [화면 설계](spec/05-ui-design.md) | USER INTERFACE DESIGNS |
| 06 | [함수 명세와 검사 표](spec/06-functions.md) | FUNCTION SPECIFICATION |
| 07 | 클래스 다이어그램 | 개인과제 CP4 |

| 폴더 | 문서 | 내용 |
|---|---|---|
| - | [결정 기록](decisions.md) | 무엇을 왜 정했는지 |
| ux | [페르소나](ux/personas.md) | 사용자 열 명과 고정 데이터(D-37) |
| ux | [유저플로우](ux/user-flows.md) | 페르소나별 흐름 32개, 06 커버리지 |
| ux | [디자인 시스템](ux/design-system.md) | 색, 글꼴, 모양, 그림(D-30) |
| research | [디자인 방향 조사](research/design.md) | 색, 글꼴, 펫 그림, 움직임, 사용자 여정(D-14) |
| research | [페르소나 선정 근거](research/persona-selection.md) | 통계, 채널, 점수 |
| research | [저장 방식 측정](research/storage.md) | D-08의 근거 |
| qa | [QA 결과](qa/README.md) | 프로토타입 유저플로우 QA, 수동 확인 목록 |
| qa | [블라인드 예측 검사](qa/blind/README.md) | 명세를 모르는 사람의 예상과 실제 동작 비교 |

## 쓰는 규칙

- 설명과 화면 글자는 한국어, 식별자(클래스, 필드)는 영어로 쓴다.
- 요구사항, 유스케이스, 규칙에는 번호를 붙이고, 다른 문서는 그 번호로 가리킨다. 같은 내용을 두 곳에 쓰지 않는다.
- 정해지지 않은 것은 감추지 않고 각 문서 끝 "미정" 절에 `Q-번호`로 적는다.
- 표기는 수업 슬라이드(`0. Diagrams`, `1. Requirements and Changes`, `0. Swing`)를 따른다.
