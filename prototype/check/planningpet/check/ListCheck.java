package planningpet.check;

import static planningpet.check.PeriodCheck.at;
import static planningpet.check.PeriodCheck.d;
import static planningpet.check.ProtoCheck.eq;

import planningpet.app.Main;
import planningpet.app.view.App;
import planningpet.common.view.Theme;
import planningpet.schedule.model.Task;
import planningpet.schedule.view.ScheduleScreen;
import planningpet.schedule.view.DragCursors;
import planningpet.schedule.view.DropNote;
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
        grab();
        notePlace();
        escape();
    }

    /** T-EDIT-26(D-99): 끄는 동안 줄은 흐리고, Esc를 누르면 끌기를 그만둔다. */
    static void escape() {
        App app = Main.create(at("11-17T09:00"));
        Task work = app.store.book.add(Task.Kind.TASK, "과제", d("11-17"), null, 30, "", null).get(0);
        app.show("일정관리");
        app.store.changed();
        app.root.setSize(1000, 680);
        PeriodCheck.layout(app.root);
        ScheduleScreen sched = ProtoCheck.find(app.root, ScheduleScreen.class);
        TimetablePanel table = ProtoCheck.find(app.root, TimetablePanel.class);
        TaskRows.TitleLabel title = title(sched.untimed, work);
        mouse(title, MouseEvent.MOUSE_PRESSED, new java.awt.Point(5, 5));
        mouse(title, MouseEvent.MOUSE_DRAGGED, javax.swing.SwingUtilities.convertPoint(table, new java.awt.Point(300, 300), title));
        String during = (sched.untimed.lifted() == work) + " " + table.dimmed().size();
        // 포커스를 가진 칸이 받은 것처럼 보낸다(창이 없으면 포커스가 없다).
        java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().redispatchEvent(title, new java.awt.event.KeyEvent(title,
                java.awt.event.KeyEvent.KEY_PRESSED, 0, 0,
                java.awt.event.KeyEvent.VK_ESCAPE, java.awt.event.KeyEvent.CHAR_UNDEFINED));
        String after = sched.untimed.lifted() + " " + table.dimmed().size() + " " + table.dragText();
        mouse(title, MouseEvent.MOUSE_DRAGGED, javax.swing.SwingUtilities.convertPoint(table, new java.awt.Point(320, 300), title));
        mouse(title, MouseEvent.MOUSE_RELEASED, javax.swing.SwingUtilities.convertPoint(table, new java.awt.Point(320, 300), title));
        eq("T-EDIT-26", "true 1 / null 0 null / 2026-11-17 null", during + " / " + after + " / " + work.date + " " + work.start);
    }

    /** T-EDIT-40(D-99, S146, S75): 글 상자 자리(오른쪽에 자리가 없으면 왼쪽, 보이는 아래 끝 안)와 주를 옮기면 닫힘. */
    static void notePlace() {
        App app = Main.create(at("11-17T09:00"));
        Task work = app.store.book.add(Task.Kind.TASK, "과제", d("11-17"), null, 30, "", null).get(0);
        app.show("일정관리");
        app.store.changed();
        app.root.setSize(1000, 680);
        PeriodCheck.layout(app.root);
        ScheduleScreen sched = ProtoCheck.find(app.root, ScheduleScreen.class);
        sched.scrollTo(13 * 60);
        PeriodCheck.layout(app.root);
        TimetablePanel table = ProtoCheck.find(app.root, TimetablePanel.class);
        DropNote note = table.note;
        int w = (table.getWidth() - TimetablePanel.GUTTER) / 7;
        int thu = TimetablePanel.GUTTER + 3 * w;
        dropAt(sched, table, work, new java.awt.Point(thu + w / 2, (14 * 60 + 7) * TimetablePanel.HOUR / 60));
        String left = work.date + " " + note.isVisible() + " " + (note.getX() + note.getWidth() <= thu);
        note.undo.doClick();
        java.awt.Rectangle seen = table.getVisibleRect();
        dropAt(sched, table, work, new java.awt.Point(thu - w / 2, seen.y + seen.height - 3));
        String low = work.date + " " + note.isVisible() + " " + (note.getY() + note.getHeight() <= seen.y + seen.height);
        sched.move(sched.weekStart().plusWeeks(1));
        String moved = note.isVisible() + "";
        sched.move(d("11-16"));
        Task log = app.store.book.add(Task.Kind.TASK, "긴 일", d("11-17"), null, 720, "", null).get(0);
        app.store.changed();
        PeriodCheck.layout(app.root);
        sched.scrollTo(24 * 60);
        PeriodCheck.layout(app.root);
        seen = table.getVisibleRect();
        dropAt(sched, table, log, new java.awt.Point(thu + w / 2, seen.y + seen.height - 3));
        java.awt.Rectangle now = table.getVisibleRect();
        String pulled = planningpet.schedule.model.TaskBook.timeText(log) + " " + (now.y <= log.startMinute() * TimetablePanel.HOUR / 60)
                + " " + now.contains(note.getBounds());
        eq("T-EDIT-40", "2026-11-19 true true / 2026-11-18 true true / false / 16:00~04:00(다음 날) true true",
                left + " / " + low + " / " + moved + " / " + pulled);
    }

    /** 일정관리 목록의 줄 t를 잡아 시간표 좌표 p에 놓는다. */
    static void dropAt(ScheduleScreen sched, TimetablePanel table, Task t, java.awt.Point p) {
        TaskRows.TitleLabel title = title(sched.untimed, t);
        mouse(title, MouseEvent.MOUSE_PRESSED, new java.awt.Point(5, 5));
        mouse(title, MouseEvent.MOUSE_DRAGGED, javax.swing.SwingUtilities.convertPoint(table, p, title));
        mouse(title, MouseEvent.MOUSE_RELEASED, javax.swing.SwingUtilities.convertPoint(table, p, title));
    }

    /** T-EDIT-24(D-99): 체크박스로는 끌리지 않고, 줄을 잡아 끌면 마우스 모양이 바뀌고 놓은 자리에 들어간다. */
    static void grab() {
        App app = Main.create(at("11-17T09:00"));
        Task work = app.store.book.add(Task.Kind.TASK, "과제", d("11-17"), null, 30, "", null).get(0);
        app.show("일정관리");
        app.store.changed();
        app.root.setSize(1000, 680);
        PeriodCheck.layout(app.root);
        ScheduleScreen sched = ProtoCheck.find(app.root, ScheduleScreen.class);
        sched.scrollTo(13 * 60);
        PeriodCheck.layout(app.root);
        TimetablePanel table = ProtoCheck.find(app.root, TimetablePanel.class);
        int w = (table.getWidth() - TimetablePanel.GUTTER) / 7;
        java.awt.Point wed = new java.awt.Point(TimetablePanel.GUTTER + 2 * w + w / 2, (14 * 60 + 7) * TimetablePanel.HOUR / 60);
        java.awt.Point mon = new java.awt.Point(TimetablePanel.GUTTER + w / 2, wed.y);
        javax.swing.JCheckBox box = PeriodCheck.box(sched.untimed, work);
        mouse(box, MouseEvent.MOUSE_PRESSED, new java.awt.Point(2, 2));
        mouse(box, MouseEvent.MOUSE_DRAGGED, javax.swing.SwingUtilities.convertPoint(table, wed, box));
        mouse(box, MouseEvent.MOUSE_EXITED, javax.swing.SwingUtilities.convertPoint(table, wed, box));
        mouse(box, MouseEvent.MOUSE_RELEASED, javax.swing.SwingUtilities.convertPoint(table, wed, box));
        String byBox = work.done + " " + work.date + " " + work.start;
        TaskRows.TitleLabel title = title(sched.untimed, work);
        String open = (title.getCursor() == DragCursors.OPEN) + "";
        mouse(title, MouseEvent.MOUSE_PRESSED, new java.awt.Point(5, 5));
        mouse(title, MouseEvent.MOUSE_DRAGGED, new java.awt.Point(10, 5));
        String five = table.dimmed() + "";
        mouse(title, MouseEvent.MOUSE_DRAGGED, new java.awt.Point(11, 5));
        String six = table.dimmed() + "";
        java.awt.Point head = new java.awt.Point(wed.x, table.getVisibleRect().y - 10);
        mouse(title, MouseEvent.MOUSE_DRAGGED, javax.swing.SwingUtilities.convertPoint(table, head, title));
        mouse(title, MouseEvent.MOUSE_RELEASED, javax.swing.SwingUtilities.convertPoint(table, head, title));
        String outside = five + " " + six + " " + work.date + " " + work.start;
        mouse(title, MouseEvent.MOUSE_PRESSED, new java.awt.Point(5, 5));
        mouse(title, MouseEvent.MOUSE_DRAGGED, javax.swing.SwingUtilities.convertPoint(table, mon, title));
        String no = (title.getCursor() == DragCursors.NO) + "";
        mouse(title, MouseEvent.MOUSE_DRAGGED, javax.swing.SwingUtilities.convertPoint(table, wed, title));
        String grab = (title.getCursor() == DragCursors.GRAB) + "";
        mouse(title, MouseEvent.MOUSE_RELEASED, javax.swing.SwingUtilities.convertPoint(table, wed, title));
        eq("T-EDIT-24", "false 2026-11-17 null / true true true / [] [2026-11-16] 2026-11-17 null / 2026-11-18 14:00 []",
                byBox + " / " + open + " " + no + " " + grab + " / " + outside + " / " + work.date + " " + work.start + " " + table.dimmed());
        DropNote note = table.note;
        String placed = note(note, TimetablePanel.GUTTER + 3 * w);
        note.doLayout();
        placed += " " + (pixels(note.undo, Theme.TASK_TEXT) > 0 && pixels(note.undo, Theme.INK) == 0);
        mouse(note, MouseEvent.MOUSE_ENTERED, new java.awt.Point(2, 2));
        String held = note.timer.isRunning() + "";
        note.undo.doClick();
        String undone = work.date + " " + work.start + " " + note.isVisible();
        title = title(sched.untimed, work);
        mouse(title, MouseEvent.MOUSE_PRESSED, new java.awt.Point(5, 5));
        mouse(title, MouseEvent.MOUSE_DRAGGED, javax.swing.SwingUtilities.convertPoint(table, mon, title));
        mouse(title, MouseEvent.MOUSE_RELEASED, javax.swing.SwingUtilities.convertPoint(table, mon, title));
        String why = note(note, mon.x);
        mouse(title, MouseEvent.MOUSE_PRESSED, new java.awt.Point(5, 5));
        mouse(title, MouseEvent.MOUSE_DRAGGED, javax.swing.SwingUtilities.convertPoint(table, wed, title));
        eq("T-EDIT-25", "true 11/18 14:00~14:30에 놓았어요 true 5000 오른쪽 true / false / 2026-11-17 null false"
                + " / true 지난 날에는 놓을 수 없어요 false 3000 오른쪽 / false",
                placed + " / " + held + " / " + undone + " / " + why + " / " + note.isVisible());
    }

    /** 글 상자가 보이는지, 글, 되돌리기가 보이는지, 머무는 시간, 시간표 좌표 x의 오른쪽에 있는지. */
    static String note(DropNote n, int x) {
        return n.isVisible() + " " + n.text.getText() + " " + n.undo.isVisible() + " " + n.timer.getInitialDelay() + " "
                + (n.getX() >= x ? "오른쪽" : "왼쪽");
    }

    /** 부품을 그려 색 c에 가까운 화소의 수를 센다. */
    static int pixels(javax.swing.JComponent part, java.awt.Color c) {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(part.getWidth(), part.getHeight(), java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();
        part.paint(g);
        g.dispose();
        int n = 0;
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                java.awt.Color p = new java.awt.Color(img.getRGB(x, y), true);
                n += p.getAlpha() > 200 && Math.abs(p.getRed() - c.getRed()) + Math.abs(p.getGreen() - c.getGreen()) + Math.abs(p.getBlue() - c.getBlue()) < 24 ? 1 : 0;
            }
        }
        return n;
    }

    static void mouse(java.awt.Component c, int id, java.awt.Point p) {
        c.dispatchEvent(new MouseEvent(c, id, 0, MouseEvent.BUTTON1_DOWN_MASK, p.x, p.y, 1, false, MouseEvent.BUTTON1));
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
