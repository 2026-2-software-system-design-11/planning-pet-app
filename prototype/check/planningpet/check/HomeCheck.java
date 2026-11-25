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
        periodStays();
        calendar();
        weekStart();
        examples();
        editedSamples();
        sundayLectures();
    }

    /** T-EX-5. 일요일에 예시로 시작해도 다음 주 시간표에 수업이 보인다(D-94). */
    static void sundayLectures() {
        Store s = at("11-22T09:00");
        new planningpet.pet.view.PickDialog().apply(s, true);
        StringBuilder next = new StringBuilder();
        for (LocalDate day = d("11-23"); day.isBefore(d("11-30")); day = day.plusDays(1)) {
            for (Task t : s.book.dayPlan(day).timed()) {
                if (t.kind == Task.Kind.FIXED) {
                    next.append(day.toString().substring(5)).append(" ").append(t.title).append(", ");
                }
            }
        }
        eq("T-EX-5", "11-23 자료구조, 11-24 운영체제, 11-25 자료구조, 11-26 운영체제, ", next.toString());
    }

    /** T-EX-4. 사용자가 고친 예시는 예시 지우기가 남긴다(D-94). */
    static void editedSamples() {
        Store s = at("11-18T09:00");
        planningpet.pet.view.PickDialog pd = new planningpet.pet.view.PickDialog();
        pd.apply(s, true);
        App app = Main.create(s);
        MainScreen main = ProtoCheck.find(app.root, MainScreen.class);
        Task draft = s.tasks.stream().filter(t -> t.title.equals("자소서 초안")).findFirst().orElseThrow();
        TaskForm f = new TaskForm(draft, draft.date, s.now);
        f.title.setText("내 자소서 진짜 초안");
        f.memo.setText("2번 문항부터");
        TaskDialog.trySave(app, f, draft);
        int all = s.tasks.size();
        java.util.List<String> asked = new java.util.ArrayList<>();
        app.schedule.chooser = (title, message, options) -> {
            asked.add(message);
            return 0;
        };
        main.sampleButton.doClick();
        eq("T-EX-4", "예시 일정 " + (all - 1) + "개를 지울까요? 새로 넣은 일정은 그대로 남아요. 고친 예시 1개는 남아요. / 내 자소서 진짜 초안 0",
                asked.get(0) + " / " + String.join(" ", s.tasks.stream().map(t -> t.title).toList()) + " " + s.sampleIds.size());
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
        eq("T-TALK-23", "'통계 레포트' 넣었구나! 오늘 남은 일 2개야. / '은행 서류' 11/20에 넣었어. / true '운동' 11/17에 넣었어.",
                today + " / " + other + " / " + saved + " " + s.talk);
        Store e = at("11-16T09:00");
        new planningpet.pet.view.PickDialog().apply(e, false);
        App ea = Main.create(e);
        ea.control.windowShown();
        ea.show("일정관리");
        e.changed();
        TaskForm fixed = new TaskForm(null, d("11-16"), e.now);
        fixed.title.setText("자료구조");
        fixed.fixed.doClick();
        fixed.start.setValue("09:00");
        boolean put = TaskDialog.trySave(ea, fixed, null);
        String hint = HomeCheck.plain(ProtoCheck.find(ea.root, planningpet.schedule.view.ScheduleScreen.class).bubble.getText());
        ea.show("홈");
        eq("T-TALK-25", "true 지금 자동 배치할 할 일은 없어. / '자료구조' 넣었구나! 오늘 남은 일 1개야.", put + " " + hint + " / "
                + ProtoCheck.find(ea.root, planningpet.home.view.PetCard.class).bubbleText());
    }

    /** T-LIST-10. 기간 할 일도 체크한 자리(시각 줄, "기간 안에 할 일")에 머문 뒤 "끝낸 일"로 간다(D-94). */
    static void periodStays() {
        Store s = at("11-18T08:00");
        Task job = PeriodCheck.period(s, "지원서", "11-16", "11-22", 60);
        s.book.setTime(job, java.time.LocalTime.of(9, 0), 60, d("11-18"), false);
        Task report = PeriodCheck.period(s, "보고서", "11-16", "11-25", 30);
        s.book.add(Task.Kind.TASK, "토익", d("11-18"), null, 30, "", null);
        App app = Main.create(s);
        MainScreen main = ProtoCheck.find(app.root, MainScreen.class);
        app.redraw();
        String before = rows(main.rows);
        PeriodCheck.box(main.rows, job).doClick();
        String first = rows(main.rows);
        main.rows.settle();
        PeriodCheck.box(main.rows, report).doClick();
        String second = rows(main.rows);
        main.rows.settle();
        eq("T-LIST-10", "[ ] 지원서 09:00~10:00 60분 ~11/22까지 / 시각 없는 할 일 / [ ] 토익 시각 정하기 30분 / 기간 안에 할 일"
                + " / [ ] 보고서 시각 정하기 30분 ~11/25까지 | [x] 지원서 09:00~10:00 60분 ~11/22까지 / 시각 없는 할 일 / [ ] 토익"
                + " 시각 정하기 30분 / 기간 안에 할 일 / [ ] 보고서 시각 정하기 30분 ~11/25까지 | 시각 없는 할 일 / [ ] 토익 시각 정하기 30분"
                + " / 기간 안에 할 일 / [x] 보고서 시각 정하기 30분 ~11/25까지 / 끝낸 일 1개 / [x] 지원서 시각 정하기 60분 ~11/22까지"
                + " | 시각 없는 할 일 / [ ] 토익 시각 정하기 30분 / 끝낸 일 2개 / [x] 지원서 시각 정하기 60분 ~11/22까지 / [x] 보고서 시각 정하기"
                + " 30분 ~11/25까지", before + " | " + first + " | " + second + " | " + rows(main.rows));
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

    /** T-EX-1~3. 첫 실행의 예시로 둘러보기, 예시 지우기, 바로 시작(D-74). */
    static void examples() {
        Store s = at("11-18T09:00");
        planningpet.pet.view.PickDialog pd = new planningpet.pet.view.PickDialog();
        pd.name.setText("콩");
        pd.apply(s, true);
        App app = Main.create(s);
        app.control.windowShown();
        MainScreen main = ProtoCheck.find(app.root, MainScreen.class);
        // D-94: 수업은 4주 요일 반복 사본이다. 수와 첫날, 끝날을 본다.
        java.util.List<String> days = s.tasks.stream().filter(t -> t.kind == Task.Kind.FIXED && t.seriesId != null)
                .map(t -> t.date.toString().substring(5)).sorted().toList();
        String fixed = days.size() + " " + days.get(0) + " " + days.get(days.size() - 1) + " ";
        eq("T-EX-1", "21 true " + "16 11-16 12-10 / [ ] 고정 자료구조 09:00~10:15 75분 / [ ] 자소서 초안 메모 14:00~15:30 90분"
                + " / 시각 없는 할 일 / [ ] 토익 단어 시각 정하기 30분 / 기간 안에 할 일 / [ ] E사 지원서 시각 정하기 120분 ~11/22까지"
                + " / 끝낸 일 1개 / [x] 알고리즘 과제 2번 10:00~11:00 60분 / 0P / 처음 만났네! 나는 콩이야. 예시 일정을 넣어 뒀어. / true",
                s.tasks.size() + " " + s.tasks.stream().allMatch(t -> s.sampleIds.contains(t.id)) + " " + fixed + "/ " + rows(main.rows)
                + " / " + s.points + "P / " + s.talk + " / " + main.sampleButton.isVisible());
        main.quick.setText("과제");
        main.quick.postActionEvent();
        PeriodCheck.box(main.rows, s.tasks.stream().filter(t -> t.title.equals("토익 단어")).findFirst().orElseThrow()).doClick();
        main.rows.settle();
        java.util.List<String> asked = new java.util.ArrayList<>();
        int[] answer = {1};
        app.schedule.chooser = (title, message, options) -> {
            asked.add(title + " / " + message + " / " + String.join(", ", options));
            return answer[0];
        };
        main.sampleButton.doClick();
        String cancelled = s.tasks.size() + " " + s.sampleIds.size();
        answer[0] = 0;
        main.sampleButton.doClick();
        planningpet.settings.view.SettingsScreen settings = ProtoCheck.find(app.root, planningpet.settings.view.SettingsScreen.class);
        eq("T-EX-2", "예시 지우기 / 예시 일정 21개를 지울까요? 새로 넣은 일정은 그대로 남아요. / 지우기, 취소 | 22 21 | 과제 10P 0 false false 예시 일정이 없어요"
                + " 예시를 지웠어. 수업부터 넣어 볼까?",
                asked.get(0) + " | " + cancelled + " | " + String.join(" ", s.tasks.stream().map(t -> t.title).toList()) + " " + s.points + "P "
                + s.sampleIds.size() + " " + main.sampleButton.isVisible() + " " + settings.clearSamples.isEnabled() + " "
                + settings.sampleCount.getText() + " " + s.talk);
        Store e = at("11-18T09:00");
        new planningpet.pet.view.PickDialog().apply(e, false);
        App ea = Main.create(e);
        ea.control.windowShown();
        eq("T-EX-3", "0 0 false 처음 만났네! 나는 나비야. 수업부터 넣어 볼까?", e.tasks.size() + " " + e.sampleIds.size() + " "
                + ProtoCheck.find(ea.root, MainScreen.class).sampleButton.isVisible() + " " + e.talk);
    }
}
