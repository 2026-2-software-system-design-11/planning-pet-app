package planningpet.proto;

import static planningpet.proto.QaRun.st;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

/** docs/user-flows.md의 ③, ④, ⑤ 흐름. */
final class Flows345 {
    private Flows345() {
    }

    static void add(Map<String, Runnable> flows) {
        flows.put("UF-3-1", () -> uf31(new Qa("UF-3-1")));
        flows.put("UF-3-2", Flows345::uf32);
        flows.put("UF-3-3", Flows345::uf33);
        flows.put("UF-4-1", () -> uf41(new Qa("UF-4-1")));
        flows.put("UF-4-2", Flows345::uf42);
        flows.put("UF-4-3", Flows345::uf43);
        flows.put("UF-5-1", () -> uf51(new Qa("UF-5-1")));
        flows.put("UF-5-2", () -> uf52(new Qa("UF-5-2")));
        flows.put("UF-5-3", Flows345::uf53);
    }

    /** 그날 시각 있는 일정을 "17:00 팀 회의, 18:00 편의점 알바"처럼. */
    static String day(Qa q, String date) {
        return q.app.store.timedOn(QaDrive.d(date)).stream().sorted(Comparator.comparing((Task t) -> t.start))
                .map(t -> t.start + " " + t.title).collect(Collectors.joining(", "));
    }

    /** 시간표 열: "팀 회의 0/2, 편의점 알바 1/2". */
    static String cols(Qa q, String date) {
        var tasks = q.app.store.timedOn(QaDrive.d(date));
        var c = Planner.columns(tasks);
        return tasks.stream().sorted(Comparator.comparing((Task t) -> t.start))
                .map(t -> t.title + " " + c.get(t)[0] + "/" + c.get(t)[1]).collect(Collectors.joining(", "));
    }

    static String dates(Qa q, String title, java.time.DayOfWeek dow) {
        return q.app.store.tasks.stream().filter(t -> t.title.equals(title) && t.date.getDayOfWeek() == dow)
                .map(t -> t.date.toString()).sorted().collect(Collectors.joining(","));
    }

    /** 일정관리에서 일정 추가로 고정 일정을 매주 반복으로 넣는다. */
    static boolean shift(Qa q, String date, String time, int minutes, String end) {
        TaskForm f = q.form(q.sched.defaultDate().toString());
        f.fixed.doClick();
        f.title.setText("편의점 알바");
        Ui.setDate(f.date, QaDrive.d(date));
        f.start.setValue(time);
        f.minutes.setValue(minutes);
        f.repeat.doClick();
        Ui.setDate(f.repeatEnd, LocalDate.parse(end));
        return q.save(f, null);
    }

