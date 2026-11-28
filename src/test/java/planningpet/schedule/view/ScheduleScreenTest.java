package planningpet.schedule.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static planningpet.schedule.model.Tasks.d;
import static planningpet.schedule.model.Tasks.task;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
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
import planningpet.common.view.Theme;
import planningpet.reward.controller.RewardController;
import planningpet.schedule.model.Task;

/** 일정관리 화면. 번호는 docs/porting/schedule.md S5c 2절, S5c2 2절, S5d 2절. 따로 적지 않으면 지금은 11-18(수) 10:00이다. */
class ScheduleScreenTest {
    private AppData data;
    private ScheduleScreen screen;
    private AppController app;
    private RewardController reward;
    /** 함수 필드가 받은 것. */
    private final List<String> heard = new ArrayList<>();
    /** 화면 높이. */
    private int height = 600;

    /** now의 앱 데이터에 tasks를 둔 화면을 1000 x height로 배치하고 데이터 변경에 refresh를 건다. */
    private ScheduleScreen open(String now, Task... tasks) {
        data = Apps.data(now, tasks);
        app = new AppController(data, new MemoryStore(), e -> { });
        reward = new RewardController(data, app);
        screen = new ScheduleScreen(data, app, reward, Apps.clock(now));
        screen.onEdit = t -> heard.add("수정 " + t.getTitle());
        screen.onDelete = t -> heard.add("삭제 " + t.getTitle());
        screen.onTime = (t, day, at) -> heard.add("시각 " + t.getTitle() + " " + day);
        screen.openAdd = day -> heard.add("추가 " + day);
        screen.openAutoPlace = start -> heard.add("자동 배치 " + start);
        data.onChange(screen::refresh);
        Snap.layOut(screen, 1000, height);
        screen.refresh();
        Snap.layOut(screen, 1000, height);
        return screen;
    }

    private ScheduleScreen open(Task... tasks) {
        return open("2026-11-18T10:00", tasks);
    }

    private static List<String> texts(JPanel p) {
        return Arrays.stream(p.getComponents()).map(c -> c instanceof AbstractButton b ? b.getText() : ((JLabel) c).getText()).toList();
    }

