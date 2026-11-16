package planningpet.proto;

import static planningpet.proto.QaRun.st;

import java.time.LocalDateTime;
import java.util.Map;
import javax.swing.JSpinner;

/** docs/user-flows.md의 ⑧, ⑨, ⑩ 흐름. */
final class Flows8910 {
    private Flows8910() {
    }

    static void add(Map<String, Runnable> flows) {
        flows.put("UF-8-1", Flows8910::uf81);
        flows.put("UF-8-2", Flows8910::uf82);
        flows.put("UF-9-1", () -> uf91(new Qa("UF-9-1")));
        flows.put("UF-9-2", Flows8910::uf92);
        flows.put("UF-9-3", Flows8910::uf93);
        flows.put("UF-10-1", () -> uf101(new Qa("UF-10-1")));
        flows.put("UF-10-2", () -> uf102(new Qa("UF-10-2")));
        flows.put("UF-10-3", Flows8910::uf103);
    }

    static final String UNPLACED = "놓지 못함|그날 빈 시간이 모자라요";

    static void uf81() {
        Qa q = new Qa("UF-8-1");
        q.start(Personas.p8());
        q.open();
        q.step("1", "일정관리를 연다");
        q.app.show("일정관리");
        q.eq("펫", "시간이 안 정해진 할 일이 4개 있어. 자동 배치를 눌러 볼래?", q.schedBubble());
        q.step("2", "자동 배치를 누른다");
        q.eq("결과 창", "11월 20일 23:40부터 11월 22일까지, 할 일마다 그 날짜 안의 빈 시간에 놓았어요"
                + " / 세법 요약|11월 20일|23:40 ~ 24:00|20분 / 재무회계|11월 21일|09:00 ~ 10:00|60분"
                + " / 경영학|11월 21일|10:00 ~ 11:00|60분 / 원가회계|" + UNPLACED + "|30분", q.placement(q.place()));
        q.step("3", "23:55에 원가회계의 시간 정하기를 누른다");
        q.at("2026-11-20T23:55");
        Task t = q.task("원가회계");
        TimeEditor e = new TimeEditor(t, q.app.store.now);
        q.eq("시작", "23:50", e.start.getValue());
        q.step("4", "그대로 저장한다");
        q.eq("저장", "false 하루 안에 끝나야 해요", TimeEditor.save(q.app, t, e) + " " + e.error.getText());
        q.step("5", "11-21 01:00에 자동 배치를 누른다");
        q.at("2026-11-21T01:00");
        q.eq("결과 창", "11월 21일 09:00부터 11월 22일까지, 할 일마다 그 날짜 안의 빈 시간에 놓았어요"
                + " / 재무회계|11월 21일|09:00 ~ 10:00|60분 / 경영학|11월 21일|10:00 ~ 11:00|60분"
                + " / 원가회계|" + UNPLACED + "|30분 / 세법 요약|" + UNPLACED + "|20분", q.placement(q.place()));
        q.end();
    }

    static void uf82() {
        Qa q = new Qa("UF-8-2");
        Store s = Personas.p8();
        s.now = LocalDateTime.parse("2026-11-23T08:00");
        q.start(s);
        q.step("1", "앱을 연다");
        q.open();
        q.eq("말풍선", "오늘은 할 일이 없어. 아래 칸에 하나 적어 볼까?", q.app.store.talk);
        q.has("위 막대", "연속 접속 1일", q.top());
        q.step("2", "기출 1회독을 넣고 시간 정하기를 누른다");
        q.quick("기출 1회독");
        q.eq("시작", "09:00", new TimeEditor(q.task("기출 1회독"), q.app.store.now).start.getValue());
        q.step("3", "일정관리에서 자동 배치를 누른다");
        q.app.show("일정관리");
        q.eq("결과 창", "11월 23일 09:00부터 11월 29일까지, 할 일마다 그 날짜 안의 빈 시간에 놓았어요"
                + " / 기출 1회독|11월 23일|09:00 ~ 09:30|30분", q.placement(q.place()));
        q.end();
    }

    static Qa uf91(Qa q) {
        q.start(Personas.p9());
        q.step("1", "앱을 연다");
        q.open();
        q.eq("말풍선", "오랜만이야! 다시 와서 반가워. 오늘 할 일은 1개야.", q.app.store.talk);
        q.eq("위 막대", "연속 접속 1일, 보유 260P, 출석 +20P", q.top());
        q.eq("레벨", "호두 Lv.3 경험치 20 / 110", q.level());
        q.end();
        return q;
    }

    static void uf92() {
        Qa q = new Qa("UF-9-2");
        Store s = Personas.p9();
        s.lastOpenDay = QaDrive.d("2026-11-09");
        q.start(s);
        q.step("1", "앱을 연다");
        q.open();
        q.eq("말풍선", "오늘 할 일 1개 남았어.", q.app.store.talk);
        q.has("위 막대", "연속 접속 1일", q.top());
        q.end();
    }

    static void uf93() {
        Qa q = new Qa("UF-9-3").after(Flows8910::uf91);
        q.step("1", "달력 13을 누른다");
        q.select("2026-11-13");
        q.eq("달력 13", "13 토익 RC(흐림)", q.cell(13));
        q.eq("목록", "시간 안 정한 할 일 / 시간 정하기 [x] 토익 RC 60분", q.rows());
        q.eq("말풍선", "11월 13일 할 일은 1개야.", q.app.store.talk);
        q.step("2", "pet에서 쿠션을 두 번 누른다");
        q.app.show("pet");
        Flows12.press(q, "쿠션");
        q.has("첫 번째", "쿠션(흐림)", q.pet());
        Flows12.press(q, "쿠션");
        q.has("두 번째", "쿠션 ✓", q.pet());
        q.step("3", "상점을 연다");
        q.has("상점", "쿠션 보유 중 꺼짐 / 화분 - 켜짐", q.shop());
        q.end();
    }
}