    static Qa uf31(Qa q) {
        q.start(Personas.p3());
        q.open();
        q.app.show("일정관리");
        q.step("1", "일정관리에서 일정 추가를 누른다");
        TaskForm f = q.form(q.sched.defaultDate().toString());
        q.eq("날짜, 종류, 반복 칸", "2026-11-16 할 일 false",
                Ui.dateOf(f.date) + (f.task.isSelected() ? " 할 일 " : " 고정 일정 ") + f.repeat.isEnabled());
        q.step("2", "종류를 고정 일정으로 바꾼다");
        f.fixed.doClick();
        q.eq("시작 시각 정하기(켜짐, 활성), 반복 칸", "true false true",
                f.timed.isSelected() + " " + f.timed.isEnabled() + " " + f.repeat.isEnabled());
        q.step("3", "편의점 알바, 11-17, 18:00, 240분을 넣고 매주 반복을 켠다");
        f.title.setText("편의점 알바");
        Ui.setDate(f.date, QaDrive.d("2026-11-17"));
        f.start.setValue("18:00");
        f.minutes.setValue(240);
        f.repeat.doClick();
        q.eq("끝 시각, 반복 끝", "끝 22:00 2027-03-02", f.endLabel.getText() + " " + Ui.dateOf(f.repeatEnd));
        q.step("4", "끝 날짜를 2026-11-10으로 바꿔 저장한다");
        Ui.setDate(f.repeatEnd, QaDrive.d("2026-11-10"));
        q.eq("저장", "false 끝 날짜가 시작 날짜보다 앞이에요", q.save(f, null) + " " + f.error.getText());
        q.step("5", "끝 날짜를 2027-05-18로 바꿔 저장한다");
        Ui.setDate(f.repeatEnd, QaDrive.d("2027-05-18"));
        q.eq("저장", "false 반복은 26번까지예요", q.save(f, null) + " " + f.error.getText());
        q.step("6", "끝 날짜를 2026-12-15로 바꿔 저장한다");
        Ui.setDate(f.repeatEnd, QaDrive.d("2026-12-15"));
        q.yes("저장", q.save(f, null));
        q.eq("화요일", "2026-11-17,2026-11-24,2026-12-01,2026-12-08,2026-12-15",
                dates(q, "편의점 알바", java.time.DayOfWeek.TUESDAY));
        q.step("7", "목요일 알바를 넣는다");
        q.yes("저장", shift(q, "2026-11-19", "18:00", 240, "2026-12-17"));
        q.eq("목요일 개수", 5, dates(q, "편의점 알바", java.time.DayOfWeek.THURSDAY).split(",").length);
        q.step("8", "토요일 알바를 넣는다(끝은 일요일)");
        q.yes("저장", shift(q, "2026-11-21", "10:00", 360, "2026-12-20"));
        q.eq("토요일", "2026-11-21,2026-11-28,2026-12-05,2026-12-12,2026-12-19",
                dates(q, "편의점 알바", java.time.DayOfWeek.SATURDAY));
        q.step("9", "시간표를 본다");
        q.eq("화 17, 목 19, 토 21", "18:00 편의점 알바 / 17:00 팀 회의, 18:00 편의점 알바 / 10:00 편의점 알바",
                day(q, "2026-11-17") + " / " + day(q, "2026-11-19") + " / " + day(q, "2026-11-21"));
        q.end();
        return q;
    }

    static void uf32() {
        Qa q = uf31(new Qa("UF-3-2"));
        q.step("1", "목 19 칸을 본다");
        q.eq("열", "팀 회의 0/2, 편의점 알바 1/2", cols(q, "2026-11-19"));
        q.step("2", "자동 배치를 누른다");
        Planner.Placement p = q.place();
        q.eq("결과 창", "11월 16일 12:30부터 11월 22일까지, 할 일마다 그 날짜 안의 빈 시간에 놓았어요"
                + " / 설계 과제|11월 19일|09:00 ~ 17:00|480분", q.placement(p));
        q.step("3", "적용을 누른다");
        q.apply(p);
        q.eq("열", "설계 과제 0/1, 팀 회의 0/2, 편의점 알바 1/2", cols(q, "2026-11-19"));
        q.end();
    }

    static void uf33() {
        Qa q = uf31(new Qa("UF-3-3"));
        q.step("1", "토 28 알바를 삭제한다");
        q.sched.move(QaDrive.d("2026-11-23"));
        q.step("2", "이후 반복 모두를 누른다");
        q.yes("지움", q.delete(q.task("편의점 알바", "2026-11-28"), 1));
        q.eq("확인 창", "'편의점 알바'는 매주 반복 일정이에요.", q.lastQuestion);
        q.eq("토요일", "2026-11-21", dates(q, "편의점 알바", java.time.DayOfWeek.SATURDAY));
        q.step("3", "토 21을 이 일정만으로 삭제한다");
        q.sched.move(QaDrive.d("2026-11-16"));
        q.yes("지움", q.delete(q.task("편의점 알바", "2026-11-21"), 0));
        q.eq("토, 화, 목 개수", "0 5 5", (dates(q, "편의점 알바", java.time.DayOfWeek.SATURDAY).isEmpty() ? 0 : -1)
                + " " + dates(q, "편의점 알바", java.time.DayOfWeek.TUESDAY).split(",").length
                + " " + dates(q, "편의점 알바", java.time.DayOfWeek.THURSDAY).split(",").length);
        q.end();
    }
}
