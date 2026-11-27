# 단위 0. 빌드와 빈 앱

[본 구현으로 옮기기](../porting.md) 1절 표의 단위 0이다. 07의 `Main`과 `AppWindow`로 빈 창을 띄우고, 빌드(D-108)와 층 의존 검사(D-109)를 둔다. 프로토타입에서 옮기는 동작이 없어 비교 기준이 없다(2절 6번). 06 검사 줄도 없어 기준은 07 실행 모드와 06 21번의 데이터 폴더 문장이다.

## 1. 화면에서 따라 할 단계

1. JDK 21을 깔고 저장소 맨 위 폴더에서 `./gradlew build`(Windows는 `gradlew.bat build`)를 친다. Gradle을 따로 깔지 않아도 래퍼가 받는다. 마지막 줄에 `BUILD SUCCESSFUL`이 보이고 `build/libs/planning-pet-app.jar`가 생긴다.
2. `java -jar build/libs/planning-pet-app.jar`를 친다. 화면 가운데에 제목 줄이 "플래닝펫 (Planning Pet)"인 빈 창이 뜬다(05 1절). 창은 1000x680보다 작게 줄어들지 않는다.
3. 창의 닫기 단추를 누르면 앱이 끝난다.
4. `java -jar build/libs/planning-pet-app.jar --data-dir`처럼 경로 없이 치면 창이 뜨지 않고 "--data-dir 뒤에 폴더 경로를 넣어 주세요"가 보인 뒤 끝난다.

이 단위가 정하지 않고 뒤 단위에 두는 것:

- 창 닫기는 25번(트레이에 남기)이 S11 단위라, 그때까지는 닫으면 끝난다(프로토타입과 같다).
- 제목 줄의 발자국 아이콘(D-47)은 S1 창 틀 단위에서 단다.
- `--autostart`는 알아보는 인자로 읽기만 하고 아직 하는 일이 없어 창이 뜬다. 창을 띄우지 않으면(07 실행 모드) 트레이가 없는 지금은 앱을 끝낼 길이 없다. S11에서 트레이와 함께 쓴다.
- `--data-dir`로 고른 폴더는 아직 읽거나 만들지 않는다. 저장(M8)과 하나만 실행(S11)이 쓴다.

## 2. 입력의 경계

입력은 실행 인자와, 인자가 없을 때 데이터 폴더를 정하는 운영체제 값(운영체제 이름, `APPDATA`, 홈 폴더)이다. `Main.dataFolder`가 받는다. 06에 이 함수의 검사 줄이 없어 모두 `E-MAIN` 번호다.

| 번호 | 입력과 경계 | 넣은 값 | 프로토타입 결과 | 본 구현 |
|---|---|---|---|---|
| E-MAIN-1 | 인자 없음, macOS | `Mac OS X`, 홈 `/Users/a` | 없음 | |
| E-MAIN-2 | 인자 없음, Windows | `Windows 11`, `APPDATA`=`C:\Users\a\AppData\Roaming` | 없음 | |
| E-MAIN-3 | 인자 없음, Windows인데 `APPDATA`가 없음 | `Windows 11`, `APPDATA` 없음, 홈 `C:\Users\a` | 없음 | |
| E-MAIN-4 | 인자 없음, 그 밖의 운영체제 | `Linux`, 홈 `/home/a` | 없음 | |
| E-MAIN-5 | 데이터 폴더 고르기 | `--data-dir /tmp/demo` | 없음 | |
| E-MAIN-6 | `--data-dir`가 마지막 인자 | `--data-dir` | 없음 | |
| E-MAIN-7 | `--data-dir` 뒤가 다른 인자 | `--data-dir --autostart` | 없음 | |
| E-MAIN-8 | `--data-dir` 뒤가 빈 글 | `--data-dir ""` | 없음 | |
| E-MAIN-9 | 컴퓨터 켤 때 실행 인자만 | `--autostart`, `Linux`, 홈 `/home/a` | 없음 | |
| E-MAIN-10 | 모르는 인자 | `--first-run`, `Linux`, 홈 `/home/a` | 없음 | |

경계마다 정한 결과와 근거:

- E-MAIN-1, 2, 4: 06 21번의 데이터 폴더 문장대로 `~/Library/Application Support/PlanningPet`, `%APPDATA%\PlanningPet`, `~/.planningpet`이다.
- E-MAIN-3: `~/.planningpet`이다. 06 21번의 "그 밖은 `~/.planningpet`"으로 읽는다.
- E-MAIN-5: 그 폴더다(07 실행 모드).
- E-MAIN-6, 7, 8: 표준 오류에 "--data-dir 뒤에 폴더 경로를 넣어 주세요"를 쓰고 창 없이 끝낸다(종료 코드 2). 07 실행 모드는 `--data-dir`를 시연과 손 확인을 실제 데이터와 떼어 놓으려고 둔다. 말없이 운영체제 데이터 폴더로 뜨면 시연 데이터가 실제 데이터에 섞인다.
- E-MAIN-9: 운영체제 데이터 폴더다. `--autostart`는 1절대로 아직 하는 일이 없다.
- E-MAIN-10: 무시하고 운영체제 데이터 폴더로 뜬다. 운영체제나 바로 가기가 붙이는 인자 때문에 앱이 뜨지 않으면 안 된다. 프로토타입의 `Main`도 모르는 인자를 무시한다.

## 3. 층 의존 검사

07 "층과 의존 방향"(D-109)을 `src/main/java`의 소스로 검사한다(`LayerTest`). 주석을 뺀 소스에서 `planningpet.{도메인}.{층}`과 `javax.swing`, `java.awt` 이름을 찾는다. 같은 층 안의 의존은 도메인이 달라도 된다(07).

| 층 | 쓰면 안 되는 것 |
|---|---|
| model | view, controller, store, Swing, AWT |
| controller | view, store |
| store | view, controller |
| view | store |

- 도메인 폴더 바로 아래 파일(진입점 `Main`)은 층 밖이라 검사하지 않는다(07).
- 도메인 안에 네 층이 아닌 폴더가 있으면 어긋남이다. D-109가 service 폴더를 두지 않는다고 정했고, 다른 이름의 폴더로 검사를 비껴가지 못하게 한다.
- 검사가 어긋남을 잡는지는 층마다 어긋난 소스 한 줄을 넣어 본 테스트로 확인한다.
