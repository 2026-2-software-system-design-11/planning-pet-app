package planningpet.common.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.io.IOException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.UIManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 화면 머리 오른쪽의 수치 카드. 번호는 docs/porting/icons.md 2절. 기준 그림은 프로토타입처럼 Theme.install 뒤에
 * 빈 글로 그렸다. install은 앱 전체 기본값을 바꾸므로 검사마다 되돌린다(ThemeInstallTest와 같다).
 */
class StatCardTest {
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

    /** 프로토타입 창 틀의 출석 단추와 같은 모양. */
    private static JButton attend(String text) {
        JButton b = new JButton(text);
        b.putClientProperty(RoundButtonUI.PRIMARY, Boolean.TRUE);
        b.setFont(Theme.bold(13));
        b.setBorder(BorderFactory.createEmptyBorder(7, 14, 9, 14));
        return b;
    }

    private static StatCard.Value value(String caption, String value) {
        StatCard.Value v = new StatCard.Value();
        v.set(caption, value);
        return v;
    }

    /** 창 없이는 배치가 돌지 않아 자식까지 배치한 뒤 기준 그림과 맞춘다. */
    private static void looksLikePrototype(String name, JComponent c, int w, int h) throws IOException {
        c.setSize(w, h);
        layOut(c);
        Snap.assertLooksLikePrototype(name, c, w, h);
    }

    private static void layOut(Component c) {
        if (c instanceof Container k) {
            k.doLayout();
            for (Component child : k.getComponents()) {
                layOut(child);
            }
        }
    }

    @Test
    void eScard1StreakCard() throws IOException {
        Card card = StatCard.of(Icons.Kind.FLAME, new StatCard.Value(), attend(""));
        assertEquals(new Insets(8, 14, 12, 16), card.getInsets());
        assertEquals(3, card.getComponentCount());
        assertEquals(new Dimension(110, 62), card.getPreferredSize());
        looksLikePrototype("stat-flame", card, 110, 62);
        Card full = StatCard.of(Icons.Kind.FLAME, value("연속 접속", "5일"), attend("출석 +20P"));
        assertEquals(new Dimension(212, 62), full.getPreferredSize());
    }

    @Test
    void eScard2PointCard() throws IOException {
        Card card = StatCard.of(Icons.Kind.COIN, new StatCard.Value(), null);
        assertEquals(2, card.getComponentCount());
        assertEquals(new Dimension(72, 62), card.getPreferredSize());
        looksLikePrototype("stat-coin", card, 72, 62);
        Card full = StatCard.of(Icons.Kind.COIN, value("보유", "1,234,567P"), null);
        assertEquals(new Dimension(183, 62), full.getPreferredSize());
    }
}
