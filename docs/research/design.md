# 디자인 방향 조사 (2026-11-14)

화면의 색, 글꼴, 펫 그림, 움직임을 정하기 전에 근거를 모은다. 질문은 네 가지다.

1. 대상인 한국 대학생은 지금 무엇으로 일정을 관리하고, 어떤 화면 언어에 익숙한가
2. 캐릭터가 중심인 앱과 데스크톱 동반자 게임은 캐릭터와 보상을 어떻게 설계했나
3. 템플릿처럼 보이지 않는 한국어 화면은 무엇으로 만들어지나
4. Swing으로 그것을 어디까지 할 수 있나

표기: [확인]은 출처 본문을 읽은 것, [발췌]는 검색 요약으로만 본 것, [추측]은 해석이다. 웹 검색으로 조사했고 사용자 인터뷰는 아직 하지 않았다.

## 1. 대학생이 이미 쓰는 것

| 도구 | 사실 | 플래닝펫에 주는 뜻 |
|---|---|---|
| 에브리타임 시간표 | 대학생 가입자 583만, 올라온 시간표 3292만 개. 시험 기간에 사용 시간이 3배가 된다 [확인](https://isplus.com/article/view/isp202204070263). 2021년 설문에서 학사관리 앱으로 94.4%가 골랐다 [확인, 수치 앞뒤가 맞지 않는 기사](https://www.venturesquare.net/824236/) | 요일과 시간 격자 위 색 블록은 대상이 매일 보는 화면 언어다 [추측]. 일정관리 시간표는 이 언어를 따른다 |
| 투두메이트 | 출시 후 십수 일 만에 가입자 27만 [발췌](https://www.jobplanet.co.kr/contents/news-1899). iOS 평점 4.8, 평가 11만 개, Windows와 macOS 앱이 있다 [확인](https://apps.apple.com/kr/app/id1505220130). 인기 이유 첫째는 친구에게 칭찬받는 동기부여 [확인](https://www.careet.net/433) | 혼자 쓰는 데스크톱 앱에서는 펫이 칭찬하는 친구 자리를 맡을 수 있다 [추측] |
| 투두메이트 리뷰 | 칭찬은 깔끔함, 카테고리 색, 귀여움. 불만은 상단이 점점 복잡해짐, macOS판에서 달력이 목록 아래로 내려가 한눈에 안 보임 [확인](https://apps.apple.com/kr/app/id1567807901) | 메인에 달력과 오늘 할 일을 나란히 두는 기획 노트의 배치가 맞다. 기능이 늘어도 첫 화면은 단순하게 둔다 |
| 열품타 | 공부량이 많을수록 진해지는 달력. 2025년 8월 홈 개편 뒤 "핵심 숫자를 크게 보던 예전 화면이 좋았다"는 불만이 몰려 예전 화면을 고를 수 있게 했다 [확인](https://apps.apple.com/kr/app/id1441909643) | 사용자가 매일 보는 숫자(오늘 남은 할 일)는 크게, 같은 자리에 둔다 |
| 노션 | 한국 사용자 10명 중 1명이 학생 계정, 개강 시즌 학교 템플릿 6만 4000건 [확인](https://v.daum.net/v/3tBn4X71ha). 커버와 이모지로 꾸미는 문화 | 꾸미기 욕구가 있다. pet 방 꾸미기가 이 욕구를 받는다 |

대학생이 언제 어떤 기기로 일정을 보는지 잰 자료는 없어서, 가까운 자료로 6절에서 추정했다.

## 2. 캐릭터와 보상

### 캐릭터 그림

- Duolingo는 벡터 도형을 아주 적게 쓰고, 실루엣만으로 읽히게 하고, 핵심 특징을 캐리커처 바로 앞까지 과장한다 [확인](https://blog.duolingo.com/shape-language-duolingos-art-style/). 2013년 재디자인에서는 실루엣을 "가장 중요한 부분"이라고 했다 [확인](https://blog.duolingo.com/reshaping-duo/).
- 공식 가이드는 둥근 모서리, 캐릭터 하나에 도형 약 15개(6개는 너무 추상, 30개는 너무 많음), 같은 도형 반복, 평면 원근을 말한다 [발췌](https://design.duolingo.com/illustration/characters).
- Neko Atsume는 단순한 그림에 작은 디테일을 더해 고양이마다 개성을 준다. 상호작용은 거의 보는 것뿐이고 일하는 사이의 휴식으로 소개된다 [확인](https://nippon.com/en/nipponblog/m00083).

### 처벌을 대하는 태도

| 앱 | 방식 | 근거 |
|---|---|---|
| Tamagotchi | 방치하면 죽는다. 책임감을 느끼게 하려는 의도 | [확인, 2차 보도](https://www.mentalfloss.com/article/642373/tamagotchi-history) |
| Forest | 딴짓하면 나무가 죽고 숲에 남는다. "판결이 아니라 정직한 기록" | [확인](https://forestapp.cc/llms-full.txt) |
| Habitica | 일일 과제를 놓치면 HP가 깎이고 죽으면 레벨과 장비를 잃는다. 체크를 풀면 받은 경험치와 골드를 되돌린다 | [발췌, 위키](https://habitica.fandom.com/wiki/Tasks) |
| Finch | 펫은 죽지 않는다. "불안을 너무 키울 수 있어서" | [발췌, 공식 FAQ](https://befinch.notion.site/Finch-FAQ-474652d0123d4883ac7a0cd6c8f5aa70), [확인](https://www.engadget.com/apps/this-self-care-virtual-pet-is-helping-me-get-my-act-together-160027169.html) |

- 연속 기록 연구(Silverman과 Barasch, 2023, 실험 7개): 기록이 끊겨 보이면 이후 참여가 줄고, 복구할 수 있으면 그 효과가 약해진다 [확인](https://www.colorado.edu/business/faculty-research/2023/04/19/or-track-how-broken-streaks-affect-consumer-decisions).
- Duolingo는 손실 회피를 쓴다고 밝히면서도 "기록을 잃을 두려움이 시작 자체를 막을 수 있다"고 인정했고, 목표에 약간의 여유를 주는 편이 엄격한 규칙보다 동기를 높인다는 연구를 인용한다 [확인](https://blog.duolingo.com/how-duolingo-streak-builds-habit).
- 플래닝펫의 체크 해제 시 보상 되돌리기(D-02)는 Habitica와 같은 방식이다. 사용자 반응을 다룬 연구는 못 찾았다.

### 데스크톱 동반자 장르

- Rusty's Retirement는 화면 아래에 자리 잡도록 설계된 방치형 게임으로 약 33만 장 팔렸다. 개발자는 "다른 일을 봐도 나쁜 일이 안 생기고, 돌아오면 진행돼 있다"는 점을 장르의 매력으로 꼽았다 [확인](https://newsletter.gamediscover.co/p/how-rustys-retirement-idle-farmed).
- Bongo Cat은 키를 누를 때마다 고양이가 작업 표시줄을 두드린다. 사용자 행동에 반응하는 것이 핵심이다 [확인](https://store.steampowered.com/app/3419430/Bongo_Cat/).
- Desktop Goose는 반대 사례다. 커서를 끌고 다니며 일을 방해하는 장난감이다 [확인](https://kotaku.com/untitled-goose-fan-game-unleashes-havoc-on-your-desktop-1841351806).
- 기획 노트의 메인 아래 띠(성취도 막대 위를 걷는 고양이)는 이 장르와 같은 자리다 [추측].

### 적게 그리고 살아 보이게

- 가장 흔한 대기 동작은 통통 튀는 숨쉬기이고, 2~4장에 한 장당 1~2픽셀만 바꾼다. 걷기는 4~6장이면 된다 [확인](https://slynyrd.com/blog/2018/8/19/pixelblog-8-intro-to-animation).
- 깜박임은 정해진 주기면 기계적으로 보이므로 난수 간격으로, 가끔 두세 번 연달아 깜박이게 한다 [발췌](https://www.indiedb.com/games/a-little-less-desperation/tutorials/dynamic-character-blink-on-idle).

## 3. 템플릿처럼 보이지 않는 화면

### AI가 만든 듯한 화면의 특징

디자이너들의 글에서 겹치는 항목이다. 모두 의견 글이다.

- Inter나 시스템 글꼴을 그대로 쓴다
- 보라에서 파랑으로 가는 그라데이션, 남보라 강조색
- 아이콘이 붙은 같은 크기의 둥근 카드 반복, 모든 곳의 옅은 그림자
- 모든 요소가 같은 무게라 위계가 없다
- 데이터가 찬 화면만 있고 빈 상태, 오류, 로딩이 없다
- 아무것도 안 움직이거나 모두 같은 페이드인
- 어느 제품에나 붙일 수 있는 문구

출처: [Kosta Canatselis](https://world.hey.com/kostac/spot-the-slop-a-ui-designer-s-guide-to-fixing-ai-defaults-4c448c9c), [prg.sh](https://prg.sh/ramblings/Why-Your-AI-Keeps-Building-the-Same-Purple-Gradient-Website), [GeekyAnts](https://geekyants.com/blog/why-everything-your-ai-builds-looks-the-same)

### 잘 만든 쪽이 하는 것

- **글꼴**: Pretendard는 SIL OFL이라 앱에 넣어 배포할 수 있다(고지 파일 포함) [확인](https://github.com/orioncactus/pretendard). 당근 SEED는 Windows에서 맑은 고딕 대신 Pretendard를 지정하고 굵기는 400, 500, 700만 쓴다 [확인](https://seed-design.io/llms/foundations/typography.txt).
- **글자 단계**: 토스 TDS는 크기와 줄 높이를 짝지은 토큰이다. 본문은 줄 높이 1.5배(17/25.5), 제목은 1.33배 안팎. 13 크기를 "읽지 않아도 되는 글"로 정의한다 [확인](https://tossmini-docs.toss.im/tds-mobile/foundation/typography/).
- **색**: SEED는 값이 아닌 역할로 색을 정한다. 브랜드 색은 하나이고 "화면에서 가장 중요한 행동"에만 쓴다. 배경은 세 층이고, 계층은 배경 밝기 차이로 만들며 그림자는 주목도가 높은 소수 요소에만 쓴다 [확인](https://seed-design.io/llms/foundations/color/color-role.txt), [확인](https://seed-design.io/llms/foundations/elevation.txt). 토스의 가장 진한 글자색은 순흑이 아닌 #191f28이다 [확인](https://tossmini-docs.toss.im/tds-mobile/foundation/colors/).
- **간격**: SEED는 4px 단위다 [확인](https://seed-design.io/llms/foundations/spacing.txt).
- **내용 우선**: Things 3는 사용자의 내용을 가장 눈에 띄는 자리에 두고 장식을 줄인다. 애니메이션은 써 보다가 금방 질리면 뺀다 [확인](https://sspai.com/post/39724).
- **대비**: WCAG 2.2 AA는 일반 글자 4.5:1, 큰 글자 3:1, 버튼 경계와 아이콘 3:1이다 [확인](https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum.html).

## 4. Swing으로 할 수 있는 것

- **FlatLaf 3.8** (Apache-2.0): 테마 클래스 하나와 properties 파일로 색, 모서리 둥글기, 앱 고유 색 키를 한곳에서 관리한다. 변수와 `darken`, `lighten` 함수, 운영체제별 접두어를 지원한다 [확인](https://www.formdev.com/flatlaf/how-to-customize/), [확인](https://www.formdev.com/flatlaf/properties-files/).
- **창 제목 표시줄**: macOS는 투명 제목 표시줄과 full window content로 내용을 제목 표시줄까지 올릴 수 있다(신호등 버튼 자리 약 70px 비움). Windows 10, 11도 FlatLaf 창 장식으로 제목 표시줄 색과 높이를 바꾸고 내용을 올릴 수 있다 [확인](https://www.formdev.com/flatlaf/macos/), [확인](https://www.formdev.com/flatlaf/window-decorations/).
- **글꼴**: Pretendard 정적 굵기 파일을 JAR에 넣고 `Font.createFont`로 읽는다. 가변 글꼴은 AWT가 굵기 설정을 다루지 못해 피한다 [발췌](https://bugs.openjdk.org/browse/JDK-8378603). FlatLaf `defaultFont` 설정으로 앱 전체에 적용한다 [확인](https://www.formdev.com/flatlaf/typography/).
- **아이콘**: `FlatSVGIcon`(JSVG 렌더러)으로 SVG를 배율에 맞게 그리고 색을 바꾼다 [확인](https://github.com/JFormDesigner/FlatLaf/tree/main/flatlaf-extras). Lucide(ISC), Tabler(MIT), Phosphor(MIT)는 상업 사용이 되고 고지 파일을 넣는다.
- **그림 에셋**: Kenney 에셋은 전부 CC0이다 [확인](https://kenney.nl/support). OpenGameArt에 CC0 고양이 스프라이트가 있다 [발췌](https://opengameart.org/content/cute-kitty-animated-set). 고양이 방 꾸미기 전용 세트는 못 찾았다. itch.io 에셋은 제작자마다 라이선스가 다르다.
- **움직임**: FlatLaf `Animator`와 `CubicBezierEasing`으로 경과 시간 기준의 이징을 쓴다 [확인](https://github.com/JFormDesigner/FlatLaf/blob/main/flatlaf-core/src/main/java/com/formdev/flatlaf/util/Animator.java). `javax.swing.Timer`는 밀린 이벤트를 합치므로 프레임 수로 위치를 계산하면 안 된다 [확인](https://docs.oracle.com/en/java/javase/21/docs/api/java.desktop/javax/swing/Timer.html).
- **HiDPI**: Java 9부터 Windows도 자동 배율이다 [확인](https://openjdk.org/jeps/263). 흐려지는 주 원인은 논리 크기로 만든 이미지를 늘려 그리는 것이므로, 그림은 SVG나 2배 이미지로 둔다. 픽셀 아트는 150% 배율에서 고르지 않게 보인다 [추측].

## 5. 종합

### 방향을 정하는 사실

1. 대상은 이미 에브리타임의 색 블록 시간표와 투두메이트의 단순한 색 목록에 익숙하다. 새 화면 언어를 가르칠 필요가 없다.
2. 투두메이트의 동기는 친구의 칭찬이었다. 플래닝펫에서 그 자리는 펫이다.
3. 캐릭터의 품질은 실루엣과 적은 도형에서 나오고, 살아 보이는 것은 숨쉬기와 난수 깜박임 정도로 충분하다.
4. 처벌하는 펫은 불안을 만든다는 근거가 처벌의 효과보다 많다. 기획의 "다마고치형"(Q-09)은 돌보지 않으면 나빠지는 것까지 가져오지 않는 편이 맞다.
5. 메인 아래 띠는 데스크톱 동반자 장르와 같은 자리다. 이 장르가 사랑받는 이유(방해하지 않음, 행동에 반응함)를 그대로 따른다.
6. 템플릿 티는 기본값에서 나온다. 글꼴, 색, 위계, 빈 상태, 문구를 모두 이 앱에 맞게 정하면 피할 수 있다.

### 금지 목록 (초안)

- 보라와 파랑 그라데이션, 남보라 강조색
- 같은 크기 카드의 반복, 모든 요소에 그림자
- 이모지를 아이콘 대신 쓰기
- 시스템 기본 글꼴, 순흑 글자
- 빈 상태와 오류 상태 없이 꽉 찬 시연 화면만 그리기
- 어느 앱에나 붙는 문구("생산성을 높이세요")
- 펫이 슬퍼하거나 아프거나 죽는 연출

### 확인해야 할 것

- 대학생이 아침에 노트북에서 일정을 보는지: 팀원과 친구 5명 정도에게 묻는다.
- 펫 그림을 도형으로 그릴지, CC0 에셋을 쓸지: 시안을 둘 다 만들어 비교한다(Q-28).
- jpackage 설치본이 Windows 150% 배율에서 흐리지 않은지: 설치 파일을 만든 뒤 시험한다.

## 6. 사용자 여정 추정

하루와 한 해 중 언제 어떤 기기로 쓸지 추정한다. 대학생만 따로 잰 자료가 없어서 가까운 자료로 추정한다.

### 쓴 자료

| 자료 | 숫자 | 출처 |
|---|---|---|
| 20대 업무 외 인터넷 일평균 이용 시간 (2025년 1월, 15~69세 2,000명) | 모바일 평일 4시간 52분, 주말 6시간 1분. PC(PC 사용자 기준) 평일 2시간 30분, 주말 3시간 4분 | [나스미디어 2025 인터넷 이용자 조사](https://www.nasmedia.co.kr/wp-content/uploads/2025/05/2025_NPR_%EC%9A%94%EC%95%BD%EB%B3%B4%EA%B3%A0%EC%84%9C_f.pdf) |
| 에브리타임 시간대별 활성 사용자 (2019년 6월) | 아침에는 매우 낮고 점심 무렵 가장 높으며 이후 큰 변동 없음. 사용자 대부분이 월 20일 이상 실행 | [App Ape](https://www.mobiinside.co.kr/2019/08/08/app-ape-everytime/) |
| 에브리타임 계절 | 개강 직전 2월 넷째 주부터 3월 첫째 주가 가장 활발(2024). 방학인 1월과 7월에 활성 사용자 감소(2019). 시험 기간 사용 시간 3배(2022) | [서울경제](https://www.sedaily.com/NewsView/2DIC1P7XQO), [App Ape](https://www.mobiinside.co.kr/2019/08/08/app-ape-everytime/), [일간스포츠](https://isplus.com/article/view/isp202204070263) |
| 대학생 기기 | 노트북 보유 86.1%(2023년 말, 1,000명). 노트북은 필기, 과제, 강의자료에 쓴다(100명) | [시빅뉴스](https://www.civicnews.com/news/articleView.html?idxno=38742) |
| 공부 시작 | 책상에 앉으면 먼저 휴대전화로 열품타를 켠다(학생 인터뷰) | [서울신문](https://m.seoul.co.kr/news/2021/04/19/20210419016003) |

20대 모바일 시간은 보고서 본문에 적힌 값이다. 20대 PC 시간은 성별과 연령별 막대그래프에서 읽은 값이다(본문은 여성과 40대 값만 적었다). 그래프의 열 순서는 전체, 남, 여, 10대, 20대, 30대, 40대, 50대이고, 본문에 적힌 여성과 40대 값으로 열 위치를 맞춰 보았다.

### 추정한 하루와 한 해

| 때 | 기기 | 하는 일 | 근거 |
|---|---|---|---|
| 일어나서 오전 | 폰 | 오늘 할 일을 짧게 본다. 앱을 여는 사람은 아직 적다 | 학교생활 앱은 아침 사용이 매우 낮다 |
| 점심 무렵과 수업 사이 | 폰 | 하루 중 가장 많이 연다. 끝낸 일을 체크한다 | 에브리타임 점심 무렵이 가장 높다 |
| 저녁 과제와 공부 | 노트북 | 2시간 넘게 켜 두고 할 일을 하나씩 끝낸다 | 20대 PC 평일 2시간 30분, 노트북 용도가 과제와 필기 |
| 주말 | 노트북 | 다음 주를 계획한다 | 20대 PC 주말 3시간 4분으로 평일보다 길다 |
| 개강 첫 2주 | 노트북 | 설치하고 수업 시간표를 넣는다 | 개강 직전이 일 년 중 가장 활발하다 |
| 시험 기간 | 둘 다 | 할 일과 마감이 몰려 가장 많이 쓴다 | 에브리타임 사용 시간 3배 |
| 방학 | 거의 안 씀 | 몇 주씩 열지 않는다 | 1월, 7월 활성 사용자 감소 |

### 설계에 주는 뜻

1. **아침 브리핑은 "그날 처음 열 때"로 바꾼다.** 아침에 여는 사람이 적으므로 시각이 아니라 그날 첫 실행에 오늘 할 일을 말한다. 폰 앱이 생기면 브리핑은 폰 알림이 맡는다.
2. **데스크톱 앱의 본 무대는 저녁 공부 시간이다.** 2시간 넘게 켜 두는 동안 방해하지 않고 옆에 있어야 한다. 메인 아래 고양이 띠가 이 시간을 위한 자리다.
3. **폰은 오늘 체크리스트만으로 충분하다.** 폰은 하루에 여러 번 짧게 연다. 다만 체크하는 순간 고양이의 짧은 반응을 보여 주고, 노트북을 열면 폰에서 끝낸 만큼 고양이가 걸어가 있게 한다.
4. **첫 시간표 입력이 가장 큰 관문이다.** 설치가 개강 때 몰리고 그때 할 일은 수업 시간표 넣기다. 시간표를 손으로 하나씩 넣게 하면 여기서 떠난다. URL 가져오기(Q-01)의 첫 대상으로 수업 시간표를 검토한다.
5. **시험 기간 화면을 따로 그린다.** 하루 할 일이 열 개를 넘고 마감이 몰린 상태에서도 읽혀야 한다.
6. **몇 주 안 열어도 괜찮아야 한다.** 방학 뒤에 돌아왔을 때 펫이 아프거나 연속 기록이 깨졌다고 혼내지 않고 반겨 준다.

이 표는 추정이다. 같은 과 학생 5~10명에게 다섯 가지(일정을 처음 보는 때와 기기, 노트북을 켜는 때와 시간, 체크하는 기기, 공부할 때 노트북에 켜 두는 것, 계획을 세우는 기기)를 물어 맞는지 확인한다.
