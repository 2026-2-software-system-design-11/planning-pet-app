package planningpet.common.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

/** 디자인 시스템(docs/design-system.md)의 값. 이름은 문서의 이름과 같다. */
public final class Theme {
    public static final Color INK = new Color(0x354656);
    public static final Color INK_DIM = new Color(0x35, 0x46, 0x56, 102);
    public static final Color MINT = new Color(0x4DB4A0);
    public static final Color MINT_SELECTED = new Color(0x7BDECB);
    public static final Color MINT_SOFT = new Color(0xC7F0E4);
    public static final Color MINT_PALE = new Color(0xE3F7F1);
    public static final Color MINT_LINE = new Color(0xAAD9DD);
    public static final Color SIDE = new Color(0xE9F9F4);
    public static final Color BG = new Color(0xF0F9FA);
    public static final Color CARD_TOP = Color.WHITE;
    public static final Color CARD_BOTTOM = new Color(0xF5FCFF);
    public static final Color CARD_LINE = new Color(0xC2E5F0);
    public static final Color CARD_SHADOW = new Color(0xA7, 0xD2, 0xDC, 45);
    public static final Color TRACK = new Color(0xE0EEF6);
    public static final Color DAY = new Color(0xF2F9FD);
    public static final Color DAY_SELECTED = new Color(0x8FE0CA);
    public static final Color SUNDAY = new Color(0xE55C64);
    public static final Color SATURDAY = new Color(0x4C82DA);
    public static final Color TASK_TEXT = new Color(0x28977F);
    public static final int CARD_RADIUS = 28;
    public static final int BUTTON_RADIUS = 22;
    public static final int IMAGE_RADIUS = 24;
    public static final int BUBBLE_RADIUS = 16;
    private static final String FAMILY = pickFamily();

    private Theme() {
    }

    /** 맑은 고딕, 없으면 Apple SD Gothic Neo. 20 이상은 굵게. */
    public static Font font(int size) {
        return new Font(FAMILY, size >= 20 ? Font.BOLD : Font.PLAIN, size);
    }

    public static Font bold(int size) {
        return new Font(FAMILY, Font.BOLD, size);
    }

    private static String pickFamily() {
        List<String> names = List.of(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        return names.contains("Malgun Gothic") ? "Malgun Gothic"
                : names.contains("Apple SD Gothic Neo") ? "Apple SD Gothic Neo" : Font.DIALOG;
    }

    /** Swing 기본값에 글꼴, 글자 색, 버튼 그리기를 건다. 부품을 만들기 전에 한 번 부른다. */
    public static void install() {
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        for (Object key : List.copyOf(UIManager.getLookAndFeelDefaults().keySet())) {
            String k = key.toString();
            if (k.endsWith(".font")) {
                UIManager.put(key, new FontUIResource(font(14)));
            } else if (k.endsWith(".foreground") && !k.startsWith("ProgressBar")) {
                UIManager.put(key, new ColorUIResource(INK));
            }
        }
        UIManager.put("Button.font", new FontUIResource(bold(14)));
        UIManager.put("ToggleButton.font", new FontUIResource(bold(14)));
        UIManager.put("ButtonUI", RoundButtonUI.class.getName());
        UIManager.put("ToggleButtonUI", RoundButtonUI.class.getName());
        for (String k : List.of("Panel", "CheckBox", "RadioButton", "OptionPane")) {
            UIManager.put(k + ".background", new ColorUIResource(BG));
        }
        UIManager.put("ProgressBar.foreground", new ColorUIResource(MINT));
        UIManager.put("ProgressBar.background", new ColorUIResource(TRACK));
        UIManager.put("ProgressBar.selectionForeground", new ColorUIResource(INK));
    }

    /** 배경 그림이 보이게 카드 밖의 패널을 투명하게 한다. 화면을 다시 만든 뒤마다 부른다. */
    public static void clear(Component c) {
        if (c instanceof JPanel p && !(p instanceof Card)) {
            p.setOpaque(false);
        }
        if (c instanceof javax.swing.JToggleButton t && !(t.getUI() instanceof RoundButtonUI)) {
            t.setOpaque(false);
        }
        if (c instanceof javax.swing.JScrollPane s) {
            s.setOpaque(false);
            s.getViewport().setOpaque(false);
        }
        if (c instanceof Container k) {
            for (Component child : k.getComponents()) {
                clear(child);
            }
        }
    }
}
