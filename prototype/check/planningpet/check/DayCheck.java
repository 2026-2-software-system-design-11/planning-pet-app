package planningpet.check;

import static planningpet.check.ProtoCheck.eq;
import static planningpet.check.PeriodCheck.at;
import static planningpet.check.PeriodCheck.d;

import planningpet.app.Main;
import planningpet.app.model.Store;
import planningpet.app.view.App;
import planningpet.reward.model.DailyProgress;
import planningpet.reward.model.Game;
import planningpet.schedule.model.*;
import planningpet.schedule.view.*;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** 06 검사 표 가운데 앱 하루의 시간표(D-77), 겹침 묻기(D-87), 시간표 스크롤(D-89)의 줄. ProtoCheck가 부른다. */
final class DayCheck {
    private DayCheck() {
    }

    static void run() {
        nightDay();
        overlaps();
        narrowTips();
        scrolls();
    }

    /** D-89: 시간표가 처음 보이는 자리와 자동 배치 뒤. */
    private static void scrolls() {
        java.util.function.BiFunction<String, String, Integer> top = (monday, now) ->
                Planner.top(d(monday), java.time.LocalDateTime.parse("2026-" + now));
        eq("T-TT-8", "1150 1470 480 480", top.apply("11-16", "11-18T20:10") + " " + top.apply("11-16", "11-21T01:30") + " "
                + top.apply("11-23", "11-18T20:10") + " " + top.apply("11-16", "11-16T00:30"));
        App app = Main.create(at("11-18T20:10"));
        Task hw = task(app.store, "과제", "11-18", hm(21, 0), 30);
        ScheduleScreen sched = ProtoCheck.find(app.root, ScheduleScreen.class);
        app.show("일정관리");
        int opened = sched.topMinute();
        app.schedule.check(hw);
        int afterCheck = sched.topMinute();
        sched.move(sched.weekStart().plusWeeks(1));
        int nextWeek = sched.topMinute();
        sched.move(sched.weekStart().minusWeeks(1));
        int back = sched.topMinute();
        app.show("홈");
        sched.move(sched.weekStart());
        app.show("일정관리");
        eq("T-TT-8 화면", "1150 1150 480 1150 1150", opened + " " + afterCheck + " " + nextWeek + " " + back + " " + sched.topMinute());

        App pa = Main.create(at("11-18T09:00"));
        Task a = task(pa.store, "A", "11-18", null, 30);
        Task b = task(pa.store, "B", "11-20", null, 60);
        ScheduleScreen ps = ProtoCheck.find(pa.root, ScheduleScreen.class);
        pa.show("일정관리");
        ps.scrollTo(0);
        TimetablePanel table = ProtoCheck.find(pa.root, TimetablePanel.class);
        AutoPlaceDialog.apply(pa, Planner.place(pa.store, d("11-16")));
        boolean lit = table.flashing().containsAll(List.of(a, b)) && table.flashing().size() == 2;
        int afterApply = ps.topMinute();
        pa.store.changed();
        eq("T-TT-9", "11-18 09:00 11-20 09:00 480 true 2000", a.date.toString().substring(5) + " " + a.start + " "
                + b.date.toString().substring(5) + " " + b.start + " " + afterApply + " " + lit + " " + TimetablePanel.FLASH_MILLIS);
        javax.swing.Timer timer = table.flashTimer();
        String timing = timer.getInitialDelay() + " " + timer.isRepeats();
        for (java.awt.event.ActionListener l : timer.getActionListeners()) {
            l.actionPerformed(null);
        }
        eq("T-TT-9 2초 뒤", "2000 false true", timing + " " + table.flashing().isEmpty());
    }

    /** 앱 하루의 분으로 시각을 준 할 일 하나를 넣는다. */
    static Task task(Store s, String title, String date, Integer minute, int minutes) {
        return s.book.add(TaskDraft.at(Task.Kind.TASK, title, d(date), minute, minutes)).get(0);
    }

    static int hm(int h, int m) {
        return h * 60 + m;
    }

