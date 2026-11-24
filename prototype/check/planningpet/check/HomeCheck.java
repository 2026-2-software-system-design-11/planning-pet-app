package planningpet.check;

import static planningpet.check.ProtoCheck.eq;

import planningpet.app.Main;
import planningpet.app.model.MockData;
import planningpet.app.model.Store;
import planningpet.app.view.App;
import planningpet.home.view.MainScreen;
import planningpet.schedule.model.Task;
import planningpet.schedule.view.TaskDialog;
import planningpet.schedule.view.TaskForm;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 홈 결정(D-74, D-86, D-88, D-90, D-91)의 06 검사 줄. */
final class HomeCheck {
    private HomeCheck() {
    }

    static void run() {
        added();
        doneGroup();
        calendar();
        weekStart();
    }

    static LocalDate d(String monthDay) {
        return LocalDate.parse("2026-" + monthDay);
    }

    static Store at(String monthDayTime) {
        Store s = MockData.empty();
        s.now = LocalDateTime.parse("2026-" + monthDayTime);
        return s;
    }

    /** T-TALK-23. 홈에서 넣으면 펫이 이름과 오늘 남은 수를 말한다(D-90). */
    static void added() {
        Store s = at("11-16T09:00");
        s.book.add(Task.Kind.TASK, "과제", d("11-16"), null, 30, "", null);
        App app = Main.create(s);
        MainScreen main = ProtoCheck.find(app.root, MainScreen.class);
        main.quick.setText("통계 레포트");
        main.quick.postActionEvent();
        String today = s.talk;
        main.selectDate(d("11-20"));
        main.quick.setText("은행 서류");
        main.quick.postActionEvent();
        String other = s.talk;
        app.show("일정관리");
        TaskForm f = new TaskForm(null, d("11-17"), s.now);
        f.title.setText("운동");
        boolean saved = TaskDialog.trySave(app, f, null);
        eq("T-TALK-23", "'통계 레포트' 넣었구나! 오늘 남은 일 2개야. / '은행 서류' 11/20에 넣었어. / true '은행 서류' 11/20에 넣었어.",
                today + " / " + other + " / " + saved + " " + s.talk);
    }

    /** T-LIST-8. 끝낸 줄은 제자리에 머문 뒤 아래 "끝낸 일" 묶음으로 가고, 묶음은 접힌다(D-88). */
    static void doneGroup() {
        Store s = at("11-16T08:00");
        s.book.add(Task.Kind.TASK, "A", d("11-16"), java.time.LocalTime.of(9, 0), 30, "", null);
        Task b = s.book.add(Task.Kind.TASK, "B", d("11-16"), null, 30, "", null).get(0);
        s.book.add(Task.Kind.TASK, "C", d("11-16"), null, 30, "", null);
        App app = Main.create(s);
        MainScreen main = ProtoCheck.find(app.root, MainScreen.class);
        app.redraw();
        PeriodCheck.box(main.rows, b).doClick();
        String stay = rows(main.rows) + " " + main.rows.settling();
        main.rows.settle();
        String settled = rows(main.rows);
        main.rows.doneHead.doClick();
        String folded = rows(main.rows);
        main.rows.doneHead.doClick();
        PeriodCheck.box(main.rows, b).doClick();
        String back = rows(main.rows) + " " + main.rows.settling();
        eq("T-LIST-8", "[ ] A 09:00~09:30 30분 / 시각 없는 할 일 / [x] B 시각 정하기 30분 / [ ] C 시각 정하기 30분 true"
                + " | [ ] A 09:00~09:30 30분 / 시각 없는 할 일 / [ ] C 시각 정하기 30분 / 끝낸 일 1개 / [x] B 시각 정하기 30분"
                + " | [ ] A 09:00~09:30 30분 / 시각 없는 할 일 / [ ] C 시각 정하기 30분 / 끝낸 일 1개"
                + " | [ ] A 09:00~09:30 30분 / 시각 없는 할 일 / [ ] B 시각 정하기 30분 / [ ] C 시각 정하기 30분 false",
                stay + " | " + settled + " | " + folded + " | " + back);
    }

