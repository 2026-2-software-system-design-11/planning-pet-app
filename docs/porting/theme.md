# 단위 S1b. 디자인 시스템 바탕

[본 구현으로 옮기기](../porting.md) 1절 표의 단위 S1b다. [디자인 시스템](../design-system.md)의 색과 모양 값과 글꼴(`Theme`), Pretendard 글꼴 파일, 카드(`Card`), 둥근 단추(`RoundButtonUI`)를 07이 디자인 시스템 부품을 둔 `planningpet.common.view`에 옮긴다(07 "view 클래스는 이름만 그렸다"). 06에 이 부품의 검사 줄은 없고, 기준은 디자인 시스템 2~5절과 프로토타입 `common/view/Theme`, `Card`, `RoundButtonUI`, `prototype/assets/fonts/`다.

PR 하나의 크기 한도(1절) 때문에 셋으로 나눠 옮긴다. 이 기록(단위 전체의 경계), `Theme`의 색과 모양 값, 글꼴 파일과 라이선스, 글꼴 함수는 #390, `Card`와 `RoundButtonUI`는 #396, `Theme.install`과 `clear`는 #397이다. 글꼴 라이선스(94줄)가 글 파일이라 줄 수에 든다.

정한 것과 그 까닭:

- **글꼴 파일은 `src/main/resources/assets/fonts/`에 둔다.** D-108이 글꼴을 `src/main/resources`에 두고, 프로토타입과 같은 `/assets/fonts/Pretendard-{굵기}.otf` 경로로 읽는다. SIL OFL 1.1은 글꼴을 나눠 줄 때 라이선스를 함께 두게 하므로 `Pretendard-LICENSE.txt`도 같은 폴더에 두어 실행 JAR에 들어간다(프로토타입 JAR도 `assets`를 통째로 넣는다).
- **글꼴을 읽는 함수에 검사용 입구를 둔다.** `load(굵기)`는 프로토타입처럼 경로를 만들어 `load(경로, 굵기)`를 부른다. 검사가 없는 파일과 깨진 파일을 넣어 보려고 경로를 받는 입구를 패키지 안에 연다([build.md](build.md)의 `Main.dataFolder`와 같은 꼴).
- **`Theme.install`의 고르는 칸 줄(`ComboBoxUI`에 `ComboUI`를 거는 줄과 그 색 다섯 줄)은 S4가 옮긴다.** `ComboUI`는 S1c의 `Icons.DOWN`을 쓰고, 고르는 칸은 일정 입력 창(S4)에만 있다. 그래서 #397의 `install`이 끝나도 `JComboBox`는 Swing 기본 모양이다.
- **`install`과 `clear`를 부르는 곳은 이 단위에서 잇지 않는다.** 프로토타입은 `Main`이 부품을 만들기 전에 `install`을, 창을 다시 그린 뒤 `clear`를 부른다. 창은 S1d(#392)가, `Main` 잇기는 S2가 한다(porting.md 1절 "창 틀은 부품 다음이다").

## 1. 화면에서 따라 할 단계

이 단위는 부품만 옮겨서, 창에 보이는 것은 창 틀(S1d)부터다. 그때 보이는 곳은 아래다.

1. 앱을 열면 글자가 모두 Pretendard다. 화면 제목은 아주 굵은 26, 메뉴는 굵은 17, 본문은 중간 굵기 14이고 글자는 진남색(`INK`)이다(디자인 시스템 3절).
2. 홈의 달력과 할 일 목록은 흰 카드 안에 있다. 카드는 모서리가 둥글고 아래로 옅은 그림자가 퍼진다(디자인 시스템 5절 카드).
3. 화면 머리 오른쪽 카드의 "출석 +20P"는 민트로 채운 알약에 흰 글이다. 마우스를 올리면 위쪽이 밝아지고, 누르면 진한 민트가 된다. 출석하면 "출석 완료"로 흐려진다(5절 주 행동 버튼).
4. 흰 단추(예: 일정 입력 창의 "취소")에 마우스를 올리면 아래쪽이 연민트(`MINT_PALE`)로, 테두리가 `MINT_LINE`으로 바뀐다. 누르면 아래쪽이 `MINT_SOFT`다. Tab으로 초점을 옮기면 단추 안쪽에 민트 선이 보인다.
5. 사이드바의 메뉴는 바탕이 없고, 고른 화면만 연민트 알약(`NAV_PILL`)이다. 마우스를 올리면 반투명 흰 알약이 뜬다.
6. 글꼴 파일을 읽지 못하면 맑은 고딕, 그것이 없으면 Apple SD Gothic Neo로 그린다.

## 2. 입력의 경계와 프로토타입 결과

프로토타입 결과는 `prototype/src`를 컴파일한 클래스(develop `a2dc132`, 프로토타입의 마지막 변경 `2ac06b6`, JDK 21.0.11, macOS)를 부르는 버리는 Java 파일에서 얻었다. 클래스 경로에 `prototype/`(그 아래 `assets/fonts/`)를 두고 `java.awt.headless=true`로, 부품을 정한 크기로 두고 같은 크기의 `BufferedImage`에 그렸다. 단추는 프로토타입 `Main`처럼 `install` 뒤에 만들었다. "본 구현" 칸의 "픽셀 같음"은 본 구현을 같은 파일로 그려 두 그림의 픽셀이 모두 같다는 뜻이다. 결과는 `ThemeTest`, `ThemeFontTest`(#390), `CardTest`, `RoundButtonUITest`(#396), `ThemeInstallTest`(#397)가 번호 이름의 테스트로 확인한다. 이 맥에는 맑은 고딕이 없어 글꼴을 못 읽을 때 Apple SD Gothic Neo로 갔다.

| 번호 | 입력과 경계 | 넣은 값 | 프로토타입 결과 | 본 구현 |
|---|---|---|---|---|
| E-THEME-1 | 색 값 | 디자인 시스템 2절 표의 색 이름 57개 | 모두 표의 값과 알파가 같다 | 같음 |
| E-THEME-2 | 모양 값 | 디자인 시스템 4절의 카드 모서리, 버튼 모서리 상한, 그림 자르기, 말풍선, 카드 사이 | `CARD_RADIUS` 18, `BUTTON_RADIUS` 22(상한 44의 반), `IMAGE_RADIUS` 16, `BUBBLE_RADIUS` 16, `GAP` 12. 모서리 값은 `fillRoundRect`의 호 지름으로 쓴다 | 같음 |
| E-FONT-1 | 글꼴 파일을 읽음 | `font(14)`, `bold(14)`, `heavy(14)`, `regular(12)` | Pretendard Medium 14, Pretendard Bold 14, Pretendard ExtraBold 14, Pretendard Regular 12. 굵기는 파일이 정해 스타일은 모두 보통이다 | 같음. "플래닝펫 Planning 0123"을 네 함수로 그린 그림도 픽셀 같음 |
| E-FONT-2 | 굵기가 바뀌는 크기 | `font(19)`, `font(20)` | Pretendard Medium 19, Pretendard ExtraBold 20 | 같음 |
| E-FONT-3 | 글꼴 파일이 없음 | 클래스 경로에 `assets`를 두지 않음 | Apple SD Gothic Neo 12에서 크기만 바꾼 글꼴. `font(19)`, `regular`는 보통, `font(20)`, `bold`, `heavy`는 굵게 | 같음 |
| E-FONT-4 | 글꼴 파일이 깨짐 | 네 글꼴 파일 자리에 라이선스 글 파일 | E-FONT-3과 같다 | 같음 |
| E-FONT-5 | 글꼴에 없는 글자 | "😀", "漢字", "플래닝펫 할 일", "Planning Pet 0123", "…", "★", "♥", "→" | 이모지와 한자만 첫 글자부터 그리지 못한다(`canDisplayUpTo` 0). 나머지는 다 그린다 | 같음 |
| E-CARD-1 | 보통 크기 | `Card(layout)` 200x100, 1000x700 | 흰색에서 `CARD_BOTTOM`으로 가는 판, `CARD_EDGE` 가장자리, 아래로 3까지 퍼지는 그림자. 여백 위 14, 왼쪽 16, 아래 18, 오른쪽 16, 불투명 아님 | #396 |
| E-CARD-2 | 모서리를 준 카드 | `Card(layout, 24)` 200x100 | 모서리 24, 여백 0 | #396 |
| E-CARD-3 | 한 색 판 | `filled(SIDE)` 200x100 | `SIDE`로 칠하고 가장자리는 반투명 흰색 | #396 |
| E-CARD-4 | 크기 0 | 0x0 | 아무것도 그리지 않고 예외 없음 | #396 |
| E-CARD-5 | 그림자보다 작은 크기 | 4x8, 5x9 | 4x8은 판 없이 그림자 조각만, 5x9는 판이 1픽셀이고 그림자 조각이 남는다. 예외 없음 | #396 |
| E-BTN-1 | 기본 | "저장" 160x40 | 흰색에서 `#F5FAFB`로 가는 알약, `CARD_EDGE` 테두리, 옅은 그림자, 굵은 14 `INK` 글. 여백 위아래 9, 좌우 12, 불투명 아님, 알맞은 크기 48x36 | #396 |
| E-BTN-2 | 빈 글 | "" 160x40 | 글 없는 알약. 알맞은 크기 24x18 | #396 |
| E-BTN-3 | 아주 긴 글 | "일정 입력 창에서 아주 긴 제목을 넣어 단추보다 길어진 글" 160x40, "저장" 40x10 | 알맞은 크기 327x36. 160에서는 "일정 입력 창에서 아주 ..."로 줄여 그린다. 40x10에서는 "..."만 남는다 | #396 |
| E-BTN-4 | 마우스 올림 | 기본에 올림 | 아래쪽 `MINT_PALE`, 테두리 `MINT_LINE` | #396 |
| E-BTN-5 | 누름 | 누르고 그 위에 있음 | 아래쪽 `MINT_SOFT`, 테두리 `MINT_LINE`. 올림과 누름이 같이 있으면 누름 | #396 |
| E-BTN-6 | 누른 채 밖으로 끌어냄 | 눌림, 단추 위가 아님 | 기본 모양 | #396 |
| E-BTN-7 | 흐림 | 누를 수 없음, 마우스 올림 | `#EDF3F5` 바탕, `CARD_EDGE` 테두리, 그림자 없음, `INK_DIM` 글. 올림을 그리지 않는다 | #396 |
| E-BTN-8 | 주 행동 | `PRIMARY` "출석 +20P" | `MINT_TOP`에서 `MINT_DEEP`으로 가는 채움, 반투명 흰 안쪽 선, 흰 글 | #396 |
| E-BTN-9 | 주 행동 올림과 누름 | `PRIMARY`에 올림, 누름 | 올림은 위쪽이 `MINT_TOP.brighter()`, 누름은 위아래 모두 `MINT_DEEP` | #396 |
| E-BTN-10 | 주 행동 흐림 | `PRIMARY` "출석 완료", 누를 수 없음 | E-BTN-7과 같은 흐림 모양 | #396 |
| E-BTN-11 | 토글 | `JToggleButton` "월" 눌림, 안 눌림 | 눌림은 E-BTN-8의 민트 채움과 흰 글, 안 눌림은 기본 | #396 |
| E-BTN-12 | 메뉴 | `NAV` "홈" 안 고름, 올림, 고름과 `PRIMARY`, 누를 수 없음 | 안 고름은 바탕 없이 글만, 올림은 반투명 흰 알약, 고름은 `NAV_PILL` 알약에 `INK` 글(`PRIMARY`여도 채우지 않음), 누를 수 없으면 바탕 없이 `INK_DIM` 글 | #396 |
| E-BTN-13 | 흐린 글 표시 | `DIM` "방에서 치우기" | 기본 알약에 `INK_DIM` 글 | #396 |
| E-BTN-14 | 글자색을 따로 줌 | "되돌리기"에 `TASK_TEXT`, 그것에 `PRIMARY`, 누를 수 없음 | `TASK_TEXT` 글, `PRIMARY`면 흰 글, 누를 수 없으면 `INK_DIM` 글 | #396 |
| E-BTN-15 | 키보드 초점 | 기본, 누를 수 없음, `NAV` 고름, `PRIMARY`에 초점 | 기본과 `PRIMARY`는 안쪽에 반투명 민트 선(`#1FB99C`, 알파 110), 누를 수 없음과 `NAV`는 선 없음 | #396 |
| E-BTN-16 | 모서리 상한 | "저장" 160x49, 160x50 | 둘 다 호 지름 44(높이에서 5를 뺀 44와 45, 상한 44) | #396 |
| E-BTN-17 | 크기 0과 아주 작은 크기 | 0x0, 4x5 | 0x0은 아무것도 그리지 않고, 4x5는 그림자 조각만 그린다. 예외 없음 | #396 |
| E-THEME-3 | 기본값 걸기 | `install()` 뒤 새 단추, 토글, 글 | 단추와 토글의 UI가 `RoundButtonUI`, 모든 `.font`가 `font(14)`, 단추와 토글은 `bold(14)`, `.foreground`가 `INK`(`ProgressBar`는 `MINT`), `Panel`, `CheckBox`, `RadioButton`, `OptionPane`의 바탕이 `BG`, `ProgressBar.background`가 `TRACK` | #397 |
| E-THEME-4 | 두 번 걸기 | `install()` 두 번 | 한 번과 같다 | #397 |
| E-THEME-5 | 고르는 칸 | `install()` 뒤 `JComboBox` | `ComboUI`, 바탕 `CARD_TOP` | #397. 위 "정한 것"대로 S4가 옮긴다 |
| E-THEME-6 | 투명하게 하기 | 패널 안 패널, 카드와 그 안 패널, Swing 기본 토글, 둥근 토글, 스크롤 칸, 불투명 글 | 패널, 카드 안 패널, 기본 토글, 스크롤 칸과 그 보이는 칸이 투명해진다. 카드, 둥근 토글, 글은 그대로 | #397 |
| E-THEME-7 | 빈 부품 | `clear(null)` | 아무것도 하지 않는다 | #397 |

## 3. 알게 된 것

- **이모지와 한자는 네모로 그려진다(E-FONT-5).** "가😀나", "가漢字나"를 `JLabel`에 `font(20)`으로 창 없이 그리면 이모지와 한자 자리에 빈 네모가 그려진다(버리는 파일의 그림 `work/impl/i390/glyph/glyph.png`, 저장소 밖). 일정 제목은 이모지를 받으므로([day-rules.md](day-rules.md) E-WORD-17~20) 그런 제목은 홈 목록, 시간표, 말풍선에서 네모로 보인다. 글꼴을 못 읽어 시스템 글꼴로 가면 둘 다 그린다. 이 단위는 옮기는 단위라 프로토타입대로 두고, 어떻게 할지는 열린 선택으로 남겼다(backlog). 정해지면 `Theme`의 글꼴 함수와 그것을 부르는 화면의 단위에서 한다.
