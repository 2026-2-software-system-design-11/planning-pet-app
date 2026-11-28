# 단위 S4. 일정 입력 창 TaskDialog

[본 구현으로 옮기기](../porting.md) 1절 표의 단위 S4(일정 입력 창)다. 07의 `TaskDialog`와 `TaskController`([class-schedule-rules](../diagrams/class-schedule-rules.puml))의 `add`, `update`, `delete`, 고르는 칸 `ComboUI`와 그것을 거는 `Theme.install`의 줄([theme.md](theme.md))을 옮긴다. 기준은 05 "일정 입력 창"과 "그 밖의 창과 알림", 06 2~5번과 14번, 22번 T-SAVE-3, 07 [seq-uc03](../diagrams/seq-uc03.puml), [seq-uc05](../diagrams/seq-uc05.puml), [seq-uc06](../diagrams/seq-uc06.puml), 프로토타입 `schedule/view/TaskDialog`(102줄), `TaskForm`(501줄), `schedule/controller/TaskController`(328줄 가운데 S4 몫), `common/view/ComboUI`(66줄)이다.

프로토타입 997줄과 06 검사 줄 30개 남짓, 새 경계 줄 60개 남짓을 테스트까지 옮기면 PR 한도의 일곱 배가 넘어 열셋으로 나눈다. 기대는 몫이 다 끝난 것 가운데 작은 것부터 놓았다.

| 몫 | 이슈 | 옮기는 것 |
|---|---|---|
| S4a | #504 | 이 기록 파일, 물음 창 셋(겹침, 반복 사본 저장 범위, 삭제 확인)의 내용과 띄워 답을 받는 함수 |
| S4b | #505 | 입력 창 도우미 `Ui`의 `flow`, `minutes`, `dayEditor`, `dateOf`, `setDate`와 시작 칸 값과 글 `Times` |
| S4c | #506 | 고르는 칸: `Icons`의 `DOWN`, `ComboUI`, `Theme.install`의 고르는 칸 줄 |
| S4d | #507 | 일정 넣기 controller `TaskController.add` |
| S4e | #508 | 고치기와 지우기 controller `TaskController.update`, `delete` |
| S4f | #509 | 입력 창 칸과 추가 창 처음 값, `draft()` |
| S4g | #510 | 칸끼리 켜고 끄기와 끝 시각 글 |
| S4h | #511 | 손댄 값 지키기와 날짜 따라가기(D-101) |
| S4i | #512 | 칸 아래 오류 글(`Ui.wrap`, D-43, 마감 칸 바로 검사) |
| S4j | #513 | 수정 창 처음 값과 반복 사본(`TaskBook.seriesEnd`) |
| S4k | #514 | 수정 창 잠금, 정해 둔 날, 끝낸 일, 앱 시계 |
| S4l | #515 | 창 몸과 저장, 삭제 단추 |
| S4m | #516 | 홈에 잇기와 창 자리 |

이 단위가 옮기지 않는 것:

- 칸에 쳐 넣은 글 읽기(`Typed`, `TypedField`, 칸 아래 흐린 글과 빨간 글, 친 채 저장 막기, "밤 4시"의 날짜 옮기기, 쳐 넣은 뒤 30분 안내 숨기기)는 C1이다. 시작 칸과 걸리는 시간 칸은 그때까지 Swing 기본 편집기다.
- 홈 빠른 추가(프로토타입 `TaskController.quickAdd`)는 C4다. C4가 S4의 `add(draft, ask)`를 부른다.
- 일정관리에서 여는 입력 창(`fromHome`, `sayAtHome`, 일정관리의 "일정 추가"), 시각 고치기 창 `TimeEditor`, `clearTime`, `editTime`은 S5다. S4의 입력 창은 홈에서만 열려 넣은 말은 늘 홈 말풍선이다.
- 끌어 놓기(`drop`, `blocked`, `keep`)는 C2, 목록 누르기(`listClickTime`)는 C3, 예시 지우기(`samples`, `clearSamples`)는 C5, 자동 배치 결과 창(`applyPlacement`, `takePlaced`)은 S6이다.
- 체크(`TaskController.check`)는 S3a의 `RewardController.complete`, `uncomplete`가 이미 했다.

