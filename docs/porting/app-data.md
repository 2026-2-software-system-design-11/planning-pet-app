# 단위 M7. 앱 데이터

[본 구현으로 옮기기](../porting.md) 1절 표의 단위 M7이다. 07의 `AppData`를 `planningpet.app.model`에, `PetState`와 값 목록 `Pose`, `Slot`, `WearId`를 `planningpet.pet.model`에, 값 목록 `ItemId`를 `planningpet.shop.model`에 둔다. 기준은 06 데이터 절의 `AppData`, `PetState` 값과 처음 값, 프로토타입 `app/model/Store`, `pet/model/PetState`, `Poses`, `Wardrobe`, `shop/model/Shop`의 아이템 목록이다. 값을 바꾸는 규칙(보상 8~11번, 상점 20번, 장신구 28번, 펫 30번)은 그 일을 옮기는 단위(M9, M14, M15)가 옮긴다. 22번의 되돌리기에 쓰는 `copy`와 `restore`는 PR 하나의 크기 한도(1절) 때문에 22번과 함께 M8 저장 단위가 옮긴다.

07 안에서 정한 것과 그 까닭:

- **값은 private 필드이고 읽는 메서드를 둔다.** 프로토타입 `Store`는 값을 public 필드로 열었다. 본 구현은 D-109대로 메서드로만 바꾼다. 값을 바꾸는 메서드는 M2a의 `Task`처럼 그 일을 옮기는 단위가 더한다. 이 단위는 고른 펫을 정하는 `addPet`, `setActiveKind`만 더한다. 06 30번의 `choose`, `adopt`(S2, M14)와 저장의 읽기(M8)가 쓰고, 이 단위의 고른 펫 검사가 쓴다. 모음 값(`pets`, `attendDays`, `ownedItems` 같은 것)은 바꿀 수 없는 모음으로 읽는다.
- **시계는 생성자로 받고 `today()`는 그 시계의 앱 날짜다.** 07 실행 모드대로다. 프로토타입은 `Store.now`에 시각을 넣었다. 일정 다루기(`TaskBook`)는 `AppData`가 만들어 담고, 넣은 순 번호와 앱의 오늘을 `AppData`에서 받는다(07 `AppData *-- TaskBook`).
- **종류마다 하나인 펫은 `pet(kind)`로 찾는다.** 07은 `activePet()`만 그렸고 읽는 메서드는 그리지 않는다(07 읽는 법). 프로토타입 이름대로 옮긴다.
- **값 목록은 `Category`처럼 `label()`로 화면 이름을 준다.** `Pose`는 D-50 순서, `Slot`은 D-51 칸 순서, `WearId`는 D-51의 칸 순서와 칸 안 레벨 순서, `ItemId`는 D-52의 가격 순서(같은 가격은 D-52에 적은 순서)다. 장신구가 열리는 레벨과 칸(`Wardrobe`)은 M14, 아이템의 가격과 분류(`Item`)는 M15가 옮긴다.
- **`ItemId`는 `planningpet.shop.model`에 둔다.** D-52의 아이템이고 그것을 사고파는 `Shop`이 상점 도메인이다(06 읽는 법의 도메인 표). 07은 패키지를 적지 않았다. `Slot`은 07 "이름이 같은 `Slot` 둘"대로 `planningpet.pet.model`이다. `PetState.wear`가 `Map<Slot, WearId>`라 이 단위에 둔다.
- **`SampleLook`은 record다.** 07은 `Map<String, SampleLook>`의 타입으로만 적었고 값은 07 저장 형식 `sample` 표의 제목, 시작 시각, `nextDay`, 메모, 날짜다. 프로토타입은 이 값을 글 하나로 이어 두었다.
- **07 그림과 다른 점.** 07 그림은 `AppData *-- "1..2" PetState`인데 07 seq-uc01은 첫 실행이면 빈 `AppData`이고 06 30번 `choose`가 펫을 만들어 넣는다. 본 구현은 seq-uc01대로 펫 없이 시작하므로 반려동물 고르기 창을 닫기 전까지 펫이 0이다.

## 1. 화면에서 따라 할 단계

