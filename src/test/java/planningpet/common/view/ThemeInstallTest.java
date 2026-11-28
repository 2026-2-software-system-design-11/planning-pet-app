package planningpet.common.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.awt.Font;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Theme.install. 번호는 docs/porting/theme.md 2절. install은 앱 전체 기본값을 바꾸므로 검사마다 되돌린다. */
class ThemeInstallTest {
    private final String plainButton = new JButton().getUI().getClass().getName();

    @AfterEach
    void undoInstall() {
        for (Object key : List.copyOf(UIManager.getLookAndFeelDefaults().keySet())) {
            UIManager.put(key, null);
        }
        UIManager.put("swing.boldMetal", null);
        assertEquals(plainButton, new JButton().getUI().getClass().getName(), "되돌린 뒤 install 전의 단추");
    }

    private static String look(Font f) {
        return f.getFontName() + " " + f.getSize2D();
    }

    /** install이 건드리는 기본값(글꼴, 글자색, 바탕, UI)을 이름 순으로. */
    private static Map<String, String> installed() {
        Map<String, String> values = new TreeMap<>();
        for (Object key : UIManager.getLookAndFeelDefaults().keySet()) {
            String k = key.toString();
            if (k.endsWith(".font") || k.endsWith(".foreground") || k.endsWith(".background") || k.endsWith("UI")) {
                values.put(k, String.valueOf(UIManager.get(key)));
            }
        }
        return values;
    }

    @Test
    void eTheme3InstallsFontsColorsAndButtons() {
        Theme.install();
        JButton button = new JButton("저장");
        assertEquals(List.of("RoundButtonUI", "RoundButtonUI"), List.of(button.getUI().getClass().getSimpleName(),
                new JToggleButton("월").getUI().getClass().getSimpleName()));
        assertEquals(look(Theme.bold(14)), look(button.getFont()));
        assertEquals(look(Theme.bold(14)), look(UIManager.getFont("ToggleButton.font")));
        for (Object key : UIManager.getLookAndFeelDefaults().keySet()) {
            String k = key.toString();
            if (k.endsWith(".font") && !k.equals("Button.font") && !k.equals("ToggleButton.font")) {
                assertEquals(look(Theme.font(14)), look(UIManager.getFont(key)), k);
            }
            if (k.endsWith(".foreground") && !k.startsWith("ProgressBar")) {
                assertEquals(Theme.INK, UIManager.getColor(key), k);
            }
        }
        List<Color> colors = List.of(Theme.MINT, Theme.TRACK, Theme.INK, Theme.BG, Theme.BG, Theme.BG, Theme.BG);
        assertEquals(colors, List.of("ProgressBar.foreground", "ProgressBar.background", "ProgressBar.selectionForeground",
                "Panel.background", "CheckBox.background", "RadioButton.background", "OptionPane.background")
                .stream().map(UIManager::getColor).toList());
    }

    @Test
    void eTheme4InstallTwiceIsSameAsOnce() {
        Theme.install();
        Map<String, String> once = installed();
        Theme.install();
        assertEquals(once, installed());
    }

    @Test
    void eTheme5ComboBox() {
        Theme.install();
        JComboBox<String> combo = new JComboBox<>();
        assertEquals(ComboUI.class, combo.getUI().getClass());
        assertEquals(Theme.CARD_TOP, combo.getBackground());
        List<Color> colors = List.of(Theme.CARD_TOP, Theme.CARD_TOP, Theme.INK_DIM, Theme.MINT_PALE, Theme.INK);
        assertEquals(colors, List.of("ComboBox.background", "ComboBox.disabledBackground", "ComboBox.disabledForeground",
                "ComboBox.selectionBackground", "ComboBox.selectionForeground").stream().map(UIManager::getColor).toList());
    }
}