## S4a. 물음 창 셋

일정 입력 창과 목록 줄의 휴지통이 묻는 창 셋(05 그 밖의 창과 알림)을 `TaskDialog`에 둔다. 겹침(D-87), 반복 사본 저장 범위(D-42), 삭제 확인(D-41)이다. 06 줄은 T-OVL-1의 물음 글과 단추, 처음 단추다. T-OVL-3, T-OVL-5의 글은 M5 `Overlap`이 이미 확인했다([task-lists.md](task-lists.md)).

07 안에서 정한 것과 그 까닭:

- **물음 창은 view인 `TaskDialog`에 둔다.** 프로토타입은 `TaskController.chooser`가 `JOptionPane.showOptionDialog`를 부른다. 07은 controller가 창을 모르게 하고 겹침과 범위를 `ask`, `chooseScope` 인자로 받는다(seq-uc03, seq-uc05). 지우기는 범위를 받는 `delete(task, scope)`라 확인은 부르는 쪽이 먼저 한다(seq-uc06).
- **창 내용과 띄우기를 나눈다.** `java.awt.headless=true`에서는 창을 만들 수 없어 내용은 `JOptionPane`을 돌려주는 패키지 안 함수(`overlapPane`, `scopePane`, `deletePane`)로 떼어 창 없이 본다. 07 `UnreadableDialog.pane`, [pick.md](pick.md)의 `AppWindow.savePane`과 같은 꼴이다. 띄우는 함수(`askOverlap`, `chooseScope`, `confirmDelete`)는 `showOptionDialog`의 몸과 같은 차례로 창을 만들고 띄우고 닫은 뒤 고른 단추의 번호를 찾는다(`answer`, 닫으면 -1). 창이 뜨는 자리, 단추 누르기, 처음 단추의 키보드 초점은 창 없이 넣을 수 없음, 코드로만 확인했다(Swing 기본 그대로).
- **답은 07의 값으로 바꾼다.** 겹침은 넣기면 참, 범위는 `EditScope`, 삭제는 `DeleteScope`이고 취소와 창 닫기는 null이다. 반복 사본이 아닌 삭제는 `ONLY_THIS`다(`DeleteScope` 주석).
- **부모 창은 부르는 쪽이 넘긴다.** 프로토타입은 셋 다 앱 창(`app.root`)을 부모로 띄운다. 모달 입력 창 위에 뜨는지는 운영체제마다 화면에서 볼 일이라, 무엇을 넘길지는 창을 여는 S4l, S4m이 정한다.
- **창 제목 "반복 일정 저장", "일정 삭제"와 처음 단추 취소는 프로토타입대로다.** 05는 겹침 창의 제목과 처음 단추(넣기)만 적었다. 물음 글은 D-42의 "매주 반복 일정이에요"가 아니라 나중에 쓴 05의 "반복 일정이에요"다.

### 1. 화면에서 따라 할 단계

창을 여는 단추는 S4l(입력 창의 저장, 삭제)과 S4m(홈 목록의 휴지통)이 잇는다. 그때 보이는 곳은 아래다.

1. 11-16 14:00, 60분 할 일 "자소서 초안"이 있는 날 일정 추가 창에서 "면접 준비" 14:30, 60분을 저장하면 제목 "시간이 겹쳐요", 글 "14:00~15:00 '자소서 초안'과 30분 겹쳐요. 그래도 넣을까요?", 단추 넣기, 취소의 물음 창이 뜨고 넣기가 골라져 있다(06 T-OVL-1).
2. 매주 반복 고정 일정 "자료구조"의 사본을 일정 수정 창에서 고쳐 저장하면 제목 "반복 일정 저장", 글 "'자료구조'는 반복 일정이에요. 어디까지 바꿀까요?", 단추 이 일정만, 이후 반복 모두, 취소의 창이 뜨고 취소가 골라져 있다.
3. 반복이 아닌 "자소서 초안"의 휴지통을 누르면 제목 "일정 삭제", 글 "'자소서 초안'을 삭제할까요?", 단추 삭제, 취소의 창이 뜨고 취소가 골라져 있다.
4. "자료구조" 사본의 휴지통을 누르면 제목 "일정 삭제", 글 "'자료구조'는 반복 일정이에요.", 단추 이 일정만, 이후 반복 모두, 취소의 창이 뜨고 취소가 골라져 있다.
5. 어느 창이든 제목 줄의 닫기 단추로 닫으면 취소와 같다.

