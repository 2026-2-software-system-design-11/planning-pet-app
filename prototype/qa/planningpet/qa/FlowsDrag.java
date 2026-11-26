package planningpet.qa;

import planningpet.schedule.model.Task;
import planningpet.schedule.view.DragCursors;
import planningpet.schedule.view.TimetablePanel;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;

/** UF-1-5(D-99): 일정관리 목록 줄을 시간표 빈칸으로 끌어 놓고 되돌리기. */
final class FlowsDrag {
    private FlowsDrag() {
    }

    static void uf15() {
        Qa q = new Qa("UF-1-5");
        q.start(Personas.p1());
        q.open();
        q.step("1", "일정관리를 연다");
        q.app.show("일정관리");
        q.app.root.setSize(1000, 680);
        layout(q.app.root);
        q.sched.scrollTo(15 * 60);
        layout(q.app.root);
        TimetablePanel table = QaDrive.find(q.app.root, TimetablePanel.class);
        q.eq("펫", "이번 주 남은 날의 시각 없는 할 일이 3개 있어. 자동 배치를 눌러 볼래?", q.schedBubble());
        q.step("2", "11/14 토익 단어 줄의 제목을 잡아 금 13일 칸 16:10 자리 위로 끈다");
        Task toeic = q.task("토익 단어");
        Component title = drag(q, table, toeic, 4, false);
        q.eq("흐린 날, 상자, 마우스", "[2026-11-09, 2026-11-10, 2026-11-11, 2026-11-12, 2026-11-13] 지난 날 금지",
                table.dimmed() + " " + table.dragText() + " " + (title.getCursor() == DragCursors.NO ? "금지" : "다른 모양"));
        q.step("3", "토 14일 칸 16:10 자리로 옮겨 놓는다");
        mouse(title, MouseEvent.MOUSE_DRAGGED, at(table, title, 5));
        q.eq("상자", "16:00~16:30", table.dragText());
        mouse(title, MouseEvent.MOUSE_RELEASED, at(table, title, 5));
        q.eq("토익 단어", "2026-11-14 16:00~16:30", toeic.date + " " + planningpet.schedule.model.TaskBook.timeText(toeic));
        q.eq("글 상자", "true 11/14 16:00~16:30에 놓았어요 true", note(table));
        q.eq("오른쪽 목록", "[ ] 11/10 빨래 30분 지난 날 / [ ] 11/12 도서관 책 반납 30분 지난 날 / [ ] 11/14 운동 30분"
                + " / [ ] 11/15 은행 서류 30분", q.schedRows());
        q.eq("펫", "이번 주 남은 날의 시각 없는 할 일이 2개 있어. 자동 배치를 눌러 볼래?", q.schedBubble());
        q.step("4", "되돌리기를 누른다");
        table.note.undo.doClick();
        q.eq("토익 단어", "2026-11-14 null", toeic.date + " " + toeic.start);
        q.eq("글 상자", "false", table.note.isVisible());
        q.eq("오른쪽 목록", "[ ] 11/10 빨래 30분 지난 날 / [ ] 11/12 도서관 책 반납 30분 지난 날 / [ ] 11/14 토익 단어 30분"
                + " / [ ] 11/14 운동 30분 / [ ] 11/15 은행 서류 30분", q.schedRows());
        q.eq("펫", "이번 주 남은 날의 시각 없는 할 일이 3개 있어. 자동 배치를 눌러 볼래?", q.schedBubble());
        q.step("5", "다시 토 14일 16:10 자리에 끌어 놓고 홈으로 간다");
        drag(q, table, toeic, 5, true);
        q.app.show("홈");
        q.eq("글 상자", "false", table.note.isVisible());
        q.has("목록", "토익 단어 16:00~16:30 30분", q.rows());
    }

    /** t 줄의 제목을 잡아 i번째 날 칸 16:10 자리로 끌고, release면 놓는다. 잡은 제목 칸을 돌려준다. */
    private static Component drag(Qa q, TimetablePanel table, Task t, int i, boolean release) {
        Component title = null;
        for (Component row : q.sched.untimed.getComponents()) {
            var l = row instanceof java.awt.Container k ? QaDrive.find(k, planningpet.schedule.view.TaskRows.TitleLabel.class) : null;
            title = l != null && l.getText().contains(t.title) ? l : title;
        }
        mouse(title, MouseEvent.MOUSE_PRESSED, new Point(5, 5));
        mouse(title, MouseEvent.MOUSE_DRAGGED, at(table, title, i));
        if (release) {
            mouse(title, MouseEvent.MOUSE_RELEASED, at(table, title, i));
        }
        return title;
    }

    /** 시간표의 i번째 날 칸 가운데, 16:10 자리를 c의 좌표로. */
    private static Point at(TimetablePanel table, Component c, int i) {
        int w = (table.getWidth() - TimetablePanel.GUTTER) / 7;
        return SwingUtilities.convertPoint(table, new Point(TimetablePanel.GUTTER + i * w + w / 2, (16 * 60 + 10) * TimetablePanel.HOUR / 60), c);
    }

    private static String note(TimetablePanel t) {
        return t.note.isVisible() + " " + t.note.text.getText() + " " + t.note.undo.isVisible();
    }

    private static void mouse(Component c, int id, Point p) {
        c.dispatchEvent(new MouseEvent(c, id, 0, MouseEvent.BUTTON1_DOWN_MASK, p.x, p.y, 1, false, MouseEvent.BUTTON1));
    }

    private static void layout(Component c) {
        c.doLayout();
        if (c instanceof java.awt.Container k) {
            for (Component child : k.getComponents()) {
                layout(child);
            }
        }
    }
}
