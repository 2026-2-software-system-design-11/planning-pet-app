package planningpet.check;

import static planningpet.check.ProtoCheck.eq;

import planningpet.app.Main;
import planningpet.app.model.MockData;
import planningpet.app.model.Store;
import planningpet.app.view.App;
import planningpet.common.view.Ui;
import planningpet.home.view.MainScreen;
import planningpet.pet.model.Talk;
import planningpet.reward.model.DailyProgress;
import planningpet.reward.model.Game;
import planningpet.schedule.model.*;
import planningpet.schedule.view.*;
import planningpet.stats.model.Stats;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/** 06 검사 표 가운데 D-64(반복)와 D-65(기간 할 일)의 줄. ProtoCheck가 부른다. */
final class PeriodCheck {
    private PeriodCheck() {
    }

    /** "11-16"은 2026년, "2027-03-15"는 그대로. */
    static LocalDate d(String s) {
        return LocalDate.parse(s.length() == 5 ? "2026-" + s : s);
    }

    static TaskDraft draft(Task.Kind kind, String date, String start) {
        return new TaskDraft(kind, "일", d(date), start == null ? null : LocalTime.parse(start), 30, "");
    }

    /** 검사 문장을 "칸: 문장"으로 잇는다. 통과면 "오류 없음". */
    static String msgs(List<String[]> p) {
        return p.isEmpty() ? "오류 없음" : p.stream().map(x -> x[0] + ": " + x[1]).collect(Collectors.joining(" / "));
    }

    static Task period(Store s, String title, String date, String due, int minutes) {
        return s.book.add(new TaskDraft(Task.Kind.TASK, title, d(date), null, minutes, "").withDue(d(due))).get(0);
    }

    static Store at(String dateTime) {
        Store s = MockData.empty();
        s.now = java.time.LocalDateTime.parse("2026-" + dateTime);
        return s;
    }

    static void run() {
        validate();
        add();
        edit();
        lists();
        rewards();
        talkAndCalendar();
        autoPlace();
    }

    static void validate() {
        TaskDraft task = draft(Task.Kind.TASK, "11-16", null);
        eq("T-VAL-11", "오류 없음", msgs(TaskCheck.check(task.withRepeat(Repeat.DAILY, d("12-13")), false)));
        eq("T-VAL-13", "오류 없음 / 반복: 반복이 너무 많아요. 끝 날짜를 앞당겨 주세요",
                msgs(TaskCheck.check(task.withRepeat(Repeat.DAILY, d("2027-03-15")), false)) + " / "
                + msgs(TaskCheck.check(task.withRepeat(Repeat.DAILY, d("2027-03-16")), false)));
        eq("D-64 요일은 하나 이상", "반복: 요일을 하나 이상 골라 주세요",
                msgs(TaskCheck.check(task.withRepeat(Repeat.weekly(List.of()), d("11-30")), false)));
        Repeat wed = Repeat.weekly(DayOfWeek.WEDNESDAY);
        eq("D-64 반복하는 날 없음", "반복: 끝 날짜 안에 반복하는 날이 없어요 / 오류 없음",
                msgs(TaskCheck.check(task.withRepeat(wed, d("11-17")), false)) + " / "
                + msgs(TaskCheck.check(task.withRepeat(wed, d("11-18")), false)));
        TaskDraft p18 = draft(Task.Kind.TASK, "11-18", null);
        eq("D-65 마감은 날짜와 같거나 뒤", "마감: 마감이 날짜보다 앞이에요 / 오류 없음 / 마감: 마감이 날짜보다 앞이에요 / 오류 없음",
                msgs(TaskCheck.check(p18.withDue(d("11-17")), false)) + " / "
                + msgs(TaskCheck.check(p18.withDue(d("11-18")), false)) + " / "
                + msgs(TaskCheck.check(p18.withDue(d("11-17")), true)) + " / "
                + msgs(TaskCheck.check(p18.withDue(d("11-17")), true, true)));
        eq("D-65 기간 할 일은 반복하지 않는다", "반복: 기간 안에 하는 할 일은 반복할 수 없어요",
                msgs(TaskCheck.check(task.withDue(d("11-22")).withRepeat(Repeat.DAILY, d("11-22")), false)));
        eq("D-65 기간은 할 일만", "마감: 기간은 할 일만 정할 수 있어요",
                msgs(TaskCheck.check(draft(Task.Kind.FIXED, "11-16", "10:00").withDue(d("11-22")), false)));
        eq("T-DEF-5", "2027-03-01", DefaultDates.repeatEnd(Task.Kind.FIXED, d("11-16")));
        eq("D-64 할 일 반복 끝 4주", "2026-12-14", DefaultDates.repeatEnd(Task.Kind.TASK, d("11-16")));
        eq("D-65 마감 기본은 그 주 일요일", "2026-11-22 2026-11-22", DefaultDates.due(d("11-18")) + " " + DefaultDates.due(d("11-22")));
    }

    /** 사본들을 "날짜 시작 seq"로 잇고, 같은 반복인지와 반복 방식을 붙인다. */
    static String copies(List<Task> made) {
        String days = made.stream().map(t -> t.date.toString().substring(5) + " " + t.start + " " + t.seq)
                .collect(Collectors.joining(", "));
        boolean same = made.stream().allMatch(t -> t.seriesId != null && t.seriesId.equals(made.get(0).seriesId));
        return made.size() + "개 " + days + " / 같은 반복 " + same + " / " + made.get(0).repeat;
    }

