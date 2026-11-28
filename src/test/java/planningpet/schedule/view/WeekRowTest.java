package planningpet.schedule.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static planningpet.schedule.model.Tasks.d;
import static planningpet.schedule.model.Tasks.task;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;
import planningpet.app.controller.AppController;
import planningpet.app.model.AppData;
import planningpet.app.model.Apps;
import planningpet.app.model.MemoryStore;
import planningpet.common.view.CircleButton;
import planningpet.common.view.Input;
import planningpet.common.view.Snap;
import planningpet.common.view.Theme;
import planningpet.reward.controller.RewardController;
import planningpet.schedule.model.Repeat;
import planningpet.schedule.model.Task;

/** 일정관리 목록의 할 일 한 줄. 번호는 docs/porting/schedule.md S5a2 2절. 줄은 앱처럼 Theme.clear 뒤 폭 200으로 배치한다. */
class WeekRowTest {
    private static final String GRAY = "<font color='#8A96A0'>";
    private static final String RED = "<font color='#C0392B'>";
    /** 수정, 삭제, 시각 함수가 받은 것. */
    private final List<String> heard = new ArrayList<>();

    /** now의 앱 데이터에 tasks를 둔 목록. 누름은 heard에 적는다. */
    private TaskRows rows(String now, Task... tasks) {
        AppData data = Apps.data(now, tasks);
        RewardController reward = new RewardController(data, new AppController(data, new MemoryStore(), e -> { }));
        return new TaskRows(data, reward, t -> heard.add("수정 " + t.getTitle()), t -> heard.add("삭제 " + t.getTitle()),
                (t, day, at) -> heard.add("시각 " + t.getTitle() + " " + day));
    }

    /** 오늘 11-18 10:00의 목록에서 t의 day 줄. */
    private JPanel row(Task t, LocalDate day) {
        return row(rows("2026-11-18T10:00", t), t, day);
    }

    /** 줄 앞 날짜와 목록의 날이 day인 줄을 만들어 Theme.clear 뒤 폭 200, 알맞은 높이로 배치한다. */
    private static JPanel row(TaskRows r, Task t, LocalDate day) {
        JPanel p = r.row(t, null, day, day);
        Theme.clear(p);
        Snap.layOut(p, 200, p.getPreferredSize().height);
        return p;
    }

    private static String title(JPanel row) {
        return ((JLabel) row.getComponent(1)).getText();
    }

    /** 둘째 줄 오른쪽의 단추 칸(시계, 연필, 휴지통). 둘째 줄의 자식은 표시(있으면)와 단추 칸이다. */
    private static JPanel buttons(JPanel row) {
        JPanel line = (JPanel) row.getComponent(3);
        return (JPanel) line.getComponent(line.getComponentCount() - 1);
    }

    /** 둘째 줄 표시의 글. 표시가 없으면 null. */
    private static String marks(JPanel row) {
        JPanel line = (JPanel) row.getComponent(3);
        return line.getComponentCount() == 2 ? ((JLabel) line.getComponent(0)).getText() : null;
    }

    /** 부품을 그린 그림에서 가장 진한 픽셀의 알파. 흐린 글(INK_DIM)은 그 알파를 넘지 않는다. */
    private static int darkest(JComponent c) {
        return Arrays.stream(Snap.pixels(Snap.paint(c))).map(p -> p >>> 24).max().orElse(0);
    }

    private static TimePill clock(JPanel row) {
        return (TimePill) buttons(row).getComponent(0);
    }

    @Test
    void eWrow8RowDateAndClock() {
        JPanel p = row(task("과제", d(18)).task(), d(18));
        assertEquals("<html>11/18 과제</html>", title(p));
        assertEquals("", clock(p).getText());
        assertEquals(new Dimension(26, 26), clock(p).getPreferredSize());
        assertEquals("시각 정하기", clock(p).getToolTipText());
        assertEquals("<html>12/1 과제</html>", title(row(task("과제", d(31)).task(), d(31))));
        Task t = task("과제", d(18)).task();
        assertEquals("<html>과제</html>", title(rows("2026-11-18T10:00", t).row(t, "", null, d(18))));
    }

