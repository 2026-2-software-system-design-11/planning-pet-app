package planningpet.qa;

import planningpet.app.model.Store;
import planningpet.common.view.Ui;
import planningpet.pet.view.PickDialog;
import planningpet.schedule.model.Task;
import planningpet.schedule.view.*;
import static planningpet.qa.QaRun.st;

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

    /** D-76: 표 아래 "놓지 못한 일" 칸의 끝 줄. */
    static final String HINT = "다른 일을 옮기거나, 걸리는 시간을 나누거나, 시각을 정해 보세요";

    static void uf81() {
        Qa q = new Qa("UF-8-1");
        q.start(Personas.p8());
        q.open();
        q.step("1", "일정관리를 연다");
        q.app.show("일정관리");
        q.eq("펫", "이번 주 남은 날의 시각 없는 할 일이 4개 있어. 자동 배치를 눌러 볼래?", q.schedBubble());
        q.step("2", "자동 배치를 누른다");
        q.eq("결과 창", "11월 20일 23:40부터 11월 22일까지, 할 일마다 그 날짜(기간 할 일은 기간) 안의 빈 시간에 놓았어요"
                + " / 세법 요약|11월 20일|23:40 ~ 24:00|20분 / 재무회계|11월 21일|09:00 ~ 10:00|60분"
                + " / 경영학|11월 21일|10:00 ~ 11:00|60분 / 놓지 못한 일 / 원가회계 날짜 11/20(금) 그날 빈 시간이 모자라요 / " + HINT,
                q.placement(q.place()));
        q.step("3", "23:55에 원가회계의 시각 정하기를 누른다");
        q.at("2026-11-20T23:55");
        Task t = q.task("원가회계");
        TimeEditor e = new TimeEditor(t, q.app.store.now);
        q.eq("시작", "23:55", e.start.getValue());
        q.step("4", "그대로 저장한다");
        q.eq("저장", "false 하루 안에 끝나야 해요", TimeEditor.save(q.app, t, e) + " " + e.error.getText());
        q.step("5", "11-21 01:00에 자동 배치를 누른다");
        q.at("2026-11-21T01:00");
        q.eq("결과 창", "11월 21일 09:00부터 11월 22일까지, 할 일마다 그 날짜(기간 할 일은 기간) 안의 빈 시간에 놓았어요"
                + " / 재무회계|11월 21일|09:00 ~ 10:00|60분 / 경영학|11월 21일|10:00 ~ 11:00|60분"
                + " / 놓지 못한 일 / 원가회계 날짜 11/20(금) 그날 빈 시간이 모자라요 / 세법 요약 날짜 11/20(금) 그날 빈 시간이 모자라요 / "
                + HINT, q.placement(q.place()));
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
        q.step("2", "기출 1회독을 넣고 시각 정하기를 누른다");
        q.quick("기출 1회독");
        q.eq("시작", "09:00", new TimeEditor(q.task("기출 1회독"), q.app.store.now).start.getValue());
        q.step("3", "일정관리에서 자동 배치를 누른다");
        q.app.show("일정관리");
        q.eq("결과 창", "11월 23일 09:00부터 11월 29일까지, 할 일마다 그 날짜(기간 할 일은 기간) 안의 빈 시간에 놓았어요"
                + " / 기출 1회독|11월 23일|09:00 ~ 09:30|30분", q.placement(q.place()));
        q.end();
    }

    static Qa uf91(Qa q) {
        q.start(Personas.p9());
        q.step("1", "앱을 연다");
        q.open();
        q.eq("말풍선", "오랜만이야! 다시 와서 반가워. 오늘 할 일은 1개야.", q.app.store.talk);
        q.eq("위 막대", "연속 접속 1일, 보유 260P, 출석 +20P", q.top());
        q.eq("레벨", "호두 Lv. 3 경험치 20 / 110", q.level());
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
        q.eq("목록", "시각 없는 할 일 / [x] 토익 RC 시각 정하기 60분", q.rows());
        q.eq("말풍선", "11월 13일 할 일은 1개야.", q.app.store.talk);
        q.step("2", "pet에서 쿠션을 두 번 누른다");
        q.app.show("나의 방");
        Flows12.press(q, "쿠션");
        q.eq("첫 번째", "배치 아이템 0 / 13, 하트, 쿠션 보유 중", q.pet());
        Flows12.press(q, "쿠션");
        q.has("두 번째", "쿠션 보유 중 ✓", q.pet());
        q.step("3", "상점을 연다");
        q.eq("상점", "털실 공 살 수 있어요 켜짐 / 뼈다귀 살 수 있어요 켜짐 / 쿠션 보유 중 방에서 치우기 켜짐 / 화분 살 수 있어요 켜짐 / 러그 살 수 있어요 켜짐 / 액자 살 수 있어요 켜짐"
                + " / 걸이 화분 살 수 있어요 켜짐 / 조명 살 수 있어요 켜짐 / 책장 살 수 있어요 켜짐 / 책상 살 수 있어요 켜짐 / 소파 40P 모자라요 꺼짐 / 캣타워 90P 모자라요 꺼짐 / 우드 침대 140P 모자라요 꺼짐", q.shop());
        q.step("4", "보유한 아이템에서 쿠션을 고르고 되팔기를 누른다");
        int before = q.app.store.points;
        q.pet.shop.ownedMode.doClick();
        q.pet.shop.selected = null;
        q.pet.shop.select("쿠션");
        q.eq("되팔기 버튼(D-85)", "되팔기 +50P 켜짐", q.pet.shop.sell.getText() + (q.pet.shop.sell.isVisible() ? " 켜짐" : " 숨음"));
        q.pet.shop.sell.doClick();
        q.eq("되판 뒤", "+50P, 보유 false, 방 false, 아직 가진 아이템이 없어요. 상점에서 사면 바로 방에 놓여요.",
                "+" + (q.app.store.points - before) + "P, 보유 " + q.app.store.owned.contains("쿠션") + ", 방 "
                + q.app.store.placed.contains("쿠션") + ", " + text(q.pet.shop.cards).strip());
        q.pet.shop.shopMode.doClick();
        q.end();
    }

    static Qa uf101(Qa q) {
        Store s = Personas.p10();
        q.step("1", "강아지를 고르고 이름을 비운 채 시작하기");
        PickDialog pd = new PickDialog();
        pd.dog.setSelected(true);
        pd.apply(s);
        q.start(s);
        q.open();
        q.eq("말풍선", "안녕, 나는 바둑이야. 일정관리에서 이번 학기 수업부터 넣어 볼까?", q.app.store.talk);
        q.step("2", "pet을 연다");
        q.app.show("나의 방");
        q.yes("부제", text(q.app.root).contains("나만의 공간을 꾸미고, 반려동물과 함께 시간을 보내요!"));
        q.yes("강아지 칩", q.pet.summary().length() > 0 && text(q.app.root).contains("강아지"));
        q.end();
        return q;
    }

    static String text(java.awt.Container c) {
        StringBuilder b = new StringBuilder();
        for (java.awt.Component k : c.getComponents()) {
            if (k instanceof javax.swing.JLabel l) {
                b.append(l.getText()).append('\n');
            } else if (k instanceof java.awt.Container inner) {
                b.append(text(inner));
            }
        }
        return b.toString();
    }

    /** 숫자 칸에 글을 쳐 넣고 칸을 벗어난 뒤 남은 값. 칸이 받지 않으면 이전 값이다. */
    static Object type(JSpinner spinner, String text) {
        var field = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
        field.setText(text);
        try {
            field.commitEdit();
        } catch (java.text.ParseException ignored) {
            // 범위 밖이면 받지 않는다.
        }
        return spinner.getValue();
    }

    static TaskForm timed(Qa q, String title, String start, int minutes) {
        TaskForm f = q.form("2026-11-17");
        f.title.setText(title);
        f.timed.doClick();
        f.start.setValue(start);
        f.minutes.setValue(minutes);
        return f;
    }

    static Qa uf102(Qa q) {
        q.after(Flows8910::uf101);
        q.app.show("홈");
        q.step("1", "논문 읽기 23:30 30분");
        TaskForm f = timed(q, "논문 읽기", "23:30", 30);
        q.eq("끝", "24:00에 끝나요", f.endLabel.getText());
        q.yes("저장", q.save(f, null));
        q.has("목록", "[ ] 논문 읽기 23:30~24:00 30분", q.rows());
        q.step("2", "논문 정리 23:30 40분으로 저장한다");
        TaskForm g = timed(q, "논문 정리", "23:30", 40);
        q.eq("끝, 저장", "24:10에 끝나요 false 하루 안에 끝나야 해요", g.endLabel.getText() + " " + q.save(g, null) + " " + g.errorText());
        q.step("3", "실험 정리 720분으로 저장한다");
        TaskForm h = q.form("2026-11-17");
        h.title.setText("실험 정리");
        h.minutes.setValue(720);
        q.yes("저장", q.save(h, null));
        q.has("목록", "[ ] 실험 정리 시각 정하기 720분", q.rows());
        q.step("4", "실험 정리 걸리는 시간에 730을 쳐 넣고 칸을 벗어난다");
        Task exp = q.task("실험 정리");
        TaskForm edit = new TaskForm(exp, exp.date, q.app.store.now);
        q.eq("걸리는 시간", 720, type(edit.minutes, "730"));
        q.step("5", "메모 칸에 1001자를 붙여 넣는다");
        edit.memo.setText("메".repeat(1001));
        q.eq("메모 길이", 1000, edit.memo.getText().length());
        q.end();
        return q;
    }

    static void uf103() {
        Qa q = new Qa("UF-10-3").after(Flows8910::uf102);
        Task t = q.task("논문 읽기");
        q.step("1", "논문 읽기를 체크한다");
        q.check(t);
        q.eq("상태", "10P 경험치10 횟수1 성취도1/2", st(q));
        q.eq("말풍선", "잘했어! +10P", q.app.store.talk);
        q.step("2", "수정으로 11-20, 20:00, 120분으로 저장한다");
        TaskForm f = new TaskForm(t, t.date, q.app.store.now);
        Ui.setDate(f.date, QaDrive.d("2026-11-20"));
        f.start.setValue("20:00");
        f.minutes.setValue(120);
        q.yes("저장", q.save(f, t));
        q.eq("17일 목록에 없음, 달력 20", "true 20 논문 읽기(흐림)", !q.rows().contains("논문 읽기") + " " + q.cell(20));
        q.eq("보상 그대로", "10P 횟수1 끝냄 true", q.app.store.points + "P 횟수" + q.count() + " 끝냄 " + t.done);
        q.step("3", "달력 20에서 논문 읽기를 삭제한다");
        q.select("2026-11-20");
        q.yes("지움", q.delete(t, 0));
        q.eq("확인 창", "일정 삭제 / '논문 읽기'를 삭제할까요? / 삭제, 취소", q.lastTitle + " / " + q.lastQuestion + " / " + q.lastOptions);
        q.eq("상태", "10P 경험치10 횟수1 있음 false", q.app.store.points + "P 경험치" + q.app.store.pet().exp + " 횟수" + q.count()
                + " 있음 " + q.app.store.tasks.contains(t));
        q.end();
    }
}
