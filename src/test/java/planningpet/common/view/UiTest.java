package planningpet.common.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import javax.swing.JLabel;
import org.junit.jupiter.api.Test;

/** 입력칸의 도우미. 번호는 docs/porting/pick.md 2절. */
class UiTest {
    @Test
    void eHint1() {
        for (String text : new String[] {"비워 두면 나비, 바둑이로 불러요", "", "<html><b>x</b></html>"}) {
            JLabel l = Ui.hint(text);
            assertEquals(text, l.getText());
            assertEquals(Theme.INK_DIM, l.getForeground());
            assertEquals(102, l.getForeground().getAlpha());
            assertEquals(Theme.font(12), l.getFont());
            assertFalse(l.isOpaque());
        }
        assertNull(Ui.hint(null).getText());
    }
}
