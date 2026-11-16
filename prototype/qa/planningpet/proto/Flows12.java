package planningpet.proto;

import static planningpet.proto.QaRun.st;

import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.JButton;

/** docs/user-flows.md의 ①, ② 흐름. 단계 번호와 문장은 문서와 같다. */
final class Flows12 {
    private Flows12() {
    }

    static void add(Map<String, Runnable> flows) {
        flows.put("UF-1-1", Flows12::uf11);
        flows.put("UF-1-2", Flows12::uf12);
        flows.put("UF-1-3", Flows12::uf13);
        flows.put("UF-2-1", () -> uf21(new Qa("UF-2-1")));
        flows.put("UF-2-2", Flows12::uf22);
        flows.put("UF-2-3", Flows12::uf23);
        flows.put("UF-2-4", Flows12::uf24);
        flows.put("UF-2-5", Flows12::uf25);
    }

    static void uf11() {
        Qa q = new Qa("UF-1-1");
        q.start(Personas.p1());
        q.step("1", "앱을 연다");
        q.open();
        q.eq("목록", "10:00~11:00 [x] 알고리즘 과제 2번 60분 / 14:00~15:30 [ ] 자소서 초안 메모 90분 / 시간 안 정한 할 일"
                + " / 시간 정하기 [ ] 토익 단어 30분 / 시간 정하기 [ ] 운동 30분", q.rows());
        q.eq("말풍선", "오늘 할 일 3개 남았어. '은행 서류' 마감이 내일이야.", q.app.store.talk);
        q.eq("위 막대", "연속 접속 5일, 보유 100P, 출석 +20P", q.top());
        q.eq("성취도", "1/4", q.progress());
        q.eq("레벨", "나비 Lv.3 경험치 80 / 110", q.level());
        q.eq("달력 14", "14 알고리즘…(진함)", q.cell(14));
        q.eq("달력 15", "15 은행 서류(진함)", q.cell(15));
        q.eq("달력 13", "13 토익 스터…(흐림)", q.cell(13));
        q.step("2", "자소서 초안을 체크한다");
        q.check(q.task("자소서 초안"));
        q.eq("상태", "110P 경험치220 횟수2 성취도2/4", st(q));
        q.eq("말풍선", "잘했어! +10P", q.app.store.talk);
        q.has("위 막대", "보유 110P", q.top());
        q.eq("레벨", "나비 Lv.3 경험치 90 / 110", q.level());
        q.step("3", "자소서 초안 체크를 푼다");
        q.check(q.task("자소서 초안"));
        q.eq("상태", "100P 경험치210 횟수1 성취도1/4", st(q));
        q.eq("말풍선", "체크를 풀었어. -10P", q.app.store.talk);
        q.step("4", "다시 체크한다");
        q.check(q.task("자소서 초안"));
        q.eq("상태", "110P 경험치220 횟수2 성취도2/4", st(q));
        q.eq("말풍선", "잘했어! +10P", q.app.store.talk);
        q.step("5", "출석 +20P를 누른다");
        q.attend();
        q.eq("위 막대", "연속 접속 5일, 보유 130P, 출석 완료(비활성)", q.top());
        q.eq("말풍선", "출석했어! +20P", q.app.store.talk);
        q.step("6", "토익 단어를 체크한다");
        q.check(q.task("토익 단어"));
        q.eq("상태", "140P 경험치230 횟수3 성취도3/4", st(q));
        q.step("7", "운동을 체크한다");
        q.check(q.task("운동"));
        q.eq("상태", "150P 경험치240 횟수4 성취도4/4", st(q));
        q.eq("말풍선", "잘했어! 레벨이 올랐어. 이제 Lv.4, 나 좀 자란 것 같아.", q.app.store.talk);
        q.eq("레벨", "나비 Lv.4 경험치 0 / 140", q.level());
        q.step("8", "같은 날 창을 다시 연다");
        q.open();
        q.eq("말풍선", "오늘 할 일은 다 끝냈어!", q.app.store.talk);
        q.has("위 막대", "연속 접속 5일", q.top());
        q.step("9", "달력 18을 누르고 일정 추가를 누른다");
        q.select("2026-11-18");
        q.eq("목록", "09:00~10:15 [ ] 고정 자료구조 75분 / 시간 안 정한 할 일 / 시간 정하기 [ ] 통계 레포트 120분", q.rows());
        q.eq("말풍선", "11월 18일 할 일은 2개야.", q.app.store.talk);
        TaskForm f = new TaskForm(null, q.main.calendar.selected(), q.app.store.now);
        q.eq("입력 창 날짜", "2026-11-18", Ui.dateOf(f.date));
        q.eq("입력 창 시작 시각", "09:00", f.start.getValue());
        q.end();
    }