    /** D-77: 하루 칸은 다음 날 04:00(28:00)까지다. */
    private static void nightDay() {
        Store s = at("11-16T09:00");
        String texts = List.of(task(s, "a", "11-16", hm(9, 0), 30), task(s, "b", "11-16", hm(23, 30), 30),
                task(s, "c", "11-16", hm(23, 30), 90), task(s, "d", "11-16", hm(25, 30), 30)).stream()
                .map(TaskBook::timeText).collect(Collectors.joining(", "));
        eq("T-LIST-2", "09:00~09:30, 23:30~24:00, 23:30~01:00(다음 날), 01:30~02:00(다음 날)", texts);

        eq("T-DEF-14", "01:30(다음 날) 09:00 03:55(다음 날)",
                Times.value(DefaultDates.start(d("11-20"), java.time.LocalDateTime.parse("2026-11-21T01:28"))) + " "
                + Times.value(DefaultDates.start(d("11-21"), java.time.LocalDateTime.parse("2026-11-21T01:28"))) + " "
                + Times.value(DefaultDates.start(d("11-20"), java.time.LocalDateTime.parse("2026-11-21T03:58"))));
        Store early = at("11-21T01:28");
        App ea = Main.create(early);
        TaskForm ef = TaskDialog.form(ea, null, early.today());
        ef.timed.setSelected(true);
        ef.sync();
        eq("T-DEF-14 입력 창", "2026-11-20 01:30(다음 날) (토 01:30) 02:00(다음 날)에 끝나요",
                planningpet.common.view.Ui.dateOf(ef.date) + " " + ef.start.getValue() + " " + ef.realTime.getText() + " "
                + ef.endLabel.getText());

        Store n = at("11-21T01:30");
        App app = Main.create(n);
        Task a = task(n, "A", "11-20", hm(23, 40), 30);
        task(n, "B", "11-20", hm(8, 0), 60);
        task(n, "C", "11-20", null, 30);
        TaskForm f = TaskDialog.form(app, null, n.today());
        f.title.setText("복습");
        f.timed.setSelected(true);
        f.sync();
        boolean saved = TaskDialog.trySave(app, f, null);
        Task review = n.tasks.stream().filter(t -> t.title.equals("복습")).findFirst().orElseThrow();
        TaskBook.DayPlan plan = n.book.dayPlan(d("11-20"));
        eq("T-LIST-7", "true 2026-11-20 01:30 true / B A 복습 / false / 0 / 4", saved + " " + review.date + " " + review.start
                + " " + review.nextDay + " / " + plan.timed().stream().map(t -> t.title).collect(Collectors.joining(" "))
                + " / " + n.book.dayPlan(d("11-21")).timed().contains(review) + " / "
                + DailyProgress.of(n.book, n.today()).done() + " / " + DailyProgress.of(n.book, n.today()).total());

        // 시간표: 복습은 11-20 칸 자정 아래, A는 23:40부터 자정 너머로 이어진다.
        app.show("일정관리");
        TimetablePanel table = ProtoCheck.find(app.root, TimetablePanel.class);
        Map<Rectangle, Task> blocks = paint(table);
        Rectangle rr = rect(blocks, review);
        Rectangle ra = rect(blocks, a);
        int hour = TimetablePanel.HOUR;
        eq("D-77 시간표 자정 아래", "28 true true true", table.getPreferredSize().height / hour + " "
                + (rr.y == (24 * 60 + 90) * hour / 60) + " " + (friday(rr) && friday(ra))
                + " " + (ra.y + ra.height > 24 * hour));

        Store g = at("11-21T01:45");
        Task late = task(g, "복습", "11-20", hm(25, 30), 30);
        new Game(g).complete(late);
        eq("T-PRG-7", "2026-11-20 1/1 2026-11-20", late.rewardDay + " " + DailyProgress.of(g.book, g.today()).done() + "/"
                + DailyProgress.of(g.book, g.today()).total() + " " + g.rewardCountDay);

        Store c = at("11-16T09:00");
        Task ca = task(c, "A", "11-16", hm(23, 30), 90);
        Task cb = task(c, "B", "11-16", hm(24, 30), 30);
        var cols = Planner.columns(List.of(ca, cb));
        eq("T-TT-6", "0/2 1/2", cols.get(ca)[0] + "/" + cols.get(ca)[1] + " " + cols.get(cb)[0] + "/" + cols.get(cb)[1]);

        Store v = at("11-16T09:00");
        String vals = PeriodCheck.msgs(TaskCheck.check(TaskDraft.at(Task.Kind.TASK, "밤", d("11-16"), hm(23, 30), 40), false)) + " / "
                + PeriodCheck.msgs(TaskCheck.check(TaskDraft.at(Task.Kind.TASK, "밤", d("11-16"), hm(27, 30), 30), false)) + " / "
                + PeriodCheck.msgs(TaskCheck.check(TaskDraft.at(Task.Kind.TASK, "밤", d("11-16"), hm(27, 30), 40), false));
        eq("T-VAL-8, T-VAL-20", "오류 없음 / 오류 없음 / 시작 시각: 다음 날 04:00까지 끝나야 해요", vals);

        // 자동 배치는 자정 앞까지만 놓는다(잠자는 시간에 놓지 않는다).
        Store p = at("11-16T23:40");
        task(p, "E", "11-16", null, 30);
        eq("D-77 자동 배치는 24:00까지", "0 E", Planner.place(p, d("11-16")).placed().size() + " "
                + Planner.place(p, d("11-16")).unplaced().get(0).title);

        // 수정 창은 다음 날 시각을 그대로 보이고 저장해도 그대로다.
        TaskForm edit = TaskDialog.form(app, review, review.date);
        eq("D-77 수정 창", "01:30(다음 날) (토 01:30)", edit.start.getValue() + " " + edit.realTime.getText());
    }

