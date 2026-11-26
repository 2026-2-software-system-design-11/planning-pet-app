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
        narrow();
        pills();
        rewards();
        talkAndCalendar();
        autoPlace();
        pastCopies();
    }

    /** D-69: 지난 반복 사본은 밀린 일로 보이지 않는다. */
    static void pastCopies() {
        App app = Main.create(at("11-18T09:00"));
        List<Task> runs = daily(app.store);
        app.store.book.add(Task.Kind.TASK, "과제", d("11-16"), null, 30, "", null);
        app.show("일정관리");
        app.store.changed();
        ScheduleScreen sched = ProtoCheck.find(app.root, ScheduleScreen.class);
        eq("D-69 지난 반복 사본", "11/16 운동 30분 / 11/16 과제 30분 지난 날 / 11/17 운동 30분", String.join(" / ",
                java.util.Arrays.asList(rows(sched.untimed).split(" / ")).subList(0, 3)));
        box(sched.untimed, runs.get(0)).doClick();
        eq("D-69 지난 사본도 체크", "true", runs.get(0).done);
        TaskBook.CellPreview c17 = app.store.book.cellPreview(d("11-17"));
        TaskBook.CellPreview c18 = app.store.book.cellPreview(d("11-18"));
        eq("D-69 지난 반복 사본은 진하지 않다", "1개 false 1개 true", c17.text() + " " + c17.open() + " " + c18.text() + " " + c18.open());
        App fin = Main.create(at("11-18T09:00"));
        fin.store.book.add(Task.Kind.TASK, "과제", d("11-16"), null, 30, "", null).get(0).done = true;
        fin.store.book.add(Task.Kind.TASK, "빨래", d("11-16"), null, 30, "", null);
        fin.show("일정관리");
        fin.store.changed();
        eq("D-78 끝낸 일에는 지난 날이 없다", "11/16 과제 30분 / 11/16 빨래 30분 지난 날",
                rows(ProtoCheck.find(fin.root, ScheduleScreen.class).untimed));
        Store b = at("11-18T09:00");
        daily(b);
        b.book.add(Task.Kind.TASK, "과제", d("11-17"), null, 30, "", null);
        eq("D-69 놓친 반복은 어제 일로 세지 않는다", "오늘 할 일 1개 남았어. 어제 못 끝낸 일 1개는 달력에 남아 있어. 내일 '운동'이 있어.",
                new Talk(b).briefing());
        Stats.Rate week = Stats.rate(b, Stats.Period.week(d("11-16")));
        eq("D-69 통계에는 센다", "0/3", week.done() + "/" + week.total());
    }

    static void validate() {
        TaskDraft task = draft(Task.Kind.TASK, "11-16", null);
        eq("T-VAL-11", "오류 없음", msgs(TaskCheck.check(task.withRepeat(Repeat.DAILY, d("12-13")), false)));
        eq("T-VAL-13", "오류 없음 / 반복: 반복이 너무 많아요. 반복 끝나는 날을 앞당겨 주세요",
                msgs(TaskCheck.check(task.withRepeat(Repeat.DAILY, d("2027-03-15")), false)) + " / "
                + msgs(TaskCheck.check(task.withRepeat(Repeat.DAILY, d("2027-03-16")), false)));
        eq("D-64 요일은 하나 이상", "반복: 요일을 하나 이상 골라 주세요",
                msgs(TaskCheck.check(task.withRepeat(Repeat.weekly(List.of()), d("11-30")), false)));
        Repeat wed = Repeat.weekly(DayOfWeek.WEDNESDAY);
        eq("D-64 반복하는 날 없음", "반복: 반복 끝나는 날 안에 반복하는 날이 없어요 / 오류 없음",
                msgs(TaskCheck.check(task.withRepeat(wed, d("11-17")), false)) + " / "
                + msgs(TaskCheck.check(task.withRepeat(wed, d("11-18")), false)));
        TaskDraft p18 = draft(Task.Kind.TASK, "11-18", null);
        eq("D-65 마감은 날짜와 같거나 뒤", "마감: 마감일이 시작일보다 앞이에요 / 오류 없음 / 마감: 마감일이 시작일보다 앞이에요",
                msgs(TaskCheck.check(p18.withDue(d("11-17")), false)) + " / "
                + msgs(TaskCheck.check(p18.withDue(d("11-18")), false)) + " / "
                + msgs(TaskCheck.check(p18.withDue(d("11-17")), true)));
        // T-VAL-17의 끝낸 할 일: 끝내도 날짜 칸(기간 첫날)이 그대로이므로 수정 창에서 마감을 검사한다(D-66).
        App app = Main.create(at("11-18T09:00"));
        Task done = period(app.store, "끝냄", "11-18", "11-22", 30);
        app.schedule.check(done);
        TaskForm f = new TaskForm(done, done.date, app.store.now);
        Ui.setDate(f.due, d("11-17"));
        eq("D-66 끝낸 할 일도 마감 검사", "마감: 마감일이 시작일보다 앞이에요", msgs(f.problems()));
        eq("D-65 기간 할 일은 반복하지 않는다", "반복: 기간 안에 하는 할 일은 반복할 수 없어요",
                msgs(TaskCheck.check(task.withDue(d("11-22")).withRepeat(Repeat.DAILY, d("11-22")), false)));
        eq("D-65 기간은 할 일만", "마감: 기간은 할 일만 정할 수 있어요",
                msgs(TaskCheck.check(draft(Task.Kind.FIXED, "11-16", "10:00").withDue(d("11-22")), false)));
        eq("T-VAL-5", "시작 시각: 고정 일정은 시작 시각이 있어야 해요",
                msgs(TaskCheck.check(draft(Task.Kind.FIXED, "11-16", null), false)));
        eq("T-VAL-6", "시작 시각: 5분 단위로 정해 주세요",
                msgs(TaskCheck.check(draft(Task.Kind.TASK, "11-16", "09:03"), false)));
        StringBuilder spans = new StringBuilder();
        for (int m : new int[] {0, 27, 730, 10, 75, 720}) {
            spans.append(" / ").append(msgs(TaskCheck.check(TaskDraft.at(Task.Kind.TASK, "일", d("11-16"), null, m), false)));
        }
        String span = "걸리는 시간: 10분부터 720분까지, 5분 단위예요";
        eq("T-VAL-9", " / " + span + " / " + span + " / " + span + " / 오류 없음 / 오류 없음 / 오류 없음", spans);
        String both = "시작 시각: 다음 날 04:00까지 끝나야 해요. 5분 단위로 정해 주세요. ";
        List<String[]> late = TaskCheck.check(TaskDraft.at(Task.Kind.TASK, "일", d("11-16"), 27 * 60 + 53, 10), false);
        eq("T-VAL-21 10분", both + "03:50(다음 날)이면 둘 다 맞아요", msgs(late));
        eq("T-VAL-21 27분", both + "걸리는 시간 25분이면 03:35(다음 날)이 맞아요 / " + span,
                msgs(TaskCheck.check(TaskDraft.at(Task.Kind.TASK, "일", d("11-16"), 27 * 60 + 53, 27), false)));
        eq("T-VAL-21 730분", both + "걸리는 시간 720분이면 16:00이 맞아요 / " + span,
                msgs(TaskCheck.check(TaskDraft.at(Task.Kind.TASK, "일", d("11-16"), 27 * 60 + 53, 730), false)));
        // D-100: 보이는 제목 칸 폭을 넘는 오류 글만 빈칸에서 줄을 바꿔 그 폭 안에 들고, 글은 그대로 남는다. 창처럼 패널을 놓은 뒤
        // 보고, 테마 글꼴과 상관없어야 하므로 라벨 글꼴을 바꿔 가며 본다. 칸 안에 드는 반복 오류는 한 줄 그대로다.
        List<String[]> longest = TaskCheck.check(TaskDraft.at(Task.Kind.TASK, "일", d("11-16"), 27 * 60 + 53, 27), false);
        TaskForm wrap = new TaskForm(null, d("11-16"), at("11-16T09:00").now);
        javax.swing.JLabel label = wrap.errors.get("시작 시각");
        int wide = wrap.panel.getPreferredSize().width;
        wrap.panel.setSize(wrap.panel.getPreferredSize());
        wrap.panel.doLayout();
        int room = wrap.title.getWidth();
        wrap.showErrors(TaskCheck.check(TaskDraft.at(Task.Kind.TASK, "운동", d("11-16"), null, 30)
                .withRepeat(Repeat.DAILY, LocalDate.of(2027, 6, 1)), false));
        javax.swing.JLabel many = wrap.errors.get("반복");
        eq("D-100 칸 안에 드는 오류 글은 한 줄", "true 반복이 너무 많아요. 반복 끝나는 날을 앞당겨 주세요",
                (room > wrap.title.getPreferredSize().width) + " " + many.getText());
        StringBuilder fits = new StringBuilder();
        for (java.awt.Font font : new java.awt.Font[] {label.getFont(), new java.awt.Font(java.awt.Font.DIALOG, java.awt.Font.PLAIN, 12),
                new java.awt.Font(java.awt.Font.DIALOG, java.awt.Font.BOLD, 16)}) {
            label.setFont(font);
            wrap.showErrors(longest);
            String[] lines = label.getText().replaceAll("^<html>|</html>$", "").split("<br>");
            fits.append(lines.length > 1).append(' ').append(label.getPreferredSize().width <= room).append(' ')
                    .append(String.join(" ", lines).equals(longest.get(0)[1])).append(' ')
                    .append(wrap.panel.getPreferredSize().width == wide).append(" / ");
        }
        eq("D-100 오류 글 줄바꿈", "true true true true / ".repeat(3) + longest.get(0)[1] + " / " + longest.get(1)[1], fits + wrap.errorText());
        eq("T-DEF-5", "2027-03-01", DefaultDates.repeatEnd(Task.Kind.FIXED, d("11-16")));
        eq("D-64 할 일 반복 끝 4주", "2026-12-14", DefaultDates.repeatEnd(Task.Kind.TASK, d("11-16")));
        eq("D-65 마감 기본은 그 주 일요일", "2026-11-22 2026-11-29", DefaultDates.due(d("11-18")) + " " + DefaultDates.due(d("11-22")));
        TaskForm follow = new TaskForm(null, d("11-16"), at("11-16T09:00").now);
        follow.title.setText("운동");
        follow.repeat.setSelectedIndex(1);
        Ui.setDate(follow.date, d("12-01"));
        String followed = Ui.dateOf(follow.repeatEnd).toString();
        Ui.setDate(follow.repeatEnd, d("12-10"));
        Ui.setDate(follow.date, d("12-20"));
        eq("D-79 끝 날짜는 손대기 전까지 따라간다", "2026-12-29 2026-12-10 반복: 반복 끝나는 날이 날짜보다 앞이에요",
                followed + " " + Ui.dateOf(follow.repeatEnd) + " " + msgs(follow.problems()));
        TaskForm keep = new TaskForm(null, d("11-16"), at("11-16T09:00").now);
        TaskForm plain = new TaskForm(null, d("11-16"), at("11-16T09:00").now);
        String kept = "";
        for (TaskForm k : new TaskForm[] {keep, plain}) {
            k.title.setText("보고서");
            k.period.doClick();
            if (k == keep) {
                Ui.setDate(k.due, d("12-10"));
            }
            k.fixed.doClick();
            k.task.doClick();
            k.period.doClick();
            kept += Ui.dateOf(k.due) + " ";
        }
        Ui.setDate(keep.date, d("12-12"));
        eq("T-DEF-16", "2026-12-10 2026-11-22 2026-12-10 마감: 마감일이 시작일보다 앞이에요",
                kept + Ui.dateOf(keep.due) + " " + msgs(keep.problems()));
        TaskForm unpicked = new TaskForm(null, d("11-16"), at("11-16T09:00").now);
        unpicked.title.setText("보고서");
        unpicked.period.doClick();
        Ui.setDate(unpicked.date, d("11-23"));
        String moved = Ui.dateOf(unpicked.due) + " " + msgs(unpicked.problems());
        Ui.setDate(unpicked.date, d("11-25"));
        eq("T-DEF-18", "2026-11-29 오류 없음 2026-11-29 오류 없음",
                moved + " " + Ui.dateOf(unpicked.due) + " " + msgs(unpicked.problems()));
        App saved = Main.create(at("11-16T09:00"));
        Task report = period(saved.store, "보고서", "11-16", "12-10", 30);
        TaskForm edit = new TaskForm(report, report.date, saved.store.now);
        edit.period.doClick();
        Ui.setDate(edit.date, d("11-20"));
        edit.period.doClick();
        String reopened = Ui.dateOf(edit.due) + " ";
        Ui.setDate(edit.date, d("12-12"));
        eq("T-DEF-17", "2026-12-10 2026-12-10 마감: 마감일이 시작일보다 앞이에요",
                reopened + Ui.dateOf(edit.due) + " " + msgs(edit.problems()));
        TaskForm names = new TaskForm(null, d("11-16"), at("11-16T09:00").now);
        names.period.doClick();
        String on = names.dateLabel.getText() + " " + names.dueLabel.getText();
        names.period.doClick();
        names.repeat.setSelectedIndex(1);
        names.timed.doClick();
        names.start.setValue("10:00");
        names.minutes.setValue(45);
        eq("D-73 입력 창 칸 이름", "시작일 마감일 / 날짜 true 반복 일정은 기간을 정할 수 없어요 10:45에 끝나요", on + " / "
                + names.dateLabel.getText() + " " + names.periodHint.isVisible() + " " + names.periodHint.getText() + " "
                + names.endLabel.getText());
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
        eq("D-65 기간 할 일", "2026-11-18 2026-11-18 2026-11-22 null", p.date + " " + p.startDay + " " + p.dueDate + " " + p.seriesId);
        Task timed = MockData.empty().book.add(new TaskDraft(Task.Kind.TASK, "T", d("11-18"), LocalTime.of(10, 0), 30, "")
                .withDue(d("11-22"))).get(0);
        TaskForm form = new TaskForm(null, d("11-18"), at("11-18T09:00").now);
        form.timed.doClick();
        form.period.doClick();
        eq("D-68 기간 할 일은 시각 없이", "null false false 기간 할 일의 시각은 목록에서 정해요", timed.start + " " + form.timed.isSelected()
                + " " + form.timed.isEnabled() + " " + form.timed.getToolTipText());
        App wa = Main.create(at("11-16T09:00"));
        TaskForm wf = new TaskForm(null, d("11-16"), wa.store.now);
        wf.fixed.doClick();
        wf.title.setText("편의점 알바");
        wf.start.setValue("18:00");
        wf.repeat.setSelectedIndex(2);
        wf.days[0].doClick();
        wf.days[1].doClick();
        wf.days[3].doClick();
        Ui.setDate(wf.repeatEnd, d("11-26"));
        eq("D-75 요일 반복을 넣은 뒤", "true 17일(화)부터 화, 목마다 넣었어요", TaskDialog.trySave(wa, wf, null) + " " + wa.store.talk);
        Store del = MockData.empty();
        List<Task> made = daily(del);
        del.book.delete(made.get(3), true);
        eq("D-64 매일 반복 지우기", "[2026-11-16, 2026-11-17, 2026-11-18]", del.tasks.stream().map(t -> t.date).toList());
    }

    /** 그날 dayPlan의 어느 묶음에든 그 일정이 있는가. */
    static boolean on(Store s, Task t, String date) {
        return !group(s, t, date).equals("x");
    }

    /** 그날 dayPlan에서 그 일정이 든 묶음: due, timed, untimed, period, 없으면 x. */
    static String group(Store s, Task t, String date) {
        TaskBook.DayPlan p = s.book.dayPlan(d(date));
        return p.dueToday().contains(t) ? "due" : p.timed().contains(t) ? "timed" : p.untimed().contains(t) ? "untimed"
                : p.period().contains(t) ? "period" : "x";
    }

    static String groups(Store s, Task t, String... days) {
        return java.util.Arrays.stream(days).map(day -> group(s, t, day)).collect(Collectors.joining(" "));
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
        App rc = Main.create(at("11-16T09:00"));
        List<Task> runs = rc.store.book.add(new TaskDraft(Task.Kind.TASK, "운동", d("11-16"), LocalTime.of(7, 0), 30, "")
                .withRepeat(Repeat.DAILY, d("11-22")));
        String[] asked = {""};
        int[] answer = {0};
        rc.schedule.chooser = new planningpet.schedule.controller.TaskController.Chooser() {
            @Override
            public int choose(String title, String message, String[] options) {
                return choose(title, message, options, options.length - 1);
            }

            @Override
            public int choose(String title, String message, String[] options, int initial) {
                asked[0] = message + " " + String.join(", ", options) + " 처음 " + options[initial];
                return answer[0];
            }
        };
        TimeEditor.clear(rc, runs.get(2));
        String one = runs.stream().map(t -> String.valueOf(t.start)).collect(Collectors.joining(" "));
        answer[0] = 1;
        TimeEditor.clear(rc, runs.get(3));
        eq("D-72 반복 사본의 시각 빼기", "'운동'은 반복 일정이에요. 어디까지 시각을 뺄까요? 이 일정만, 이후 반복 모두, 취소 처음 이 일정만"
                + " / 07:00 07:00 null 07:00 07:00 07:00 07:00 / 07:00 07:00 null null null null null", asked[0] + " / " + one + " / "
                + runs.stream().map(t -> String.valueOf(t.start)).collect(Collectors.joining(" ")));
        App pc = Main.create(at("11-18T09:00"));
        Task pp = period(pc.store, "P", "11-16", "11-22", 30);
        pc.store.book.setTime(pp, LocalTime.of(14, 0), 30, d("11-20"), false);
        TimeEditor.clear(pc, pp);
        eq("D-72 기간 할 일의 시각 빼기", "null 2026-11-18 2026-11-16 2026-11-22 / x x period period period period due",
                pp.start + " " + pp.date + " " + pp.startDay + " " + pp.dueDate + " / "
                + groups(pc.store, pp, "11-16", "11-17", "11-18", "11-19", "11-20", "11-21", "11-22"));
        App later = Main.create(at("11-17T09:00"));
        TaskDraft pd = new TaskDraft(Task.Kind.TASK, "P", d("11-16"), null, 90, "초안").withDue(d("11-22"));
        Task lp = later.store.book.add(pd).get(0);
        later.store.book.setTime(lp, LocalTime.of(9, 0), 90, d("11-18"), false);
        TaskForm lf = new TaskForm(lp, lp.date, later.store.now);
        Ui.setDate(lf.due, d("11-24"));
        TaskDialog.trySave(later, lf, lp);
        eq("D-93 마감 미루기", "2026-11-16 2026-11-18 09:00 90 초안 2026-11-24 ~11/24까지 null [P]", lp.startDay + " " + lp.date + " "
                + lp.start + " " + lp.minutes + " " + lp.memo + " " + lp.dueDate + " " + TaskBook.dueText(lp) + " "
                + later.store.book.cellPreview(d("11-22")) + " " + later.store.book.cellPreview(d("11-24")).due());
        TaskForm weekly = new TaskForm(null, d("11-18"), later.store.now);
        weekly.repeat.setSelectedIndex(2);
        StringBuilder picked = new StringBuilder();
        for (javax.swing.JToggleButton b : weekly.days) {
            picked.append(b.isSelected() ? b.getText() : "");
        }
        eq("D-93 요일 반복의 처음 칸", "수", picked);
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
        Store s = at("11-16T09:00");
        Task p = period(s, "P", "11-16", "11-18", 30);
        eq("D-65 기간 동안 날마다", "x period period due x ~11/18까지",
                groups(s, p, "11-15", "11-16", "11-17", "11-18", "11-19") + " " + TaskBook.dueText(p));
        App miss = Main.create(at("11-18T09:00"));
        Task m = period(miss.store, "P", "11-16", "11-22", 30);
        miss.store.book.setTime(m, LocalTime.of(9, 0), 30, d("11-17"), false);
        String before = groups(miss.store, m, "11-17", "11-18", "11-21", "11-22");
        MainScreen home = ProtoCheck.find(miss.root, MainScreen.class);
        home.selectDate(d("11-18"));
        String row = rows(home.rows);
        TimeEditor e = new TimeEditor(m, d("11-18"), miss.store.now);
        e.start.setValue("14:00");
        TimeEditor.save(miss, m, e);
        eq("D-67 정한 날을 놓치면 다음 날부터", "x period period due / 기간 안에 할 일 / P 30분 ~11/22까지 17일에 하려던 일"
                + " / 11-18 timed", before + " / " + row + " / " + m.date.toString().substring(5) + " " + group(miss.store, m, "11-18"));
        Store over = at("11-18T09:00");
        Task o = period(over, "P", "11-16", "11-22", 30);
        over.book.setTime(o, LocalTime.of(9, 0), 30, d("11-17"), false);
        over.now = d("11-23").atTime(9, 0);
        eq("D-67 마감이 지나면 마감 날에만", "x x x due x", groups(over, o, "11-17", "11-18", "11-21", "11-22", "11-23"));
        Store today = at("11-18T09:00");
        today.book.add(Task.Kind.TASK, "과제", d("11-18"), null, 30, "", null).get(0).done = true;
        today.book.add(Task.Kind.TASK, "빨래", d("11-18"), null, 30, "", null);
        Task e2 = period(today, "E사 지원서", "11-16", "11-22", 120);
        String plan = names(today.book.dayPlan(d("11-18")));
        today.book.setTime(e2, LocalTime.of(14, 0), 120, d("11-18"), false);
        eq("D-70 오늘로 정하면 오늘 할 일", "untimed 과제 빨래 period E사 지원서 / timed E사 지원서 untimed 과제 빨래",
                plan + " / " + names(today.book.dayPlan(d("11-18"))));
        App app = Main.create(at("11-18T09:00"));
        period(app.store, "P", "11-12", "11-20", 30);
        period(app.store, "Q", "11-10", "11-17", 30);
        app.show("일정관리");
        app.store.changed();
        ScheduleScreen sched = ProtoCheck.find(app.root, ScheduleScreen.class);
        eq("D-65 기간 할 일은 한 줄", "11/16 P 30분 ~11/20까지 / 11/17 Q 30분 ~11/17까지 지난 날 / 11/20 P 30분 ~11/20까지 마감",
                rows(sched.untimed));
        app.store.now = d("11-20").atTime(9, 0);
        app.store.changed();
        eq("D-93 마감 날이 오늘이면 오늘 마감", "11/16 P 30분 ~11/20까지 / 11/17 Q 30분 ~11/17까지 지난 날 / 11/20 P 30분 ~11/20까지 오늘 마감",
                rows(sched.untimed));
        App wk = Main.create(at("11-17T09:00"));
        period(wk.store, "P", "11-16", "11-20", 30);
        period(wk.store, "Q", "11-16", "11-29", 30);
        Task r = period(wk.store, "R", "11-16", "11-19", 30);
        wk.schedule.check(r);
        eq("D-93 첫날과 마감 날", "11/16 P, 11/16 Q, 11/17 R, 11/20 P", wk.store.book.untimedOfWeek(d("11-16")).stream()
                .map(x -> x.day().getMonthValue() + "/" + x.day().getDayOfMonth() + " " + x.task().title).collect(Collectors.joining(", ")));
        App ga = Main.create(at("11-17T09:00"));
        Store g = ga.store;
        g.book.add(Task.Kind.TASK, "끝냄", d("11-17"), null, 30, "", null).get(0).done = true;
        period(g, "P", "11-16", "11-22", 30);
        DailyProgress pr = DailyProgress.of(g.book, g.today());
        String card = ProtoCheck.find(ga.root, planningpet.home.view.PetCard.class).periodText();
        g.now = d("11-22").atTime(9, 0);
        DailyProgress last = DailyProgress.of(g.book, g.today());
        eq("D-70 기간 안에 할 일은 세지 않는다", "1/1 기간 할 일 1개 (~11/22), 마감 날 0/1", pr.done() + "/" + pr.total() + " " + card
                + ", 마감 날 " + last.done() + "/" + last.total());
    }

    /** 묶음마다 이름과 제목. 빈 묶음은 뺀다. */
    static String names(TaskBook.DayPlan p) {
        StringBuilder b = new StringBuilder();
        String[] heads = {"due", "timed", "untimed", "period"};
        List<List<Task>> all = List.of(p.dueToday(), p.timed(), p.untimed(), p.period());
        for (int i = 0; i < 4; i++) {
            if (!all.get(i).isEmpty()) {
                b.append(b.length() == 0 ? "" : " ").append(heads[i]).append(" ")
                        .append(all.get(i).stream().map(t -> t.title).collect(Collectors.joining(" ")));
            }
        }
        return b.toString();
    }

    static void rewards() {
        App app = Main.create(at("11-17T09:00"));
        Task p = period(app.store, "P", "11-16", "11-22", 30);
        MainScreen main = ProtoCheck.find(app.root, MainScreen.class);
        main.selectDate(d("11-18"));
        javax.swing.JCheckBox box = box(main.rows, p);
        box.doClick();
        eq("D-66 끝내면 끝낸 날에만", "10P 보상 10 2026-11-16 2026-11-17 2026-11-22 true false false 18일 목록에 없음 true",
                app.store.points + "P 보상 " + p.rewardPoints + " " + p.date + " " + p.doneDay + " " + p.dueDate + " "
                + app.store.book.dayPlan(d("11-17")).untimed().contains(p)
                + " " + on(app.store, p, "11-16") + " " + on(app.store, p, "11-18") + " 18일 목록에 없음 " + (box(main.rows, p) == null));
        app.schedule.check(p);
        StringBuilder days = new StringBuilder();
        for (String day : new String[] {"11-16", "11-17", "11-18", "11-19", "11-20", "11-21", "11-22"}) {
            days.append(on(app.store, p, day) ? "o" : "x");
        }
        eq("D-66 풀면 처음 계획으로", "false 2026-11-16 null 2026-11-22 ooooooo", p.done + " " + p.date + " " + p.doneDay + " "
                + p.dueDate + " " + days);
        App plan = Main.create(at("11-16T09:00"));
        Task q = period(plan.store, "Q", "11-16", "11-22", 60);
        plan.store.book.setTime(q, LocalTime.of(9, 0), 60, d("11-18"), false);
        plan.schedule.check(q);
        String afterDone = q.date + " " + q.start + " " + q.doneDay + " " + plan.store.book.dayPlan(d("11-16")).untimed().contains(q)
                + " " + on(plan.store, q, "11-18") + " " + plan.store.book.timedOn(d("11-18")).isEmpty();
        plan.schedule.check(q);
        eq("D-66 계획한 칸이 빈다", "2026-11-18 09:00 2026-11-16 true false true, 풀면 true false",
                afterDone + ", 풀면 " + plan.store.book.dayPlan(d("11-18")).timed().contains(q) + " " + on(plan.store, q, "11-16"));
        Store st = at("11-16T09:00");
        period(st, "P", "11-09", "11-16", 30);
        period(st, "Q", "11-16", "11-22", 30);
        Task r = period(st, "R", "11-10", "11-20", 30);
        new Game(st).complete(r);
        st.now = d("11-17").atTime(9, 0);
        Stats.Rate week = Stats.rate(st, Stats.Period.week(d("11-16")));
        Stats.Rate last = Stats.rate(st, Stats.Period.week(d("11-09")));
        eq("D-65 기간 할 일은 마감 날에 센다", "R 2026-11-16, 1/2 50%, 0/0", "R " + r.doneDay + ", " + week.done() + "/" + week.total()
                + " " + week.percent() + "%, " + last.done() + "/" + last.total());
    }

    static void talkAndCalendar() {
        Store s = at("11-17T09:00");
        period(s, "E사 지원서", "11-16", "11-22", 120);
        period(s, "F사 서류", "11-16", "11-18", 30);
        s.book.add(Task.Kind.TASK, "과제", d("11-16"), null, 30, "", null);
        eq("D-65 브리핑의 기간 할 일", "오늘은 할 일이 없어. 어제 못 끝낸 일 1개는 달력에 남아 있어. 'F사 서류' 마감이 내일(18일)이야, D-1.",
                new Talk(s).briefing());
        Store m = at("11-18T09:00");
        Task e = period(m, "E사 지원서", "11-16", "11-22", 120);
        m.book.setTime(e, LocalTime.of(9, 0), 120, d("11-17"), false);
        String day18 = new Talk(m).briefing();
        m.now = d("11-19").atTime(9, 0);
        eq("D-67 어제 하기로 한 일", "오늘은 할 일이 없어. 어제 하기로 한 'E사 지원서'를 못 했어. 마감은 22일이야."
                + " / 오늘은 할 일이 없어. 'E사 지원서' 마감이 3일 뒤(22일)야, D-3.",
                day18 + " / " + new Talk(m).briefing());
        App done = Main.create(at("11-18T09:00"));
        done.store.book.add(Task.Kind.TASK, "과제", d("11-18"), null, 30, "", null).get(0).done = true;
        Task laundry = done.store.book.add(Task.Kind.TASK, "빨래", d("11-18"), null, 30, "", null).get(0);
        period(done.store, "E사 지원서", "11-16", "11-22", 120);
        done.schedule.check(laundry);
        eq("D-70 다 끝내면 기간 할 일 안내", "오늘 할 일 다 끝냈어! E사 지원서는 22일까지야, 4일 남았어.", done.store.talk);
        Store order = at("11-18T09:00");
        order.book.add(Task.Kind.TASK, "빨래", d("11-18"), null, 30, "", null);
        order.book.add(Task.Kind.TASK, "과제", d("11-19"), null, 30, "", null);
        period(order, "E사 지원서", "11-16", "11-20", 120);
        eq("D-71 마감과 내일 일", "오늘 할 일 1개 남았어. 'E사 지원서' 마감이 모레(20일)야, D-2. 내일 '과제'가 있어.", new Talk(order).briefing());
        Store only = at("11-18T09:00");
        period(only, "E사 지원서", "11-16", "11-22", 120);
        eq("D-70 할 일이 없어도 기간 할 일 안내", "오늘은 할 일이 없어. E사 지원서는 22일까지야, 4일 남았어.", new Talk(only).briefing());
        Store far = at("11-18T09:00");
        period(far, "2026 하반기 공채 자기소개서 1번 문항", "11-16", "11-20", 90);
        eq("T-TALK-4", "오늘은 할 일이 없어. '2026 하반기 공채 자기…' 마감이 모레(20일)야, D-2.", new Talk(far).briefing());
        Store last = at("11-22T09:00");
        Task due = period(last, "E사 지원서", "11-16", "11-22", 120);
        eq("D-70 마감 날", "오늘 할 일 1개 남았어. 오늘이 E사 지원서 마감이야! true", new Talk(last).briefing() + " "
                + last.book.dayPlan(d("11-22")).dueToday().contains(due));
        Store twice = at("11-21T09:00");
        Task missed = period(twice, "E사 지원서", "11-16", "11-22", 120);
        twice.book.setTime(missed, LocalTime.of(9, 0), 120, d("11-20"), false);
        period(twice, "F사 서류", "11-16", "11-23", 30);
        eq("T-TALK-27", "오늘은 할 일이 없어. 어제 하기로 한 'E사 지원서'를 못 했어. 마감은 22일이야. 'F사 서류' 마감이 모레(23일)야, D-2.",
                new Talk(twice).briefing());
        App home = Main.create(at("11-18T09:00"));
        TaskForm f = new TaskForm(null, d("11-18"), home.store.now);
        f.title.setText("E사 지원서");
        f.period.doClick();
        Ui.setDate(f.due, d("11-22"));
        TaskDialog.trySave(home, f, null);
        String added = home.store.talk;
        MainScreen main = ProtoCheck.find(home.root, MainScreen.class);
        main.selectDate(d("11-19"));
        String other = home.store.talk;
        main.selectDate(d("11-18"));
        eq("T-TALK-26", "'E사 지원서' 넣었구나! 22일까지야. / 11월 19일은 할 일이 없어. 기간 할 일 1개가 있어. / 오늘은 할 일이 없어."
                + " E사 지원서는 22일까지야, 4일 남았어.", added + " / " + other + " / " + home.store.talk);
        Store timed = at("11-22T09:00");
        Task set = period(timed, "E사 지원서", "11-16", "11-22", 120);
        timed.book.setTime(set, LocalTime.of(14, 0), 120, d("11-22"), false);
        eq("T-TALK-24", "오늘 할 일 1개 남았어. 오늘이 E사 지원서 마감이야!", new Talk(timed).briefing());
        Store c = MockData.empty();
        period(c, "E사 지원서", "11-16", "11-22", 120);
        TaskBook.CellPreview c22 = c.book.cellPreview(d("11-22"));
        eq("D-65 기간 할 일은 마감 날 칸", "null 1개 true [E사 지원서]", c.book.cellPreview(d("11-16")) + " " + c22.text() + " " + c22.open()
                + " " + c22.due());
        App cal = Main.create(at("11-17T09:00"));
        Task p = period(cal.store, "P", "11-16", "11-22", 60);
        cal.store.book.setTime(p, LocalTime.of(9, 0), 60, d("11-18"), false);
        cal.redraw();
        MainScreen month = ProtoCheck.find(cal.root, MainScreen.class);
        eq("T-CAL-8", "18 1개(진함) / 22 마감 P 마감", HomeCheck.cell(month, 18) + " / " + HomeCheck.cell(month, 22) + " "
                + HomeCheck.tip(month, 22));
    }

    static String placed(Planner.Placement p) {
        return p.placed().stream().map(x -> x.task().title + " " + x.day() + " " + x.start()).collect(Collectors.joining(", "))
                + (p.unplaced().isEmpty() ? "" : " / 놓지 못함 " + p.unplaced().size());
    }

    static void autoPlace() {
        App app = Main.create(at("11-16T22:30"));
        Task p = period(app.store, "P", "11-14", "11-18", 120);
        Planner.Placement pl = Planner.place(app.store, d("11-16"));
        String got = placed(pl);
        AutoPlaceDialog.apply(app, pl);
        eq("D-65 빈 시간이 있는 가장 이른 날", "P 2026-11-17 09:00, 적용 뒤 2026-11-17 2026-11-18",
                got + ", 적용 뒤 " + p.date + " " + p.dueDate);
        Store q = at("11-16T15:23");
        period(q, "Q", "11-19", "11-22", 30);
        eq("D-65 첫날이 앞날이면 첫날부터", "Q 2026-11-19 09:00", placed(Planner.place(q, d("11-16"))));
        Store s = at("11-16T15:23");
        period(s, "S", "11-16", "11-25", 30);
        eq("T-AUTO-13 보이는 주 안에서만", "S 2026-11-23 09:00", placed(Planner.place(s, d("11-23"))));
        Store full = at("11-22T23:40");
        full.book.add(Task.Kind.TASK, "E", d("11-22"), null, 30, "", null);
        period(full, "P", "11-16", "11-22", 30);
        period(full, "Q", "11-16", "11-29", 30);
        eq("D-76 놓지 못한 일의 까닭", "P 마감 11/22(일) 마감까지 빈 시간이 모자라요 / Q 마감 11/29(일) 이번 주엔 자리가 없어요."
                + " 마감 11/29(일)까지 시간이 있으니 다음 주에서 자동 배치하면 놓을 수 있어요 / E 날짜 11/22(일) 그날 빈 시간이 모자라요",
                Planner.place(full, d("11-16")).unplaced().stream().map(t -> t.title + " " + Planner.dueText(t) + " "
                        + Planner.reason(full, t, d("11-16"))).collect(Collectors.joining(" / ")));
        Store busy = at("11-22T23:40");
        Task wide = period(busy, "Q", "11-16", "11-29", 720);
        String open = Planner.reason(busy, wide, d("11-16"));
        for (LocalDate day = d("11-23"); day.isBefore(d("11-30")); day = day.plusDays(1)) {
            busy.book.add(Task.Kind.FIXED, "알바", day, LocalTime.of(9, 0), 720, "", null);
        }
        eq("T-AUTO-17", "true / 이번 주엔 자리가 없어요. 다음 주에도 자리가 없어요. 걸리는 시간을 나누거나 다른 일을 옮겨 보세요",
                open.endsWith("다음 주에서 자동 배치하면 놓을 수 있어요") + " / " + Planner.reason(busy, wide, d("11-16")));
        App again = Main.create(at("11-18T10:00"));
        Task m = period(again.store, "P", "11-16", "11-22", 30);
        again.store.book.setTime(m, LocalTime.of(9, 0), 30, d("11-17"), false);
        Planner.Placement mp = Planner.place(again.store, d("11-16"));
        String mg = placed(mp);
        AutoPlaceDialog.apply(again, mp);
        eq("D-67 못 끝낸 계획도 다시 놓는다", "P 2026-11-18 10:00, 적용 뒤 2026-11-18 10:00 false", mg + ", 적용 뒤 " + m.date + " " + m.start
                + " " + m.missed(again.store.today()));
    }

    /** T-LIST-9(D-94): 처음 창 크기 1000x680에서도 목록 줄의 제목이 "…" 앞 네 글자 넘게 보이고 표시는 둘째 줄이다. */
    static void narrow() {
        App app = Main.create(at("11-19T09:00"));
        period(app.store, "졸업 작품 기획서", "11-12", "11-19", 60);
        Task e = period(app.store, "E사 지원서", "11-16", "11-22", 120);
        app.store.book.setTime(e, LocalTime.of(9, 0), 120, d("11-18"), false);
        app.store.changed();
        MainScreen home = ProtoCheck.find(app.root, MainScreen.class);
        String homeTitles = titles(app.root, home.rows);
        String homeRows = rows(home.rows);
        app.show("일정관리");
        app.store.changed();
        ScheduleScreen sched = ProtoCheck.find(app.root, ScheduleScreen.class);
        eq("D-94 처음 창 크기에서도 제목이 읽힌다", "졸업 작품 기획서, E사 지원서 / 졸업 작품 기획서 60분 ~11/19까지 오늘 마감"
                + " / 기간 안에 할 일 / E사 지원서 120분 ~11/22까지 18일에 하려던 일 / 11/16 졸업 작품 기획서, 11/19 졸업 작품 기획서,"
                + " 11/19 E사 지원서, 11/22 E사 지원서",
                homeTitles + " / " + homeRows + " / " + titles(app.root, sched.untimed));
    }

    /** T-EDIT-15(D-94): 지난 날 목록의 기간 할 일과 끝낸 줄의 시각 알약은 누를 수 없다. */
    static void pills() {
        App app = Main.create(at("11-19T09:00"));
        Task job = period(app.store, "지원서", "11-16", "11-22", 60);
        Task toeic = app.store.book.add(Task.Kind.TASK, "토익", d("11-19"), null, 30, "", null).get(0);
        app.schedule.check(toeic);
        MainScreen home = ProtoCheck.find(app.root, MainScreen.class);
        home.rows.settle();
        home.selectDate(d("11-17"));
        javax.swing.JButton past = pill(home.rows, job);
        String before = past.isEnabled() + " " + past.getToolTipText();
        past.doClick();
        home.selectDate(d("11-19"));
        eq("D-94 지난 날과 끝낸 줄의 알약", "false 지난 날에는 시각을 정할 수 없어요 / true / false / 2026-11-16 null",
                before + " / " + pill(home.rows, job).isEnabled() + " / " + pill(home.rows, toeic).isEnabled() + " / " + job.date + " "
                        + job.start);
    }

    /** 목록에서 그 일의 줄에 있는 시각 알약. */
    static javax.swing.JButton pill(java.awt.Container list, Task t) {
        for (java.awt.Component row : list.getComponents()) {
            if (row instanceof java.awt.Container k && box(k, t) != null) {
                return button(k);
            }
        }
        return null;
    }

    private static javax.swing.JButton button(java.awt.Container c) {
        for (java.awt.Component child : c.getComponents()) {
            if (child instanceof javax.swing.JButton b && (b.getText().contains(":") || b.getText().equals("시각 정하기"))) {
                return b;
            }
            if (child instanceof java.awt.Container k && button(k) != null) {
                return button(k);
            }
        }
        return null;
    }

    /** 메인 창을 1000x680으로 늘어놓고 목록의 제목 칸이 그리는 글을 잇는다. "…"로 줄인 글은 앞 네 글자 넘게 보여야 한다. */
    static String titles(java.awt.Container root, java.awt.Container list) {
        root.setSize(1000, 680);
        layout(root);
        layout(root);
        List<String> out = new java.util.ArrayList<>();
        collectTitles(list, out);
        return String.join(", ", out);
    }

    private static void collectTitles(java.awt.Component c, List<String> out) {
        if (c instanceof TaskRows.TitleLabel l) {
            // 다 보이면 줄의 글(날짜와 제목), 줄였으면 "줄임(…)", 네 글자 이하만 보이면 "잘림(…)".
            String shown = l.shown();
            String text = l.getText().replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").replace(" 메모", "").strip();
            out.add(!shown.endsWith("…") ? text : shown.length() - 1 > 4 ? "줄임(" + shown + ")" : "잘림(" + shown + ")");
        } else if (c instanceof java.awt.Container k) {
            for (java.awt.Component child : k.getComponents()) {
                collectTitles(child, out);
            }
        }
    }

    private static void layout(java.awt.Component c) {
        c.doLayout();
        if (c instanceof java.awt.Container k) {
            for (java.awt.Component child : k.getComponents()) {
                layout(child);
            }
        }
    }

    static javax.swing.JCheckBox box(java.awt.Component c, Task t) {
        if (c instanceof javax.swing.JCheckBox b && b.getClientProperty(Task.class) == t) {
            return b;
        }
        if (c instanceof java.awt.Container k) {
            for (java.awt.Component child : k.getComponents()) {
                javax.swing.JCheckBox b = box(child, t);
                if (b != null) {
                    return b;
                }
            }
        }
        return null;
    }

    /** 목록의 줄마다 글(라벨)을 화면 순서대로 잇는다. 줄 사이는 " / ". */
    static String rows(java.awt.Container list) {
        return java.util.Arrays.stream(list.getComponents()).map(row -> String.join(" ", labels(row, new java.util.ArrayList<>())))
                .collect(Collectors.joining(" / "));
    }

    private static List<String> labels(java.awt.Component c, List<String> out) {
        if (c instanceof javax.swing.JLabel l) {
            String t = l.getText() == null ? "" : l.getText().replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").strip();
            if (!t.isEmpty()) {
                out.add(t);
            }
        } else if (c instanceof java.awt.Container k) {
            for (java.awt.Component child : k.getComponents()) {
                labels(child, out);
            }
        }
        return out;
    }
}