    /** 목록을 줄마다 "[x] 제목 시각 분"처럼 읽는다(QA의 rows와 같다). */
    static String rows(java.awt.Container list) {
        java.util.List<String> out = new java.util.ArrayList<>();
        for (java.awt.Component c : list.getComponents()) {
            java.util.List<String> parts = new java.util.ArrayList<>();
            collect(c, parts);
            out.add(String.join(" ", parts));
        }
        return String.join(" / ", out);
    }

    private static void collect(java.awt.Component c, java.util.List<String> parts) {
        if (c instanceof javax.swing.JCheckBox b) {
            parts.add(b.isSelected() ? "[x]" : "[ ]");
        } else if (c instanceof javax.swing.JLabel l && !plain(l.getText()).isEmpty()) {
            parts.add(plain(l.getText()));
        } else if (c instanceof javax.swing.JButton b) {
            if (!b.getText().isBlank()) {
                parts.add(b.getText());
            }
        } else if (c instanceof java.awt.Container k) {
            for (java.awt.Component child : k.getComponents()) {
                collect(child, parts);
            }
        }
    }

    static String plain(String html) {
        return html == null ? "" : html.replaceAll("(?i)<br>", " ").replaceAll("<[^>]+>", "").replace("&lt;", "<")
                .replace("&gt;", ">").replace("&amp;", "&").replaceAll("\\s+", " ").strip();
    }

    /** T-CAL-5의 빨간 점과 툴팁, T-CAL-7. 고정 일정은 설정을 켜야 달력 칸 수에 든다(D-86). */
    static void calendar() {
        Store s = at("11-18T09:00");
        s.book.add(Task.Kind.FIXED, "수업", d("11-16"), java.time.LocalTime.of(10, 0), 75, "", null);
        s.book.add(Task.Kind.FIXED, "수업", d("11-19"), java.time.LocalTime.of(10, 0), 75, "", null);
        s.book.add(Task.Kind.TASK, "과제", d("11-19"), null, 30, "", null);
        App app = Main.create(s);
        app.redraw();
        MainScreen main = ProtoCheck.find(app.root, MainScreen.class);
        String off = cell(main, 16) + ", " + cell(main, 19);
        planningpet.settings.view.SettingsScreen settings = ProtoCheck.find(app.root, planningpet.settings.view.SettingsScreen.class);
        settings.calendarFixed.doClick();
        eq("T-CAL-7", "16, 19 1개(진함) / 16 1개(흐림), 19 2개(진함) true", off + " / " + cell(main, 16) + ", " + cell(main, 19)
                + " " + s.calendarFixed);
        Store p = at("11-18T09:00");
        p.book.add(new planningpet.schedule.model.TaskDraft(Task.Kind.TASK, "E사 지원서", d("11-16"), null, 120, "")
                .withDue(d("11-22")));
        App pa = Main.create(p);
        pa.redraw();
        MainScreen pm = ProtoCheck.find(pa.root, MainScreen.class);
        eq("T-CAL-5 빨간 점", "22 1개 마감(진함) E사 지원서 마감", cell(pm, 22) + " " + tip(pm, 22));
    }

    /** 홈 달력 칸의 글. 끝내지 않은 일이 있으면 "(진함)", 모두 끝냈으면 "(흐림)"(QA의 cell과 같다). */
    static String cell(MainScreen main, int day) {
        javax.swing.JLabel l = label(main, day);
        if (l == null) {
            return "없음";
        }
        String t = l.getText();
        return plain(t) + (t.contains("#28977F") ? "(진함)" : t.contains("#8A96A0") ? "(흐림)" : "");
    }

    static String tip(MainScreen main, int day) {
        return label(main, day).getToolTipText();
    }

    private static javax.swing.JLabel label(MainScreen main, int day) {
        for (java.awt.Component c : main.calendar.grid.getComponents()) {
            if (c instanceof javax.swing.JLabel l && plain(l.getText()).matches(day + "( .*)?")) {
                return l;
            }
        }
        return null;
    }

