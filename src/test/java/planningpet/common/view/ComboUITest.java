package planningpet.common.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.Dimension;
import java.io.IOException;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.UIManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 고르는 칸(디자인 시스템 5절, D-97). 번호는 docs/porting/task-dialog.md S4c. 기준 그림은 프로토타입처럼 Theme.install 뒤에
 * 빈 글 항목 하나로 그렸다. install은 앱 전체 기본값을 바꾸므로 검사마다 되돌린다(ThemeInstallTest와 같다).
 */
class ComboUITest {
    @BeforeEach
    void install() {
        Theme.install();
    }

    @AfterEach
    void undoInstall() {
        for (Object key : List.copyOf(UIManager.getLookAndFeelDefaults().keySet())) {
            UIManager.put(key, null);
        }
        UIManager.put("swing.boldMetal", null);
    }

    /** ComboUI를 건 고르는 칸. */
    private static JComboBox<String> combo(String... items) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setUI(new ComboUI());
        return c;
    }

    private static void looks(String name, JComboBox<String> c, int w, int h) throws IOException {
        Snap.assertLaidOutLooksLikePrototype(name, c, w, h);
    }

    @Test
    void eCombo1Look() throws IOException {
        for (int[] s : new int[][] {{120, 24}, {200, 40}, {0, 0}, {20, 10}}) {
            looks("combo-" + s[0] + "x" + s[1], combo(""), s[0], s[1]);
        }
        JComboBox<String> c = combo("안 함", "매일", "요일");
        assertSame(UIManager.getBorder("TextField.border"), c.getBorder());
        assertEquals(Theme.CARD_TOP, c.getBackground());
        assertEquals(new Dimension(53, 24), c.getPreferredSize());
    }
}
