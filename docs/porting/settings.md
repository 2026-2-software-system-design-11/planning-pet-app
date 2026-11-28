# 단위 S10. 설정

[본 구현으로 옮기기](../porting.md) 1절 표의 단위 S10이다. 설정(05 설정, UC-18)과 06 25번의 컴퓨터 켤 때 실행을 옮긴다. 기준은 05 설정과 그 그림 [ui-settings](../diagrams/ui-settings.puml), 06 25번(T-WIN-4~6), 06 데이터 절의 `calendarFixed`와 `weekStart`, 07 [class-home-settings](../diagrams/class-home-settings.puml), D-29, D-86, D-91, D-114, 프로토타입 `settings/view/SettingsScreen`(83줄)과 `settings/controller/SettingsController`(30줄)다. 프로토타입에는 컴퓨터 켤 때 실행 코드가 없고 설정 화면의 체크박스가 늘 비활성이다. 그래서 `Autostart`와 `OsAutostart`는 비교 기준이 없고 06 25번과 아래 경계 줄이 기준이다(porting.md 2절 6번).

S10은 테스트를 더하면 약 740줄이라 PR 한도를 넘어 둘로 나눈다. S10a를 쓰다 실측이 한도를 넘어 Windows 등록과 `here()`를 S10b로 옮겼다. 아래 Windows 줄과 E-AUTO-9~12는 S10b가 옮긴다.

| 몫 | 이슈 | 옮기는 것 |
|---|---|---|
| S10a | #523 | `settings.model.Autostart`, `settings.store.OsAutostart`의 macOS 등록, `settings.controller.SettingsController`의 `setCalendarFixed`, `setWeekStart`, `setAutostart` |
| S10b | #525 | `settings.view.SettingsScreen`, `OsAutostart`의 Windows 등록과 이 컴퓨터의 값을 쓰는 `OsAutostart.here()`, `Main.open`이 설정을 앱 창에 붙이고 다시 그리기를 거는 일(#496 꼴), T-WIN-4~6의 화면 몫 |

