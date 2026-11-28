package planningpet.schedule.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.io.IOException;
import javax.swing.JCheckBox;
import org.junit.jupiter.api.Test;
import planningpet.common.view.Snap;

/** 목록 줄의 둥근 체크. 번호는 docs/porting/task-rows.md S3r 2절. 기준 그림은 proto/rchk-*.png이고 글이 없다. */
class RoundCheckTest {
    private static JCheckBox box(boolean on) {
        JCheckBox b = RoundCheck.style(new JCheckBox());
        b.setSelected(on);
        return b;
    }

    private static void looks(String name, JCheckBox b, int w, int h) throws IOException {
        Snap.assertLaidOutLooksLikePrototype("rchk-" + name, b, w, h);
    }

    @Test
    void eRchk1OffAndOn() throws IOException {
        looks("off", box(false), 24, 24);
        looks("on", box(true), 24, 24);
    }

    @Test
    void eRchk5OtherSizes() throws IOException {
        looks("40x30", box(true), 40, 30);
        looks("10x10", box(true), 10, 10);
        looks("0x0", box(true), 0, 0);
    }

    @Test
    void eRchk6Properties() {
        JCheckBox plain = new JCheckBox();
        JCheckBox b = RoundCheck.style(plain);
        assertSame(plain, b);
        assertEquals(new Dimension(24, 24), b.getPreferredSize());
        assertEquals(new Insets(2, 2, 2, 2), b.getInsets());
        assertEquals(RoundCheck.SIZE, b.getIcon().getIconWidth());
        assertEquals(20, b.getIcon().getIconHeight());
        assertFalse(b.isOpaque());
        assertFalse(b.isFocusPainted());
        assertEquals(Cursor.HAND_CURSOR, b.getCursor().getType());
        assertEquals("", b.getText());
    }

    @Test
    void eRchk7Click() {
        JCheckBox b = box(false);
        int[] fired = {0};
        b.addActionListener(e -> fired[0]++);
        b.doClick(0);
        assertTrue(b.isSelected());
        assertEquals(1, fired[0]);
        b.doClick(0);
        assertFalse(b.isSelected());
        assertEquals(2, fired[0]);
    }
}
