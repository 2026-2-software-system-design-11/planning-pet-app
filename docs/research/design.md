# 디자인 방향 조사 (2026-11-14)

화면의 색, 글꼴, 펫 그림, 움직임을 정하기 전에 근거를 모은다. 질문은 네 가지다.

1. 대상인 한국 대학생은 지금 무엇으로 일정을 관리하고, 어떤 화면 언어에 익숙한가
2. 캐릭터가 중심인 앱과 데스크톱 동반자 게임은 캐릭터와 보상을 어떻게 설계했나
3. 템플릿처럼 보이지 않는 한국어 화면은 무엇으로 만들어지나
4. Swing으로 그것을 어디까지 할 수 있나

표기: [확인]은 출처 본문을 읽은 것, [발췌]는 검색 요약으로만 본 것, [추측]은 해석이다. 웹 검색으로 조사했고 사용자 인터뷰는 아직 하지 않았다.

## 1. 대학생이 이미 쓰는 것

| 도구 | 사실 | 우리에게 주는 뜻 |
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
- 우리 앱의 체크 해제 시 보상 되돌리기(D-02)는 Habitica와 같은 방식이다. 사용자 반응을 다룬 연구는 못 찾았다.

### 데스크톱 동반자 장르

- Rusty's Retirement는 화면 아래에 자리 잡도록 설계된 방치형 게임으로 약 33만 장 팔렸다. 개발자는 "다른 일을 봐도 나쁜 일이 안 생기고, 돌아오면 진행돼 있다"는 점을 장르의 매력으로 꼽았다 [확인](https://newsletter.gamediscover.co/p/how-rustys-retirement-idle-farmed).
- Bongo Cat은 키를 누를 때마다 고양이가 작업 표시줄을 두드린다. 사용자 행동에 반응하는 것이 핵심이다 [확인](https://store.steampowered.com/app/3419430/Bongo_Cat/).
- Desktop Goose는 반대 사례다. 커서를 끌고 다니며 일을 방해하는 장난감이다 [확인](https://kotaku.com/untitled-goose-fan-game-unleashes-havoc-on-your-desktop-1841351806).
- 기획 노트의 메인 아래 띠(성취도 막대 위를 걷는 고양이)는 이 장르와 같은 자리다 [추측].

### 적게 그리고 살아 보이게

- 가장 흔한 대기 동작은 통통 튀는 숨쉬기이고, 2~4장에 한 장당 1~2픽셀만 바꾼다. 걷기는 4~6장이면 된다 [확인](https://slynyrd.com/blog/2018/8/19/pixelblog-8-intro-to-animation).
- 깜박임은 정해진 주기면 기계적으로 보이므로 난수 간격으로, 가끔 두세 번 연달아 깜박이게 한다 [발췌](https://www.indiedb.com/games/a-little-less-desperation/tutorials/dynamic-character-blink-on-idle).

