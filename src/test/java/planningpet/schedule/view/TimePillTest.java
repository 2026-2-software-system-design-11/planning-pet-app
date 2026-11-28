package planningpet.schedule.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Cursor;
import org.junit.jupiter.api.Test;
import planningpet.common.view.Theme;

/** 목록 줄의 시각 알약. 번호는 docs/porting/task-rows.md S3s 2절. */
class TimePillTest {
    private static final String T = "10:00~11:00";
    private static final String WHY = "지난 날에는 시각을 정할 수 없어요";

    private static TimePill pill(String time) {
        return new TimePill(time, false, false, null);
    }

    @Test
    void eTpill1Time() {
        TimePill p = pill(T);
        assertEquals(T, p.getText());
        assertTrue(p.isEnabled());
        assertEquals("눌러서 시각을 바꿔요", p.getToolTipText());
        assertEquals(Cursor.HAND_CURSOR, p.getCursor().getType());
        assertEquals(T, p.getAccessibleContext().getAccessibleName());
    }

    @Test
    void eTpill2Empty() {
        TimePill p = pill("");
        assertEquals("시각 정하기", p.getText());
    }

    @Test
    void eTpill3Done() {
        for (String time : new String[] {T, ""}) {
            TimePill p = new TimePill(time, true, true, null);
            assertFalse(p.isEnabled());
            assertNull(p.getToolTipText());
            assertEquals(Cursor.DEFAULT_CURSOR, p.getCursor().getType());
        }
    }

    @Test
    void eTpill4Locked() {
        for (String time : new String[] {"", T}) {
            TimePill p = new TimePill(time, false, true, WHY);
            assertFalse(p.isEnabled());
            assertEquals(WHY, p.getToolTipText());
        }
    }

    @Test
    void eTpill10Properties() {
        TimePill p = pill(T);
        assertFalse(p.isOpaque());
        assertFalse(p.isContentAreaFilled());
        assertFalse(p.isBorderPainted());
        assertFalse(p.isFocusPainted());
        assertTrue(p.isRolloverEnabled());
        assertEquals(Theme.bold(12), p.getFont());
        assertNull(p.getIcon());
        int[] fired = {0};
        p.addActionListener(e -> fired[0]++);
        p.doClick(0);
        TimePill locked = new TimePill("", false, true, WHY);
        locked.addActionListener(e -> fired[0] += 10);
        locked.doClick(0);
        assertEquals(1, fired[0]);
    }
}