    static void uf12() {
        Qa q = new Qa("UF-1-2");
        q.start(Personas.p1());
        q.open();
        q.step("1", "일정관리를 연다");
        q.app.show("일정관리");
        q.eq("오른쪽 목록", "[ ] 11/10 빨래 30분 / [ ] 11/12 도서관 책 반납 30분 / [ ] 11/14 토익 단어 30분"
                + " / [ ] 11/14 운동 30분 / [ ] 11/15 은행 서류 30분", q.schedRows());
        q.eq("펫", "시간이 안 정해진 할 일이 3개 있어. 자동 배치를 눌러 볼래?", q.schedBubble());
        q.step("2", "자동 배치를 누른다");
        Planner.Placement p = q.place();
        q.eq("결과 창", "11월 14일 15:30부터 11월 15일까지, 할 일마다 그 날짜 안의 빈 시간에 놓았어요"
                + " / 토익 단어|11월 14일|15:30 ~ 16:00|30분 / 운동|11월 14일|16:00 ~ 16:30|30분"
                + " / 은행 서류|11월 15일|09:00 ~ 09:30|30분", q.placement(p));
        q.step("3", "적용을 누른다");
        q.apply(p);
        q.eq("오른쪽 목록", "[ ] 11/10 빨래 30분 / [ ] 11/12 도서관 책 반납 30분", q.schedRows());
        q.eq("자동 배치 버튼", false, q.sched.autoPlace.isEnabled());
        q.eq("펫(확인할 점, 명세대로면 그대로)", "시간이 안 정해진 할 일이 3개 있어. 자동 배치를 눌러 볼래?", q.schedBubble());
        q.step("4", "홈으로 간다");
        q.app.show("홈");
        q.eq("목록", "10:00~11:00 [x] 알고리즘 과제 2번 60분 / 14:00~15:30 [ ] 자소서 초안 메모 90분"
                + " / 15:30~16:00 [ ] 토익 단어 30분 / 16:00~16:30 [ ] 운동 30분", q.rows());
        q.end();
    }

    static void uf13() {
        Qa q = new Qa("UF-1-3");
        q.start(Personas.p1());
        q.open();
        q.step("1", "출석한다");
        q.attend();
        q.eq("포인트", 120, q.app.store.points);
        q.step("2", "pet을 연다");
        q.app.show("pet");
        q.eq("pet", "꾸미기 3 / 8, 오늘 1개 끝냈어., 쿠션 ✓ 화분 ✓ 러그(흐림) 상점", q.pet());
        q.step("3", "상점을 연다");
        q.eq("상점", "쿠션 보유 중 꺼짐 / 화분 보유 중 꺼짐 / 러그 보유 중 꺼짐 / 액자 - 켜짐 / 조명 30P 모자라요 꺼짐"
                + " / 책장 80P 모자라요 꺼짐 / 책상 130P 모자라요 꺼짐 / 소파 180P 모자라요 꺼짐", q.shop());
        q.step("4", "액자를 산다");
        q.yes("샀음", q.buy("액자"));
        q.eq("포인트", 0, q.app.store.points);
        q.eq("pet", "꾸미기 4 / 8, 액자 고마워!, 쿠션 ✓ 화분 ✓ 러그(흐림) 액자 ✓ 상점", q.pet());
        q.step("5", "홈에서 알고리즘 과제 2번 체크를 푼다");
        q.app.show("홈");
        q.check(q.task("알고리즘 과제 2번"));
        q.eq("상태", "-10P 경험치200 횟수0 성취도0/4", st(q));
        q.eq("말풍선", "체크를 풀었어. -10P", q.app.store.talk);
        q.step("6", "다시 체크한다");
        q.check(q.task("알고리즘 과제 2번"));
        q.eq("상태", "0P 경험치210 횟수1 성취도1/4", st(q));
        q.yes("액자 그대로", q.app.store.owned.contains("액자"));
        q.step("7", "pet에서 러그를 두 번 누른다");
        q.app.show("pet");
        press(q, "러그");
        q.has("첫 번째", "러그 ✓", q.pet());
        press(q, "러그");
        q.has("두 번째", "러그(흐림)", q.pet());
        q.end();
    }

    /** pet 화면 내 아이템 줄에서 이름이 name으로 시작하는 버튼을 누른다. */
    static void press(Qa q, String name) {
        for (var c : q.pet.items.getComponents()) {
            if (c instanceof AbstractButton b && !(c instanceof JButton) && b.getText().startsWith(name)) {
                b.doClick();
                return;
            }
        }
        throw new IllegalStateException("버튼 없음: " + name);
    }
}