    /** 물음을 적어 두고 answer번째 버튼을 고른다. */
    static String[] ask(App app, int answer) {
        String[] asked = {""};
        app.schedule.chooser = new planningpet.schedule.controller.TaskController.Chooser() {
            @Override
            public int choose(String title, String message, String[] options) {
                return choose(title, message, options, options.length - 1);
            }

            @Override
            public int choose(String title, String message, String[] options, int initial) {
                asked[0] += title + ": " + message + " " + String.join(", ", options) + " 처음 " + options[initial] + " | ";
                return answer;
            }
        };
        return asked;
    }

    static TaskForm form(App app, String title, String date, String start, int minutes) {
        TaskForm f = TaskDialog.form(app, null, d(date));
        f.title.setText(title);
        if (start != null) {
            f.timed.setSelected(true);
            f.sync();
            f.start.setValue(start);
        }
        f.minutes.setValue(minutes);
        return f;
    }

    /** D-87: 겹치면 막지 않고 저장 전에 묻는다. */
    private static void overlaps() {
        App app = Main.create(at("11-16T09:00"));
        Store s = app.store;
        task(s, "자소서 초안", "11-16", hm(14, 0), 60);
        String[] asked = ask(app, 1);
        boolean cancelled = TaskDialog.trySave(app, form(app, "면접 준비", "11-16", "14:30", 60), null);
        String q1 = asked[0];
        int afterCancel = s.book.timedOn(d("11-16")).size();
        asked = ask(app, 0);
        boolean saved = TaskDialog.trySave(app, form(app, "면접 준비", "11-16", "14:30", 60), null);
        eq("T-OVL-1", "시간이 겹쳐요: 14:00~15:00 '자소서 초안'과 30분 겹쳐요. 그래도 넣을까요? 넣기, 취소 처음 넣기 | "
                + "false 1 true 2", q1 + cancelled + " " + afterCancel + " " + saved + " "
                + s.book.timedOn(d("11-16")).size());

        App b = Main.create(at("11-16T09:00"));
        task(b.store, "자소서 초안", "11-16", hm(14, 0), 60);
        String[] none = ask(b, 1);
        boolean all = TaskDialog.trySave(b, form(b, "가", "11-16", "15:00", 30), null)
                && TaskDialog.trySave(b, form(b, "나", "11-16", "13:00", 60), null)
                && TaskDialog.trySave(b, form(b, "다", "11-16", null, 30), null);
        eq("T-OVL-2", "true ", all + " " + none[0]);

        App c = Main.create(at("11-16T09:00"));
        c.store.book.add(TaskDraft.at(Task.Kind.FIXED, "실습", d("11-16"), hm(8, 0), 480));
        task(c.store, "과제", "11-16", hm(8, 30), 30);
        String[] q3 = ask(c, 1);
        TaskDialog.trySave(c, form(c, "일지", "11-16", "08:00", 60), null);
        eq("T-OVL-3", "08:00~16:00 '실습'과 60분 겹쳐요. 그래도 넣을까요?", q3[0].replaceAll("^시간이 겹쳐요: | 넣기, 취소.*$", ""));

        App r = Main.create(at("11-16T09:00"));
        Task hw = task(r.store, "과제", "11-17", hm(18, 30), 60);
        Task report = task(r.store, "보고서", "11-16", hm(10, 0), 30);
        String[] q4 = ask(r, 0);
        TaskForm job = form(r, "알바", "11-16", "18:00", 240);
        job.fixed.doClick();
        job.repeat.setSelectedIndex(2);
        for (int i = 0; i < 7; i++) {
            job.days[i].setSelected(i == 1);
        }
        planningpet.common.view.Ui.setDate(job.repeatEnd, d("11-30"));
        job.sync();
        boolean jobSaved = TaskDialog.trySave(r, job, null);
        String first = q4[0];
        q4[0] = "";
        TaskForm edit = TaskDialog.form(r, report, report.date);
        edit.start.setValue("10:15");
        boolean reportSaved = TaskDialog.trySave(r, edit, report);
        String second = q4[0];
        TimeEditor te = new TimeEditor(hw, r.store.now);
        te.start.setValue("19:00");
        boolean hwSaved = TimeEditor.save(r, hw, te);
        eq("T-OVL-4", "true 18:30~19:30 '과제'와 60분 겹쳐요. 그래도 넣을까요? / true  / true "
                + "18:00~22:00 '알바'와 60분 겹쳐요. 그래도 넣을까요?",
                jobSaved + " " + first.replaceAll("^시간이 겹쳐요: | 넣기, 취소.*$", "") + " / " + reportSaved + " " + second
                + " / " + hwSaved + " " + q4[0].replaceAll("^시간이 겹쳐요: | 넣기, 취소.*$", ""));
    }