이 단위가 옮기지 않는 것: 설정 "목록 누르기"(체크박스 줄과 `SettingsController.setListClickTime`)는 C3다. "예시 지우기"의 남은 예시 세기와 지우기는 C5이고, S10b는 홈(#496)처럼 그 함수를 받는 필드만 둔다. 트레이, 창 닫기, `--autostart`로 창 없이 뜨기는 S11이다.

## S10a. 컴퓨터 켤 때 실행과 설정 controller

07 안에서 정한 것과 그 까닭:

- **운영체제 일은 `settings.model.Autostart` 뒤에 둔다(07 층과 의존 방향).** `available()`, `registered()`, `set(on)`의 셋이다. `set`은 운영체제가 등록이나 빼기를 거절하면 `IOException`을 던지고 그 이유를 글로 담는다. 05 설정 1번이 실패하면 체크를 되돌리고 이유를 창으로 알리므로, 창을 띄우는 S10b가 그 글을 받는다.
- **설치된 앱인지는 시스템 속성 `jpackage.app-path`로 안다.** 설치 파일은 `jpackage`로 만들고(D-114), `jpackage`의 실행기는 이 속성에 실행 파일 자리를 넣는다. 속성이 없으면 실행 JAR이고 `available()`이 false다(D-29). 등록하는 경로는 이 값이다. 06, 07에 없어 메인 에이전트와 맞췄다.
- **Linux는 늘 쓸 수 없다.** 06 25번은 Windows와 macOS의 등록만 적고, 설치 파일도 두 운영체제용이다(D-29, D-114). 운영체제는 시스템 속성 `os.name`이 "Mac"이나 "Windows"로 시작하는지로 본다(06 21번의 데이터 폴더와 같은 꼴).
- **macOS는 `~/Library/LaunchAgents/com.planningpet.app.plist` 파일이다.** 켜면 `Label` `com.planningpet.app`, `ProgramArguments`(앱 경로, `--autostart`), `RunAtLoad` true를 쓰고, 끄면 파일을 지운다. 등록 상태는 파일이 있는지다. `launchctl`은 부르지 않는다. 이 폴더의 파일은 다음 로그인 때 운영체제가 읽는다. 앱 경로는 XML 글자로 바꿔 넣는다(`&`, `<`, `>`).
- **Windows는 `reg` 명령으로 `HKCU\Software\Microsoft\Windows\CurrentVersion\Run`의 값 `PlanningPet`을 다룬다.** 켜면 `reg add`로 `"앱 경로" --autostart`를 쓰고, 끄면 등록되어 있을 때만 `reg delete`를 부른다. 등록 상태는 `reg query`의 종료 코드가 0인지다. 따옴표는 `reg`가 글로 받도록 `\"`로 넘긴다. 값 이름은 메인 에이전트와 맞췄다. 외부 의존성을 더하지 않으려고(D-108) 레지스트리 라이브러리 대신 운영체제에 든 `reg`를 쓴다.
- **명령을 돌리는 일과 홈 폴더는 생성자로 받는다.** 검사는 `@TempDir` 폴더를 홈으로, 부른 명령을 적는 대역을 명령 실행(S10b)으로 넘긴다. 그래서 검사가 이 컴퓨터의 LaunchAgents나 레지스트리를 건드리지 않는다. 앱은 `OsAutostart.here()`로 이 컴퓨터의 값을 쓴다.
- **쓸 수 없는데 `set`을 부르면 `IllegalStateException`이고 아무것도 바꾸지 않는다.** 화면의 체크박스는 비활성이라 화면 밖 길이다. 등록 상태 읽기는 쓸 수 없어도 운영체제를 읽는다(설치한 앱이 등록해 둔 것을 실행 JAR에서 열어도 그대로 보인다).
- **`SettingsController`는 07 이름을 쓴다.** 프로토타입 `showFixedOnCalendar`, `weekStart`는 07의 `setCalendarFixed`, `setWeekStart`다. 두 설정은 저장할 데이터라 22번 `change`로 감싸고, 저장 실패 창의 이름은 05 설정의 칸 이름 "달력에 고정 일정 보이기", "한 주 시작"이다(메인 에이전트와 맞춤). `setAutostart`는 운영체제 등록만 바꾸고 06 데이터 표에 없는 값이라 저장하지 않고 알림도 보내지 않는다. 화면은 `registered()`를 다시 읽어 보인다(06 25번).

### 1. 화면에서 따라 할 단계

설정 화면은 S10b(#525) 뒤에 앱 창에서 볼 수 있다. 이 몫의 규칙이 드러나는 곳이다.

1. 설치 파일로 설치한 앱을 macOS에서 열고 설정 화면에서 "컴퓨터 켤 때 같이 실행"을 켠다. `~/Library/LaunchAgents/com.planningpet.app.plist`가 생긴다. 다른 화면에 갔다 설정으로 돌아오면 체크된 채다(T-WIN-4).
2. Finder에서 그 파일을 지우거나 macOS 설정의 로그인 항목에서 끈 뒤 설정 화면을 다시 연다. 체크가 꺼진 채다(T-WIN-5).
3. `java -jar`로 연 앱의 설정 화면은 체크박스가 비활성이다(T-WIN-6). Linux도 같다.
4. "달력에 고정 일정 보이기"를 켜면 홈 달력 칸의 수에 고정 일정이 들어간다(T-CAL-7). "한 주 시작"에서 "일요일"을 고르면 홈 달력이 일요일부터 놓인다. 저장할 수 없으면 "저장하지 못했어요" 창에 "한 주 시작 저장에 실패해서 바꾸기 전으로 돌렸어요."가 보이고 "월요일"로 돌아간다(05 설정 4번).

### 2. 입력의 경계와 결과

프로토타입 결과는 `work/impl/proto-kit`으로 프로토타입(마지막 변경 `2ac06b63`, JDK 21.0.11, macOS)의 `Main.create(MockData.empty())`에 부른 결과다. 컴퓨터 켤 때 실행은 프로토타입에 없어 "없음"이다. 앱 경로는 따로 적지 않으면 `/Applications/PlanningPet.app/Contents/MacOS/PlanningPet`이고 홈은 `@TempDir` 폴더다. "본 구현" 칸의 결과는 `OsAutostartTest`, `SettingsControllerTest`(E-AUTO-9~12는 S10b)가 번호 이름의 테스트로 확인한다.

| 번호 | 입력과 경계 | 넣은 값 | 프로토타입 결과 | 본 구현 |
|---|---|---|---|---|
| T-WIN-4 | macOS, 설치된 앱, 등록 없음 | `set(true)`, 이어서 `registered()` | 없음 | plist가 생기고 `registered()` true. controller의 `setAutostart(true)`도 같고 저장하지 않는다 |
| T-WIN-5 | T-WIN-4 뒤 운영체제에서 등록을 지움 | plist를 지우고 `registered()` | 없음 | false |
| T-WIN-6 | 실행 JAR(`jpackage.app-path` 없음) | macOS의 `available()`(Windows는 S10b) | 없음(체크박스가 늘 비활성) | false |
| E-AUTO-1 | plist 내용 | `set(true)` | 없음 | `Label` `com.planningpet.app`, `ProgramArguments` 앱 경로와 `--autostart`, `RunAtLoad` true |
| E-AUTO-2 | LaunchAgents 폴더가 없음 | 빈 홈에서 `set(true)` | 없음 | 폴더를 만들고 plist를 쓴다 |
| E-AUTO-3 | 이미 등록됨 | `set(true)` 두 번, 둘째는 다른 경로 | 없음 | 파일 하나, 둘째 경로로 덮어쓴다 |
| E-AUTO-4 | 등록 없음에서 끄기 | `set(false)` | 없음 | 오류 없음, 파일 없음 |
| E-AUTO-5 | 경로에 빈칸, `&`, `<` | `/Applications/A & B <1>/PlanningPet` | 없음 | `&amp;`, `&lt;`, `&gt;`로 바꿔 쓴다 |
| E-AUTO-6 | 쓸 수 없음 | LaunchAgents 자리에 같은 이름의 파일 | 없음 | `IOException`, `registered()` false |
| E-AUTO-7 | 쓸 수 없는데 켜기 | 실행 JAR에서 `set(true)` | 없음 | `IllegalStateException`, 파일 없음 |
| E-AUTO-8 | Linux | 앱 경로가 있는 Linux의 `available()`, `registered()` | 없음 | 둘 다 false |
| E-AUTO-9 | Windows 켜기 | `C:\Program Files\PlanningPet\PlanningPet.exe`, `set(true)` | 없음 | `reg add HKCU\...\Run /v PlanningPet /t REG_SZ /d \"C:\Program Files\PlanningPet\PlanningPet.exe\" --autostart /f` 한 번 |
| E-AUTO-10 | Windows 등록 상태 | `reg query`의 종료 코드 0, 1 | 없음 | true, false. 명령을 돌리지 못하면 false |
| E-AUTO-11 | Windows 끄기 | 등록됨, 등록 없음에서 `set(false)` | 없음 | 등록됐을 때만 `reg delete ... /v PlanningPet /f` |
| E-AUTO-12 | Windows가 거절 | `reg add`가 종료 코드 1, 출력 "액세스가 거부되었습니다." | 없음 | `IOException`, 글은 그 출력 |
| E-SET-1 | 달력에 고정 일정 보이기(T-CAL-7의 설정 몫) | `setCalendarFixed(true)`, 다시 `true` | 값 true, 알림 한 번씩(같은 값도) | 같음. 저장도 한 번씩 |
| E-SET-2 | 한 주 시작 | `setWeekStart(SUNDAY)`, `MONDAY` | 값이 그대로 바뀐다 | 같음. 저장 한 번씩 |
| E-SET-3 | 저장 실패 | 늘 실패하는 저장소에서 `setWeekStart(SUNDAY)`, `setCalendarFixed(true)` | 없음(프로토타입은 저장하지 않는다) | `MONDAY`, false로 돌아가고 "한 주 시작 저장에 실패해서 바꾸기 전으로 돌렸어요.", "달력에 고정 일정 보이기 저장에 실패해서 바꾸기 전으로 돌렸어요." |
| E-SET-4 | 등록 실패 | 거절하는 `Autostart`에서 `setAutostart(true)` | 없음 | 그 `IOException`이 화면으로 가고 데이터와 저장은 그대로 |
| E-SET-5 | 월요일, 일요일 밖의 한 주 시작 | 화면 밖 길로 `setWeekStart(WEDNESDAY)`, `setWeekStart(null)` | 수요일이 저장된다. null은 저장된 뒤 홈 달력의 다시 그리기가 `NullPointerException` | 프로토타입과 일부러 다름(porting.md 4절): `IllegalArgumentException`, 값과 저장과 알림 그대로 |

### 3. 검사하지 못한 것

- `OsAutostart.here()`가 실제 `reg` 프로세스를 돌리는 일과 이 컴퓨터의 홈 폴더를 쓰는 일은 검사하지 않는다. 검사가 이 컴퓨터를 실제로 등록하게 되기 때문이다.
- Windows에서 `\"`로 넘긴 따옴표가 레지스트리 값에 따옴표로 들어가는지, macOS가 다음 로그인 때 plist를 읽어 앱을 `--autostart`로 띄우는지는 설치 파일(R 단위)로 손으로 본다.
