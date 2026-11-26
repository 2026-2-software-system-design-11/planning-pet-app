package planningpet.check;

import static planningpet.check.PeriodCheck.at;
import static planningpet.check.PeriodCheck.d;
import static planningpet.check.ProtoCheck.eq;

import planningpet.app.Main;
import planningpet.app.view.App;
import planningpet.common.view.Theme;
import planningpet.schedule.model.Task;
import planningpet.schedule.model.TaskDraft;
import planningpet.schedule.view.ScheduleScreen;
import planningpet.schedule.view.DragCursors;
import planningpet.schedule.view.DragReturn;
import planningpet.schedule.view.DropNote;
import planningpet.schedule.view.TaskRows;
import planningpet.schedule.view.TimeEditor;
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
        pastDueClock();
        editorSpot();
        twoLines();
        places();
        pastDue();
        drops();
        preview();
        grab();
        notePlace();
        noteBelow();
        escape();
        rollEdge();
        outside();
        setting();
    }

    /** T-EDIT-44(D-102): 설정 "목록 누르기"는 처음 꺼져 있고, 켜면 앱 데이터에 남으며, 화면 밖에서 바뀐 값을 설정 화면이 따른다. */
    static void setting() {
        App app = Main.create(at("11-17T09:00"));
        var screen = ProtoCheck.find(app.root, planningpet.settings.view.SettingsScreen.class);
        String before = screen.listClickTime.getText() + " " + screen.listClickTime.isSelected() + " " + app.store.listClickTime;
        screen.listClickTime.doClick();
        String on = screen.listClickTime.isSelected() + " " + app.store.listClickTime;
        app.settings.listClickTime(false);
        eq("T-EDIT-44", "줄을 한 번 누르면 시각 고치기 창 열기 false false / true true / false",
                before + " / " + on + " / " + screen.listClickTime.isSelected());
    }

    /** T-EDIT-28(D-99): 시간표 밖에 놓으면 바뀌는 것이 없고, 상자가 손을 놓은 자리에서 목록의 그 줄 자리로 돌아간다. */
    static void outside() {
        App app = Main.create(at("11-17T09:00"));
        Task work = app.store.book.add(Task.Kind.TASK, "과제", d("11-17"), null, 30, "", null).get(0);
        app.show("일정관리");
        app.store.changed();
        javax.swing.JRootPane frame = new javax.swing.JRootPane();
        frame.setContentPane(app.root);
        frame.setSize(1000, 680);
        PeriodCheck.layout(frame);
        ScheduleScreen sched = ProtoCheck.find(app.root, ScheduleScreen.class);
        TimetablePanel table = ProtoCheck.find(app.root, TimetablePanel.class);
        TaskRows.TitleLabel title = title(sched.untimed, work);
        java.awt.Container row = title.getParent();
        mouse(title, MouseEvent.MOUSE_PRESSED, new java.awt.Point(5, 5));
        mouse(title, MouseEvent.MOUSE_DRAGGED, javax.swing.SwingUtilities.convertPoint(table, new java.awt.Point(300, 300), title));
        mouse(title, MouseEvent.MOUSE_DRAGGED, new java.awt.Point(20, 60));
        mouse(title, MouseEvent.MOUSE_RELEASED, new java.awt.Point(20, 60));
        DragReturn back = ProtoCheck.find(frame.getLayeredPane(), DragReturn.class);
        java.awt.Point at = javax.swing.SwingUtilities.convertPoint(title, new java.awt.Point(20, 60), frame.getLayeredPane());
        java.awt.Rectangle home = javax.swing.SwingUtilities.convertRectangle(row.getParent(), row.getBounds(), frame.getLayeredPane());
        String out = work.date + " " + work.start + " " + table.note.isVisible() + " / " + back.from.contains(at) + " " + back.to.equals(home)
                + " " + back.timer.isRunning();
        int w = (table.getWidth() - TimetablePanel.GUTTER) / 7;
        java.awt.Point mon = new java.awt.Point(TimetablePanel.GUTTER + w / 2, table.getVisibleRect().y + 100);
        dropAt(sched, table, work, mon);
        java.awt.Point monAt = javax.swing.SwingUtilities.convertPoint(table, mon, frame.getLayeredPane());
        boolean returned = java.util.Arrays.stream(frame.getLayeredPane().getComponents())
                .anyMatch(c -> c instanceof DragReturn r && r.from.contains(monAt) && r.to.equals(home));
        eq("T-EDIT-28", "2026-11-17 null false / true true true / 2026-11-17 null true 지난 날에는 놓을 수 없어요",
                out + " / " + work.date + " " + work.start + " " + returned + " " + table.note.text.getText());
    }

    /** T-EDIT-27(D-99, S149, S152): 끄는 동안 시간표 끝에 닿으면 굴러가고 목록 위에서는 굴러가지 않는다. 빠르기는 끝에 가까울수록 빠르다. */
    static void rollEdge() {
        App app = Main.create(at("11-17T09:00"));
        Task work = app.store.book.add(Task.Kind.TASK, "과제", d("11-17"), null, 30, "", null).get(0);
        app.show("일정관리");
        app.store.changed();
        app.root.setSize(1000, 680);
        PeriodCheck.layout(app.root);
        ScheduleScreen sched = ProtoCheck.find(app.root, ScheduleScreen.class);
        sched.scrollTo(13 * 60);
        TimetablePanel table = ProtoCheck.find(app.root, TimetablePanel.class);
        java.awt.Container view = table.getParent();
        int h = view.getHeight();
        TaskRows.TitleLabel title = title(sched.untimed, work);
        mouse(title, MouseEvent.MOUSE_PRESSED, new java.awt.Point(5, 5));
        boolean running = true;
        int[] tops = new int[4];
        tops[0] = sched.topMinute();
        java.awt.Point[] spots = {new java.awt.Point(100, h - 3), new java.awt.Point(-1, h - 3), new java.awt.Point(100, -15)};
        for (int k = 0; k < spots.length; k++) {
            java.awt.Point p = javax.swing.SwingUtilities.convertPoint(view, spots[k], title);
            p.x = k == 1 ? title.getWidth() / 2 : p.x;
            mouse(title, MouseEvent.MOUSE_DRAGGED, p);
            running &= sched.untimed.rolling().isRunning();
            for (int n = 0; n < 10; n++) {
                sched.untimed.rolling().getActionListeners()[0].actionPerformed(null);
            }
            tops[k + 1] = sched.topMinute();
        }
        mouse(title, MouseEvent.MOUSE_RELEASED, new java.awt.Point(5, 5));
        String speeds = (int) TimetablePanel.rollSpeed(h - 29, h) + " " + (int) TimetablePanel.rollSpeed(h, h) + " "
                + (int) TimetablePanel.rollSpeed(h + 50, h) + " " + (int) TimetablePanel.rollSpeed(5, h) + " " + (int) TimetablePanel.rollSpeed(h / 2, h);
        eq("T-EDIT-27", "true true true true / 66 240 240 -210 0", running + " " + (tops[1] > tops[0]) + " " + (tops[2] == tops[1]) + " " + (tops[3] < tops[2])
                + " / " + speeds);
    }

    /** T-EDIT-26(D-99): 끄는 동안 줄은 흐리고, Esc를 누르면 끌기를 그만둔다. */
    static void escape() {
        App app = Main.create(at("11-17T09:00"));
        Task work = app.store.book.add(Task.Kind.TASK, "과제", d("11-17"), null, 30, "", null).get(0);
        app.show("일정관리");
        app.store.changed();
        javax.swing.JRootPane frame = new javax.swing.JRootPane();
        frame.setContentPane(app.root);
        frame.setSize(1000, 680);
        PeriodCheck.layout(frame);
        ScheduleScreen sched = ProtoCheck.find(app.root, ScheduleScreen.class);
        TimetablePanel table = ProtoCheck.find(app.root, TimetablePanel.class);
        TaskRows.TitleLabel title = title(sched.untimed, work);
        java.awt.Container row = title.getParent();
        int before = pixels((javax.swing.JComponent) row, Theme.INK);
        mouse(title, MouseEvent.MOUSE_PRESSED, new java.awt.Point(5, 5));
        mouse(title, MouseEvent.MOUSE_DRAGGED, javax.swing.SwingUtilities.convertPoint(table, new java.awt.Point(300, 300), title));
        String during = (sched.untimed.lifted() == work) + " " + table.dimmed().size() + " " + (before > 0) + " "
                + (pixels((javax.swing.JComponent) row, Theme.INK) == 0);
        // 포커스를 가진 칸이 받은 것처럼 보낸다(창이 없으면 포커스가 없다).
        java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().redispatchEvent(title, new java.awt.event.KeyEvent(title,
                java.awt.event.KeyEvent.KEY_PRESSED, 0, 0,
                java.awt.event.KeyEvent.VK_ESCAPE, java.awt.event.KeyEvent.CHAR_UNDEFINED));
        DragReturn back = ProtoCheck.find(frame.getLayeredPane(), DragReturn.class);
        String after = sched.untimed.lifted() + " " + table.dimmed().size() + " " + table.dragText() + " "
                + back.to.equals(javax.swing.SwingUtilities.convertRectangle(row.getParent(), row.getBounds(), frame.getLayeredPane())) + " "
                + (pixels((javax.swing.JComponent) row, Theme.INK) == before);
        mouse(title, MouseEvent.MOUSE_DRAGGED, javax.swing.SwingUtilities.convertPoint(table, new java.awt.Point(320, 300), title));
        mouse(title, MouseEvent.MOUSE_RELEASED, javax.swing.SwingUtilities.convertPoint(table, new java.awt.Point(320, 300), title));
        eq("T-EDIT-26", "true 1 true true / null 0 null true true / 2026-11-17 null", during + " / " + after + " / " + work.date + " " + work.start);
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

    /** T-EDIT-50~53(S158, S147, S148): 블록 양쪽에 글 상자가 들지 않으면 블록 바로 아래(왼쪽 끝 맞춤, 넘치면 밂), 아래 자리가 없으면 바로 위. */
    static void noteBelow() {
        eq("T-EDIT-50", "2026-11-18 22:00~00:30(다음 날) 아래 맞춤 false true true", noteSpot("11-16", 150, 2, false));
        eq("T-EDIT-51", "2026-11-19 22:00~00:30(다음 날) 아래 밂 false true true / 2026-11-18 22:00~00:30(다음 날) 아래 밂 false true true",
                noteSpot("11-16", 150, 3, false) + " / " + noteSpot("11-16", 150, 2, true));
        eq("T-EDIT-52", "2026-11-18 16:00~04:00(다음 날) 위 밂 false true true", noteSpot("11-16", 720, 2, false));
        eq("T-EDIT-53", "2026-11-17 22:00~00:30(다음 날) 아래 맞춤 false true true", noteSpot("11-15", 150, 2, false));
    }

    /**
     * 창 1000x680, 오늘 today(11-15면 일요일 시작 주), 시간표를 맨 아래로 굴린 채 시각 없는 할 일 minutes분을 col번째 날 칸 22:00에
     * (600분이 넘으면 보이는 아래 끝 바로 위에) 놓고 글 상자 자리를 그려진 블록과 견준다. fixed면 그 칸에 고정 일정 22:00~02:00을 두고 "넣기".
     */
    static String noteSpot(String today, int minutes, int col, boolean fixed) {
        planningpet.app.model.Store s = at(today + "T09:00");
        s.weekStart = today.equals("11-15") ? java.time.DayOfWeek.SUNDAY : java.time.DayOfWeek.MONDAY;
        App app = Main.create(s);
        Task t = app.store.book.add(Task.Kind.TASK, "빨래", d(today), null, minutes, "", null).get(0);
        if (fixed) {
            app.store.book.add(Task.Kind.FIXED, "알바", d(today).plusDays(col), java.time.LocalTime.of(22, 0), 240, "", null);
            DayCheck.ask(app, 0);
        }
        app.show("일정관리");
        app.store.changed();
        app.root.setSize(1000, 680);
        PeriodCheck.layout(app.root);
        ScheduleScreen sched = ProtoCheck.find(app.root, ScheduleScreen.class);
        TimetablePanel table = ProtoCheck.find(app.root, TimetablePanel.class);
        sched.scrollTo(28 * 60);
        PeriodCheck.layout(app.root);
        int w = (table.getWidth() - TimetablePanel.GUTTER) / 7;
        java.awt.Rectangle seen = table.getVisibleRect();
        dropAt(sched, table, t, new java.awt.Point(TimetablePanel.GUTTER + col * w + w / 2,
                minutes > 600 ? seen.y + seen.height - 3 : 22 * TimetablePanel.HOUR));
        table.paint(new java.awt.image.BufferedImage(table.getWidth(), table.getHeight(), java.awt.image.BufferedImage.TYPE_INT_ARGB).createGraphics());
        java.awt.Rectangle b = table.blockAreas().entrySet().stream().filter(e -> e.getValue() == t).findFirst().get().getKey();
        java.awt.Rectangle n = table.note.getBounds();
        java.awt.Rectangle now = table.getVisibleRect();
        String side = n.y == b.y + b.height + 2 ? "아래" : n.y + n.height + 2 == b.y ? "위" : "옆";
        String x = n.x == b.x ? "맞춤" : n.x + n.width == table.getWidth() - 2 ? "밂" : n.x + "";
        return t.date + " " + planningpet.schedule.model.TaskBook.timeText(t) + " " + side + " " + x + " " + n.intersects(b) + " "
                + (n.x >= 0 && n.x + n.width <= table.getWidth()) + " " + (now.contains(n) && now.y <= b.y);
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

    /** D-99 놓을 자리(06의 4번 dropStart, dropRange). 마감이 지난 기간 할 일은 놓을 날이 없다(#272). */
    static void places() {
        App app = Main.create(at("11-18T09:00"));
        Task q = PeriodCheck.period(app.store, "Q", "11-10", "11-16", 30);
        Task p = PeriodCheck.period(app.store, "P", "11-20", "11-22", 30);
        var book = app.store.book;
        String ranges = java.util.Arrays.toString(book.dropRange(q)) + " " + java.util.Arrays.toString(book.dropRange(p));
        String starts = planningpet.schedule.model.TaskBook.dropStart(14 * 60 + 7, 30) + " "
                + planningpet.schedule.model.TaskBook.dropStart(27 * 60 + 40, 60);
        eq("D-99 놓을 자리", "[2026-11-18, 2026-11-16] [2026-11-20, 2026-11-22] 840 1620", ranges + " " + starts);
    }

    /** T-EDIT-21(D-99, #272): 마감이 지난 기간 할 일은 끌리지 않고, 오늘이나 내일에 놓아도 그대로다. */
    static void pastDue() {
        App app = Main.create(at("11-18T09:00"));
        Task q = PeriodCheck.period(app.store, "Q", "11-10", "11-16", 30);
        Task r = app.store.book.add(new TaskDraft(Task.Kind.TASK, "R", d("11-10"), null, 30, "초안").withDue(d("11-16"))).get(0);
        app.show("일정관리");
        app.store.changed();
        app.root.setSize(1000, 680);
        PeriodCheck.layout(app.root);
        ScheduleScreen sched = ProtoCheck.find(app.root, ScheduleScreen.class);
        TimetablePanel table = ProtoCheck.find(app.root, TimetablePanel.class);
        TaskRows.TitleLabel title = title(sched.untimed, q);
        // #277: 끌 수 없는 까닭을 툴팁으로 보이고, 메모가 있으면 그 아래에 잇는다.
        String why = "마감이 지난 일은 옮길 수 없어요";
        String rTip = String.valueOf(title(sched.untimed, r).getToolTipText());
        String tips = why.equals(title.getToolTipText()) + " " + (rTip.indexOf(why) >= 0 && rTip.indexOf(why) < rTip.indexOf("초안")) + " / ";
        String open = (title.getCursor() == DragCursors.OPEN) + "";
        mouse(title, MouseEvent.MOUSE_PRESSED, new java.awt.Point(5, 5));
        mouse(title, MouseEvent.MOUSE_DRAGGED, new java.awt.Point(30, 5));
        String drag = open + " " + table.dimmed();
        mouse(title, MouseEvent.MOUSE_RELEASED, new java.awt.Point(30, 5));
        eq("T-EDIT-21", "true true / false [] / null 2026-11-10 시각 없음 / null 2026-11-10 시각 없음 / 2026-11-16 [2026-11-16]",
                tips + drag + " / " + dropped(app, q, "11-18", 600) + " / " + dropped(app, q, "11-19", 600) + " / " + q.dueDate + " "
                        + java.util.stream.Stream.of("11-16", "11-17", "11-18", "11-19").map(x -> d(x)).filter(x -> q.shownOn(x, d("11-18"))).toList());
    }

    /** T-WEEK-6(D-102, S160): 첫날 줄(마감 전이면 오늘 묶음, D-105)과 마감 날 줄은 표시가 단추와 한 줄에 들어 두 줄(56px)이다. "~10/20까지"는 날짜 글이 가장 넓다. */
    static void twoLines() {
        App app = Main.create(at("11-18T09:00"));
        PeriodCheck.period(app.store, "지원서", "11-12", "11-20", 60);
        PeriodCheck.period(app.store, "발표", "11-18", "11-18", 60);
        Task wide = PeriodCheck.period(app.store, "토익", "11-19", "11-22", 30);
        wide.dueDate = java.time.LocalDate.of(2027, 10, 20);
        app.show("일정관리");
        app.store.changed();
        app.root.setSize(1000, 680);
        PeriodCheck.layout(app.root);
        PeriodCheck.layout(app.root);
        ScheduleScreen sched = ProtoCheck.find(app.root, ScheduleScreen.class);
        List<String> got = new ArrayList<>();
        for (java.awt.Component row : sched.untimed.getComponents()) {
            TaskRows.TitleLabel t = row instanceof java.awt.Container k ? find(k) : null;
            if (t != null) {
                got.add(t.getText().replaceAll("<[^>]+>", "").trim() + " " + row.getHeight());
            }
        }
        javax.swing.JButton pill = PeriodCheck.pill(sched.untimed, wide);
        got.add("시계와 연필 " + pill.getPreferredSize().equals(pill.getParent().getComponent(1).getPreferredSize()));
        eq("T-WEEK-6", "11/18 지원서 56, 11/18 발표 56, 11/19 토익 56, 11/20 지원서 56, 시계와 연필 true / 11/18 지원서 60분 ~11/20까지"
                + " / 11/18 발표 60분 오늘 마감 / 11/19 토익 30분 ~10/20까지 / 11/20 지원서 60분 마감", String.join(", ", got) + " / " + PeriodCheck.rows(sched.untimed));
    }

    /** T-EDIT-45(05 시각 고치기 창): 창은 단추 바로 아래이고, 앱 창 오른쪽 밖으로 나가면 왼쪽으로 밀려 앱 창 안에 든다. */
    static void editorSpot() {
        App app = Main.create(at("11-17T09:00"));
        Task work = app.store.book.add(Task.Kind.TASK, "과제", d("11-18"), null, 30, "", null).get(0);
        Task read = app.store.book.add(Task.Kind.TASK, "독서", d("11-17"), null, 30, "", null).get(0);
        java.awt.Dimension size = new TimeEditor(work, d("11-18"), app.store.now).panel.getPreferredSize();
        // 앱 창이 화면 (100, 40)에 있다고 보고 화면 좌표로 견준다. 창 자리의 x나 y를 앱 창 자리 없이 쓰면 틀린다(N7c).
        java.awt.Point origin = new java.awt.Point(100, 40);
        List<String> got = new ArrayList<>();
        for (String screen : List.of("일정관리", "홈")) {
            app.show(screen);
            app.store.changed();
            app.root.setSize(1000, 680);
            PeriodCheck.layout(app.root);
            java.awt.Container list = screen.equals("홈") ? ProtoCheck.find(app.root, planningpet.home.view.MainScreen.class).rows
                    : ProtoCheck.find(app.root, ScheduleScreen.class).untimed;
            javax.swing.JButton pill = PeriodCheck.pill(list, screen.equals("홈") ? read : work);
            java.awt.Rectangle b = javax.swing.SwingUtilities.convertRectangle(pill.getParent(), pill.getBounds(), app.root);
            java.awt.Point p = TimeEditor.spot(pill, size, app.root, origin);
            got.add(screen + " " + (p.x + size.width <= 100 + 1000) + " " + (p.x <= 100 + b.x) + " " + (p.y == 40 + b.y + b.height + 2));
        }
        app.root.setSize(size.width - 50, 680);
        got.add("좁은 창 " + TimeEditor.spot(app.root, size, app.root, origin).x);
        eq("T-EDIT-45", "일정관리 true true true, 홈 true true true, 좁은 창 100", String.join(", ", got));
    }

    /** T-EDIT-42(D-102): 마감이 지난 기간 할 일 줄의 시각 단추는 흐리고, 단추의 동작을 불러도 컨트롤러가 막는다. */
    static void pastDueClock() {
        App app = Main.create(at("11-18T09:00"));
        Task q = PeriodCheck.period(app.store, "Q", "11-10", "11-16", 30);
        List<String> opened = new ArrayList<>();
        app.schedule.timeOpener = (t, day, anchor) -> opened.add("창 " + t.title + " " + day);
        app.show("일정관리");
        app.store.changed();
        ScheduleScreen sched = ProtoCheck.find(app.root, ScheduleScreen.class);
        javax.swing.JButton pill = PeriodCheck.pill(sched.untimed, q);
        app.settings.listClickTime(true);
        click(title(sched.untimed, q), 1);
        for (java.awt.event.ActionListener l : pill.getActionListeners()) {
            l.actionPerformed(new java.awt.event.ActionEvent(pill, java.awt.event.ActionEvent.ACTION_PERFORMED, ""));
        }
        eq("T-EDIT-42", "false 마감이 지난 일은 시각을 정할 수 없어요 [] 2026-11-10 null", pill.isEnabled() + " " + pill.getToolTipText() + " " + opened + " " + q.date + " " + q.start);
    }

    /** T-EDIT-16(D-102): 한 번은 아무 일도 없고 두 번은 수정 창, 시각 단추는 둘째 줄 연필 앞. 설정을 켜면 두 목록 모두 한 번에 창, 두 번째는 없음. */
    static void rowClick() {
        App app = Main.create(at("11-17T09:00"));
        Task work = app.store.book.add(Task.Kind.TASK, "과제", d("11-18"), null, 30, "", null).get(0);
        Task wash = app.store.book.add(Task.Kind.TASK, "빨래", d("11-18"), null, 30, "", null).get(0);
        Task read = app.store.book.add(Task.Kind.TASK, "독서", d("11-17"), null, 30, "", null).get(0);
        app.schedule.check(wash);
        List<String> opened = new ArrayList<>();
        app.schedule.timeOpener = (t, day, anchor) -> opened.add("창 " + t.title + " " + day);
        app.schedule.opener = (t, day) -> opened.add("수정 " + t.title);
        app.show("일정관리");
        app.store.changed();
        ScheduleScreen sched = ProtoCheck.find(app.root, ScheduleScreen.class);
        click(title(sched.untimed, work), 1);
        click(title(sched.untimed, work), 2);
        javax.swing.JButton pill = PeriodCheck.pill(sched.untimed, work);
        pill.doClick(0);
        java.awt.Component[] line = pill.getParent().getComponents();
        String off = String.join(", ", opened) + " / " + (line[0] == pill) + " " + pill.getText().isEmpty() + " " + pill.getToolTipText() + " "
                + ((javax.swing.JButton) line[1]).getToolTipText() + " "
                + PeriodCheck.pill(sched.untimed, wash).isEnabled();
        opened.clear();
        app.settings.listClickTime(true);
        click(title(sched.untimed, work), 1);
        click(title(sched.untimed, work), 2);
        app.show("홈");
        app.store.changed();
        var home = ProtoCheck.find(app.root, planningpet.home.view.MainScreen.class);
        click(title(home.rows, read), 1);
        click(title(home.rows, read), 2);
        app.root.setSize(1000, 680);
        PeriodCheck.layout(app.root);
        TaskRows.TitleLabel at = title(home.rows, read);
        java.awt.Point pressed = javax.swing.SwingUtilities.convertPoint(at, new java.awt.Point(5, 5), app.root);
        javax.swing.JButton homePill = PeriodCheck.pill(home.rows, read);
        eq("T-EDIT-16", "수정 과제, 창 과제 2026-11-18 / true true 시각 정하기 수정 false / 창 과제 2026-11-18, 창 독서 2026-11-17 true",
                off + " / " + String.join(", ", opened) + " " + (TimeEditor.spot(homePill, new java.awt.Dimension(300, 150), app.root, new java.awt.Point()).y > pressed.y));
        App late = Main.create(at("11-18T09:00"));
        Task job = PeriodCheck.period(late.store, "지원서", "11-12", "11-20", 30);
        late.schedule.timeOpener = (t, day, anchor) -> late.schedule.saveTime(t, 14 * 60, 30, day);
        late.show("일정관리");
        ScheduleScreen lastWeek = ProtoCheck.find(late.root, ScheduleScreen.class);
        lastWeek.move(d("11-09"));
        late.store.changed();
        String row = PeriodCheck.rows(lastWeek.untimed);
        PeriodCheck.pill(lastWeek.untimed, job).doClick(0);
        eq("T-EDIT-22", "11/15 지원서 30분 ~11/20까지 / 2026-11-18 14:00 2026-11-20", row + " / " + job.date + " " + job.start + " " + job.dueDate);
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
