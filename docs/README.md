# 설계 문서

교수님이 체크포인트마다 요구한 문서를 순서대로 쓴다. 팀 과제 양식은 CP1만 받았으므로, 다음 문서 목록은 개인과제 CP3 양식(`ssd_ip_checkpoint3_report_format.doc`)과 CP4 제출물 구성을 따른다.

| 순서 | 문서 | 양식의 절 |
|---|---|---|
| 01 | [개요와 요구사항](01-requirements.md) | CHARACTERISTICS, REQUIREMENT LISTS |
| 02 | [유스케이스](02-usecases.md) | USECASES |
| 03 | [유스케이스 다이어그램](03-usecase-diagram.md) | USECASE DIAGRAM |
| 04 | 시퀀스 다이어그램 | SEQUENCE DIAGRAM |
| 05 | [화면 설계](05-ui-design.md) | USER INTERFACE DESIGNS |
| 06 | [함수 명세와 검사 표](06-functions.md) | FUNCTION SPECIFICATION |
| 07 | 클래스 다이어그램 | 개인과제 CP4 |
| - | [페르소나](personas.md) | 양식에 없음. 사용자 열 명과 고정 데이터(D-37) |
| - | [유저플로우](user-flows.md) | 양식에 없음. 페르소나별 흐름 32개, 06 커버리지 |
| - | [페르소나 선정 근거](research/personas.md) | 양식에 없음. 통계, 채널, 점수 |
| - | [디자인 시스템](design-system.md) | 양식에 없음. 색, 글꼴, 모양, 그림(D-30) |
| - | [결정 기록](decisions.md) | 양식에 없음. 무엇을 왜 정했는지 |
| - | [저장 방식 측정](research/storage.md) | 양식에 없음. D-08의 근거 |

## 쓰는 규칙

- 설명과 화면 글자는 한국어, 식별자(클래스, 필드)는 영어로 쓴다.
- 요구사항, 유스케이스, 규칙에는 번호를 붙이고, 다른 문서는 그 번호로 가리킨다. 같은 내용을 두 곳에 쓰지 않는다.
- 정해지지 않은 것은 감추지 않고 각 문서 끝 "미정" 절에 `Q-번호`로 적는다.
- 표기는 수업 슬라이드(`0. Diagrams`, `1. Requirements and Changes`, `0. Swing`)를 따른다.
