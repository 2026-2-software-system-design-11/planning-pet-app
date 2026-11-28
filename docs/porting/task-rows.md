# 단위 S3. 할 일 목록 줄 TaskRows

[본 구현으로 옮기기](../porting.md) 1절 표의 단위 S3(홈 목록) 가운데 목록 줄 `TaskRows`다. 07의 `TaskRows`([class-schedule-rules](../diagrams/class-schedule-rules.puml))를 `planningpet.schedule.view`에 두고, 홈 할 일 목록이 쓰는 몫(`showPlan`)만 옮긴다. 1절 표는 `TaskRows`를 S5에서 S3로 당겼다(#441). 홈 목록의 다른 S3 몫은 [home.md](home.md)에 있다. 기준은 05 홈 "할 일 목록"과 입력 항목 4, 7, 8, 06 6번, 8번, 9번, 07 [seq-uc07](../diagrams/seq-uc07.puml), [seq-uc08](../diagrams/seq-uc08.puml), 프로토타입 `schedule/view/TaskRows`(650줄)와 그것이 쓰는 `RoundCheck`, `TimePill`이다.

이 단위가 옮기지 않는 것:

- 일정관리 오른쪽 주 목록(`show`, 줄 앞 날짜, 작은 시계 단추, 둘째 줄 `SecondLine`)은 S5다.
- 줄을 시간표로 끌어 놓기(D-99)는 C2, 설정 "목록 누르기"(D-102)는 C3이다. C3 전에는 그 설정이 꺼진 것으로 본다.
- 수정, 삭제, 시각 단추가 여는 것(일정 입력 창 S4, 삭제 확인 창, 시각 고치기 창 S5)은 그 단위가 한다. 줄은 누름을 받는 함수로 넘긴다.

프로토타입 `TaskRows`의 홈 몫은 그것이 쓰는 부품과 테스트를 더하면 PR 한도의 세 배가 넘어 일곱으로 나눈다.

| 몫 | 이슈 | 옮기는 것 |
|---|---|---|
| S3r | #475 | 이 기록 파일, `Icons`의 `TRASH`, `CLOCK`, 둥근 체크 `RoundCheck` |
| S3s | #476 | 시각 알약 `TimePill`의 홈 알약(시각 글, "시각 정하기", 끝냄과 잠금의 흐림, 툴팁, 같은 폭) |
| S3t | #477 | 제목 칸 `TitleLabel`(`Ui.fit`으로 줄이기, 흐린 "메모", 끝낸 일 취소선), 둘째 줄 표시(마감 글, 하려던 일, "오늘 마감", "마감"), "고정" 꼬리표 |
| S3u | #478 | 줄 하나: 바탕과 아래 선, 둥근 체크로 `RewardController.complete`, `uncomplete` 부르기, 걸리는 시간, 연필과 휴지통, 시각 알약의 잠금, 메모 툴팁 |
| S3v | #479 | `showPlan`: 맨 위 `dueToday`, `timed`, "시각 없는 할 일", "기간 안에 할 일" 묶음, 빈 날 안내, `Scrollable` |
| S3w | #480 | 맨 아래 "끝낸 일 {수}개" 묶음: 체크한 자리에 1.5초 머물기, 접기(D-88, D-94) |
| S3x | #481 | 줄의 오른쪽 버튼 메뉴("수정", "삭제")와 두 번 누르기 수정 |

07 안에서 정한 것과 그 까닭:

- **줄이 쓰는 부품은 프로토타입 이름 그대로 `schedule.view`의 패키지 안 클래스로 둔다.** 07은 `TaskRows`만 그렸고(07 "view 클래스는 이름만 그렸다"), 둥근 체크 `RoundCheck`와 시각 알약 `TimePill`은 목록 줄과 S5의 시간표 블록만 쓴다. 홈 달력의 `DayCell`과 같은 꼴이다([home.md](home.md) S3e).
- **아이콘은 프로토타입 `Kind`의 차례대로 더한다.** 프로토타입은 `TRASH`, `CLOCK`이 `PENCIL` 뒤 여러 종 다음에 있고 본 구현은 그 사이 종을 아직 옮기지 않아, 지금 끝인 `PENCIL` 뒤에 `TRASH`, `CLOCK` 순으로 둔다([icons.md](icons.md), [home.md](home.md) S3c와 같은 꼴).
- **수정, 삭제, 시각 단추는 누름을 받는 함수로 받는다.** 프로토타입은 `TaskController`의 `edit`, `delete`, `editTime`을 부르지만 그 controller와 그것이 여는 창은 S4, S5다. `PetCard`가 `Runnable`을, `CalendarPanel`이 `Consumer<LocalDate>`를 받는 것과 같은 꼴이다.
- **제목 줄이기는 `Ui.fit`을 쓴다.** 프로토타입 `TaskRows`에는 `PetCardArt.fit`과 같은 몸의 `fit`이 따로 있다. S3d(#450)가 그것을 `Ui.fit`으로 옮기며 코드 포인트로 자르게 고쳤으므로(porting.md 4절 S3d 줄) 따로 옮기지 않는다.

## S3r. 둥근 체크와 휴지통, 시계 아이콘

목록 줄의 둥근 체크(05 입력 항목 4, D-59)인 `RoundCheck`를 `planningpet.schedule.view`에, 줄 끝 삭제 버튼의 휴지통 `TRASH`와 시각 알약의 시계 `CLOCK`을 `common.view`의 `Icons`에 둔다. 06에 이 부품의 검사 줄은 없고, 기준은 [디자인 시스템](../design-system.md) 9절, 10절 홈과 프로토타입 `schedule/view/RoundCheck`, `common/view/Icons`다.

- **`RoundCheck`는 기본 `JCheckBox`에 그림만 거는 도우미다.** 프로토타입처럼 `style(JCheckBox)`가 아이콘, 투명, 초점 그림 끔, 마우스 올림 켬, 여백 2, 손 커서를 걸고 같은 체크박스를 돌려준다. 고르기와 동작 알림은 `JCheckBox` 그대로다.

### 1. 화면에서 따라 할 단계

이 단위는 부품만 옮겨서, 창에 보이는 것은 목록 줄(S3u)부터다. 그때 보이는 곳은 아래다.

1. 홈 할 일 목록의 줄마다 왼쪽에 둥근 네모 체크가 있다. 끝내지 않은 일은 흰 바탕에 옅은 파랑(`CARD_LINE`) 테두리다(디자인 시스템 10절 홈).
2. 체크에 마우스를 올리면 손 모양 커서가 되고 연민트(`MINT_PALE`) 바탕에 민트 테두리로 바뀐다.
3. 체크를 누르면 민트 채움(위 `MINT_TOP`에서 아래 `MINT_DEEP`)에 흰 체크 표시가 된다. 다시 누르면 처음 모양이다.
4. Tab으로 체크에 초점이 가면 체크 바깥에 민트 고리가 생긴다.
5. 줄 끝 휴지통 버튼(S3u)에 휴지통 선 아이콘, 시각 알약(S3s) 앞에 시계 선 아이콘이 있다(디자인 시스템 9절).

### 2. 입력의 경계와 프로토타입 결과

프로토타입 결과는 `prototype/src`를 저장소 밖에 컴파일한 클래스(develop `2c636a7`, 프로토타입의 마지막 변경 `2ac06b6`, JDK 21.0.11, macOS)를 `java.awt.headless=true`로 부르는 버리는 Java 파일에서 얻었다. 아이콘은 [icons.md](icons.md)와 같은 줄 그림이다. 체크는 `style(new JCheckBox())`를 정한 칸 크기로 두고 둘레 4가 더 큰 그림에 그렸다([theme.md](theme.md)의 `Snap`과 같다). 마우스 올림과 누름은 버튼 모델의 rollover, armed, pressed로, 키보드 초점은 창 없이 초점 주인을 정하는 초점 관리자로 넣었다. `Theme.install` 앞과 뒤의 그림이 같다. 그린 그림에 글이 없어 기준 그림을 그대로 `src/test/resources`에 둔다. "본 구현" 칸의 "픽셀 같음"은 본 구현이 같은 크기로 그린 그림과 기준 그림의 픽셀이 모두 같다는 뜻이고, 결과는 `IconsTest`와 `RoundCheckTest`가 번호 이름의 테스트로 확인한다.

| 번호 | 입력과 경계 | 넣은 값 | 프로토타입 결과 | 본 구현 |
|---|---|---|---|---|
| E-ICON-1~3 | 휴지통과 시계 | `TRASH`, `CLOCK`을 [icons.md](icons.md) E-ICON-1~3의 크기와 색으로 | 휴지통은 굵기 2의 뚜껑 선, 손잡이, 아래가 좁은 통, 안의 세로줄 둘이다. 시계는 지름 17의 원과 12시에서 가운데를 지나 4시쪽으로 꺾인 바늘이다. 0은 아무것도 그리지 않는다 | 같음. 픽셀 같음 |
| E-RCHK-1 | 끝내지 않음과 끝냄 | 24x24, 고르지 않음과 고름 | 여백 2 안에 20 칸의 둥근 네모다. 고르지 않으면 `CARD_TOP` 바탕과 `CARD_LINE` 테두리(굵기 1.5), 고르면 `MINT_TOP`에서 `MINT_DEEP`으로 가는 채움에 흰 체크 표시(굵기 2.2)다 | 같음. 픽셀 같음 |
| E-RCHK-2 | 마우스 올림 | 고르지 않음과 고름에 rollover | 고르지 않으면 `MINT_PALE` 바탕과 `MINT` 테두리다. 고르면 올리지 않은 고름과 픽셀이 같다 | 같음. 픽셀 같음 |
| E-RCHK-3 | 누름과 흐림 | 고르지 않음에 armed와 pressed, 고르지 않음과 고름을 흐리게 | 모두 누르지 않은 흐리지 않은 그림과 픽셀이 같다 | 같음. 픽셀 같음 |
| E-RCHK-4 | 키보드 초점 | 고르지 않음, 고름, 마우스 올린 고르지 않음에 초점 | 체크 바깥 2에 민트(#1FB99C, 알파 110) 고리(굵기 1.5)가 더해진다 | 같음. 픽셀 같음 |
| E-RCHK-5 | 알맞은 크기와 다른 칸 | 고름을 40x30, 10x10, 0x0에 | 40x30은 왼쪽 여백 2에서 세로 가운데에 20 칸, 10x10은 잘린 일부, 0x0은 아무것도 그리지 않는다. 예외 없음 | 같음. 픽셀 같음 |
| E-RCHK-6 | 체크박스 속성 | `style(new JCheckBox())` | 같은 체크박스를 돌려준다. 알맞은 크기 24x24, 여백 2, 아이콘 20x20, 불투명 아님, 초점 그림 꺼짐, 마우스 올림 켜짐, 손 모양 커서, 글 "" | 같음 |
| E-RCHK-7 | 누르기 | `doClick` 두 번 | 처음 고름, 다음 고르지 않음이 되고 동작 알림이 한 번씩 간다 | 같음 |
