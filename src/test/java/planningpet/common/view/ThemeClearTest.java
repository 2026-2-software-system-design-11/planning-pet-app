package planningpet.common.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.FlowLayout;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.plaf.basic.BasicToggleButtonUI;
import org.junit.jupiter.api.Test;

/** Theme.clear. 번호는 docs/porting/theme.md 2절. */
class ThemeClearTest {
    private static <C extends JComponent> C opaque(C c) {
        c.setOpaque(true);
        return c;
    }

    @Test
    void eTheme6ClearsPanelsOutsideCards() {
        JPanel root = opaque(new JPanel());
        JPanel inner = opaque(new JPanel());
        Card card = opaque(new Card(new FlowLayout()));
        JPanel inCard = opaque(new JPanel());
        JToggleButton plain = new JToggleButton("기본");
        plain.setUI(new BasicToggleButtonUI());
        opaque(plain);
        JToggleButton round = new JToggleButton("둥근");
        round.setUI(new RoundButtonUI());
        opaque(round);
        JScrollPane scroll = opaque(new JScrollPane(new JLabel("목록")));
        scroll.getViewport().setOpaque(true);
        JLabel label = opaque(new JLabel("글"));
        card.add(inCard);
        List.of(card, plain, round, scroll, label).forEach(inner::add);
        root.add(inner);
        Theme.clear(root);
        assertEquals(List.of(false, false, true, false, false, true, false, false, true),
                List.of(root.isOpaque(), inner.isOpaque(), card.isOpaque(), inCard.isOpaque(), plain.isOpaque(),
                        round.isOpaque(), scroll.isOpaque(), scroll.getViewport().isOpaque(), label.isOpaque()));
    }

    @Test
    void eTheme7NothingToClear() {
        assertDoesNotThrow(() -> Theme.clear(null));
    }
}
