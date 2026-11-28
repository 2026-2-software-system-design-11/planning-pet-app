# 단위 S11. 트레이, 알림, 하나만 실행

[본 구현으로 옮기기](../porting.md) 1절 표의 단위 S11이다. 07의 `AppController.dueReminders`, `onClose`, `quit`를 `planningpet.app.controller`에, `SingleInstance`를 `planningpet.app`에, 트레이 아이콘과 메뉴, 운영체제 알림을 `planningpet.app.view.Tray`에 둔다([class-app](../diagrams/class-app.puml)). 기준은 06 17번, 24번, 25번과 그 검사 줄, 02 UC-01 1a, UC-13, UC-19, D-23, D-24, D-29, 05 "그 밖의 창과 알림", 07 [seq-uc01](../diagrams/seq-uc01.puml), [seq-uc13](../diagrams/seq-uc13.puml), [seq-uc19](../diagrams/seq-uc19.puml), 07 실행 모드다. 프로토타입에는 트레이, 알림, 하나만 실행이 없어(창 닫기는 `EXIT_ON_CLOSE`) 비교 기준이 없고 06 검사 줄만 기준이다(porting.md 2절 6번).

PR 하나의 크기 한도(1절) 때문에 둘로 나눠 옮긴다.

| 몫 | 이슈 | 옮기는 것 |
|---|---|---|
| S11a | #522 | 이 기록 파일, `CloseAction`, `AppController.onClose`, `quit`, `dueReminders`와 그 `sent`, `SingleInstance` |
| S11b | #524 | `Tray`의 트레이 아이콘과 메뉴(열기, 끝내기), 30초마다 `dueReminders`와 운영체제 알림, 알림을 누르면 홈에서 그 날짜 고르기, 창 닫기를 `onClose`로 잇기, `--autostart`, macOS Cmd+Q, `Main`이 데이터를 읽기 전에 `SingleInstance`를 부르고 신호에 창을 앞으로 가져오는 일(E-MAIN-20, E-ONE-5) |

S11a만으로는 앱이 S11a의 클래스를 부르지 않았다(창 닫기는 끝내기, 알림 없음, 두 번째 실행도 앱 하나가 더 뜸). S11b가 `Main`, 창 닫기, `Tray`에서 이 클래스들을 부른다.

07 안에서 정한 것과 그 까닭:

- **`onClose`가 돌려주는 `CloseAction`은 `HIDE`, `HIDE_WITH_NOTICE`, `QUIT` 셋이다.** 07은 타입 이름만 적었다(04 UC-19 "할 일(`CloseAction`)을 돌려준다. 창을 숨기거나 끝내는 일은 view가 한다"). 트레이를 지원하지 않으면 `QUIT`이고 view가 `quit`를 부른다(seq-uc19 1b, T-WIN-3). 지원하면 창이 보이지 않는 것으로 두고(`windowVisible` false), `trayNoticeShown`이 false면 `change`로 true를 저장하고 `HIDE_WITH_NOTICE`, true면 `HIDE`다. 값 목록은 controller가 돌려주는 값이라 `app.controller`에 둔다.
- **처음 닫기 안내는 저장에 실패해도 뜬다.** 02 UC-19 1c "1a에서 안내한 것을 저장하지 못했다"와 seq-uc19가 저장과 되돌리기 뒤에 알림을 그렸다. 그래서 `onClose`는 저장 결과와 관계없이 `HIDE_WITH_NOTICE`이고, 기록이 되돌려져 다음 닫기에서 다시 안내한다(E-CLOSE-1).
- **저장 실패 창의 이름은 "처음 창 닫기 안내"다.** 06 22번 "쓰는 곳"의 "UC-19는 처음 창 닫기 안내 기록을 저장한다"와 05 "그 밖의 창과 알림" 표의 "처음 창 닫기" 줄 이름에서 왔다. 다른 갈래가 "연속 접속", "할 일 완료"처럼 06의 이름을 쓰는 것과 같다.
- **`quit`는 `Main`이 넣는 끝내기 함수를 부른다(`setQuit`).** 07 "controller는 창을 모른다"처럼 끝내는 일(`System.exit`)을 controller가 부르지 않고, 검사가 끝내기를 바꿔 끼운다. 처음 값은 아무것도 하지 않는 함수라 검사가 JVM을 끝내지 않는다. 저장은 변경마다 이미 했으므로(22번) 끝낼 때 더 저장할 것이 없다.
- **`dueReminders(now)`는 `Reminders.due(book, now, sent)`를 부른다.** `sent`는 `AppController`의 필드이고 저장하지 않는다(04 UC-13, 06 17번). 그래서 앱을 다시 켜면 아직 시작 전인 일정을 다시 알린다(E-NTF-9, D-24 "실행 중에만 기억").
- **`SingleInstance(folder, onSignal)`는 데이터 폴더의 `planningpet.lock`을 잠근다(07 실행 모드).** 폴더가 없으면 만든다. 잡으면 `127.0.0.1`의 빈 포트에서 기다리는 데몬 스레드를 두고 그 포트를 파일에 적고 true다. 연결이 하나 올 때마다 `onSignal`을 부른다. 못 잡으면 파일의 포트로 연결 하나를 보내고 false다. 연결 자체가 신호라 주고받는 글은 없다.
- **잠금은 파일 끝 너머 한 바이트에 건다.** Windows는 잠근 영역을 다른 프로세스가 읽지 못해서, 파일 전체를 잠그면 두 번째 앱이 포트를 읽지 못한다. 포트는 파일 앞쪽에 적고 잠금은 `Long.MAX_VALUE - 1`의 한 바이트에 건다. 같은 JVM 안의 두 번째 잠금은 `OverlappingFileLockException`이라 이것도 못 잡은 것으로 본다(T-ONE-2 검사가 이 길을 쓴다).
- **`Main`의 신호는 그때 떠 있는 창을 앞으로 가져온다(S11b #524).** `Main`이 데이터를 읽기 전에 `acquire`를 부르고 false면 종료 코드 0으로 끝낸다. 신호는 Swing 스레드에서 지금 창(첫 실행의 반려동물 고르기 창이나 메인 창)을 보이고 앞으로 가져온다. 메인 창이면 이어서 15번 `onWindowShown`을 부른다(02 UC-01 1a, seq-uc01). 고르기 창이면 아직 펫이 없어 15번을 부르지 않는다.
- **T-ONE-3은 실행 JAR 대신 테스트 클래스 경로의 작은 자식 JVM을 쓴다.** 07 실행 모드는 다른 프로세스 검사가 실행 JAR를 쓴다고 적었지만, 실행 JAR는 창을 띄운다. 자식 JVM은 `SingleInstance`만 잡고 기다리며 창을 띄우지 않는다. 검사는 자식을 `destroyForcibly`(Linux와 macOS에서 `kill -9`와 같은 SIGKILL)로 끝내고, 검사가 실패해도 `finally`에서 자식을 끝낸다. 잠금 파일은 `@TempDir` 폴더에 있어 검사가 끝나면 지워진다.

S11b(#524)에서 07 안에서 정한 것과 그 까닭:

- **`Tray(app, watcher, clock, window, home, front)`가 30초마다 23번 `tick`과 17번 `dueReminders`를 부르고 받은 알림을 띄운다.** 차례는 날 바뀜이 먼저다. 알림의 제목은 `Reminder.title`("플래닝펫"), 내용은 `text`다. 처음 창 닫기 안내도 제목을 앱 이름 "플래닝펫"으로 둔다(05는 내용만 적었다). `front`는 `Main`이 넣는 창 보이기와 앞으로 가져오기이고 두 번째 실행의 신호가 쓰는 함수와 같다.
- **알림을 누르면 창을 앞으로 가져오고, 홈으로 바꾸고, 그 할 일의 `date`를 고르고, 15번을 부른다(seq-uc13).** 고르는 날은 `Reminder.start`의 날이 아니라 할 일의 `date`다. D-77로 다음 날 00:30에 시작하는 일도 목록은 `date`의 날에 있다. `MainScreen.showDay`가 달력의 `pick`을 불러 그 날과 그 달로 옮기고 `HomeController.pick`에 넘긴다(UC-09와 같다).
- **알림 뒤 첫 트레이 사건만 알림 누르기로 본다.** Windows의 Java `TrayIcon`은 알림 누르기와 아이콘 두 번 누르기를 같은 `ActionEvent`로만 알려 둘을 가르지 못한다. 그래서 일정 시작 알림을 띄운 뒤 처음 오는 사건 한 번은 그 알림의 날짜로 열고, 그 뒤의 사건과 알림이 없을 때의 사건은 열기와 같다. 처음 창 닫기 안내를 띄우면 남은 알림 날짜를 지운다. 운영체제가 알림 누르기를 알려 주는지는 05 "확인하지 않은 것"대로 손으로 본다.
- **트레이 메뉴는 `menu()`가 돌려주는 이름과 함수의 차례(열기, 끝내기)다.** 열기는 `front`와 15번, 끝내기는 `AppController.quit`다. `PopupMenu`, `MenuItem`, `TrayIcon`은 headless에서 `HeadlessException`이라 메뉴와 알림의 동작은 함수로 검사하고, 운영체제에 다는 `install`은 3절에 적는다.
- **창 닫기는 `AppWindow.close(traySupported, hide, notice)`가 한다(seq-uc19).** `frame`은 닫기 버튼을 `DO_NOTHING_ON_CLOSE`로 두고 그 사건에 이 함수를 잇는다. `onClose`가 `QUIT`이면 `quit`, `HIDE`면 숨기기, `HIDE_WITH_NOTICE`면 숨긴 뒤 `Tray.notice`다. `traySupported`는 `SystemTray.isSupported()`가 참이고 아이콘을 실제로 달았는지다. 달지 못하면 숨긴 창을 열 길이 없다.
- **`Main`은 `setQuit`에 종료 코드 0의 끝내기를 넣는다.** 끝내기는 오류가 아니다. macOS Cmd+Q는 `Desktop.setQuitHandler`로 받아 Swing 스레드에서 `AppController.quit`를 부른다(트레이 메뉴의 끝내기와 같다). 그 기능이 없는 운영체제(Windows)에서는 잇지 않는다.
- **`--autostart`면 메인 창을 만들되 띄우지 않고 15번을 부르지 않는다(07 실행 모드, T-BRF-3).** 트레이의 열기나 두 번째 실행의 신호가 처음 창을 띄우고 15번을 부른다. 트레이를 달지 못했으면 보통 실행처럼 창을 띄우고 15번을 부른다. 06, 05가 정하지 않음, 페르소나 투표 전 잠정이다. 창 없이 남으면 앱을 끝낼 길이 없어 그대로 두는 길은 뺐고, 창을 띄우며 안내 글을 보이는 길과 함께 투표에 올렸다. 이 갈래는 `Main.open`의 조건 한 줄이다. 데이터가 없는 첫 실행은 `--autostart`여도 고르기 창을 띄운다(펫이 없어 메인 창과 트레이가 없다). 못 읽음 창도 같다.
- **`Main`의 `Shell`에 `tray`(아이콘 달기), `front`, `hide`, `quitKey`(Cmd+Q 잇기)를 더하고 `main`은 창을 만들기만 한다.** 메인 창을 여는 차례는 Cmd+Q 잇기, 트레이 달기, 창 만들기, 앞으로 가져오기와 15번, 신호 잇기, 타이머다. 트레이를 먼저 달아야 창 닫기가 트레이 지원을 안다.

## 1. 화면에서 따라 할 단계

`java -jar build/libs/planning-pet-app.jar --data-dir 폴더`로 연다([build.md](build.md)). 1~3은 `Main`이 `SingleInstance`를 부르는 S11b(#524)부터, 4~8은 S11b의 트레이부터 이렇게 된다.

1. 앱을 열어 둔 채 같은 폴더로 한 번 더 연다. 두 번째 앱은 창 없이 끝나고 첫 앱의 메인 창이 앞으로 온다. 펫이 "오늘 할 일 2개 남았어."처럼 그날의 보통 말을 한다(UC-01 1a, T-ONE-2, 06 15번 `USUAL`). 폴더에 `planningpet.lock`이 생긴다.
2. 빈 폴더로 열어 반려동물 고르기 창이 뜬 채 같은 폴더로 한 번 더 열면 두 번째 앱은 끝나고 고르기 창이 앞으로 온다(E-ONE-5).
3. 첫 앱을 강제로 끝낸 뒤(작업 관리자의 작업 끝내기, `kill -9`) 다시 열면 앱이 뜬다(T-ONE-3).
4. 메인 창의 닫기 버튼을 처음 누르면 창이 사라지고 트레이에 발자국 아이콘이 남으며 운영체제 알림 "플래닝펫은 트레이에서 계속 알림을 보내요"가 뜬다. 다시 열고 닫으면 알림 없이 사라진다(T-WIN-1, T-WIN-2).
5. 트레이 아이콘을 누르면 메뉴 "열기", "끝내기"가 뜬다. 열기는 창을 다시 보이고 펫이 그날의 보통 말을 한다(T-BRF-2). 끝내기는 앱을 끝내고 아이콘이 사라진다.
6. 15:00 할 일 "통계 과제"를 넣어 두고 14:50이 되면 30초 안에 제목 "플래닝펫", 내용 "통계 과제 10분 뒤 시작"인 알림이 한 번 뜬다. 창을 닫아 두어도 뜬다(T-NTF-1). 알림을 누르면 메인 창이 앞으로 오고 홈의 달력이 그 할 일의 날짜를 고른다.
7. macOS에서 Cmd+Q를 누르면 앱이 끝난다.
8. `--autostart`를 붙여 열면 창 없이 트레이 아이콘만 생긴다. 트레이의 열기를 누르면 창이 뜨고 그날 처음이면 브리핑을 한다(T-BRF-3).

## 2. 입력의 경계와 결과

프로토타입 결과는 모두 "없음"이다. 06에 검사 줄이 없는 경계는 `E-CLOSE`(T-WIN은 06, frame.md의 E-WIN과 겹치지 않게), `E-NTF`(reminders.md의 E-NTF-7에 이어), `E-ONE` 번호다. 따로 적지 않으면 지금은 11-16(월) 14:50이고 할 일은 11-16 15:00 "통계 과제"다. "본 구현" 칸의 결과는 `AppControllerCloseTest`, `AppControllerReminderTest`, `SingleInstanceTest`가 번호 이름의 테스트로 확인한다. E-MAIN-20~24와 E-ONE-5는 S11b(#524)의 `MainStartTest`가, E-TRAY-2~7은 `TrayTest`가, E-CLOSE-4~6은 `AppWindowTest`가 확인한다.

| 번호 | 입력과 경계 | 넣은 값 | 프로토타입 결과 | 본 구현 |
|---|---|---|---|---|
| T-WIN-1 | 처음 닫기 | `trayNoticeShown` false, 트레이 지원 | 없음(창 닫기는 끝내기) | `HIDE_WITH_NOTICE`, 창이 보이지 않음, `trayNoticeShown` true, 저장 1번, 알림 1번 |
| T-WIN-2 | 두 번째 닫기 | `trayNoticeShown` true, 트레이 지원 | 없음 | `HIDE`, 창이 보이지 않음, 저장 없음 |
| T-WIN-3 | 트레이 미지원 | `trayNoticeShown` false, 트레이 미지원 | 없음 | `QUIT`, `trayNoticeShown` false 그대로, 저장 없음. 앱을 끝내는 일은 view가 `quit`로 한다 |
| E-CLOSE-1 | 처음 닫기 저장 실패(UC-19 1c) | T-WIN-1에 늘 실패하는 저장소, 두 번 닫기 | 없음 | 두 번 다 `HIDE_WITH_NOTICE`, `trayNoticeShown` false, 저장 실패 창 "처음 창 닫기 안내 저장에 실패해서 바꾸기 전으로 돌렸어요." 두 번 |
| E-CLOSE-2 | 닫은 뒤 날이 바뀜 | 창을 보인 뒤 닫고(T-WIN-2) 11-17 04:00:10에 23번 확인 | 없음 | 말 없음, `lastOpenDay` 그대로, 알림 1번(T-DAYW-3을 실제 숨기기로) |
| E-CLOSE-3 | 끝내기 | `setQuit`로 넣은 함수, `quit` | 없음 | 넣은 함수가 1번 불린다. 넣지 않았으면 아무 일도 없다 |
| E-NTF-8 | 실행 중 보낸 알림 | 14:50에 `dueReminders`, 이어서 14:50:30 | 없음 | "통계 과제 10분 뒤 시작", 그다음 없음(T-NTF-6이 controller의 `sent`로) |
| E-NTF-9 | 다시 켠 앱 | E-NTF-8 뒤 같은 데이터로 새 `AppController`, 14:55 | 없음 | "통계 과제 5분 뒤 시작"(`sent`는 저장하지 않는다) |
| T-ONE-1 | 첫 실행 | 빈 폴더 | 없음 | true, 폴더에 `planningpet.lock`과 그 안의 포트 |
| T-ONE-2 | 이미 실행 중 | A가 잡은 폴더에서 B가 `acquire` | 없음 | B는 false, A의 `onSignal`이 1번 불린다. 메인 창 뒤의 신호는 15번을 부른다(E-MAIN-20) |
| T-ONE-3 | 강제로 끝난 앱 | 자식 JVM이 잡은 뒤 SIGKILL로 끝남, 이 JVM이 `acquire` | 없음 | true. 잠금 파일에는 새 포트 |
| E-ONE-1 | 데이터 폴더 없음 | 없는 폴더 아래의 폴더 | 없음 | 폴더를 만들고 true |
| E-ONE-2 | 두 번 신호 | A가 잡은 뒤 B와 C가 차례로 `acquire` | 없음 | B와 C 모두 false, A의 `onSignal` 2번 |
| E-ONE-3 | 포트를 읽을 수 없음 | A가 잡은 잠금 파일의 포트를 "abc"로 바꾼 뒤 B가 `acquire` | 없음 | B는 예외 없이 false, A의 `onSignal` 없음. A가 잠그고 포트를 적기 전의 짧은 틈과 같은 결과다 |
| E-ONE-4 | 잠금 파일을 만들 수 없음 | 데이터 폴더 자리에 같은 이름의 파일 | 없음 | true(실행을 잇고 이어지는 읽기와 저장의 창이 알린다). 06 24번이 정하지 않음, 페르소나 투표 전 잠정. 안내 창을 띄우고 끝내는 길과 함께 투표에 올렸다 |
| E-ONE-5 | 고르기 창 중 두 번째 실행 | 첫 실행 고르기 창이 떠 있을 때 신호 | 없음 | S11b(#524). 고르기 창을 앞으로 가져오고 15번은 부르지 않는다 |
| E-MAIN-20 | 메인 창 뒤 신호 | E-MAIN-11의 데이터로 메인 창을 연 뒤 신호 | 없음 | S11b(#524). 창을 앞으로 가져오고 15번 `USUAL`(오늘 이미 열었다). 저장 없이 알림 1번, 연속 6일 그대로 |
| E-MAIN-21 | `--autostart` | E-MAIN-11의 데이터, 트레이를 단 채 | 없음 | 부른 차례 Cmd+Q, 트레이, 메인 창, 타이머. 저장 없음, 5일, `lastOpenDay` 11-15 그대로. 그 뒤 트레이의 열기면 앞으로, 저장, 6일(T-BRF-3) |
| E-MAIN-22 | `--autostart`, 트레이 없음 | E-MAIN-21에서 트레이를 달지 못함 | 없음 | 부른 차례 Cmd+Q, 트레이, 메인 창, 앞으로, 저장, 타이머. 6일(잠정, 07 안에서 정한 것) |
| E-MAIN-23 | 트레이 없는 창 닫기 | E-MAIN-11 뒤 메인 창의 닫기 버튼 | 없음 | 끝내기 0(T-WIN-3) |
| E-MAIN-24 | Cmd+Q | E-MAIN-11 뒤 Cmd+Q | 없음 | 끝내기 0 |
| E-TRAY-2 | 30초 확인의 알림 | 14:50에 확인, 이어서 14:50:30 | 없음 | 운영체제 알림 "플래닝펫", "통계 과제 10분 뒤 시작" 1번. 다음에는 없음(T-NTF-1, T-NTF-6) |
| E-TRAY-3 | 알림 누르기 | 달력에서 12-02를 고르고 일정관리를 보는 중에 E-TRAY-2의 알림, 사건 | 없음 | 창 앞으로 1번, 지금 화면 "홈", 고른 날 11-16, 달 "2026년 11월", 15번(`lastOpenDay` 11-16) |
| E-TRAY-4 | 다음 날 시작 일의 알림 | 11-16 할 일 다음 날 00:30(D-77), 11-17을 고름, 11-17 00:20에 확인, 사건 | 없음 | "10분 뒤 시작" 알림, 고른 날 11-16(할 일의 `date`) |
| E-TRAY-5 | 알림 뒤 두 번째 사건 | E-TRAY-3 뒤 일정관리로 바꾸고 사건 | 없음 | 열기와 같다. 창 앞으로 2번째, 화면 "일정관리" 그대로, 고른 날 11-16 그대로 |
| E-TRAY-6 | 처음 닫기 안내 뒤 사건 | E-TRAY-2의 알림, `notice`, 사건 | 없음 | 둘째 알림 "플래닝펫", "플래닝펫은 트레이에서 계속 알림을 보내요". 사건은 열기와 같아 화면 "일정관리" 그대로 |
| E-TRAY-7 | 트레이 메뉴 | `menu()`의 열기, 끝내기 | 없음 | 이름 차례 열기, 끝내기. 열기는 창 앞으로 1번과 15번, 끝내기는 `setQuit`로 넣은 함수 1번 |
| E-CLOSE-4 | 창 닫기 버튼, 처음 | `close(true, 숨기기, 안내)`, `trayNoticeShown` false | 없음 | 숨기기 1번, 안내 1번, 끝내기 없음(T-WIN-1) |
| E-CLOSE-5 | 창 닫기 버튼, 두 번째 | E-CLOSE-4 뒤 한 번 더 | 없음 | 숨기기 2번째, 안내 그대로 1번(T-WIN-2) |
| E-CLOSE-6 | 창 닫기 버튼, 트레이 없음 | `close(false, 숨기기, 안내)` | 없음 | 끝내기 1번, 숨기기와 안내 없음(T-WIN-3) |

## 3. 코드로만 확인한 것

창이나 운영체제의 트레이를 띄워야 해서 검사하지 않고 코드를 읽어 확인한 줄이다. 이 줄은 받은 함수를 부르기만 한다.

- `Tray.install`: `SystemTray.isSupported()`, `menu()`의 차례대로 `MenuItem`을 단 `PopupMenu`, `AppWindow.icon()`의 `TrayIcon`, 아이콘의 사건을 알림 누르기에 잇기, `SystemTray.add`, 알림을 `TrayIcon.displayMessage`로 바꾸기.
- `AppWindow.frame`: `DO_NOTHING_ON_CLOSE`와 닫기 사건에 받은 함수 잇기.
- `Main`의 `Swing`: `tray`는 `install`, `front`는 내려 둔 창을 펴서 보이고 앞으로, `hide`는 창 숨기기, `quitKey`는 `Desktop.setQuitHandler`, `timer`는 `Tray.start`.