    /** D-87: 좁아진 블록은 마우스를 올리면 전체 제목과 시각을 보인다. */
    private static void narrowTips() {
        App app = Main.create(at("11-16T09:00"));
        Task a = task(app.store, "자소서 초안", "11-16", hm(14, 0), 60);
        Task b = task(app.store, "면접 준비", "11-16", hm(14, 30), 60);
        b.memo = "질문 목록";
        Task c = task(app.store, "운동", "11-16", hm(18, 0), 60);
        app.store.changed();
        app.show("일정관리");
        TimetablePanel table = ProtoCheck.find(app.root, TimetablePanel.class);
        Map<Rectangle, Task> blocks = paint(table);
        eq("T-TT-7", "50 50 / <b>자소서 초안</b> 14:00~15:00 / <b>면접 준비</b> 14:30~15:30<br>질문 목록 / null",
                rect(blocks, a).width + 2 + " " + (rect(blocks, b).width + 2) + " / " + body(table.tip(a)) + " / "
                + body(table.tip(b)) + " / " + table.tip(c));
    }

    static String body(String html) {
        return html == null ? null : html.replaceAll("^<html><div style='width:280px'>|</div></html>$", "");
    }

    /** 시간표를 창 없이 그리고 블록 자리를 돌려준다. */
    static Map<Rectangle, Task> paint(TimetablePanel table) {
        table.setSize(TimetablePanel.GUTTER + 7 * 100, table.getPreferredSize().height);
        BufferedImage img = new BufferedImage(table.getWidth(), table.getHeight(), BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D gr = img.createGraphics();
        table.paint(gr);
        gr.dispose();
        return table.blockAreas();
    }

    /** 11-16 주의 금요일(11-20) 칸 안인가. */
    static boolean friday(Rectangle r) {
        int left = TimetablePanel.GUTTER + 4 * 100;
        return r.x >= left && r.x < left + 100;
    }

    static Rectangle rect(Map<Rectangle, Task> blocks, Task t) {
        return blocks.entrySet().stream().filter(e -> e.getValue() == t).map(Map.Entry::getKey).findFirst().orElse(new Rectangle());
    }
}
