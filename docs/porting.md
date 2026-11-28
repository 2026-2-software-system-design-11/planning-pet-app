# 본 구현으로 옮기기

프로토타입(`prototype/`)의 동작을 본 구현(`src/`)으로 옮기는 방법과 순서다. 다른 언어로 다시 쓰는 일과 비슷해서, 정한 방법 없이 옮기면 동작이 말없이 달라진다. 본 구현의 클래스는 07 클래스 다이어그램을, 함수와 검사 줄은 [06 함수 명세](06-functions.md)를 따른다.

## 프로토타입과 본 구현

- 프로토타입은 본 구현이 옮길 동작의 비교 기준이다. 단위마다 경계를 프로토타입에 넣어 얻은 결과를 새 코드가 같게 내는지 본다(2절).
- 비교 기준을 얻는 동안 프로토타입 코드를 바꾸지 않는다. 바꾸면 기준이 바뀐다. model의 경계는 `prototype/build.sh`가 만든 `prototype/out`을 클래스 경로에 두고 그 클래스를 부르는 버리는 Java 파일로 넣어 본다(`Store.now`로 시각을 정한다). 화면의 경계는 `prototype/qa/`의 창 없이 누르는 코드와 `prototype/snap.sh`로 넣어 본다.
- 프로토타입에 없는 기능(저장 21번, 22번, 알림 17번, 앱 하나만 실행 24번, 트레이와 컴퓨터 켤 때 실행 25번, [prototype/README.md](../prototype/README.md) "빠진 것")은 비교 기준이 없다. 그 단위는 06 검사 줄만 기준이다.

## 1. 옮기는 단위와 순서

- 커밋 하나는 변경 100줄 이하이고, 그 커밋만으로 빌드되고 검사(`gradlew test`, D-108)를 통과한다. 단위 하나는 PR 하나(500줄 이하)이고 커밋 여럿으로 나눈다.
- 단위를 이슈로 열기 전에 바뀔 줄 수를 짐작한다(프로토타입의 그 클래스 줄 수와 06 검사 줄 수로). 350줄 안팎을 넘으면 단위를 둘로 나눠 연다. 쓰다가 넘칠 것 같으면 한도를 넘기는 커밋 전에 멈추고 남은 몫을 다음 단위로 옮긴다. 프로토타입의 `TaskBook`은 451줄이라 M4~M6으로 나눠 두었다.
- 이슈 하나에 PR 하나이고, PR 본문에 `Closes #이슈번호`를 꼭 적는다.
- 순서는 빌드와 빈 앱, model을 의존 순서대로(다른 클래스가 기대는 것부터), 화면, 편의 기능이다. 단위 하나가 끝날 때마다 앱이 빌드되고 그때까지 옮긴 규칙이 검사로 돈다.
- 수업 슬라이드 "1. A Simple Application"은 고객이 원하는 동작을 먼저 되게 하고, OO 원칙을 적용하고, 다시 쓰기 좋은 설계로 가는 세 단계를 가르친다. "1. Requirements and Changes"는 반복마다 감당할 만한 일을 맡아 작동하는 시스템을 낸다. 고객이 원하는 동작은 프로토타입과 06에 있고 설계는 07에 있으므로, 단위마다 그 동작을 07의 클래스로 옮기고 작동하는 앱으로 낸다.
- "기록" 칸은 그 단위를 옮기는 PR이 더하는 단위 기록 파일이다(2절).

