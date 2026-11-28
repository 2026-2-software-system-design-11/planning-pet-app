package planningpet.schedule.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Cursor;
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
import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import planningpet.app.controller.AppController;
import planningpet.app.model.AppData;
import planningpet.app.model.DataStore;
import planningpet.app.model.LoadResult;
import planningpet.common.view.RoundButtonUI;
import planningpet.common.view.Snap;
import planningpet.common.view.Theme;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.PetState;
import planningpet.reward.controller.RewardController;
import planningpet.schedule.model.Category;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Task;

/** 홈 그날 목록 showPlan. 번호는 docs/porting/task-rows.md S3w 2절. */
class TaskPlanTest {
    /** "끝낸 일" 단추는 앱처럼 RoundButtonUI로 그린다. 줄과 묶음 제목은 install 앞과 뒤가 같다(task-rows.md S3w, S3x). */
    @BeforeAll
    static void theme() {
        Theme.install();
    }

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
     * 시계 now의 앱 데이터에 tasks를 두고 새 목록에 list 날의 dayPlan을 넘겨, 앱처럼 Theme.clear 뒤 폭 480으로 배치한다. 홈 화면
     * 대신 데이터 변경 알림과 resort가 같은 날을 다시 그린다(docs/porting/task-rows.md S3x). 체크는 보상 controller를 부른다.
     */
    private static TaskRows plan(LocalDateTime now, LocalDate list, Task... tasks) {
        AppData data = new AppData(Clock.fixed(now.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault()),
                List.of(tasks), List.of());
        data.addPet(new PetState(PetKind.CAT, "나비"));
        data.setActiveKind(PetKind.CAT);
        AppController app = new AppController(data, new DataStore() {
            public LoadResult load() { return LoadResult.firstRun(); }
            public void save(AppData d) { }
        }, e -> { });
        TaskRows rows = new TaskRows(data, new RewardController(data, app), t -> { }, t -> { }, (t, d, at) -> { });
        data.onChange(() -> rows.resort.run());
        rows.putClientProperty(AppData.class, data);
        pick(rows, list);
        return rows;
    }

    /** 홈 달력에서 그날을 고른 것처럼 그날 목록을 그리고, 그 뒤 resort도 그날을 다시 그린다. */
    private static void pick(TaskRows rows, LocalDate day) {
        AppData data = (AppData) rows.getClientProperty(AppData.class);
        rows.resort = () -> {
            rows.showPlan(data.getBook().dayPlan(day));
            Theme.clear(rows);
            Snap.layOut(rows, 480, rows.getPreferredSize().height);
        };
        rows.resort.run();
    }

