package planningpet.home.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.KeyboardFocusManager;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Set;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.Test;
import planningpet.pet.model.Bubble;
import planningpet.pet.model.PetKind;

/** 홈 펫 카드 누르기와 상태 테두리. 번호는 docs/porting/home.md S3p 2절. */
class PetCardPressTest {
    private static final int STANDARD = KeyEvent.KEY_LOCATION_STANDARD;
    private int presses;

    private PetCard card() {
        return new PetCard(PetCardTest.data(PetKind.CAT, 0), new Bubble(), () -> presses++);
    }

    private static int count(InputMap m) {
        return m.allKeys() == null ? 0 : m.allKeys().length;
    }

    /** 키를 누르고 떼어 "누른 뒤 알린 수, 뗀 뒤 알린 수, 누름 사건을 먹었는지"를 낸다. */
    private String key(int code, int mods, char ch, int where) {
        PetCard c = card();
        presses = 0;
        KeyboardFocusManager focus = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        KeyEvent press = new KeyEvent(c, KeyEvent.KEY_PRESSED, 0, mods, code, ch, where);
        focus.redispatchEvent(c, press);
        int pressed = presses;
        focus.redispatchEvent(c, new KeyEvent(c, KeyEvent.KEY_RELEASED, 0, mods, code, ch, where));
        return pressed + " " + presses + " " + press.isConsumed();
    }

    @Test
    void ePpress1Properties() {
        PetCard c = card();
        assertTrue(c.isFocusable());
        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        KeyStroke space = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0);
        assertEquals(Set.of(enter, space), Set.of(c.getInputMap().allKeys()));
        assertEquals(c.getInputMap().get(enter), c.getInputMap().get(space));
        assertNotNull(c.getActionMap().get(c.getInputMap().get(enter)));
        assertEquals(List.of(0, 0), List.of(count(c.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)),
                count(c.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW))));
    }

    @Test
    void ePpress2Keys() {
        assertEquals("1 1 true", key(KeyEvent.VK_ENTER, 0, '\n', STANDARD));
        assertEquals("1 1 true", key(KeyEvent.VK_SPACE, 0, ' ', STANDARD));
        assertEquals("1 1 true", key(KeyEvent.VK_ENTER, 0, '\n', KeyEvent.KEY_LOCATION_NUMPAD));
        assertEquals("0 0 false", key(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK, '\n', STANDARD));
        assertEquals("0 0 false", key(KeyEvent.VK_SPACE, InputEvent.CTRL_DOWN_MASK, ' ', STANDARD));
        assertEquals("0 0 false", key(KeyEvent.VK_ENTER, InputEvent.META_DOWN_MASK, '\n', STANDARD));
        assertEquals("0 0 false", key(KeyEvent.VK_A, 0, 'a', STANDARD));
        assertEquals("0 0 false", key(KeyEvent.VK_ESCAPE, 0, KeyEvent.CHAR_UNDEFINED, STANDARD));
        PetCard held = card();
        presses = 0;
        for (int i = 0; i < 3; i++) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().redispatchEvent(held,
                    new KeyEvent(held, KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_ENTER, '\n'));
        }
        assertEquals(3, presses, "떼지 않고 되풀이된 누름");
    }
}