    /** T-DEF-15, T-AUTO-16, T-STAT-10과 화면. 한 주 시작(D-91)을 앱 전체가 따른다. */
    static void weekStart() {
        java.time.DayOfWeek sun = java.time.DayOfWeek.SUNDAY;
        eq("T-DEF-15", "2026-11-21 2026-11-28 2026-11-28 2026-11-22",
                planningpet.schedule.model.DefaultDates.due(d("11-18"), sun) + " " + planningpet.schedule.model.DefaultDates.due(d("11-21"), sun)
                + " " + planningpet.schedule.model.DefaultDates.due(d("11-22"), sun) + " "
                + planningpet.schedule.model.DefaultDates.forSchedule(d("11-22"), d("11-18")));
        Store mon = at("11-16T15:23");
        mon.book.add(Task.Kind.TASK, "Q", d("11-22"), null, 30, "", null);
        var placed = planningpet.schedule.model.Planner.place(mon, d("11-16")).placed();
        Store su = at("11-16T15:23");
        su.weekStart = sun;
        su.book.add(Task.Kind.TASK, "Q", d("11-22"), null, 30, "", null);
        eq("T-AUTO-16", "Q 2026-11-22 09:00 / 0", placed.get(0).task().title + " " + placed.get(0).day() + " " + placed.get(0).start()
                + " / " + planningpet.schedule.model.Planner.targets(su, d("11-15")).size());
        Store st = at("11-17T09:00");
        st.weekStart = sun;
        planningpet.stats.model.Stats.Period w = planningpet.stats.model.Stats.Period.week(st.today(), sun);
        StringBuilder cells = new StringBuilder();
        planningpet.stats.model.Stats.trend(st, planningpet.stats.model.Stats.Period.month(d("11-14")))
                .forEach(b -> cells.append(b.from().getDayOfMonth()).append("-").append(b.to().getDayOfMonth()).append(" "));
        eq("T-STAT-10", "2026-11-15~2026-11-21 1-7 8-14 15-21 22-28 29-30 ", w.from() + "~" + w.to() + " " + cells);

        App app = Main.create(MockData.sample());
        app.redraw();
        MainScreen main = ProtoCheck.find(app.root, MainScreen.class);
        planningpet.schedule.view.ScheduleScreen sched = ProtoCheck.find(app.root, planningpet.schedule.view.ScheduleScreen.class);
        planningpet.stats.view.StatsScreen stats = ProtoCheck.find(app.root, planningpet.stats.view.StatsScreen.class);
        planningpet.settings.view.SettingsScreen settings = ProtoCheck.find(app.root, planningpet.settings.view.SettingsScreen.class);
        String before = main.calendar.weekText() + " " + firstCell(main) + " " + sched.weekStart() + " " + stats.period().from()
                + " " + planningpet.common.view.Ui.dateOf(TaskDialog.form(app, null, d("11-14")).due);
        settings.sunday.doClick();
        planningpet.schedule.view.TaskForm f = TaskDialog.form(app, null, d("11-14"));
        String after = main.calendar.weekText() + " " + firstCell(main) + " " + sched.weekStart() + " " + stats.period().from()
                + " " + planningpet.common.view.Ui.dateOf(f.due) + " " + app.store.weekStart;
        eq("D-91 설정 한 주 시작을 앱 전체가 따른다", "월화수목금토일 빈칸 2026-11-09 2026-11-09 2026-11-15 / 일월화수목금토 1 2026-11-08 2026-11-08 2026-11-21 SUNDAY",
                before + " / " + after);
    }

    /** 달력 첫 칸의 글. 빈 칸이면 "빈칸". */
    private static String firstCell(MainScreen main) {
        String t = plain(((javax.swing.JLabel) main.calendar.grid.getComponent(0)).getText());
        return t.isEmpty() ? "빈칸" : t.split(" ")[0];
    }
}
