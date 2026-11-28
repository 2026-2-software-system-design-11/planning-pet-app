package planningpet.schedule.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import org.junit.jupiter.api.Test;
import planningpet.app.model.AppData;
import planningpet.common.view.Snap;
import planningpet.common.view.Theme;
import planningpet.schedule.model.Category;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Task;

/** 홈 그날 목록 showPlan. 번호는 docs/porting/task-rows.md S3w 2절. */
class TaskPlanTest {
    private static LocalDate day(int d) {
        return LocalDate.of(2026, 11, d);
    }

    /** 할 일이나 고정 일정. due가 있으면 date부터 due까지의 기간 할 일이다. */
    private static Task task(long seq, Kind kind, String title, LocalDate date, LocalTime start, int minutes, LocalDate due,
            boolean done) {
        return new Task(title, seq, kind, title, date, start, minutes, "", false, Category.OTHER, null, null, due,
                due == null ? null : date, done, done ? date : null, 0, 0, null, null);
    }

    /** E-PLAN-3의 네 묶음: 11-18 마감 "기획서", 10:00 "보고서", 시각 없는 "토익 공부", 11-22 마감 "지원서". */
    private static Task[] mix() {
        return new Task[] {task(1, Kind.TASK, "기획서", day(12), null, 60, day(18), false),
            task(2, Kind.TASK, "보고서", day(18), LocalTime.of(10, 0), 60, null, false),
            task(3, Kind.TASK, "토익 공부", day(18), null, 30, null, false),
            task(4, Kind.TASK, "지원서", day(16), null, 60, day(22), false)};
    }

    /**
     * 시계 now의 앱 데이터에 tasks를 두고 새 목록에 list 날의 dayPlan을 넘겨, 앱처럼 Theme.clear 뒤 폭 480으로 배치한다. 체크를
     * 누르지 않아 보상 controller는 없다.
     */
    private static TaskRows plan(LocalDateTime now, LocalDate list, Task... tasks) {
        AppData data = new AppData(Clock.fixed(now.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault()),
                List.of(tasks), List.of());
        TaskRows rows = new TaskRows(data, null, t -> { }, t -> { }, (t, d, at) -> { });
        rows.showPlan(data.getBook().dayPlan(list));
        Theme.clear(rows);
        Snap.layOut(rows, 480, rows.getPreferredSize().height);
        return rows;
    }

    /** 자식마다 자리와 크기, 줄이면 제목과 알약 글, 라벨이면 [글]. */
    private static String kids(JPanel rows) {
        List<String> out = new ArrayList<>();
        for (Component c : rows.getComponents()) {
            String at = " " + c.getX() + "," + c.getY() + " " + c.getWidth() + "x" + c.getHeight();
            if (c instanceof JLabel l) {
                out.add("[" + l.getText() + "]" + at);
            } else {
                JPanel row = (JPanel) c;
                Task t = (Task) ((JComponent) ((JPanel) row.getComponent(0)).getComponent(0)).getClientProperty(Task.class);
                out.add(t.getTitle() + " " + ((TimePill) ((JPanel) row.getComponent(2)).getComponent(0)).getText() + at);
            }
        }
        return String.join(", ", out);
    }

