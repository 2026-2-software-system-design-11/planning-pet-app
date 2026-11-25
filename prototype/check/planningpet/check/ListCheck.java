package planningpet.check;

import static planningpet.check.PeriodCheck.at;
import static planningpet.check.PeriodCheck.d;
import static planningpet.check.ProtoCheck.eq;

import planningpet.app.Main;
import planningpet.app.view.App;
import planningpet.schedule.model.Task;
import planningpet.schedule.view.ScheduleScreen;
import planningpet.schedule.view.TaskRows;
import planningpet.schedule.view.TimetablePanel;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/** 06 검사 표 가운데 D-99(일정관리 목록에서 시각 정하기)의 줄. ProtoCheck가 부른다. */
final class ListCheck {
    private ListCheck() {
    }

    static void run() {
        rowClick();
        places();
        drops();
        preview();
    }

    /** T-EDIT-23(D-99): 끄는 동안 흐린 날과 따라오는 상자의 글. */
    static void preview() {
        App app = Main.create(at("11-17T09:00"));
        Task p = PeriodCheck.period(app.store, "P", "11-16", "11-20", 30);
        app.show("일정관리");
        app.store.changed();
        TimetablePanel table = ProtoCheck.find(app.root, TimetablePanel.class);
        table.setSize(TimetablePanel.GUTTER + 7 * 100, TimetablePanel.HOUR * TimetablePanel.HOURS);
        table.drag(p);
        boolean wed = table.dragTo(new java.awt.Point(TimetablePanel.GUTTER + 3 * 100 + 50, (14 * 60 + 7) * TimetablePanel.HOUR / 60));
        String over = table.dragText();
        boolean mon = table.dragTo(new java.awt.Point(TimetablePanel.GUTTER + 50, 600));
        eq("T-EDIT-23", "[2026-11-16, 2026-11-21, 2026-11-22] false 14:00~14:30 true 지난 날",
                table.dimmed() + " " + wed + " " + over + " " + mon + " " + table.dragText());
        table.paint(new java.awt.image.BufferedImage(800, 400, java.awt.image.BufferedImage.TYPE_INT_ARGB).createGraphics());
    }

    /** D-99 놓을 자리(06의 4번 dropStart, dropRange)와 T-EDIT-21의 보이는 날. */
    static void places() {
        App app = Main.create(at("11-18T09:00"));
        Task q = PeriodCheck.period(app.store, "Q", "11-10", "11-16", 30);
        Task p = PeriodCheck.period(app.store, "P", "11-20", "11-22", 30);
        var book = app.store.book;
        String ranges = java.util.Arrays.toString(book.dropRange(q)) + " " + java.util.Arrays.toString(book.dropRange(p));
        String starts = planningpet.schedule.model.TaskBook.dropStart(14 * 60 + 7, 30) + " "
                + planningpet.schedule.model.TaskBook.dropStart(27 * 60 + 40, 60);
        Runnable undo = book.keep(q, false);
        book.placeAt(q, d("11-19"), 600, false);
        String placed = q.date + " " + q.dueDate + " " + book.dayPlan(d("11-19")).timed().stream().map(t -> t.title).toList() + " " + q.shownOn(d("11-16"), d("11-18"));
        undo.run();
        eq("D-99 놓을 자리, T-EDIT-21", "[2026-11-18, null] [2026-11-20, 2026-11-22] 840 1620 2026-11-19 2026-11-16 [Q] false / 2026-11-10 null",
                ranges + " " + starts + " " + placed + " / " + q.date + " " + q.start);
    }

    /** T-EDIT-16(D-99): 일정관리 목록 줄을 한 번 누르면 시각 고치기 창, 두 번 눌러도 창 하나, 끝낸 줄은 없다. */
    static void rowClick() {
        App app = Main.create(at("11-17T09:00"));
        Task work = app.store.book.add(Task.Kind.TASK, "과제", d("11-18"), null, 30, "", null).get(0);
        Task wash = app.store.book.add(Task.Kind.TASK, "빨래", d("11-18"), null, 30, "", null).get(0);
        app.schedule.check(wash);
        List<String> opened = new ArrayList<>();
        app.schedule.timeOpener = (t, day, anchor) -> opened.add("창 " + t.title + " " + day);
        app.schedule.opener = (t, day) -> opened.add("수정 " + t.title);
        app.show("일정관리");
        app.store.changed();
        ScheduleScreen sched = ProtoCheck.find(app.root, ScheduleScreen.class);
        click(title(sched.untimed, work), 1);
        click(title(sched.untimed, work), 1);
        click(title(sched.untimed, work), 2);
        click(title(sched.untimed, wash), 1);
        eq("T-EDIT-16", "창 과제 2026-11-18, 창 과제 2026-11-18", String.join(", ", opened));
        App late = Main.create(at("11-18T09:00"));
        Task job = PeriodCheck.period(late.store, "지원서", "11-12", "11-20", 30);
        late.schedule.timeOpener = (t, day, anchor) -> late.schedule.saveTime(t, 14 * 60, 30, day);
        late.show("일정관리");
        late.store.changed();
        click(title(ProtoCheck.find(late.root, ScheduleScreen.class).untimed, job), 1);
        eq("T-EDIT-22", "2026-11-18 14:00 2026-11-20", job.date + " " + job.start + " " + job.dueDate);
    }