    @Test
    void eWrow8RowHeightAndButtons() {
        JPanel p = row(task("과제", d(18)).task(), d(18));
        assertEquals(new Insets(3, 6, 3, 4), p.getInsets());
        assertEquals(56, p.getPreferredSize().height);
        assertEquals(new Dimension(Integer.MAX_VALUE, 56), p.getMaximumSize());
        assertEquals(new Rectangle(36, 3, 120, 24), p.getComponent(1).getBounds());
        JPanel east = (JPanel) p.getComponent(2);
        assertEquals(1, east.getComponentCount());
        assertEquals("30분", ((JLabel) east.getComponent(0)).getText());
        assertEquals(new Rectangle(112, 0, 78, 26), buttons(p).getBounds());
        Component[] b = buttons(p).getComponents();
        assertEquals(new Rectangle(0, 0, 26, 26), b[0].getBounds());
        assertEquals(new Rectangle(26, 0, 26, 26), b[1].getBounds());
        assertEquals(new Rectangle(52, 0, 26, 26), b[2].getBounds());
        assertEquals("수정", ((CircleButton) b[1]).getToolTipText());
        assertEquals("삭제", ((CircleButton) b[2]).getToolTipText());
        ((CircleButton) b[1]).doClick();
        ((CircleButton) b[2]).doClick();
        assertEquals(List.of("수정 과제", "삭제 과제"), heard);
        Task t = task("과제", d(18)).task();
        JPanel home = rows("2026-11-18T10:00", t).row(t, "", null, d(18));
        assertEquals(new Insets(5, 6, 6, 4), home.getInsets());
        assertEquals(44, home.getMaximumSize().height);
        assertEquals(4, ((JPanel) home.getComponent(2)).getComponentCount());
    }

    @Test
    void tEdit16ListPresses() {
        Task homework = task("과제", d(18)).task();
        Task laundry = task("빨래", d(18)).done(d(17)).task();
        TaskRows r = rows("2026-11-17T10:00", homework, laundry);
        JPanel p = row(r, homework, d(18));
        Input.mouse(p.getComponent(1), MouseEvent.MOUSE_CLICKED);
        Input.mouse(p, MouseEvent.MOUSE_CLICKED);
        assertEquals(List.of(), heard);
        Component title = p.getComponent(1);
        title.dispatchEvent(new MouseEvent(title, MouseEvent.MOUSE_CLICKED, 0, 0, 5, 5, 2, false, MouseEvent.BUTTON1));
        assertEquals(List.of("수정 과제"), heard);
        clock(p).doClick();
        assertEquals(List.of("수정 과제", "시각 과제 2026-11-18"), heard);
        TimePill done = clock(row(r, laundry, d(18)));
        assertFalse(done.isEnabled());
        done.doClick();
        assertEquals(2, heard.size());
    }

    @Test
    void tEdit22LastWeekRowOpensToday() {
        Task p = task("지원서", d(12)).minutes(60).period(d(12), d(20)).task();
        TimePill clock = clock(row(p, d(15)));
        assertTrue(clock.isEnabled());
        assertEquals("시각 정하기", clock.getToolTipText());
        clock.doClick();
        assertEquals(List.of("시각 지원서 2026-11-18"), heard);
    }

    @Test
    void tEdit42PastDueClock() {
        TimePill clock = clock(row(task("Q", d(10)).period(d(10), d(16)).task(), d(16)));
        assertFalse(clock.isEnabled());
        assertEquals("마감이 지난 일은 시각을 정할 수 없어요", clock.getToolTipText());
        clock.doClick();
        assertEquals(List.of(), heard);
        // 흐린 것을 건너뛰고 단추의 동작을 부르면 창을 연 날은 목록의 날 11-16이다. 창을 막는 일은 창을 여는 쪽(S5f)이다.
        for (ActionListener l : clock.getActionListeners()) {
            l.actionPerformed(new ActionEvent(clock, ActionEvent.ACTION_PERFORMED, ""));
        }
        assertEquals(List.of("시각 Q 2026-11-16"), heard);
    }

