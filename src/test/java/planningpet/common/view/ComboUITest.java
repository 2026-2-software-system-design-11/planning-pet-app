package planningpet.common.view;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.Component;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicComboPopup;
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

    /** 아이콘을 16x16 그림의 (2, 2)에 그린 픽셀. */
    private static int[] pixels(Icon icon) {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        icon.paintIcon(null, g, 2, 2);
        g.dispose();
        return img.getRGB(0, 0, 16, 16, null, 0, 16);
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

    @Test
    void eCombo4ArrowButton() {
        JComboBox<String> c = combo("안 함");
        Snap.layOut(c, 120, 30);
        JButton arrow = (JButton) c.getComponent(0);
        assertEquals("ComboBox.arrowButton", arrow.getName());
        assertEquals(new Rectangle(92, 2, 26, 26), arrow.getBounds());
        assertEquals(BasicButtonUI.class, arrow.getUI().getClass());
        assertEquals(List.of(false, false, true, false), List.of(arrow.isContentAreaFilled(), arrow.isOpaque(), arrow.isFocusable(),
                arrow.isRequestFocusEnabled()));
        assertArrayEquals(pixels(Icons.icon(Icons.Kind.DOWN, 12, Theme.ICON)), pixels(arrow.getIcon()));
        assertArrayEquals(pixels(Icons.icon(Icons.Kind.DOWN, 12, Theme.INK_DIM)), pixels(arrow.getDisabledIcon()));
    }

    /** 칸을 (x, y)에서 누른다. */
    private static void press(Component c, int x, int y, int button) {
        c.dispatchEvent(new MouseEvent(c, MouseEvent.MOUSE_PRESSED, 0, 0, x, y, 1, false, button));
    }

    /** 창 없는 칸은 누르기를 받지 않아 보인다고 답하게 한다. 그러면 목록을 띄우려다 화면 크기를 몰라 예외가 난다. */
    @Test
    void eCombo5PressAnywhereOpensList() {
        JComboBox<String> c = new JComboBox<>(new String[] {"안 함", "매일", "요일"}) {
            @Override
            public boolean isShowing() {
                return true;
            }
        };
        c.setUI(new ComboUI());
        Snap.layOut(c, 120, 24);
        assertThrows(HeadlessException.class, () -> press(c, 20, 12, MouseEvent.BUTTON1));
        assertThrows(HeadlessException.class, () -> press(c.getComponent(0), 5, 5, MouseEvent.BUTTON1));
        press(c, 20, 12, MouseEvent.BUTTON3);
        c.setEnabled(false);
        press(c, 20, 12, MouseEvent.BUTTON1);
        press(c.getComponent(0), 5, 5, MouseEvent.BUTTON1);
        assertFalse(c.isPopupVisible());
    }

    @Test
    void eCombo3KeyboardFocus() throws IOException {
        class Focus extends DefaultKeyboardFocusManager {
            void own(Component c) {
                setGlobalFocusOwner(c);
            }
        }
        Focus focus = new Focus();
        KeyboardFocusManager.setCurrentKeyboardFocusManager(focus);
        try {
            JComboBox<String> c = combo("");
            focus.own(c);
            looks("combo-focus", c, 120, 24);
            c = combo("");
            c.setEnabled(false);
            focus.own(c);
            looks("combo-focus-disabled", c, 120, 24);
        } finally {
            KeyboardFocusManager.setCurrentKeyboardFocusManager(null);
        }
    }

    @Test
    void eCombo2Disabled() throws IOException {
        JComboBox<String> c = combo("");
        c.setEnabled(false);
        looks("combo-disabled", c, 120, 24);
        c = combo("안 함");
        c.setEnabled(false);
        Snap.paint(c, 120, 24);
        Component value = (Component) c.getRenderer();
        assertEquals(List.of(Theme.INK_DIM, Theme.CARD_TOP), List.of(value.getForeground(), value.getBackground()));
    }

    @Test
    void eCombo6ListChosenRow() {
        JComboBox<Object> c = new JComboBox<>(new Object[] {"안 함", "매일", "요일"});
        c.setUI(new ComboUI());
        JList<Object> list = ((BasicComboPopup) c.getUI().getAccessibleChild(c, 0)).getList();
        assertEquals(List.of(Theme.MINT_PALE, Theme.INK), List.of(list.getSelectionBackground(), list.getSelectionForeground()));
        Component chosen = c.getRenderer().getListCellRendererComponent(list, "매일", 1, true, false);
        assertEquals(List.of(Theme.MINT_PALE, Theme.INK), List.of(chosen.getBackground(), chosen.getForeground()));
        Component other = c.getRenderer().getListCellRendererComponent(list, "요일", 2, false, false);
        assertEquals(List.of(Theme.CARD_TOP, Theme.INK), List.of(other.getBackground(), other.getForeground()));
    }
}
