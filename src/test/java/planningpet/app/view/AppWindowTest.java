package planningpet.app.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import planningpet.common.view.Snap;

/**
 * 앱의 창 틀. 번호는 docs/porting/frame.md 2절. 기준 그림은 프로토타입 App을 Theme.install 뒤에 만들어 글을 모두 비우고
 * 그렸다. 창(JFrame)은 headless 검사에서 만들 수 없어 창 내용과 창 아이콘을 그려 맞춘다.
 */
class AppWindowTest {
    private static Component at(Component c, int... path) {
        for (int i : path) {
            c = ((Container) c).getComponent(i);
        }
        return c;
    }

    @Test
    void eWin1Bounds() {
        AppWindow w = new AppWindow();
        assertEquals(new Dimension(1000, 680), w.root().getPreferredSize());
        w.root().setSize(1000, 680);
        w.root().doLayout();
        assertEquals(new Rectangle(10, 10, 140, 660), at(w.root(), 0).getBounds());
        assertEquals(new Rectangle(162, 10, 828, 660), at(w.root(), 1).getBounds());
        assertEquals(new Dimension(0, 64), at(w.root(), 0, 0, 0).getPreferredSize());
    }

    @Test
    void eWin11WindowIcon() throws IOException {
        Snap.assertLooksLikePrototype("frame-icon", AppWindow.icon());
    }
}
