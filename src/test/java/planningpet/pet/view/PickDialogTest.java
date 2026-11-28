package planningpet.pet.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Dimension;
import java.awt.Insets;
import java.io.IOException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import org.junit.jupiter.api.Test;
import planningpet.common.view.Snap;

/**
 * 반려동물 고르기 창의 내용. 번호는 docs/porting/pick.md 2절. 기준 그림(proto/pickdlg-*.png)은 글을 모두 비우고 그렸다.
 */
class PickDialogTest {
    /** 글을 모두 비운 패널을 기준 그림과 맞춘다. */
    private static void looksLikePrototype(String name, int w, int h) throws IOException {
        PickDialog d = new PickDialog();
        Snap.blank(d.panel);
        Snap.assertLaidOutLooksLikePrototype("pickdlg-" + name, d.panel, w, h);
    }

    @Test
    void eDlg1Parts() {
        PickDialog d = new PickDialog();
        assertTrue(d.panel.isOpaque());
        assertEquals(BoxLayout.Y_AXIS, ((BoxLayout) d.panel.getLayout()).getAxis());
        assertEquals(new Insets(16, 0, 22, 0), d.panel.getInsets());
        assertEquals(2, d.panel.getComponentCount());
    }

    @Test
    void eDlg3DefaultSize() throws IOException {
        assertEquals(new Dimension(800, 38), new PickDialog().panel.getPreferredSize());
        looksLikePrototype("800x300", 800, 300);
    }

    @Test
    void eDlg10BackdropWidth() {
        PickDialog d = new PickDialog();
        Dimension wide = new Dimension(1000, 5);
        d.panel.add(new Box.Filler(wide, wide, wide));
        assertEquals(new Dimension(1000, 43), d.panel.getPreferredSize());
    }
}
