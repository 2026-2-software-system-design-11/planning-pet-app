package planningpet.schedule.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;
import planningpet.common.view.CircleButton;
import planningpet.common.view.Icons;
import planningpet.common.view.Snap;

/** 홈 목록의 할 일 한 줄. 번호는 docs/porting/task-rows.md S3v 2절. */
class TaskRowTest {
    /** 자식의 종류와 자리. 칸(JPanel) 안의 자식은 [ ] 안에 잇는다. */
    private static String bounds(Container p) {
        List<String> parts = new ArrayList<>();
        for (Component c : p.getComponents()) {
            Class<?> k = c.getClass().isAnonymousClass() ? c.getClass().getSuperclass() : c.getClass();
            parts.add(k.getSimpleName() + " " + c.getX() + "," + c.getY() + " " + c.getWidth() + "x" + c.getHeight()
                    + (c instanceof JPanel in ? " [" + bounds(in) + "]" : ""));
        }
        return String.join(", ", parts);
    }

    @Test
    void eRow1MiddleCells() {
        JPanel left = TaskRows.middle(RoundCheck.style(new JCheckBox()));
        Snap.layOut(left, 24, 28);
        assertEquals("JCheckBox 0,2 24x24", bounds(left));
        assertTrue(left.isOpaque());
        JLabel minutes = new JLabel("30분");
        minutes.setPreferredSize(new Dimension(34, 20));
        JPanel east = TaskRows.middle(new TimePill("", false, false, null), minutes,
                new CircleButton(Icons.Kind.PENCIL, 28, "수정", true), new CircleButton(Icons.Kind.TRASH, 28, "삭제", true));
        assertEquals(new Dimension(217, 28), east.getPreferredSize());
        Snap.layOut(east, 217, 28);
        assertEquals("TimePill 0,0 115x28, JLabel 119,4 34x20, CircleButton 157,0 28x28, CircleButton 189,0 28x28", bounds(east));
    }
}