    /** 놓은 결과 글과 일의 날짜, 시각. */
    static String dropped(App app, Task t, String day, int minute) {
        var r = app.schedule.drop(t, d(day), minute);
        return r.text() + " " + t.date + " " + (t.start == null ? "시각 없음" : planningpet.schedule.model.TaskBook.timeText(t));
    }

    /** T-EDIT-17~20(D-99): 끌어 놓기의 30분 맞춤, 04:00 당김, 놓을 수 없는 날, 반복 사본과 되돌리기. */
    static void drops() {
        App app = Main.create(at("11-17T09:00"));
        Task work = app.store.book.add(Task.Kind.TASK, "과제", d("11-17"), null, 30, "", null).get(0);
        var r = app.schedule.drop(work, d("11-19"), 14 * 60 + 7);
        String placed = r.text() + " " + work.date + " " + work.start;
        r.undo().run();
        eq("T-EDIT-17", "11/19 14:00~14:30에 놓았어요 2026-11-19 14:00 / 2026-11-17 null", placed + " / " + work.date + " " + work.start);
        Task review = app.store.book.add(Task.Kind.TASK, "복습", d("11-17"), null, 60, "", null).get(0);
        eq("T-EDIT-18", "다음 날 04:00까지 끝나야 해서 03:00(다음 날)에 놓았어요 2026-11-17 03:00~04:00(다음 날)",
                dropped(app, review, "11-17", 27 * 60 + 40));
        Task wash = app.store.book.add(Task.Kind.TASK, "빨래", d("11-17"), null, 30, "", null).get(0);
        Task p = PeriodCheck.period(app.store, "P", "11-16", "11-20", 30);
        Task q = PeriodCheck.period(app.store, "Q", "11-10", "11-16", 30);
        app.store.now = java.time.LocalDateTime.parse("2026-11-18T09:00");
        eq("T-EDIT-21", "지난 날에는 놓을 수 없어요 2026-11-10 시각 없음 / 11/19 10:00~10:30에 놓았어요 2026-11-19 10:00~10:30",
                dropped(app, q, "11-17", 600) + " / " + dropped(app, q, "11-19", 600));
        app.store.now = java.time.LocalDateTime.parse("2026-11-17T09:00");
        eq("T-EDIT-19", "지난 날 지난 날 11/17~11/20만 돼요 null / 지난 날에는 놓을 수 없어요 2026-11-17 시각 없음"
                + " / 11/17부터 11/20까지 놓을 수 있어요 2026-11-16 시각 없음 / 11/18 09:00~09:30에 놓았어요 2026-11-18 09:00~09:30",
                app.schedule.blocked(wash, d("11-16")) + " " + app.schedule.blocked(p, d("11-16")) + " "
                        + app.schedule.blocked(p, d("11-21")) + " " + app.schedule.blocked(p, d("11-18")) + " / "
                        + dropped(app, wash, "11-16", 600) + " / " + dropped(app, p, "11-21", 600) + " / " + dropped(app, p, "11-18", 540));
        App rep = Main.create(at("11-17T09:00"));
        List<Task> runs = PeriodCheck.daily(rep.store);
        rep.schedule.chooser = (title, message, options) -> 1;
        Runnable undo = rep.schedule.drop(runs.get(2), d("11-18"), 7 * 60).undo();
        String after = times(runs);
        undo.run();
        eq("T-EDIT-20", "- - 07:00 07:00 07:00 07:00 07:00 / - - - - - - -", after + " / " + times(runs));
    }

    private static String times(List<Task> runs) {
        return String.join(" ", runs.stream().map(t -> t.start == null ? "-" : t.start.toString()).toList());
    }

    /** 누르기 한 번(count 1)이나 빨리 두 번 누른 두 번째(count 2)를 보낸다. 두 번은 1과 2가 이어서 온다. */
    static void click(java.awt.Component c, int count) {
        c.dispatchEvent(new MouseEvent(c, MouseEvent.MOUSE_CLICKED, 0, 0, 5, 5, count, false, MouseEvent.BUTTON1));
    }

    /** 목록에서 그 일의 줄에 있는 제목 칸. */
    static TaskRows.TitleLabel title(java.awt.Container list, Task t) {
        for (java.awt.Component row : list.getComponents()) {
            if (row instanceof java.awt.Container k && PeriodCheck.box(k, t) != null) {
                return find(k);
            }
        }
        return null;
    }

    private static TaskRows.TitleLabel find(java.awt.Container c) {
        for (java.awt.Component child : c.getComponents()) {
            if (child instanceof TaskRows.TitleLabel l) {
                return l;
            }
            if (child instanceof java.awt.Container k && find(k) != null) {
                return find(k);
            }
        }
        return null;
    }
}
