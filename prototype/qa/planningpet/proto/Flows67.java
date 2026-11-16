package planningpet.proto;

import static planningpet.proto.QaRun.st;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;

/** docs/user-flows.md의 ⑥, ⑦ 흐름. */
final class Flows67 {
    private Flows67() {
    }

    static void add(Map<String, Runnable> flows) {
        flows.put("UF-6-1", Flows67::uf61);
        flows.put("UF-6-2", Flows67::uf62);
        flows.put("UF-6-3", Flows67::uf63);
        flows.put("UF-7-1", () -> uf71(new Qa("UF-7-1")));
        flows.put("UF-7-2", () -> uf72(new Qa("UF-7-2")));
        flows.put("UF-7-3", () -> uf73(new Qa("UF-7-3"), true));
        flows.put("UF-7-4", Flows67::uf74);
    }

    static void uf61() {
        Qa q = new Qa("UF-6-1");
        q.start(Personas.p6());
        q.step("1", "앱을 연다");
        q.open();
        q.eq("말풍선", "오늘 할 일 3개 남았어.", q.app.store.talk);
        q.eq("목록", "08:00~16:00 [ ] 고정 병원 실습 480분 / 시간 안 정한 할 일 / 시간 정하기 [ ] 실습 일지 60분"
                + " / 시간 정하기 [ ] 간호사 국시 문제 30분", q.rows());
        q.eq("성취도, 레벨", "0/3 두부 Lv.4 경험치 60 / 140", q.progress() + " " + q.level());
        q.step("2", "병원 실습을 체크한다");
        q.check(q.task("병원 실습", "2026-11-19"));
        q.eq("상태", "160P 경험치310 횟수1 성취도1/3", st(q));
        q.eq("말풍선", "잘했어! +10P", q.app.store.talk);
        q.step("3", "실습 일지를 체크한다");
        q.check(q.task("실습 일지"));
        q.eq("상태", "170P 경험치320 횟수2 성취도2/3", st(q));
        q.step("4", "간호사 국시 문제를 체크한다");
        q.check(q.task("간호사 국시 문제"));
        q.eq("상태", "180P 경험치330 횟수3 성취도3/3", st(q));
        q.eq("말풍선", "오늘 할 일을 다 끝냈어! 푹 쉬어.", q.app.store.talk);
        q.step("5", "일정관리를 연다");
        q.app.show("일정관리");
        q.eq("열, 체크", "병원 실습 0/1 true", Flows345.cols(q, "2026-11-19") + " " + q.task("병원 실습", "2026-11-19").done);
        q.end();
    }

    static void uf62() {
        Qa q = new Qa("UF-6-2");
        q.start(Personas.p6());
        q.open();
        q.step("1", "달력 20을 누른다");
        q.select("2026-11-20");
        q.eq("목록", "08:00~16:00 [ ] 고정 병원 실습 480분", q.rows());
        q.eq("말풍선", "11월 20일 할 일은 1개야.", q.app.store.talk);
        q.step("2", "08:00~16:00을 누른다");
        Task t = q.task("병원 실습", "2026-11-20");
        TimeEditor e = new TimeEditor(t, q.app.store.now);
        q.eq("창", "08:00 480 시간 빼기 false", e.start.getValue() + " " + e.minutes.getValue() + " 시간 빼기 " + e.clear.isVisible());
        q.step("3", "시작을 09:00으로 바꿔 저장한다");
        e.start.setValue("09:00");
        q.yes("저장", TimeEditor.save(q.app, t, e));
        q.eq("목록", "09:00~17:00 [ ] 고정 병원 실습 480분", q.rows());
        Task next = q.task("병원 실습", "2026-11-27");
        q.eq("다음 주 금 27, 같은 반복", "08:00 true", next.start + " " + next.seriesId.equals(t.seriesId));
        q.end();
    }

    static void uf63() {
        Qa q = new Qa("UF-6-3");
        q.start(Personas.p6());
        q.open();
        TaskForm f = q.form(q.main.calendar.selected().toString());
        q.step("1", "제목을 비우고 저장한다");
        q.eq("저장", "false 제목을 써 주세요", q.save(f, null) + " " + f.error.getText());
        q.step("2", "제목 칸에 101자를 붙여 넣는다");
        f.title.setText("가".repeat(101));
        q.eq("제목 길이", 100, f.title.getText().length());
        q.step("3", "국시 모의고사, 걸리는 시간 25로 저장한다");
        f.title.setText("국시 모의고사");
        f.minutes.setValue(25);
        q.eq("저장", "false 10분부터 720분까지, 10분 단위예요", q.save(f, null) + " " + f.error.getText());
        q.step("4", "제목을 비우고 25인 채로 저장한다");
        f.title.setText("");
        q.eq("오류 두 개(칸 순서)", "false 제목을 써 주세요 / 10분부터 720분까지, 10분 단위예요", q.save(f, null) + " " + f.error.getText());
        q.step("5", "국시 모의고사, 30분으로 저장한다");
        f.title.setText("국시 모의고사");
        f.minutes.setValue(30);
        q.yes("저장", q.save(f, null));
        q.has("목록", "시간 정하기 [ ] 국시 모의고사 30분", q.rows());
        q.end();
    }

    static Qa uf71(Qa q) {
        Store s = Personas.p7();
        q.step("1", "앱을 연다(반려동물 고르기 창)");
        PickDialog pd = new PickDialog();
        q.eq("고양이 골라짐, 이름 칸", "true ''", pd.cat.isSelected() + " '" + pd.name.getText() + "'");
        q.eq("안내", "비워 두면 나비, 바둑이로 불러요", Qa.plain(hint(pd.panel)));
        q.step("2", "이름 콩을 쓰고 시작하기를 누른다");
        pd.name.setText("콩");
        pd.apply(s);
        q.start(s);
        q.open();
        q.eq("말풍선", "안녕, 나는 콩이야. 일정관리에서 이번 학기 수업부터 넣어 볼까?", q.app.store.talk);
        q.eq("위 막대", "연속 접속 1일, 보유 0P, 출석 +20P", q.top());
        q.eq("레벨", "콩 Lv.1 경험치 0 / 50", q.level());
        q.eq("목록", "아직 일정이 없어요. 작은 계획을 추가해 보세요.", q.rows());
        q.eq("성취도", "비어 있음", q.progress());
        q.step("3", "pet을 연다");
        q.app.show("pet");
        q.eq("pet", "꾸미기 0 / 8, 오늘은 아직 끝낸 일이 없어., 상점", q.pet());
        q.end();
        return q;
    }

    /** 반려동물 고르기 창에서 "나비"가 들어간 안내 글. */
    static String hint(java.awt.Container c) {
        for (java.awt.Component k : c.getComponents()) {
            if (k instanceof javax.swing.JLabel l && l.getText().contains("나비")) {
                return l.getText();
            }
            if (k instanceof java.awt.Container inner && !hint(inner).isEmpty()) {
                return hint(inner);
            }
        }
        return "";
    }

    static boolean fixed(Qa q, String title, String date, String time, int minutes, String end) {
        TaskForm f = q.form(q.sched.defaultDate().toString());
        f.fixed.doClick();
        f.title.setText(title);
        Ui.setDate(f.date, LocalDate.parse(date));
        f.start.setValue(time);
        f.minutes.setValue(minutes);
        f.repeat.doClick();
        if (end != null) {
            Ui.setDate(f.repeatEnd, LocalDate.parse(end));
        }
        return q.save(f, null);
    }
}
