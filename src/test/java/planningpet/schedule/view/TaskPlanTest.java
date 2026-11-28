package planningpet.schedule.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Dimension;
import java.awt.Insets;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import org.junit.jupiter.api.Test;
import planningpet.app.model.AppData;
import planningpet.common.view.Snap;
import planningpet.common.view.Theme;
import planningpet.schedule.model.Task;

/** 홈 그날 목록 showPlan. 번호는 docs/porting/task-rows.md S3w 2절. */
class TaskPlanTest {
    private static LocalDate day(int d) {
        return LocalDate.of(2026, 11, d);
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
}