    /** 앱의 할 일 카드처럼 가로 막대가 없고 테두리가 없는 스크롤 창에 넣는다. */
    private static JScrollPane scroll(TaskRows rows) {
        JScrollPane sp = new JScrollPane(rows, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(BorderFactory.createEmptyBorder());
        return sp;
    }

    /** 글을 비우고 줄의 제목 칸을 숨겨 그린 그림을 기준 그림 proto/plan-{name}.png와 견준다. */
    private static void looks(String name, TaskRows rows) throws IOException {
        Snap.blank(rows);
        for (Component c : rows.getComponents()) {
            if (c instanceof JPanel row) {
                row.getComponent(1).setVisible(false);
            }
        }
        BufferedImage img = new BufferedImage(rows.getWidth() + 4, rows.getHeight() + 4, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        rows.paint(g);
        g.dispose();
        Snap.assertLooksLikePrototype("plan-" + name, img);
    }

    @Test
    void ePlan1EmptyDay() {
        TaskRows rows = plan(day(18).atTime(10, 0), day(18));
        assertEquals(1, rows.getComponentCount());
        JLabel none = (JLabel) rows.getComponent(0);
        assertEquals("아직 일정이 없어요. 작은 계획을 추가해 보세요.", none.getText());
        assertEquals(Theme.font(13), none.getFont());
        assertEquals(Theme.INK_SUB, none.getForeground());
        assertEquals(new Insets(16, 6, 0, 0), none.getInsets());
        assertFalse(none.isOpaque());
        assertEquals(0f, none.getAlignmentX());
        assertEquals(new Dimension(239, 33), rows.getPreferredSize());
        assertTrue(rows.getLayout() instanceof BoxLayout);
        assertFalse(rows.isOpaque());
    }

    @Test
    void ePlan2DueAndTimed() {
        Task[] mix = mix();
        TaskRows rows = plan(day(18).atTime(10, 0), day(18), mix[0], mix[1]);
        assertEquals("기획서 시각 정하기 0,0 480x54, 보고서 10:00~11:00 0,54 480x39", kids(rows));
    }

    @Test
    void ePlan3FourGroups() throws IOException {
        TaskRows rows = plan(day(18).atTime(10, 0), day(18), mix());
        assertEquals("기획서 시각 정하기 0,0 480x54, 보고서 10:00~11:00 0,54 480x39, [시각 없는 할 일] 0,93 75x33, "
                + "토익 공부 시각 정하기 0,126 480x39, [기간 안에 할 일] 0,165 75x33, 지원서 시각 정하기 0,198 480x54", kids(rows));
        JLabel head = (JLabel) rows.getComponent(2);
        assertEquals(Theme.bold(12), head.getFont());
        assertEquals(Theme.INK_SUB, head.getForeground());
        assertEquals(new Insets(14, 6, 4, 0), head.getInsets());
        assertFalse(head.isOpaque());
        assertEquals(0f, head.getAlignmentX());
        assertEquals(new Dimension(303, 252), rows.getPreferredSize());
        looks("mix", rows);
    }

    @Test
    void ePlan4EmptyGroups() {
        Task[] mix = mix();
        assertEquals("보고서 10:00~11:00 0,0 480x39", kids(plan(day(18).atTime(10, 0), day(18), mix[1])));
        assertEquals("[기간 안에 할 일] 0,0 75x33, 지원서 시각 정하기 0,33 480x54", kids(plan(day(18).atTime(10, 0), day(20), mix)));
    }

    @Test
    void ePlan5OnlyDone() {
        TaskRows rows = plan(day(18).atTime(10, 0), day(18),
                task(1, Kind.TASK, "빨래", day(18), LocalTime.of(10, 0), 60, null, true),
                task(2, Kind.TASK, "청소", day(18), null, 30, null, true));
        assertEquals(0, rows.getComponentCount());
    }

    @Test
    void ePlan6ShowAgain() {
        TaskRows rows = plan(day(18).atTime(10, 0), day(18), mix());
        rows.showPlan(new AppData(Clock.systemDefaultZone()).getBook().dayPlan(day(18)));
        assertEquals(1, rows.getComponentCount());
        assertEquals("아직 일정이 없어요. 작은 계획을 추가해 보세요.", ((JLabel) rows.getComponent(0)).getText());
    }

    @Test
    void ePlan7Scroll() {
        TaskRows rows = plan(day(18).atTime(10, 0), day(18), mix());
        assertTrue(rows.getScrollableTracksViewportWidth());
        assertFalse(rows.getScrollableTracksViewportHeight());
        assertEquals(rows.getPreferredSize(), rows.getPreferredScrollableViewportSize());
        for (int o : new int[] {SwingConstants.VERTICAL, SwingConstants.HORIZONTAL}) {
            assertEquals(16, rows.getScrollableUnitIncrement(null, o, 1));
            assertEquals(64, rows.getScrollableBlockIncrement(null, o, -1));
        }
        JScrollPane sp = scroll(rows);
        // 높이 200은 목록 높이 252보다 작아 세로 막대(폭 17)가 보인다.
        for (int[] w : new int[][] {{300, 283}, {480, 463}, {700, 683}}) {
            Snap.layOut(sp, w[0], 200);
            assertEquals(new Dimension(w[1], 252), rows.getSize());
        }
    }

    @Test
    void tList1TimeThenSeq() {
        TaskRows rows = plan(day(16).atTime(8, 0), day(16),
                task(3, Kind.TASK, "A", day(16), LocalTime.of(15, 0), 30, null, false),
                task(1, Kind.TASK, "B", day(16), null, 30, null, false),
                task(4, Kind.TASK, "C", day(16), LocalTime.of(9, 0), 30, null, false),
                task(2, Kind.TASK, "D", day(16), LocalTime.of(15, 0), 30, null, false),
                task(6, Kind.FIXED, "E", day(16), LocalTime.of(10, 30), 30, null, false),
                task(5, Kind.TASK, "F", day(16), null, 30, null, true));
        // 끝낸 F는 "끝낸 일" 묶음(#480) 전에는 빠진다(docs/porting/task-rows.md S3w).
        assertEquals("C 09:00~09:30 0,0 480x39, E 10:30~11:00 0,39 480x39, D 15:00~15:30 0,78 480x39, A 15:00~15:30 0,117 480x39, "
                + "[시각 없는 할 일] 0,156 75x33, B 시각 정하기 0,189 480x39", kids(rows));
    }

    @Test
    void tList9TitlesInFirstWindow() {
        Task due = task(1, Kind.TASK, "졸업 작품 기획서", day(12), null, 60, day(19), false);
        Task missed = new Task("E사 지원서", 2, Kind.TASK, "E사 지원서", day(18), LocalTime.of(9, 0), 120, "", false,
                Category.OTHER, null, null, day(22), day(16), false, null, 0, 0, null, null);
        TaskRows rows = plan(day(19).atTime(9, 0), day(19), due, missed);
        // 프로토타입 메인 창 1000x680의 할 일 카드 스크롤 창 크기다(docs/porting/task-rows.md S3w). 창 전체는 MainScreen이 본다.
        Snap.layOut(scroll(rows), 383, 392);
        assertEquals("졸업 작품 기획서 시각 정하기 0,0 383x54, [기간 안에 할 일] 0,54 75x33, E사 지원서 시각 정하기 0,87 383x54", kids(rows));
        String[][] want = {{"졸업 작품 기획서", "<font color='#8A96A0'>~11/19까지</font> <font color='#C0392B'>오늘 마감</font>"},
            {"E사 지원서", "<font color='#8A96A0'>~11/22까지</font> <font color='#8A96A0'>18일에 하려던 일</font>"}};
        for (int i = 0; i < 2; i++) {
            JPanel row = (JPanel) rows.getComponent(i * 2);
            assertEquals(want[i][0], ((TaskRows.TitleLabel) row.getComponent(1)).shown());
            JLabel marks = (JLabel) row.getComponent(3);
            assertEquals("<html>" + want[i][1] + "</html>", marks.getText());
            assertEquals(new Rectangle(6, 33, 373, 15), marks.getBounds());
        }
    }
}
