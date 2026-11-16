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
}
