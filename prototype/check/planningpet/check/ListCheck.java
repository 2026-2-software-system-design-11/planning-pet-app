package planningpet.check;

import static planningpet.check.PeriodCheck.at;
import static planningpet.check.PeriodCheck.d;
import static planningpet.check.ProtoCheck.eq;

import planningpet.app.Main;
import planningpet.app.view.App;
import planningpet.schedule.model.Task;
import planningpet.schedule.view.ScheduleScreen;
import planningpet.schedule.view.TaskRows;
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
