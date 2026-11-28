package planningpet.common.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Cursor;
import java.awt.Dimension;
import org.junit.jupiter.api.Test;

/** 알약 탭. 번호는 docs/porting/character.md S8d 2절. 그림을 프로토타입과 견주지 않고 크기와 칠한 색을 본다. */
class PillTest {
    private static int width(int size, String text) {
        return new Pill("", null, 14).getFontMetrics(Theme.bold(size)).stringWidth(text);
    }

    @Test
    void ePill1OneLineSize() {
        Pill icon = new Pill("상태", Icons.Kind.CHART, 14);
        Pill plain = new Pill("상태", null, 14);
        int text = width(14, "상태");
        assertEquals(new Dimension(text + 60, 32), icon.getPreferredSize());
        assertEquals(new Dimension(text + 34, 32), plain.getPreferredSize());
        icon.showIcon(false);
        assertEquals(plain.getPreferredSize(), icon.getPreferredSize());
    }

    @Test
    void ePill6Properties() {
        Pill p = new Pill("상태", Icons.Kind.CHART, 14);
        assertEquals(Cursor.HAND_CURSOR, p.getCursor().getType());
        assertFalse(p.isFocusPainted());
        assertFalse(p.isOpaque());
        assertFalse(p.isContentAreaFilled());
        assertFalse(p.isBorderPainted());
        assertTrue(p.isRolloverEnabled());
        assertEquals(Theme.bold(14), p.getFont());
    }
}
