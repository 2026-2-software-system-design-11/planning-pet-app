package planningpet.schedule.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
}