    @Test
    void eWrow9CheckDoesNotLinger() {
        Task t = task("과제", d(18)).task();
        TaskRows r = rows("2026-11-18T10:00", t);
        ((AbstractButton) ((JPanel) row(r, t, d(18)).getComponent(0)).getComponent(0)).doClick();
        assertTrue(t.isDone());
        assertFalse(r.settling());
    }

    @Test
    void eWrow2PastCopyIsDim() {
        Task copy = task("운동", d(16)).series("s", Repeat.DAILY).task();
        JPanel p = row(copy, d(16));
        assertEquals(Theme.INK_DIM.getAlpha(), darkest((JComponent) p.getComponent(1)));
        assertEquals(Theme.INK_DIM, ((JPanel) p.getComponent(2)).getComponent(0).getForeground());
        assertNull(marks(p));
        ((AbstractButton) ((JPanel) p.getComponent(0)).getComponent(0)).doClick();
        assertTrue(copy.isDone());
        JPanel today = row(task("운동", d(18)).series("s", Repeat.DAILY).task(), d(18));
        assertEquals(255, darkest((JComponent) today.getComponent(1)));
        assertEquals(Theme.INK_SUB, ((JPanel) today.getComponent(2)).getComponent(0).getForeground());
    }

    @Test
    void eWrow3PastDay() {
        assertEquals("<html>" + RED + "지난 날</font></html>", marks(row(task("과제", d(17)).task(), d(17))));
        assertNull(marks(row(task("빨래", d(17)).done(d(17)).task(), d(17))));
        assertNull(marks(row(task("과제", d(18)).task(), d(18))));
        JPanel q = row(task("Q", d(10)).period(d(10), d(16)).task(), d(16));
        assertEquals("<html>" + GRAY + "~11/16까지</font> " + RED + "지난 날</font></html>", marks(q));
        // 표시와 단추가 한 줄에 들지 않아 둘째 줄이 두 줄이고 그 줄만 높다(E-WROW-8).
        assertEquals(new Dimension(Integer.MAX_VALUE, 71), q.getMaximumSize());
        assertEquals(new Rectangle(112, 15, 78, 26), buttons(q).getBounds());
        Task p = task("지원서", d(12)).period(d(12), d(20)).task();
        assertEquals("<html>" + GRAY + "~11/20까지</font></html>", marks(row(p, d(15))));
    }

    @Test
    void eWrow4DueDayRow() {
        Task p = task("지원서", d(16)).period(d(16), d(22)).task();
        assertEquals("<html>" + RED + "마감</font></html>", marks(row(p, d(22))));
        assertEquals("<html>" + GRAY + "~11/22까지</font></html>", marks(row(p, d(18))));
        Task due = task("기획서", d(16)).period(d(16), d(18)).task();
        assertEquals("<html>" + RED + "오늘 마감</font></html>", marks(row(due, d(18))));
        JPanel home = rows("2026-11-18T10:00", p).row(p, "", null, d(22));
        assertEquals("<html>" + GRAY + "~11/22까지</font> " + RED + "마감</font></html>", ((JLabel) home.getComponent(3)).getText());
    }

    @Test
    void eWrow5LockedTip() {
        String why = "마감이 지난 일은 옮길 수 없어요";
        JPanel bare = row(task("Q", d(10)).period(d(10), d(16)).task(), d(16));
        assertEquals(why, bare.getToolTipText());
        assertEquals(why, ((JComponent) bare.getComponent(1)).getToolTipText());
        Task memo = task("Q", d(10)).period(d(10), d(16)).memo("자료\n찾기").task();
        assertEquals(TaskRows.lockedTip(memo), row(memo, d(16)).getToolTipText());
        Task open = task("지원서", d(16)).period(d(16), d(22)).memo("자료").task();
        assertEquals(TaskRows.memoTip(open), row(open, d(18)).getToolTipText());
        assertEquals(TaskRows.memoTip(memo), rows("2026-11-18T10:00", memo).row(memo, "", null, d(16)).getToolTipText());
    }
}