    /** 요일 머리 글. */
    private List<String> header() {
        return Arrays.stream(screen.header.getComponents()).map(c -> ((JLabel) c).getText()).toList();
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
    void eSch3Header() {
        open();
        assertEquals(List.of("월 16", "화 17", "수 18", "목 19", "금 20", "토 21", "일 22"), header());
        assertEquals(new Insets(0, 52, 0, 0), screen.header.getInsets());
        List<Boolean> opaque = Arrays.stream(screen.header.getComponents()).map(Component::isOpaque).toList();
        assertEquals(List.of(false, false, true, false, false, false, false), opaque);
        assertEquals(Theme.DAY_SELECTED, screen.header.getComponent(2).getBackground());
        data.setWeekStart(DayOfWeek.SUNDAY);
        screen.refresh();
        assertEquals(List.of("일 15", "월 16", "화 17", "수 18", "목 19", "금 20", "토 21"), header());
        screen.next.doClick();
        assertEquals(List.of(), Arrays.stream(screen.header.getComponents()).filter(Component::isOpaque).toList());
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

    @Test
    void eSch5Today() {
        open();
        assertEquals(List.of(false, "이번 주를 보고 있어요"), List.of(screen.today.isEnabled(), screen.today.getToolTipText()));
        screen.prev.doClick();
        assertTrue(screen.today.isEnabled());
        assertNull(screen.today.getToolTipText());
        screen.today.doClick();
        assertEquals(List.of(d(16), false), List.of(screen.weekStart(), screen.today.isEnabled()));
    }

    @Test
    void eSch6WeekStartChange() {
        open().next.doClick();
        data.setWeekStart(DayOfWeek.SUNDAY);
        screen.refresh();
        assertEquals(d(22), screen.weekStart());
        data.setWeekStart(DayOfWeek.MONDAY);
        screen.refresh();
        assertEquals(d(16), screen.weekStart());
    }

    @Test
    void eSch7List() {
        open(task("과제", d(18)).task(), task("빨래", d(24)).task(), task("수업", d(18)).at("09:00").task());
        assertEquals(1, screen.untimed.getComponentCount());
        screen.next.doClick();
        assertEquals(1, screen.untimed.getComponentCount());
        screen.next.doClick();
        assertEquals("이번 주에 시각 없는 할 일이 없어요.", ((JLabel) screen.untimed.getComponent(0)).getText());
    }

    /** 목록 줄 둘째 줄의 단추(시계, 연필, 휴지통)와 "자동 배치"를 누르고 시간표 블록을 두 번 누른다. */
    @Test
    void eSch8Hooks() {
        open(task("과제", d(18)).task(), task("수업", d(18)).at("09:00").task());
        JPanel line = (JPanel) ((JComponent) screen.untimed.getComponent(0)).getComponent(3);
        JPanel buttons = (JPanel) line.getComponent(line.getComponentCount() - 1);
        for (Component b : buttons.getComponents()) {
            ((AbstractButton) b).doClick();
        }
        screen.autoPlace.doClick();
        Snap.paint(screen.table, 752, screen.table.getPreferredSize().height);
        screen.table.dispatchEvent(new MouseEvent(screen.table, MouseEvent.MOUSE_CLICKED, 0, 0, 300, 410, 2, false, MouseEvent.BUTTON1));
        assertEquals(List.of("시각 과제 2026-11-18", "수정 과제", "삭제 과제", "자동 배치 2026-11-16", "수정 수업"), heard);
    }

    /** 06 14번: 일정관리 "일정 추가"가 넘기는 날. */
    @Test
    void tDef2to4AddDay() {
        open().add.doClick();
        screen.next.doClick();
        screen.add.doClick();
        screen.move(LocalDate.of(2026, 11, 9));
        screen.add.doClick();
        assertEquals(List.of("추가 2026-11-18", "추가 2026-11-23", "추가 2026-11-09"), heard);
    }

    @Test
    void tDef15SundayAddDay() {
        open();
        data.setWeekStart(DayOfWeek.SUNDAY);
        screen.next.doClick();
        screen.add.doClick();
        assertEquals(List.of(d(22), List.of("추가 2026-11-22")), List.of(screen.weekStart(), heard));
    }

    @Test
    void tAuto8Button() {
        assertFalse(open(task("빨래", d(16)).done(d(16)).task()).autoPlace.isEnabled());
        assertTrue(open(task("과제", d(19)).task()).autoPlace.isEnabled());
    }

    /** 06 19번: 열 때 맨 위는 19:10, 다음 주는 08:00, 데이터가 바뀌어도 그대로, 숨었다 다시 보이면 내린다. */
    @Test
    void tTt8FirstTop() {
        height = 300;
        open("2026-11-18T20:10", task("과제", d(23)).at("09:00").task());
        assertEquals(1150, screen.topMinute());
        screen.next.doClick();
        assertEquals(480, screen.topMinute());
        screen.scrollTo(600);
        data.changed();
        assertEquals(600, screen.topMinute());
        boolean[] on = {false};
        screen.shown = () -> on[0];
        screen.refresh();
        on[0] = true;
        screen.refresh();
        assertEquals(480, screen.topMinute());
    }

    @Test
    void eSch9ScrollEnds() {
        open();
        screen.scrollTo(-30);
        assertEquals(0, screen.topMinute());
        int view = screen.scroll.getViewport().getExtentSize().height;
        screen.scrollTo(5000);
        assertEquals(Math.round((1232 - view) * 60.0 / 44), screen.topMinute());
        screen.scrollTo(61);
        assertEquals(List.of(44, 60), List.of(screen.scroll.getViewport().getViewPosition().y, screen.topMinute()));
    }

    /** 말풍선 글: 폭 150의 줄바꿈 칸에 꺾쇠를 바꾼 글. */
    private static String said(String line) {
        return "<html><div style='width:150px'>" + line.replace("<", "&lt;").replace(">", "&gt;") + "</div></html>";
    }

    private static String hint(int n) {
        return said(n > 0 ? "이번 주 남은 날의 시각 없는 할 일이 " + n + "개 있어. 자동 배치를 눌러 볼래?" : "지금 자동 배치할 할 일은 없어.");
    }

    /** 06 16번 SCHEDULE_HINT의 대상 수 3과 0을 오른쪽 카드 아래 말풍선으로 본다(pet-talk.md E-TALK-31의 화면 몫). 주를 옮겨도 힌트다. */
    @Test
    void eTalk31HintCounts() {
        assertEquals(hint(3), open(task("가", d(18)).task(), task("나", d(19)).task(), task("다", d(20)).task()).bubble.getText());
        screen.next.doClick();
        assertEquals(hint(0), screen.bubble.getText());
        assertEquals(hint(0), open().bubble.getText());
        Card side = (Card) ((BorderLayout) screen.getLayout()).getLayoutComponent(BorderLayout.EAST);
        JPanel corner = (JPanel) ((BorderLayout) side.getLayout()).getLayoutComponent(BorderLayout.SOUTH);
        assertEquals(screen.bubble, ((BorderLayout) corner.getLayout()).getLayoutComponent(BorderLayout.NORTH));
    }

    /** 보이는 중에 새 말이 생기면 그 말, 새 말 없이 바뀌면 힌트다. 다시 쓴 말은 새 말이 아니다(D-39, D-106). */
    @Test
    void eSch10NewLineWhileShown() {
        Task homework = task("과제", d(18)).task();
        open(homework, task("빨래", d(19)).task());
        assertEquals(hint(2), screen.bubble.getText());
        reward.complete(homework);
        assertFalse(app.getBubble().getLine().isBlank());
        assertEquals(said(app.getBubble().getLine()), screen.bubble.getText());
        app.getBubble().retell("오늘 할 일 1개 남았어.");
        data.changed();
        assertEquals(hint(1), screen.bubble.getText());
    }

    /** 숨은 동안은 글을 바꾸지 않고, 다시 보이거나 주를 옮기면 새 말이 있어도 힌트다. */
    @Test
    void eSch11HiddenAndReopen() {
        Task homework = task("과제", d(18)).task();
        open(homework);
        boolean[] on = {false};
        screen.shown = () -> on[0];
        reward.complete(homework);
        assertEquals(hint(1), screen.bubble.getText());
        on[0] = true;
        screen.refresh();
        assertEquals(hint(0), screen.bubble.getText());
        app.say("새 말", null);
        assertEquals(said("새 말"), screen.bubble.getText());
        app.say("또 새 말", null);
        screen.next.doClick();
        assertEquals(hint(0), screen.bubble.getText());
    }

    /** 말풍선 글의 꺾쇠와 &를 글자로 보인다. */
    @Test
    void eSch12BubbleText() {
        open();
        app.say("<b>안녕</b> & 잘 가", null);
        assertEquals("<html><div style='width:150px'>&lt;b&gt;안녕&lt;/b&gt; &amp; 잘 가</div></html>", screen.bubble.getText());
    }
}