이 단위는 화면이 없다. 값이 화면에 드러나는 곳은 아래이고, 화면은 S1~S10 단위가 옮긴다.

1. 빈 폴더로 앱을 열면 메인보다 먼저 반려동물 고르기 창이 뜨고, 이름 "콩"으로 "바로 시작"을 누르면 메인 위 막대가 0P, 연속 접속 1일이고 펫 카드가 "콩 Lv.1 경험치 0 / 50"이다. 나의 방은 "보유 포인트 0P", "배치 아이템 0 / 13"이다([UF-7-1](../user-flows.md) 1~3단계, D-27).
2. 나의 방에서 "+"를 눌러 강아지를 데려오면 강아지가 고른 펫이 되고, 방 왼쪽 위 나비 칩을 누르면 다시 나비가 고른 펫이다. 홈 펫 카드는 고른 펫을 보인다([UF-1-4](../user-flows.md) 3, 4단계, D-48).
3. 캐릭터 창의 자세 칸은 앉기, 자기, 기지개, 밥 먹기, 노래이고 처음은 앉기다. 꾸미기 탭의 칸은 목걸이, 액세서리, 모자, 의상이고 처음은 기본 목걸이만 입었다(D-50, D-51).
4. 나의 방 상점 칸은 털실 공부터 우드 침대까지 열세 아이템이 가격 순이다(D-52, [UF-1-3](../user-flows.md) 3단계).
5. 설정의 "달력에 고정 일정 보이기"와 "목록 누르기"는 처음 꺼져 있고 "한 주 시작"은 월요일이다(D-86, D-102, D-91).
6. 시계가 11-17 03:59이면 메인 목록 제목은 "11월 16일 (월) 할 일"이고, 04:00이 지나면 "11월 17일 (화) 할 일"이다(D-15, 06 1번).

## 2. 입력의 경계와 프로토타입 결과

프로토타입 결과는 `prototype/src`를 컴파일한 클래스로 `new Store()`, `Store.today`, `Store.pet`, `onChange`, `changed`, `book.add`, `new PetState`, `Poses.ALL`, `Wardrobe`, `Shop.ITEMS`를 부르는 버리는 Java 파일에서 얻었다(develop `9a59f15`, JDK 21.0.11). 06 데이터 절에는 검사 줄이 없어 T-WEAR-2 밖은 `E-APP`, `E-PET`, `E-WEAR`, `E-SHOP` 번호다. "본 구현" 칸의 결과는 `AppDataTest`, `PetStateTest`, `WearIdTest`, `ItemIdTest`가 번호 이름의 테스트로 확인한다.

