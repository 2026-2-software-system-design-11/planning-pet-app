package planningpet.schedule.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static planningpet.schedule.model.Tasks.d;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.junit.jupiter.api.Test;
import planningpet.app.controller.AppController;
import planningpet.app.model.AppData;
import planningpet.app.model.Apps;
import planningpet.app.model.MemoryStore;
import planningpet.common.view.Card;
import planningpet.common.view.Snap;
import planningpet.reward.controller.RewardController;
import planningpet.schedule.model.Task;

/** 일정관리 화면. 번호는 docs/porting/schedule.md S5c 2절. 따로 적지 않으면 지금은 11-18(수) 10:00이다. */
class ScheduleScreenTest {
    private AppData data;
    private ScheduleScreen screen;

    /** now의 앱 데이터에 tasks를 둔 화면을 1000 x 600으로 배치하고 데이터 변경에 refresh를 건다. */
    private ScheduleScreen open(String now, Task... tasks) {
        data = Apps.data(now, tasks);
        RewardController reward = new RewardController(data, new AppController(data, new MemoryStore(), e -> { }));
        screen = new ScheduleScreen(data, reward, Apps.clock(now));
        data.onChange(screen::refresh);
        Snap.layOut(screen, 1000, 600);
        screen.refresh();
        Snap.layOut(screen, 1000, 600);
        return screen;
    }

    private ScheduleScreen open(Task... tasks) {
        return open("2026-11-18T10:00", tasks);
    }

    private static List<String> texts(JPanel p) {
        return Arrays.stream(p.getComponents()).map(c -> c instanceof AbstractButton b ? b.getText() : ((JLabel) c).getText()).toList();
    }

    @Test
    void eSch1Layout() {
        open("2026-11-18T10:00");
        assertEquals(new Insets(8, 8, 8, 12), screen.getInsets());
        BorderLayout layout = (BorderLayout) screen.getLayout();
        JPanel top = (JPanel) layout.getLayoutComponent(BorderLayout.NORTH);
        JPanel bar = (JPanel) top.getComponent(0);
        JPanel bars = (JPanel) ((BorderLayout) bar.getLayout()).getLayoutComponent(BorderLayout.WEST);
        assertEquals(List.of("<", screen.range.getText(), ">", "오늘"), texts(bars));
        assertEquals(List.of("자동 배치", "일정 추가"), texts((JPanel) ((BorderLayout) bar.getLayout()).getLayoutComponent(BorderLayout.EAST)));
        JPanel swatches = (JPanel) ((BorderLayout) top.getLayout()).getLayoutComponent(BorderLayout.SOUTH);
        assertEquals(List.of("고정 일정", "할 일", "끝냄"), texts(swatches));
        JLabel fixed = (JLabel) swatches.getComponent(0);
        assertEquals(List.of(12, 12, 12), List.of(fixed.getIcon().getIconWidth(), fixed.getIcon().getIconHeight(), fixed.getFont().getSize()));
        Card center = (Card) layout.getLayoutComponent(BorderLayout.CENTER);
        assertEquals(List.of(screen.scroll), Arrays.asList(center.getComponents()));
        assertEquals(List.of(screen.table, screen.header), List.of(screen.scroll.getViewport().getView(), screen.scroll.getColumnHeader().getView()));
        Card side = (Card) layout.getLayoutComponent(BorderLayout.EAST);
        assertEquals(240, side.getWidth());
        BorderLayout sides = (BorderLayout) side.getLayout();
        assertEquals("<html><b>시각 없는 할 일</b></html>", ((JLabel) sides.getLayoutComponent(BorderLayout.NORTH)).getText());
        assertEquals(screen.untimed, ((JScrollPane) sides.getLayoutComponent(BorderLayout.CENTER)).getViewport().getView());
    }

    @Test
    void eSch2Range() {
        assertEquals("<html><b>11월 16일 ~ 11월 22일</b></html>", open().range.getText());
        assertEquals("<html><b>11월 30일 ~ 12월 6일</b></html>", open("2026-12-02T10:00").range.getText());
        assertEquals("<html><b>12월 28일 ~ 1월 3일</b></html>", open("2026-12-31T10:00").range.getText());
    }

    @Test
    void eSch4Weeks() {
        assertEquals(d(16), open().weekStart());
        screen.prev.doClick();
        assertEquals(LocalDate.of(2026, 11, 9), screen.weekStart());
        screen.next.doClick();
        screen.next.doClick();
        assertEquals(d(23), screen.weekStart());
        assertEquals(d(16), open("2026-11-23T03:59").weekStart());
        assertEquals(d(23), open("2026-11-23T04:00").weekStart());
    }
}
