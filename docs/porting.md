# 본 구현으로 옮기기

프로토타입(`prototype/`)의 동작을 본 구현(`src/`)으로 옮기는 방법과 순서다. 다른 언어로 다시 쓰는 일과 비슷해서, 정한 방법 없이 옮기면 동작이 말없이 달라진다. 본 구현의 클래스는 07 클래스 다이어그램을, 함수와 검사 줄은 [06 함수 명세](06-functions.md)를 따른다.

## 프로토타입과 본 구현

- 프로토타입은 본 구현이 옮길 동작의 비교 기준이다. 단위마다 경계를 프로토타입에 넣어 얻은 결과를 새 코드가 같게 내는지 본다(2절).
- 비교 기준을 얻는 동안 프로토타입 코드를 바꾸지 않는다. 바꾸면 기준이 바뀐다. model의 경계는 `prototype/build.sh`가 만든 `prototype/out`을 클래스 경로에 두고 그 클래스를 부르는 버리는 Java 파일로 넣어 본다(`Store.now`로 시각을 정한다). 화면의 경계는 `prototype/qa/`의 창 없이 누르는 코드와 `prototype/snap.sh`로 넣어 본다.
- 프로토타입에 없는 기능(저장 21번, 22번, 알림 17번, 앱 하나만 실행 24번, 트레이와 컴퓨터 켤 때 실행 25번, [prototype/README.md](../prototype/README.md) "빠진 것")은 비교 기준이 없다. 그 단위는 06 검사 줄만 기준이다.

## 1. 옮기는 단위와 순서

- 커밋 하나는 변경 100줄 이하이고, 그 커밋만으로 빌드되고 검사(`gradlew test`, D-108)를 통과한다. 단위 하나는 PR 하나(500줄 이하)이고 커밋 여럿으로 나눈다.
- 순서는 빌드와 빈 앱, model을 의존 순서대로(다른 클래스가 기대는 것부터), 화면, 편의 기능이다. 단위 하나가 끝날 때마다 앱이 빌드되고 그때까지 옮긴 규칙이 검사로 돈다.
- 수업 슬라이드 "1. A Simple Application"은 고객이 원하는 동작을 먼저 되게 하고, OO 원칙을 적용하고, 다시 쓰기 좋은 설계로 가는 세 단계를 가르친다. "1. Requirements and Changes"는 반복마다 감당할 만한 일을 맡아 작동하는 시스템을 낸다. 고객이 원하는 동작은 프로토타입과 06에 있고 설계는 07에 있으므로, 단위마다 그 동작을 07의 클래스로 옮기고 작동하는 앱으로 낸다.
- "기록" 칸은 그 단위를 옮기는 PR이 더하는 단위 기록 파일이다(2절).