| 순서 | 단위 | 07 클래스 | 06 | 기록 |
|---|---|---|---|---|
| 0 | 빌드와 빈 앱 | `Main`, `AppWindow` | 없음(D-108, D-109의 층 의존 검사) | [docs/porting/build.md](porting/build.md) |
| M1 | 날짜 규칙 | `AppDay`, `Rules`, `Words` | 1번, 데이터 절 "한 주", 16번의 글자 규칙 | [docs/porting/day-rules.md](porting/day-rules.md) |
| M2a | 일정 값 | `Task`, 값 목록 `Kind`, `Category`, `PetKind` | 데이터 절 `Task`와 그 절의 규칙(보이는 날 빼고) | [docs/porting/task.md](porting/task.md) |
| M2b | 일정 입력 검사 | `TaskDraft`, `FieldError` | 2번(반복 칸 빼고) | [docs/porting/task-check.md](porting/task-check.md) |
| M3 | 반복 | `Repeat`, `TaskDraft.validate`의 반복 칸 | 데이터 절 `repeat`, 2번 T-VAL-11~13, 15, 16, 18 | [docs/porting/repeat.md](porting/repeat.md) |
| M4 | 일정 넣기와 겹침 | `TaskBook.add`, `overlap` | 3번 | [docs/porting/task-add.md](porting/task-add.md) |
| M5 | 그날 목록과 주의 목록 | `TaskBook.dayPlan`, `untimedOfWeek`, `timeText`, `dueText`, `periodNote`, `Task.shownOn`, 겹침 물음 글(`Overlap`) | 6번, 7번, 데이터 절 "보이는 날", 3번의 겹침 물음 글 | [docs/porting/task-lists.md](porting/task-lists.md) |
| M6 | 일정 고치기와 지우기 | `TaskBook.update`, `clearTime`, `delete` | 4번(끌어 놓기 빼고), 5번 | [docs/porting/task-edit.md](porting/task-edit.md) |
| M7 | 앱 데이터 | `AppData`(`copy`, `restore` 빼고), `PetState`, `Pose`, `Slot`, `WearId`, `ItemId`, `SampleLook` | 데이터 절 `AppData`, `PetState` | [docs/porting/app-data.md](porting/app-data.md) |
| M8 | 저장 | `DataStore`, `SqliteDataStore`, `Schema`, `AppController.change`, `AppData.copy`, `restore` | 21번, 22번(프로토타입 기준 없음) | [docs/porting/store.md](porting/store.md) |
| M9 | 보상 | `Rewards`, `Level`, `DailyProgress`, `Visits`, `Task.finish`, `unfinish` | 8~13번 | [docs/porting/rewards.md](porting/rewards.md) |
| M10 | 펫 말 | `PetTalk`, `Bubble` | 16번 | [docs/porting/pet-talk.md](porting/pet-talk.md) |
| M11 | 화면이 쓰는 일정 계산 | `DefaultDates`, `CalendarCell`, `TimetableLayout` | 14번, 19번, 26번 | [docs/porting/screen-calc.md](porting/screen-calc.md) |
| M12 | 자동 배치 | `AutoPlacer` | 18번 | [docs/porting/auto-place.md](porting/auto-place.md) |
| M13 | 알림 고르기 | `Reminders` | 17번(프로토타입 기준 없음) | [docs/porting/reminders.md](porting/reminders.md) |
| M14 | 펫 | `Bond`, `Wardrobe`, `Poses`, `PetState.pat` | 27번, 28번이 쓰는 장신구 표 | [docs/porting/pet.md](porting/pet.md) |
| M15 | 상점과 방 | `Shop`, `Item`, `Room` | 20번 | [docs/porting/shop.md](porting/shop.md) |
| M16 | 통계 | `Stats` | 29번 | [docs/porting/stats.md](porting/stats.md) |
| S1 | 창이 보일 때와 날 바뀜 | `AppController.onWindowShown`, `say`, `DayWatcher`, `Tray`의 30초 타이머, `RewardController.attend` | 11번, 15번, 23번 | [docs/porting/app-window.md](porting/app-window.md) |
| S1b | 디자인 시스템 바탕 | `common.view`의 `Theme`, 글꼴 파일, `Card`, `RoundButtonUI` | 없음([디자인 시스템](design-system.md)) | [docs/porting/theme.md](porting/theme.md) |
| S1c | 아이콘과 수치 카드 | `common.view`의 `Icons`(창 틀이 쓰는 것), `StatCard`, 창 바탕 그림 | 없음([디자인 시스템](design-system.md)) | [docs/porting/icons.md](porting/icons.md) |
| S1d | 창 틀 | `AppWindow`(05 창 구조의 배치, 화면 머리 오른쪽 카드, 화면 바꾸기, 알림 받아 다시 그리기) | 11번의 출석 단추, 15번의 연속 접속 | [docs/porting/frame.md](porting/frame.md) |
| S2a | 펫 얼굴과 로고 | `PetArt`의 `drawFace`(장신구 없는 고양이), `drawLogo`와 그 얼굴이 쓰는 그리는 부품, `AppWindow` 로고 자리 | 없음([디자인 시스템](design-system.md) 10절) | [docs/porting/pet-art.md](porting/pet-art.md) |
| S2b | 강아지 얼굴 | `PetArt.drawFace`의 펫 종류 인자와 강아지 얼굴(#408) | 없음([디자인 시스템](design-system.md) 10절) | [docs/porting/pet-art.md](porting/pet-art.md) |
| S2c | 앉은 펫 | `PetArt.drawAt`(장신구 없는 앉기 자세), `PetPaintBody`의 몸, 앞발, 꼬리, 움직임 시각과 고를 때 튀기(#411) | 없음([디자인 시스템](design-system.md) 10절) | [docs/porting/pet-art.md](porting/pet-art.md) |
| S2d | 펫 목걸이 | `PetArt.drawAt`, `drawFace`의 장신구 인자(`WearId`), `PetPaintWear`의 목걸이 넷(#409) | 없음(D-51, [디자인 시스템](design-system.md) 10절) | [docs/porting/pet-art.md](porting/pet-art.md) |
| S2e | 고르기 창 그림 도구 | `PickScene`(하늘 언덕 바탕, 리본, 덤불, 데이지, 별, #416) | 없음([디자인 시스템](design-system.md) 10절) | [docs/porting/pick.md](porting/pick.md) |
| S2f | 입력 도우미와 아이콘 셋 | `common.view`의 `Ui`(`hint`, `limit`), `Icons`의 `PENCIL`, `CHECK`, `SPARK`(#417) | 06 읽는 법의 글자 수(이름 10자) | [docs/porting/pick.md](porting/pick.md) |
| S2g | 둥근 입력칸 | `common.view`의 `RoundField`(알약 칸, 왼쪽 아이콘, 흐린 예시 글, 칸 안 글자 수 `count`, `countText`, #421) | 06 읽는 법의 글자 수(이름 10자) | [docs/porting/pick.md](porting/pick.md) |
| S2h | 동물 카드 | `PickCard`(움직이지 않는 카드 #418, 마우스 올림과 초점 모양, 움직임 #424), 강조선 자리 `PetArt.heightRatio`(앉기) | 없음([디자인 시스템](design-system.md) 10절, 05 입력 항목 1) | [docs/porting/pick.md](porting/pick.md) |
| S2i | 고르기 창 위쪽 줄 | `PickDialog`의 창 내용(바탕 패널, 로고, 부제, 동물 카드 줄, #419) | 없음(05 반려동물 고르기 창, [디자인 시스템](design-system.md) 10절) | [docs/porting/pick.md](porting/pick.md) |
| S2j | 고르기 창 리본과 아래쪽 줄 | `PickDialog`의 리본 제목, 이름 줄 카드(`nameBar`, 10자로 막은 이름 칸 `name`), 안내, 버튼 줄(#427) | 없음(05 반려동물 고르기 창과 입력 항목 2, 3, [디자인 시스템](design-system.md) 10절) | [docs/porting/pick.md](porting/pick.md) |
| S2k | 펫 만들기와 고르기 controller | `PetController.choose`(바로 시작 갈래, `examples` 인자는 C5), `adopt`, `select`(#430) | 30번(`step`, `rename`, `pose`는 S8), 15번 T-BRF-6, 16번 `ADOPTED`, `SELECTED` | [docs/porting/pet-controller.md](porting/pet-controller.md) |
| S2l | 데이터 파일을 못 읽음 창 | `UnreadableDialog`의 창 내용(#431) | 21번의 `UNREADABLE`(프로토타입 기준 없음) | [docs/porting/unreadable.md](porting/unreadable.md) |
| S2m | 고르기 창 띄우기와 저장 실패 창 | `PickDialog`의 창 만들기(`dialog`, 제목, Enter, 닫기 알림)와 바로 시작(`onStart`), 저장 실패 창 `AppWindow.saveFailed`(#435) | 22번의 저장 실패 창, 30번의 바로 시작 | [docs/porting/pick.md](porting/pick.md) |
| S2 | 반려동물 고르기 창 | `PickDialog`, `PetController.adopt`, `select`(S2k가 옮긴다), `Main` 잇기(읽기, 못 읽음 창 띄우기와 끝내기(창 내용은 S2l), 첫 실행 갈래(고르기 창과 저장 실패 창은 S2m), 창과 타이머 잇기) | 30번, 21번의 시작할 때 읽기 | [docs/porting/pick.md](porting/pick.md), [docs/porting/main.md](porting/main.md) |
| S2n | 펫 머리 장신구 | `PetPaintWear.headWear`(꽃 핀, 리본 핀, 안경, 밀짚모자, 베레모, 왕관)와 그것을 그리는 `PetArt.drawFace`, `drawAt`(#414). 옷은 #438 | 없음(D-51, [디자인 시스템](design-system.md) 10절) | [docs/porting/pet-art.md](porting/pet-art.md) |
| S2o | 펫 옷 | `PetPaintWear`의 스카프와 망토 고리(`neckWear`), 조끼 `vest`, 망토 뒤판 `capeBehind`와 그것을 그리는 `PetArt.drawAt`(#438) | 없음(D-51, [디자인 시스템](design-system.md) 10절) | [docs/porting/pet-art.md](porting/pet-art.md) |
| S3a | 할 일 끝내기와 체크 풀기 controller | `RewardController.complete`, `uncomplete`(#441) | 8번, 9번, 16번 `COMPLETED`, `UNCOMPLETED`, 22번 T-SAVE-1, T-SAVE-5 | [docs/porting/home.md](porting/home.md) |
| S3 | 홈 목록 | `MainScreen`, `PetCard`, `CalendarPanel`, `HomeController`(#442), `TaskRows`(홈 목록이 쓰므로 S5에서 당겨 온다). `RewardController.complete`, `uncomplete`는 S3a | 6번, 13번, 16번, 26번 | [docs/porting/home.md](porting/home.md) |
| S4 | 일정 입력 창 | `TaskDialog`, `TaskController.add`, `update`, `delete`, 고르는 칸 `ComboUI`와 그것을 거는 `Theme.install`의 줄([theme.md](porting/theme.md)) | 2~5번, 14번 | `docs/porting/task-dialog.md` |
| S5 | 일정관리 | `ScheduleScreen`, `TimetablePanel`, `TimeEditor`, `TaskController.clearTime` | 4번의 시각 고치기 창, 7번, 19번 | `docs/porting/schedule.md` |
| S6 | 자동 배치 결과 창 | `AutoPlaceDialog`, `TaskController.applyPlacement` | 18번 | `docs/porting/auto-place-dialog.md` |
| S7 | 나의 방 | `PetScreen`, `RoomView`, `ShopPanel`, `ShopController` | 20번 | `docs/porting/room.md` |
| S8 | 캐릭터 창 | `CharacterDialog`, `PetController.step`, `rename`, `pose`, `applyWear` | 27번, 28번, 30번 | `docs/porting/character.md` |
| S9 | 통계 화면 | `StatsScreen`, 통계 화면일 때 창 바탕의 자는 고양이([icons.md](porting/icons.md)) | 29번 | `docs/porting/stats-screen.md` |
| S10 | 설정 | `SettingsScreen`, `SettingsController`, `Autostart`, `OsAutostart` | 25번의 컴퓨터 켤 때 실행 | `docs/porting/settings.md` |
| S11 | 트레이, 알림, 하나만 실행 | `Tray`, `AppController.dueReminders`, `onClose`, `quit`, `SingleInstance` | 17번, 24번, 25번(프로토타입 기준 없음) | `docs/porting/tray.md` |
| C1 | 칸에 쳐 넣기 | `Typed` | 32번 | `docs/porting/typed.md` |
| C2 | 끌어 놓기와 되돌리기 | `TaskBook.placeAt`, `dropRange`, `dropStart`, `TaskController.placeAt`, `undoPlace` | 4번의 끌어 놓기 | `docs/porting/drag.md` |
| C3 | 설정 "목록 누르기" | `SettingsController.setListClickTime`, `TaskRows` | 데이터 절 `listClickTime` | `docs/porting/list-click.md` |
| C4 | 빠른 추가 | `TaskController.add` | 3번의 빠른 추가 | `docs/porting/quick-add.md` |
| C5 | 예시로 둘러보기 | `Examples`, `TaskController.clearSamples` | 31번 | `docs/porting/examples.md` |
| R | 릴리스 | 없음 | 없음(D-114의 설치 파일과 GitHub Releases) | `docs/porting/release.md` |

고른 순서의 까닭:

- **날짜 규칙이 처음이다.** "오늘"이 필요한 모든 곳이 1번을 쓰고, 이 클래스들은 다른 클래스에 기대지 않는다.
- **반복은 일정 넣기 앞이다.** 3번이 반복 사본을 `Repeat`로 만든다. 2번의 반복 칸 검사도 사본 수를 세므로 M2b에서 빼고 M3에 둔다.
- **목록은 고치기와 지우기 앞이다.** 4번, 5번 검사 줄에 고친 결과를 그날 목록과 시간표에 보이는 모양으로 적은 줄이 있어, 그 줄을 6번, 7번으로 확인한다.
- **앱 데이터는 저장 앞이다.** `AppData`의 값을 한 번에 다 두어야 저장 표(07 저장 형식)가 단위마다 바뀌지 않는다. 보상, 상점, 통계 규칙은 `AppData`를 받으므로 그 뒤다. 22번은 controller지만 저장 실패를 창 없이 검사하므로(T-SAVE-1~3) 저장과 함께 둔다.
- **창 틀은 부품 다음이다.** 프로토타입 창 틀은 디자인 시스템 부품(`Theme`, `Card`, `RoundButtonUI`, `Icons`, `StatCard`)과 로고 그림(`PetArt`)에 기대고, 이것을 다 옮기면 PR 한도의 세 배가 넘는다. 창 틀을 Swing 기본 모양으로 지었다가 다시 입히는 중간 단계를 두지 않으려고 S1b, S1c가 부품을 먼저 옮기고 S1d가 창 틀을 짓는다. S1은 화면 없이 검사하는 15번, 23번, 출석의 controller다. 첫 실행 갈래가 고르기 창 없이 생기지 않도록 `Main` 잇기는 고르기 창과 함께 S2가 한다.
- **화면은 홈 목록, 일정 입력 창, 일정관리 순이다.** 앱을 열면 처음 보는 것이 홈이고, 일정을 넣어야 다른 화면에 보일 것이 생긴다. 반려동물 고르기 창은 빈 폴더로 처음 열 때 뜨므로 홈 앞이다.
- **편의 기능은 끝이다.** 편의 기능은 핵심 길로 이미 되는 결과에 더 짧은 길을 더하는 것이다. 칸에 쳐 넣기는 목록에서 고르기, 끌어 놓기와 "목록 누르기"는 시각 고치기 창, 빠른 추가는 일정 입력 창, 예시로 둘러보기는 빈 앱에서 일정 넣기가 핵심 길이다. 끝에 둔다는 것은 순서일 뿐 빼는 것이 아니다. 편의 단위는 그 단위의 model과 화면을 함께 옮긴다.

## 2. 단위마다 할 일

단위를 옮기는 PR은 아래 순서로 한다. 1, 2, 3은 코드보다 먼저 단위 기록 파일(`docs/porting/{단위}.md`)에 적고, 1절 표의 "기록" 칸을 그 파일 링크로 바꾼다.

1. **화면에서 따라 할 단계.** 그 단위가 하는 일을 사람이 따라 할 수 있게 쓴다. 어느 화면에서, 어느 칸에 무엇을 넣고, 화면에 적힌 이름 그대로 어느 단추를 누르면, 무엇이 보이는지. 화면 글자는 [05 화면 설계](05-ui-design.md)나 프로토타입 코드에서 옮긴다. 화면이 없는 model 단위는 그 규칙이 화면에 드러나는 곳을 쓰고, [유저플로우](user-flows.md)에 그 단계가 있으면 `UF-1-1`처럼 가리킨다.
2. **입력의 경계.** 그 단위가 받는 입력마다 경계를 적는다. 빈 칸, 0, 가장 큰 값과 그보다 하나 큰 값, 자정(00:00)과 다음 날 04:00(앱 하루의 끝, D-77), 지난 날과 오늘, 한 주의 첫날과 마지막 날(D-91), 달과 해가 바뀌는 날 같은 것이다. 06 검사 줄에 이미 있는 경계는 그 번호(`T-DAY-2`)만 적는다. 06에 없는 경계는 06 검사 번호의 앞부분을 따라 `E-DAY-1`처럼 번호를 붙인다. 06은 명세가 바뀔 때만 고친다.
3. **프로토타입 결과.** 경계마다 프로토타입에 넣은 값과 실제 결과를 적는다(넣는 방법은 위 "프로토타입과 본 구현"). 시각에 달린 결과는 넣은 시각도 적는다. 이 결과가 새 코드의 비교 기준이다.

| 번호 | 입력과 경계 | 넣은 값 | 프로토타입 결과 | 본 구현 |
|---|---|---|---|---|
| T-DAY-2 | 앱 하루의 시작 | 11-16 04:00 | 11-16 | 같음 |
| E-DAY-1 | 자정 | 11-16 00:00 | 11-15 | 같음 |

위 표는 기록 파일의 꼴을 보이는 예다. "본 구현" 칸은 옮긴 뒤 "같음" 또는 "프로토타입과 일부러 다름"과 그 까닭이다.

4. **옮기는 커밋.** 프로토타입과 똑같이 만든다. 경계 줄마다 JUnit 테스트 하나를 두고 이름에 번호를 넣는다. 06 검사 줄 가운데 그 단위까지 옮긴 클래스만으로 되는 줄의 테스트도 이 단위에 둔다. 프로토타입 결과가 06과 다르면 이 커밋의 테스트는 프로토타입 결과를 기대한다.
5. **고치는 커밋.** 고칠 것(3절의 문제, 4절 표의 다른 점)은 다음 커밋에서 따로 고친다. 코드와 테스트의 기대를 함께 바꾸고, 기록 파일의 그 줄 "본 구현" 칸에 "프로토타입과 일부러 다름"과 까닭(D-번호나 3절의 문제 종류)을 적는다.
6. **프로토타입 기준이 없는 단위.** 1, 2는 같고, 3의 프로토타입 결과 칸은 "없음"이다. 비교 기준은 06 검사 줄이다.

## 3. 찾은 것을 나누는 규칙

옮기다 찾은 것은 아래 둘로 나눈다.

- **문제**: 데이터를 잃음, 틀린 값이 저장됨, 정한 규칙이나 명세와 다름, 사용자가 무슨 일이 일어났는지 모름. 문제는 가장 단순하고 분명한 해결로 고친다(2절 5번). 명세가 바뀌면 06과 결정 기록도 고친다.
- **편의개선**: 지금 받지 않는 입력이나 길을 더 받아 주는 것. 이슈로 만들지 않고 아래 편의개선 목록에 적는다.

- 사람이 보고 느끼는 문제(읽기 어려움, 헷갈림 같은 것)는 될 수 있으면 실제 데이터로 정한다.
- 문제에 선택지를 낼 때는 선택지마다 가장 작은 꼴인지 보고, "안내 문구 추가하기"와 "편의 처리 해 주기"가 둘 다 있는지 본다.

### 편의개선 목록

| 찾은 단위 | 화면과 단계 | 지금 받지 않는 입력이나 길 |
|---|---|---|

## 4. 문서와 프로토타입이 다른 점

문서 작업 중에 찾은, 문서(결정)와 프로토타입 동작이 다른 점이다. 문서는 결정대로 쓰고 프로토타입은 고치지 않는다. 이슈로 만들지 않고 그 기능을 옮기는 단위의 고치는 커밋(2절 5번)에서 처리한다. 줄마다 어느 화면에서 무엇을 하면 프로토타입은 무엇을 보이는지, 문서는 무엇이라고 적었는지, 출처(D-번호, 05나 06 절)를 쓴다.

| 찾은 작업 | 화면과 단계 | 프로토타입 | 문서 | 출처 |
|---|---|---|---|---|
| M1 날짜 규칙 | 반려동물 고르기 창에서 이름을 "펫1"로 쓰고 바로 시작을 누른다 | "처음 만났네! 나는 펫1이야."(숫자를 읽는 소리로 받침을 본다) | 이름 마지막 글자가 받침 있는 한글이면 "이야", 그 밖이면 "야"라 "펫1야"였다. 06을 고침: 읽어서 받침이 있는 숫자(0, 1, 3, 6, 7, 8)도 "이야" | 06 16번 "하는 일", [day-rules.md](porting/day-rules.md) E-WORD-6 |
| M1 날짜 규칙 | 홈 빠른 추가 칸에 이모지 15개를 쓰고 추가를 누른다 | 말풍선 "'{이모지 7개}…' 넣었구나!". 14번째 자리가 이모지 가운데면 깨진 글자와 "…" | 제목이 15자를 넘을 때만 앞 14자 뒤에 "…". 코드를 고침: 06 읽는 법에 글자 수는 코드 포인트로 센다고 적고 본 구현이 코드 포인트로 센다 | 06 16번 "하는 일", [day-rules.md](porting/day-rules.md) E-WORD-17~20 |
| M2b 일정 입력 검사 | 일정 입력 창 제목 칸에 이모지 51개를 붙여 넣는다. 한글 99자 뒤에 이모지 하나를 넣는다. 메모 칸에 이모지 1000개를 붙여 넣는다 | 제목 칸에 50개만 들어간다. 99자 뒤에는 이모지의 앞 반쪽(깨진 글자)이 들어간다. 메모 칸에 500개만 들어간다. 칸과 저장 검사가 모두 UTF-16 단위로 잰다 | 글자 수는 코드 포인트로 센다. 제목은 100자, 메모는 1000자까지다. 저장 검사(`TaskDraft.validate`)는 M2b에서 고침. 칸의 글자 막기는 S4 일정 입력 창에서 고친다 | 06 읽는 법, 06 2번, [task-check.md](porting/task-check.md) E-VAL-1, 2, 7 |
| M6 시각 고치기 창 | 고정 일정의 시각 빼기를 화면 밖 길로 부른다(화면에는 단추가 없다) | 시작 시각이 빠진 고정 일정이 된다 | 고정 일정은 시작 시각이 있어야 해서 바뀌지 않는다. 코드를 고침: `clearTime`이 아무것도 바꾸지 않는다 | 06 4번 T-EDIT-6, [task-edit.md](porting/task-edit.md) T-EDIT-6 |
| M6 시각 고치기 창 | 일정관리 목록의 마감이 지난 기간 할 일이나 홈 지난 날 목록의 기간 할 일에서 시각 단추의 동작을 화면 밖 길로 부른다 | 지난 날의 그 시각으로 정해진다 | 창을 연 목록의 날이 오늘보다 앞이면 정하지 않고 이 함수가 막는다. 코드를 고침: `update`가 아무것도 바꾸지 않는다 | 06 4번, D-99, [task-edit.md](porting/task-edit.md) T-EDIT-42, E-EDIT-15 |
| M7 앱 데이터 | 빈 폴더로 앱을 열어 반려동물 고르기 창에서 강아지를 고르고 바로 시작을 누른다 | 고르기 창 전에도 앱 데이터에 고양이 "나비"가 있고, 고르기 창이 펫 목록을 비우고 강아지를 넣는다 | 첫 실행은 빈 앱 데이터이고 고르기 창의 `choose`가 펫을 만들어 넣는다. 코드를 고침: 앱 데이터가 펫 없이 시작하고 그때 고른 펫은 `null`이다 | 07 seq-uc01, 06 30번, [app-data.md](porting/app-data.md) E-APP-4, 5 |
| M9 보상 | 끝낸 할 일의 완료를 화면 밖 길로 다시 부른다(화면의 체크박스는 끝낸 일을 누르면 체크 풀기다) | 보상을 또 받는다. 10P, 경험치 10, 오늘 횟수 하나가 더 붙는다 | 이미 끝낸 일정이면 `IllegalStateException`. 코드를 고침: `complete`가 예외를 던지고 아무것도 바꾸지 않는다 | 06 8번 "오류", [rewards.md](porting/rewards.md) E-RWD-2 |
| M9 보상 | 끝내지 않은 할 일의 체크 풀기를 화면 밖 길로 부른다(화면의 체크박스는 끝내지 않은 일을 누르면 끝내기다) | 아무것도 바뀌지 않고 알리지도 않는다 | 끝내지 않은 할 일이면 `IllegalStateException`. 코드를 고침: `uncomplete`가 예외를 던진다 | 06 9번 "오류", [rewards.md](porting/rewards.md) E-UNDO-2 |
| M10 펫 말 | 기간 할 일 "E사 지원서"(마감 11/25)의 시각을 내일 10:00으로 정해 두고, 오늘 할 일이 없는 날 그날 처음 앱을 연다 | 브리핑 "오늘은 할 일이 없어. 내일 'E사 지원서'가 있어." | 내일 문장은 내일 날짜의 보통 할 일(`TASK`, 기간 할 일이 아님)만 말한다. 코드를 고침: 시각을 정한 기간 할 일도 빼고 "오늘은 할 일이 없어. 아래 칸에 하나 적어 볼까?" | 06 16번 5번, D-71, [pet-talk.md](porting/pet-talk.md) E-TALK-10 |
| M15 상점과 방 | 나의 방에서 사지 않은 아이템의 "방에 놓기"를 화면 밖 길로 부른다(미리보기 칸은 가진 아이템에만 "방에 놓기"를 보인다) | 보유 목록에 없는 아이템이 방에 놓이고 만족도와 배치 아이템 수가 는다 | `toggle`에 사지 않은 아이템이면 `IllegalStateException`. 코드를 고침: `Room.toggle`이 예외를 던지고 아무것도 바꾸지 않는다 | 06 20번 "오류", [shop.md](porting/shop.md) E-ROOM-1 |
| S2f 입력 도우미 | 반려동물 고르기 창 이름 칸(10자)에 한글 9자를 쓰고 이모지 하나를 넣는다. 빈 이름 칸에 이모지 11개를 붙여 넣는다 | 9자 뒤에는 이모지의 앞 반쪽(깨진 글자)만 들어간다. 이모지는 5개만 들어간다. 글자 막기(`Ui.limit`)가 UTF-16 단위로 잰다 | 글자 수는 코드 포인트로 센다. 이름은 10자까지다. 코드를 고침: `Ui.limit`이 코드 포인트로 재어 9자 뒤 이모지는 통째로 들어가고 이모지는 10개까지 들어간다. 같은 `Ui.limit`을 쓰는 제목 칸과 메모 칸(위 M2b 줄)도 S4가 이것을 걸면 코드 포인트로 막힌다. 칸 안 글자 수 "0 / 10"은 #421이 고친다 | 06 읽는 법, 05 입력 항목 2, [pick.md](porting/pick.md) E-LIMIT-6 |
| S2g 둥근 입력칸 | 반려동물 고르기 창 이름 칸(10자)에 이모지 둘을 친다. 한글 9자 뒤에 이모지 하나를 넣는다. 빈 이름 칸에 이모지 11개를 붙여 넣는다 | 칸 안 글자 수가 "4 / 10"이다. 9자 뒤 이모지는 앞 반쪽만 들어가 "10 / 10", 이모지는 5개만 들어가 "10 / 10"이다. 글자 수(`RoundField.countText`)가 UTF-16 단위로 센다 | 글자 수는 코드 포인트로 센다. 이름은 10자까지다. 코드를 고침: `countText`가 코드 포인트로 세어 "2 / 10"이다. 9자 뒤 이모지는 통째로 들어가(위 S2f 줄) "10 / 10", 이모지 11개는 10개 들어가 "10 / 10"이다. 고치기 전에는 코드 포인트로 막는 `Ui.limit`과 만나 "11 / 10", "20 / 10"으로 최대를 넘어 보였다 | 06 읽는 법, 05 입력 항목 2, D-113, [pick.md](porting/pick.md) E-ROUND-10 |
| S2h 동물 카드 | 반려동물 고르기 창을 열어 동물 카드의 펫을 본다 | 펫이 숨쉬고 머리를 기울이고 귀와 꼬리를 움직이지만 깜빡이지 않는다. 눈은 늘 감고 웃는 선이다 | [디자인 시스템](design-system.md) 10절 동물 카드 줄은 "화면에 보이는 동안 숨쉬고 깜빡인다"고 적었다. 문서를 고칠지 깜빡임을 새로 만들지는 아직 정하지 않았다. 본 구현은 프로토타입대로 깜빡이지 않는다 | 디자인 시스템 10절, [pick.md](porting/pick.md) 1절 11~14단계 |
| S2k 펫 controller | 화면 밖 길로 이름 칸 10자 막기를 넘겨 반려동물 고르기(`choose`)나 펫 데려오기(`adopt`)에 이모지 10개, 한글 9자 뒤 이모지 하나를 넘긴다(화면의 이름 칸은 S2f가 코드 포인트로 막는다) | 이름이 이모지 5개, 한글 9자와 이모지의 앞 반쪽(깨진 글자)이 된다. 이름 규칙이 UTF-16 단위로 10까지 자른다 | 글자 수는 코드 포인트로 센다. 이름은 10자까지다. 코드를 고침: `PetController.nameOf`가 코드 포인트로 잘라 이모지 10개, 한글 9자와 이모지 하나가 된다. S8의 `rename`도 이 규칙을 쓴다 | 06 읽는 법, D-27, [pet-controller.md](porting/pet-controller.md) E-PET-11 |