| 번호 | 입력과 경계 | 넣은 값 | 프로토타입 결과 | 본 구현 |
|---|---|---|---|---|
| E-APP-1 | 처음 값, 보상과 출석 | 새 앱 데이터 | `points` 0, `rewardCountDay` `null`, `rewardCount` 0, `attendanceDay` `null`, `attendDays` 없음, `lastOpenDay` `null`, `streak` 0 | 같음 |
| E-APP-2 | 처음 값, 아이템과 설정 | 새 앱 데이터 | `ownedItems`, `placedItems` 없음, `calendarFixed` false, `listClickTime` false, `weekStart` `MONDAY`. `trayNoticeShown`은 프로토타입에 없다(트레이가 없다) | 같음. `trayNoticeShown` false |
| E-APP-3 | 처음 값, 예시와 일정 | 새 앱 데이터 | `sampleIds`, `sampleLooks` 없음, 일정 0개, `deletedRewarded` 없음, `nextSeq` 1 | 같음 |
| E-APP-4 | 처음 펫 | 새 앱 데이터 | 펫 하나(고양이 "나비"), `activeKind` `CAT` | 프로토타입과 일부러 다름: 펫 없음, `activeKind` `null`. 첫 실행은 빈 앱 데이터이고 고르기 창이 펫을 넣는다(4절 표, 07 seq-uc01, 06 30번) |
| E-APP-5 | 펫이 없음 | 펫을 모두 뺀 앱 데이터의 고른 펫 | `IndexOutOfBoundsException` | 프로토타입과 일부러 다름: `null`. 고르기 창 전의 빈 앱 데이터에서 고른 펫은 없다(E-APP-4) |
| E-APP-6 | 자정 | 11-17 00:00의 `today` | 11-16 | 같음 |
| E-APP-7 | 앱 하루의 끝 | 11-17 03:59 | 11-16 | 같음 |
| E-APP-8 | 앱 하루의 시작 | 11-17 04:00 | 11-17 | 같음 |
| E-APP-9 | 달이 바뀌는 날 | 12-01 03:59 | 11-30 | 같음 |
| E-APP-10 | 해가 바뀌는 날 | 2027-01-01 03:59 | 2026-12-31 | 같음 |
| E-APP-11 | 고른 펫 | 고양이 "나비"와 강아지 "바둑이", `activeKind` `DOG` | 고른 펫 바둑이, `pet(CAT)` 나비 | 같음 |
| E-APP-12 | 고른 종류의 펫이 없음 | 고양이 "나비"만, `activeKind` `DOG` | 고른 펫 나비(첫 펫), `pet(DOG)` `null` | 같음 |
| E-APP-13 | 알림 순서 | 알림 A, B, A를 차례로 받고 `changed` | A, B, A 순서로 세 번 | 같음 |
| E-APP-14 | 알림 받는 곳 없음 | `changed` | 아무 일 없음 | 같음 |
| E-APP-15 | 넣은 순 번호 | 오늘 11-18. 할 일 하나, 이어서 고정 일정 11-16부터 11-30까지 월요일 반복 | `seq` 1, `nextSeq` 2. 사본 3개 `seq` 2, 3, 4, `nextSeq` 5 | 같음 |
| E-PET-1 | 새 펫 | 강아지 "바둑이" | 종류 `DOG`, 이름 바둑이, 경험치 0, 자세 앉기 | 같음 |
| T-WEAR-2 | 처음 장신구 | 강아지 "바둑이" | 목걸이에 기본 목걸이 하나 | 같음 |
| E-PET-2 | 자세 목록과 순서 | 모든 자세 | 앉기, 자기, 기지개, 밥 먹기, 노래 | 같음 |
| E-WEAR-1 | 칸 목록과 순서 | 모든 칸 | 목걸이, 액세서리, 모자, 의상 | 같음 |
| E-WEAR-2 | 장신구 목록과 순서 | 모든 장신구 | 13개. 기본 목걸이, 리본 목걸이, 별 목걸이, 왕관 목걸이, 꽃 핀, 리본 핀, 안경, 밀짚모자, 베레모, 왕관, 스카프, 조끼, 망토 | 같음 |
| E-SHOP-1 | 아이템 목록과 순서 | 모든 아이템 | 13개. 털실 공, 뼈다귀, 쿠션, 화분, 러그, 액자, 걸이 화분, 조명, 책장, 책상, 소파, 캣타워, 우드 침대 | 같음 |

경계마다 본 것:

- E-APP-4: 프로토타입은 첫 실행 전에도 고양이 하나가 있고, 반려동물 고르기 창(`PetController.choose`)이 펫 목록을 비우고 고른 펫을 넣는다.
- E-APP-5: 프로토타입은 늘 펫이 하나 이상이라 이 길이 없다. 고른 펫은 `activeKind`의 펫이고 없으면 첫 펫이다(E-APP-12). 본 구현은 첫 실행에 펫이 없어(E-APP-4) 그때 `null`이다. 반려동물 고르기 창은 메인 창보다 먼저 뜨므로 그동안 고른 펫을 읽는 화면이 없다(UC-01 2a).
- E-APP-6~10: 앱 날짜는 M1의 `AppDay.of`다. 시계가 새벽 4시 전이면 앞날이다(D-15, 06 1번).
- E-APP-15: 일정 다루기의 넣은 순 번호는 앱 데이터의 `nextSeq`에서 꺼내고 늘린다. 저장(M8)이 이 값을 `app` 표에 쓴다.
- E-SHOP-1: 털실 공과 뼈다귀는 둘 다 40P라 D-52에 적은 순서다. 가격과 합(T-SHOP-7)은 M15가 확인한다.
