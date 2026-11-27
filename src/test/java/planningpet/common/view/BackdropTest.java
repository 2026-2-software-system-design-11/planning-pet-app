package planningpet.common.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.BorderFactory;
import org.junit.jupiter.api.Test;

/**
 * 창 바탕. 번호는 docs/porting/icons.md 2절. 기준 그림은 프로토타입 App의 root에서 사이드바와 본문 판의 자식을 비워
 * 글 없이 그렸다. 여기서도 같은 배치(여백 10, 폭 140의 사이드바, 사이 GAP, 본문 판)에 빈 판 둘을 둔다.
 */
class BackdropTest {
    private static Backdrop window(boolean images) {
        Card side = new Card(new BorderLayout(), 22).filled(Theme.SIDE);
        side.setPreferredSize(new Dimension(140, 0));
        BorderLayout layout = new BorderLayout(Theme.GAP, 0);
        Backdrop b = images ? new Backdrop(layout, side) : new Backdrop(layout, side, null, null);
        b.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        b.add(side, BorderLayout.WEST);
        b.add(new Card(new BorderLayout(0, 10), 24).filled(Theme.PANEL), BorderLayout.CENTER);
        return b;
    }

    @Test
    void eBack1UsualWindow() throws IOException {
        assertEquals("113x51", Images.CLOUD.getWidth() + "x" + Images.CLOUD.getHeight());
        assertEquals("182x171", Images.MEADOW.getWidth() + "x" + Images.MEADOW.getHeight());
        Backdrop b = window(true);
        Snap.assertLaidOutLooksLikePrototype("back-1000x680", b, 1000, 680);
        assertEquals(150, b.getComponent(0).getX() + b.getComponent(0).getWidth());
    }

    @Test
    void eBack2LowerAndLargerWindow() throws IOException {
        Snap.assertLaidOutLooksLikePrototype("back-1000x600", window(true), 1000, 600);
        Snap.assertLaidOutLooksLikePrototype("back-2560x1440", window(true), 2560, 1440);
    }

    @Test
    void eBack3VeryLowWindow() throws IOException {
        Snap.assertLaidOutLooksLikePrototype("back-1000x150", window(true), 1000, 150);
    }

    @Test
    void eBack4ZeroSize() throws IOException {
        Snap.assertLaidOutLooksLikePrototype("back-0x0", window(true), 0, 0);
    }

    @Test
    void eBack5SidebarNotLaidOut() throws IOException {
        Snap.assertLooksLikePrototype("back-not-laid-out", window(true), 1000, 680);
    }

    @Test
    void eBack6NoImageFiles() throws IOException {
        assertNull(Images.load("없는 그림.png"));
        Snap.assertLaidOutLooksLikePrototype("back-no-images-1000x680", window(false), 1000, 680);
    }
}