    static List<Task> daily(Store s) {
        return s.book.add(new TaskDraft(Task.Kind.TASK, "운동", d("11-16"), null, 30, "").withRepeat(Repeat.DAILY, d("11-22")));
    }

    static void add() {
        Store s = MockData.empty();
        eq("T-ADD-3", "7개 11-16 10:30 1, 11-23 10:30 2, 11-30 10:30 3, 12-07 10:30 4, 12-14 10:30 5, 12-21 10:30 6,"
                + " 12-28 10:30 7 / 같은 반복 true / 월", copies(s.book.add(draft(Task.Kind.FIXED, "11-16", "10:30")
                .withRepeat(Repeat.weekly(DayOfWeek.MONDAY), d("12-28")))));
        eq("D-64 할 일 매일 반복", "7개 11-16 null 1, 11-17 null 2, 11-18 null 3, 11-19 null 4, 11-20 null 5,"
                + " 11-21 null 6, 11-22 null 7 / 같은 반복 true / DAILY", copies(daily(MockData.empty())));
        Store w = MockData.empty();
        eq("D-64 여러 요일", "4개 11-17 18:00 1, 11-19 18:00 2, 11-24 18:00 3, 11-26 18:00 4 / 같은 반복 true / 화, 목",
                copies(w.book.add(draft(Task.Kind.FIXED, "11-16", "18:00")
                        .withRepeat(Repeat.weekly(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY), d("11-26")))));
        Task p = period(MockData.empty(), "P", "11-18", "11-22", 30);
        eq("D-65 기간 할 일", "2026-11-18 2026-11-22 null", p.date + " " + p.dueDate + " " + p.seriesId);
        Store del = MockData.empty();
        List<Task> made = daily(del);
        del.book.delete(made.get(3), true);
        eq("D-64 매일 반복 지우기", "[2026-11-16, 2026-11-17, 2026-11-18]", del.tasks.stream().map(t -> t.date).toList());
    }

    /** 그날 dayPlan에 그 일정이 있는가. */
    static boolean on(Store s, Task t, String date) {
        return s.book.checkables(d(date)).contains(t);
    }

    static void edit() {
        App app = Main.create(MockData.empty());
        Task p = period(app.store, "P", "11-16", "11-22", 30);
        MainScreen main = ProtoCheck.find(app.root, MainScreen.class);
        main.selectDate(d("11-18"));
        TimeEditor e = new TimeEditor(p, main.calendar.selected(), app.store.now);
        e.start.setValue("14:00");
        TimeEditor.save(app, p, e);
        eq("D-65 시각을 정하면 그날로", "2026-11-18 14:00 2026-11-22 true false false", p.date + " " + p.start + " " + p.dueDate
                + " " + app.store.book.dayPlan(d("11-18")).timed().contains(p) + " " + on(app.store, p, "11-17") + " "
                + on(app.store, p, "11-19"));
        Store s = MockData.empty();
        List<Task> made = daily(s);
        Task c19 = made.get(3);
        s.book.update(c19, new TaskDraft(Task.Kind.TASK, "운동 30분", c19.date, null, 30, ""), true);
        eq("D-64 할 일 반복도 범위를 묻는다", "운동 운동 운동 운동 30분 운동 30분 운동 30분 운동 30분",
                made.stream().map(t -> t.title).collect(Collectors.joining(" ")));
        App a = Main.create(MockData.empty());
        Task q = period(a.store, "Q", "11-16", "11-22", 30);
        TaskForm f = new TaskForm(q, q.date, a.store.now);
        Ui.setDate(f.due, d("11-29"));
        boolean saved = TaskDialog.trySave(a, f, q);
        String first = saved + " " + q.dueDate;
        TaskForm off = new TaskForm(q, q.date, a.store.now);
        off.period.doClick();
        TaskDialog.trySave(a, off, q);
        eq("D-65 수정에서 마감 바꾸기", "true 2026-11-29, null true false", first + ", " + q.dueDate + " "
                + on(a.store, q, "11-16") + " " + on(a.store, q, "11-17"));
    }

    static void lists() {
        Store s = MockData.empty();
        Task p = period(s, "P", "11-16", "11-18", 30);
        StringBuilder shown = new StringBuilder();
        for (String day : new String[] {"11-15", "11-16", "11-17", "11-18", "11-19"}) {
            shown.append(s.book.dayPlan(d(day)).untimed().contains(p)).append(" ");
        }
        eq("D-65 기간 동안 날마다", "false true true true false ~11/18까지", shown + TaskBook.dueText(p));
        App app = Main.create(at("11-18T09:00"));
        period(app.store, "P", "11-12", "11-20", 30);
        period(app.store, "Q", "11-10", "11-17", 30);
        app.show("일정관리");
        app.store.changed();
        ScheduleScreen sched = ProtoCheck.find(app.root, ScheduleScreen.class);
        eq("D-65 기간 할 일은 한 줄", "11/16 P ~11/20까지 30분 / 11/16 Q ~11/17까지 30분 지난 날", rows(sched.untimed));
        Store g = at("11-17T09:00");
        g.book.add(Task.Kind.TASK, "끝냄", d("11-17"), null, 30, "", null).get(0).done = true;
        period(g, "P", "11-16", "11-22", 30);
        DailyProgress pr = DailyProgress.of(g.book, g.today());
        eq("D-65 기간 할 일은 보이는 날마다 센다", "1/2", pr.done() + "/" + pr.total());
    }

    static void rewards() {
        App app = Main.create(at("11-17T09:00"));
        Task p = period(app.store, "P", "11-16", "11-22", 30);
        MainScreen main = ProtoCheck.find(app.root, MainScreen.class);