### 2. 입력의 경계와 프로토타입 결과

프로토타입 결과는 develop `a5e14bb3`에서 저장소 밖에 컴파일한 프로토타입(클래스 경로에 `prototype/`)으로 `Main.create(MockData.empty())` 앱을 만들고, `app.schedule.chooser`를 받은 제목, 글, 단추, 처음 단추를 적고 정한 번호를 돌려주는 것으로 바꿔 끼워 얻었다. 겹침은 `save(null, draft)`, 범위는 반복 사본 4개 가운데 둘째의 `save(사본, draft)`, 삭제는 `delete`로 불렀다. 날짜는 11-20이다. 처음 단추는 바꿔 끼우기 전 `chooser`의 코드에서 읽었다(겹침은 넘긴 0, 나머지는 마지막 단추). 결과는 `TaskDialogAskTest`가 번호 이름의 테스트로 확인한다.

| 번호 | 입력과 경계 | 넣은 값 | 프로토타입 결과 | 본 구현 |
|---|---|---|---|---|
| T-OVL-1 | 겹침 물음 글과 단추 | "자소서 초안" 14:00, 60분이 있는 날 "면접 준비" 14:30, 60분 | "14:00~15:00 '자소서 초안'과 30분 겹쳐요. 그래도 넣을까요?" 넣기, 취소. 처음은 넣기 | 같음 |
| E-ASK-1 | 겹침 창 | T-OVL-1 | 창 제목 "시간이 겹쳐요", 물음 아이콘(`QUESTION_MESSAGE`), 단추는 넘긴 글 그대로(`DEFAULT_OPTION`). 글은 `Overlap.question()` 그대로다 | 같음 |
| E-ASK-2 | 범위 창의 받침 | 반복 사본 "대학수학", "과제" | "'대학수학'은 반복 일정이에요. 어디까지 바꿀까요?", "'과제'는 반복 일정이에요. 어디까지 바꿀까요?" 이 일정만, 이후 반복 모두, 취소. 처음은 취소. 창 제목 "반복 일정 저장" | 같음 |
| E-ASK-3 | 삭제 확인의 을, 를 | 반복이 아닌 "대학수학", "과제" | "'대학수학'을 삭제할까요?", "'과제'를 삭제할까요?" 삭제, 취소. 처음은 취소. 창 제목 "일정 삭제" | 같음 |
| E-ASK-4 | 반복 삭제 | 반복 사본 "대학수학", "과제" | "'대학수학'은 반복 일정이에요.", "'과제'는 반복 일정이에요." 이 일정만, 이후 반복 모두, 취소. 처음은 취소. 창 제목 "일정 삭제" | 같음 |
| E-ASK-5 | 고른 단추와 답 | 단추 번호 0, 1, 2, 창 닫기 -1 | 겹침은 0만 넣고(저장 참, 일정 2개) 1, 2, -1은 넣지 않는다. 범위는 0이면 이 일정만(바뀐 사본 1/4), 1이면 이후 반복 모두(3/4), 2와 -1은 저장하지 않는다. 반복이 아닌 삭제는 0만 지우고, 반복 삭제는 0이면 하나(남은 3), 1이면 이후 모두(남은 1), 2와 -1은 지우지 않는다 | 같음. 본 구현은 넣기 참, `ONLY_THIS`, `THIS_AND_AFTER`, null로 바꾸고 고치기와 지우기는 S4d, S4e의 controller가 한다. 띄우는 함수는 창 없이 `HeadlessException`으로 창을 만들려 한 것을 본다 |