| 순서 | 단위 | 07 클래스 | 06 | 기록 |
|---|---|---|---|---|
| 0 | 빌드와 빈 앱 | `Main`, `AppWindow` | 없음(D-108, D-109의 층 의존 검사) | `docs/porting/build.md` |
| M1 | 날짜 규칙 | `AppDay`, `Rules`, `Words` | 1번, 데이터 절 "한 주", 16번의 글자 규칙 | `docs/porting/day-rules.md` |
| M2 | 일정과 그 검사 | `Task`, `TaskDraft`, 값 목록 `Kind`, `Category`, `PetKind` | 데이터 절 `Task`, 2번(반복 칸 빼고) | `docs/porting/task-check.md` |
| M3 | 반복 | `Repeat`, `TaskDraft.validate`의 반복 칸 | 데이터 절 `repeat`, 2번 T-VAL-11~13, 15, 16, 18 | `docs/porting/repeat.md` |
| M4 | 일정 넣기와 겹침 | `TaskBook.add`, `overlap` | 3번 | `docs/porting/task-add.md` |
| M5 | 그날 목록과 주의 목록 | `TaskBook.dayPlan`, `untimedOfWeek`, `timeText`, `dueText`, `periodNote` | 6번, 7번 | `docs/porting/task-lists.md` |
| M6 | 일정 고치기와 지우기 | `TaskBook.update`, `clearTime`, `delete` | 4번(끌어 놓기 빼고), 5번 | `docs/porting/task-edit.md` |
| M7 | 앱 데이터 | `AppData`, `PetState`, `Pose`, `WearId`, `ItemId` | 데이터 절 `AppData`, `PetState` | `docs/porting/app-data.md` |
| M8 | 저장 | `DataStore`, `SqliteDataStore`, `Schema`, `AppController.change` | 21번, 22번(프로토타입 기준 없음) | `docs/porting/store.md` |
| M9 | 보상 | `Rewards`, `Level`, `DailyProgress`, `Visits`, `Task.finish`, `unfinish` | 8~13번 | `docs/porting/rewards.md` |
| M10 | 펫 말 | `PetTalk`, `Bubble` | 16번 | `docs/porting/pet-talk.md` |
| M11 | 화면이 쓰는 일정 계산 | `DefaultDates`, `CalendarCell`, `TimetableLayout` | 14번, 19번, 26번 | `docs/porting/screen-calc.md` |
| M12 | 자동 배치 | `AutoPlacer` | 18번 | `docs/porting/auto-place.md` |
| M13 | 알림 고르기 | `Reminders` | 17번(프로토타입 기준 없음) | `docs/porting/reminders.md` |
| M14 | 펫 | `Bond`, `Wardrobe`, `Poses`, `PetState.pat` | 27번, 28번이 쓰는 장신구 표 | `docs/porting/pet.md` |
| M15 | 상점과 방 | `Shop`, `Item`, `Room` | 20번 | `docs/porting/shop.md` |
| M16 | 통계 | `Stats` | 29번 | `docs/porting/stats.md` |
| S1 | 창 틀과 날 바뀜 | `AppWindow`, `AppController.onWindowShown`, `say`, `DayWatcher`, `RewardController.attend` | 11번, 15번, 23번 | `docs/porting/app-window.md` |
| S2 | 반려동물 고르기 창 | `PickDialog`, `PetController.adopt`, `select` | 30번 | `docs/porting/pick.md` |
| S3 | 홈 목록 | `MainScreen`, `PetCard`, `CalendarPanel`, `HomeController`, `RewardController.complete`, `uncomplete` | 6번, 8번, 9번, 13번, 16번, 26번 | `docs/porting/home.md` |
| S4 | 일정 입력 창 | `TaskDialog`, `TaskController.add`, `update`, `delete` | 2~5번, 14번 | `docs/porting/task-dialog.md` |
| S5 | 일정관리 | `ScheduleScreen`, `TimetablePanel`, `TaskRows`, `TimeEditor`, `TaskController.clearTime` | 4번의 시각 고치기 창, 7번, 19번 | `docs/porting/schedule.md` |
| S6 | 자동 배치 결과 창 | `AutoPlaceDialog`, `TaskController.applyPlacement` | 18번 | `docs/porting/auto-place-dialog.md` |
| S7 | 나의 방 | `PetScreen`, `RoomView`, `ShopPanel`, `ShopController` | 20번 | `docs/porting/room.md` |
| S8 | 캐릭터 창 | `CharacterDialog`, `PetController.step`, `rename`, `pose`, `applyWear` | 27번, 28번, 30번 | `docs/porting/character.md` |
| S9 | 통계 화면 | `StatsScreen` | 29번 | `docs/porting/stats-screen.md` |
| S10 | 설정 | `SettingsScreen`, `SettingsController`, `Autostart`, `OsAutostart` | 25번의 컴퓨터 켤 때 실행 | `docs/porting/settings.md` |
| S11 | 트레이, 알림, 하나만 실행 | `Tray`, `AppController.dueReminders`, `onClose`, `quit`, `SingleInstance` | 17번, 24번, 25번(프로토타입 기준 없음) | `docs/porting/tray.md` |
| C1 | 칸에 쳐 넣기 | `Typed` | 32번 | `docs/porting/typed.md` |
| C2 | 끌어 놓기와 되돌리기 | `TaskBook.placeAt`, `dropRange`, `dropStart`, `TaskController.placeAt`, `undoPlace` | 4번의 끌어 놓기 | `docs/porting/drag.md` |
| C3 | 설정 "목록 누르기" | `SettingsController.setListClickTime`, `TaskRows` | 데이터 절 `listClickTime` | `docs/porting/list-click.md` |
| C4 | 빠른 추가 | `TaskController.add` | 3번의 빠른 추가 | `docs/porting/quick-add.md` |
| C5 | 예시로 둘러보기 | `Examples`, `TaskController.clearSamples` | 31번 | `docs/porting/examples.md` |

고른 순서의 까닭:

- **날짜 규칙이 처음이다.** "오늘"이 필요한 모든 곳이 1번을 쓰고, 이 클래스들은 다른 클래스에 기대지 않는다.
- **반복은 일정 넣기 앞이다.** 3번이 반복 사본을 `Repeat`로 만든다. 2번의 반복 칸 검사도 사본 수를 세므로 M2에서 빼고 M3에 둔다.
- **목록은 고치기와 지우기 앞이다.** 4번, 5번 검사 줄에 고친 결과를 그날 목록과 시간표에 보이는 모양으로 적은 줄이 있어, 그 줄을 6번, 7번으로 확인한다.
- **앱 데이터는 저장 앞이다.** `AppData`의 값을 한 번에 다 두어야 저장 표(07 저장 형식)가 단위마다 바뀌지 않는다. 보상, 상점, 통계 규칙은 `AppData`를 받으므로 그 뒤다. 22번은 controller지만 저장 실패를 창 없이 검사하므로(T-SAVE-1~3) 저장과 함께 둔다.
- **화면은 홈 목록, 일정 입력 창, 일정관리 순이다.** 앱을 열면 처음 보는 것이 홈이고, 일정을 넣어야 다른 화면에 보일 것이 생긴다. 반려동물 고르기 창은 빈 폴더로 처음 열 때 뜨므로 홈 앞이다.
- **편의 기능은 끝이다.** 편의 기능은 핵심 길로 이미 되는 결과에 더 짧은 길을 더하는 것이다. 칸에 쳐 넣기는 목록에서 고르기, 끌어 놓기와 "목록 누르기"는 시각 고치기 창, 빠른 추가는 일정 입력 창, 예시로 둘러보기는 빈 앱에서 일정 넣기가 핵심 길이다. 끝에 둔다는 것은 순서일 뿐 빼는 것이 아니다. 편의 단위는 그 단위의 model과 화면을 함께 옮긴다.
