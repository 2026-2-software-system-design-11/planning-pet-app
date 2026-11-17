# 플래닝펫 프로토타입

[05 화면 설계](../docs/05-ui-design.md)대로 움직이는 Swing 프로토타입이다. 본 구현 전에 화면 흐름과 배치, 펫의 말과 보상이 어떻게 느껴지는지 보려고 만들었다. 본 구현의 코드가 아니다.

![메인](screenshots/1-main.png)

## 실행

JDK 21이 필요하다.

```sh
prototype/run.sh                 # 목 데이터로 연다
prototype/run.sh --first-run     # 데이터 없이 반려동물 고르기부터 연다
prototype/snap.sh                # 창 없이 모든 화면을 screenshots/에 그린다
prototype/check.sh               # 06 검사 표 가운데 프로토타입이 따르는 줄을 확인한다
```

Windows는 `prototype\run.bat`이다.

## 코드 구조

06의 패키지 규칙(D-46)대로 `src/planningpet/` 아래를 도메인별로 나누고, 도메인 안을 `model`, `view`, `controller`로 나눈다.

- `schedule`(일정), `reward`(보상), `pet`(펫), `shop`(상점), `home`(메인 화면), `settings`(설정), `app`(진입점, 창 틀, 앱 데이터 `Store`), `common`(앱 날짜와 글자 규칙, 디자인 시스템의 색과 부품)
- `model`은 Swing과 AWT를 쓰지 않는다. `view`는 model을 읽어 그리고, 바꿀 때는 controller를 부른다(다른 도메인의 것도 된다). controller는 바꾸는 일을 앱 controller의 `change`로 감싸고, `change`가 `Store.changed()`로 한 번 알린다
- 화면 전환과 주 옮기기처럼 model을 바꾸지 않는 다시 그리기는 `App.redraw()`로 한다
- `check.sh`가 model의 Swing import와 view의 알림, 펫 말, Model 값 대입을 찾아 있으면 실패한다
- 진입점은 `app/Main`, 목 데이터는 `app/model/MockData`다. QA 코드와 창 없이 그리는 `Snap`은 `qa/planningpet/qa/`, 단위 검사는 `check/planningpet/check/`에 있다

## 목 데이터와 목 로직

- 시계는 2026-11-14(토) 15:23에 멈춰 있다. 일정과 숫자는 05 와이어프레임과 같다.
- 데이터는 메모리에만 있고 끄면 사라진다.
- 화면에 보이는 결과는 [06 함수 명세](../docs/06-functions.md)를 따른다. 보상과 하루 상한, 완료 취소, 레벨, 출석, 연속 접속, 브리핑과 펫 대사, 자동 배치, 시간표 열 나누기, 상점이 그렇다.

## 빠진 것

- 저장(06의 21번, 22번), 알림(17번), 앱 하나만 실행(24번), 트레이와 컴퓨터 켤 때 실행(25번)
- 시계가 2026-11-14 15:23에 멈춰 있다. 날 바뀜(23번)은 30초 타이머로 확인하지만 시계가 가지 않아 화면에서는 일어나지 않는다. QA는 시계를 옮긴 뒤 확인을 부른다
- 강아지, 책상, 소파 그림. 임시 그림으로 그린다(Q-28). 색, 글꼴, 모양, 나머지 그림은 [디자인 시스템](../docs/design-system.md)을 따른다(D-30). 그림은 `planning-pet`의 `assets/`에서 가져왔다.
- 10분짜리 일정은 시간표에서 글자가 보이게 최소 높이로 그려서, 바로 뒤 일정과 겹쳐 보일 수 있다. 겹친 곳을 누르면 나중에 그린 블록이 잡힌다.
- 05와 다르게 만든 곳: 달력과 상점 표는 `JTable` 대신 `JLabel` 칸으로 그렸다. 창 없이 그린 이미지로 확인하기 쉽게 하려는 것이다.

## 화면

| 화면 | 그림 |
|---|---|
| 일정관리 | ![일정관리](screenshots/2-schedule.png) |
| 일정 입력 창 | ![일정 입력](screenshots/3-form.png) |
| 자동 배치 결과 창 | ![자동 배치](screenshots/4-autoplace.png) |
| pet | ![pet](screenshots/5-pet.png) |
| 상점 창 | ![상점](screenshots/6-shop.png) |
| 반려동물 고르기 창 | ![고르기](screenshots/7-pick.png) |
| 설정 | ![설정](screenshots/8-settings.png) |
| 시각 고치기 창(메인 시각 칸을 누름) | ![시각 고치기](screenshots/9-time-edit.png) |