    /** 자식마다 자리와 크기, 줄이면 제목과 알약 글, 라벨이면 [글], 단추면 <글>과 자리. */
    private static String kids(JPanel rows) {
        List<String> out = new ArrayList<>();
        for (Component c : rows.getComponents()) {
            String at = " " + c.getX() + "," + c.getY() + " " + c.getWidth() + "x" + c.getHeight();
            if (c instanceof JLabel l) {
                out.add("[" + l.getText() + "]" + at);
            } else if (c instanceof AbstractButton b) {
                out.add("<" + b.getText() + "> " + c.getX() + "," + c.getY());
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

    /** 묶음 단추를 글을 비워 그린 그림을 기준 그림 proto/plan-{name}.png와 견준다. */
    private static void headLooks(String name, JButton head) throws IOException {
        head.setText("");
        BufferedImage img = new BufferedImage(head.getWidth() + 4, head.getHeight() + 4, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        head.paint(g);
        g.dispose();
        Snap.assertLooksLikePrototype("plan-" + name, img);
    }

    /** 목록에서 제목이 title인 줄의 둥근 체크. 목록은 체크마다 다시 만들어지므로 그때마다 찾는다. */
    private static JCheckBox box(TaskRows rows, String title) {
        for (Component c : rows.getComponents()) {
            if (c instanceof JPanel row && row.getComponent(0) instanceof JPanel left && left.getComponent(0) instanceof JCheckBox b
                    && ((Task) b.getClientProperty(Task.class)).getTitle().equals(title)) {
                return b;
            }
        }
        throw new AssertionError(title);
    }

    /** E-DONE-3과 T-LIST-8의 11-16: 09:00 A, 시각 없는 B, C(넣은 순). */
    private static TaskRows abc(boolean done) {
        return plan(day(16).atTime(8, 0), day(16), task(1, Kind.TASK, "A", day(16), LocalTime.of(9, 0), 30, null, false),
                task(2, Kind.TASK, "B", day(16), null, 30, null, done), task(3, Kind.TASK, "C", day(16), null, 30, null, done));
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
        assertEquals("<끝낸 일 2개> 0,0, 빨래 10:00~11:00 0,33 480x39, 청소 시각 정하기 0,72 480x39", kids(rows));
    }

    @Test
    void eDone1Group() {
        Task job = new Task("지원서", 2, Kind.TASK, "지원서", day(16), null, 60, "", false, Category.OTHER, null, null, day(22),
                day(16), true, day(18), 0, 0, null, null);
        TaskRows rows = plan(day(18).atTime(10, 0), day(18),
                task(1, Kind.TASK, "빨래", day(18), LocalTime.of(10, 0), 60, null, true), job);
        assertEquals("<끝낸 일 2개> 0,0, 빨래 10:00~11:00 0,33 480x39, 지원서 시각 정하기 0,72 480x54", kids(rows));
        JButton head = rows.doneHead;
        assertEquals(Theme.bold(12), head.getFont());
        assertEquals(Theme.INK_SUB, head.getForeground());
        assertEquals(SwingConstants.LEFT, head.getHorizontalAlignment());
        assertEquals(new Insets(14, 6, 4, 0), head.getInsets());
        assertEquals(0f, head.getAlignmentX());
        assertEquals(Cursor.HAND_CURSOR, head.getCursor().getType());
        assertEquals(Boolean.TRUE, head.getClientProperty(RoundButtonUI.NAV));
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
        assertEquals("C 09:00~09:30 0,0 480x39, E 10:30~11:00 0,39 480x39, D 15:00~15:30 0,78 480x39, A 15:00~15:30 0,117 480x39, "
                + "[시각 없는 할 일] 0,156 75x33, B 시각 정하기 0,189 480x39, <끝낸 일 1개> 0,228, F 시각 정하기 0,261 480x39", kids(rows));
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

    /** E-DONE-2 접기와 E-DONE-3 묶음 그림(그림이 글을 비우므로 먼저 크기를 본다). */
    @Test
    void eDone2FoldAndPicture() throws IOException {
        TaskRows rows = abc(true);
        JButton head = rows.doneHead;
        assertEquals(new Dimension(73, 33), head.getPreferredSize());
        looks("done", rows);
        assertEquals("false RoundButtonUI", head.isOpaque() + " " + head.getUI().getClass().getSimpleName());
        assertEquals("접기 끝낸 일 2개 접기", head.getToolTipText() + " " + head.getAccessibleContext().getAccessibleName());
        headLooks("head-open", head);
        head.doClick();
        assertEquals("A 09:00~09:30 0,0 480x39, <끝낸 일 2개> 0,39", kids(rows));
        assertEquals("펼치기 끝낸 일 2개 펼치기", head.getToolTipText() + " " + head.getAccessibleContext().getAccessibleName());
        headLooks("head-folded", head);
        rows.resort.run();
        assertEquals(2, rows.getComponentCount());
        head.doClick();
        assertEquals("A 09:00~09:30 0,0 480x39, <끝낸 일 2개> 0,39, B 시각 정하기 0,72 480x39, C 시각 정하기 0,111 480x39", kids(rows));
    }

    private static final String ABC = "A 09:00~09:30 0,0 480x39, [시각 없는 할 일] 0,39 75x33, B 시각 정하기 0,72 480x39, "
            + "C 시각 정하기 0,111 480x39";

    @Test
    void eDone4Timer() {
        TaskRows rows = abc(false);
        Timer timer = rows.settleTimer;
        assertEquals("1500 1500 false 1", timer.getDelay() + " " + timer.getInitialDelay() + " " + timer.isRepeats() + " "
                + timer.getActionListeners().length);
        box(rows, "B").doClick();
        // 창 없는 목록은 보이지 않아 타이머가 돌지 않는다. 화면에서 1.5초 뒤 부르는 리스너를 부른다.
        assertEquals("false true", timer.isRunning() + " " + rows.settling());
        timer.getActionListeners()[0].actionPerformed(null);
        assertFalse(rows.settling());
        assertEquals("<끝낸 일 1개> 0,111, B 시각 정하기 0,144 480x39", kids(rows).substring(kids(rows).indexOf("<")));
        timer.start();
        rows.settle();
        assertFalse(timer.isRunning());
    }

    @Test
    void eDone5CheckAgain() {
        TaskRows rows = abc(false);
        box(rows, "B").doClick();
        box(rows, "C").doClick();
        assertEquals(ABC + " true true true", kids(rows) + " " + box(rows, "B").isSelected() + " " + box(rows, "C").isSelected()
                + " " + rows.settling());
        rows.settle();
        assertEquals("A 09:00~09:30 0,0 480x39, <끝낸 일 2개> 0,39, B 시각 정하기 0,72 480x39, C 시각 정하기 0,111 480x39", kids(rows));
    }

    @Test
    void eDone6UncheckWhileStaying() {
        TaskRows rows = abc(false);
        box(rows, "B").doClick();
        box(rows, "B").doClick();
        assertEquals(ABC + " false false", kids(rows) + " " + box(rows, "B").isSelected() + " " + rows.settling());
    }

    @Test
    void tList8StayThenGroup() {
        TaskRows rows = abc(false);
        box(rows, "B").doClick();
        assertEquals(ABC + " true true", kids(rows) + " " + box(rows, "B").isSelected() + " " + rows.settling());
        rows.settle();
        String settled = "A 09:00~09:30 0,0 480x39, [시각 없는 할 일] 0,39 75x33, C 시각 정하기 0,72 480x39, <끝낸 일 1개> 0,111";
        assertEquals(settled + ", B 시각 정하기 0,144 480x39", kids(rows));
        rows.doneHead.doClick();
        assertEquals(settled, kids(rows));
        rows.doneHead.doClick();
        box(rows, "B").doClick();
        assertEquals(ABC + " false false", kids(rows) + " " + box(rows, "B").isSelected() + " " + rows.settling());
    }

    @Test
    void eDone7DoneOnOtherDay() {
        TaskRows rows = plan(day(18).atTime(10, 0), day(20), mix()[3]);
        box(rows, "지원서").doClick();
        assertEquals("[아직 일정이 없어요. 작은 계획을 추가해 보세요.] 0,0 239x33 true", kids(rows) + " " + rows.settling());
        pick(rows, day(18));
        assertEquals("[시각 없는 할 일] 0,0 75x33, 지원서 시각 정하기 0,33 480x54 true", kids(rows) + " " + box(rows, "지원서").isSelected());
        rows.settle();
        assertEquals("<끝낸 일 1개> 0,0, 지원서 시각 정하기 0,33 480x54", kids(rows));
    }

    @Test
    void eDone8OtherDayAndBack() {
        TaskRows rows = plan(day(16).atTime(8, 0), day(16), task(1, Kind.TASK, "B", day(16), null, 30, null, false),
                task(2, Kind.TASK, "C", day(16), null, 30, null, false));
        box(rows, "B").doClick();
        pick(rows, day(17));
        assertEquals("[아직 일정이 없어요. 작은 계획을 추가해 보세요.] 0,0 239x33 true", kids(rows) + " " + rows.settling());
        pick(rows, day(16));
        assertEquals("[시각 없는 할 일] 0,0 75x33, B 시각 정하기 0,33 480x39, C 시각 정하기 0,72 480x39 true true",
                kids(rows) + " " + box(rows, "B").isSelected() + " " + rows.settling());
    }

    @Test
    void tList10PeriodStays() {
        Task job = new Task("지원서", 1, Kind.TASK, "지원서", day(18), LocalTime.of(9, 0), 60, "", false, Category.OTHER, null, null,
                day(22), day(16), false, null, 0, 0, null, null);
        TaskRows rows = plan(day(18).atTime(8, 0), day(18), job, task(2, Kind.TASK, "보고서", day(16), null, 30, day(25), false),
                task(3, Kind.TASK, "토익", day(18), null, 30, null, false));
        box(rows, "지원서").doClick();
        assertEquals("지원서 09:00~10:00 0,0 480x54, [시각 없는 할 일] 0,54 75x33, 토익 시각 정하기 0,87 480x39, [기간 안에 할 일] 0,126 75x33, "
                + "보고서 시각 정하기 0,159 480x54 true", kids(rows) + " " + box(rows, "지원서").isSelected());
        rows.settle();
        box(rows, "보고서").doClick();
        assertEquals("[시각 없는 할 일] 0,0 75x33, 토익 시각 정하기 0,33 480x39, [기간 안에 할 일] 0,72 75x33, 보고서 시각 정하기 0,105 480x54, "
                + "<끝낸 일 1개> 0,159, 지원서 시각 정하기 0,192 480x54", kids(rows));
        rows.settle();
        assertEquals("[시각 없는 할 일] 0,0 75x33, 토익 시각 정하기 0,33 480x39, <끝낸 일 2개> 0,72, 지원서 시각 정하기 0,105 480x54, "
                + "보고서 시각 정하기 0,159 480x54", kids(rows));
    }
}
