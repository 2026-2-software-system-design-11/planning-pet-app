package planningpet.common.view;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;

/**
 * 디자인 시스템 5절의 버튼. 기본은 흰 알약에 옅은 그림자, 주 행동(PRIMARY)과 눌린 토글은 민트 채움에 흰 글자,
 * 내비게이션(NAV)은 바탕 없이 아이콘과 글자만 있고 고르면 연민트 알약이다.
 */
public final class RoundButtonUI extends BasicButtonUI {
    /** 내비게이션 버튼에 붙이는 표시. */
    public static final String NAV = "planningpet.nav";
    /** 화면에서 가장 중요한 행동(상점, 출석, 저장 같은)에 붙이는 표시. */
    public static final String PRIMARY = "planningpet.primary";
    /** 치운 아이템처럼 글자를 흐리게 보일 때 붙이는 표시. */
    public static final String DIM = "planningpet.dim";

    public static ComponentUI createUI(JComponent c) {
        return new RoundButtonUI();
    }

    @Override
    public void installDefaults(AbstractButton b) {
        super.installDefaults(b);
        b.setOpaque(false);
        b.setBorder(new EmptyBorder(9, 12, 9, 12));
        b.setFocusPainted(false);
        b.setRolloverEnabled(true);
        b.setContentAreaFilled(false);
    }

    private static boolean filled(AbstractButton b) {
        return !Boolean.TRUE.equals(b.getClientProperty(NAV))
                && (Boolean.TRUE.equals(b.getClientProperty(PRIMARY)) || (b instanceof JToggleButton && b.isSelected()));
    }

    @Override
    public void paint(Graphics graphics, JComponent c) {
        AbstractButton b = (AbstractButton) c;
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        boolean nav = Boolean.TRUE.equals(b.getClientProperty(NAV));
        boolean pressed = b.getModel().isPressed() && b.getModel().isArmed();
        boolean over = b.getModel().isRollover() && b.isEnabled();
        int w = b.getWidth() - 4;
        int h = b.getHeight() - 5;
        int arc = Math.min(h, 2 * Theme.BUTTON_RADIUS);
        if (nav) {
            if (b.isSelected()) {
                g.setColor(Theme.NAV_PILL);
                g.fillRoundRect(2, 1, w, h, arc, arc);
            } else if (over) {
                g.setColor(new Color(255, 255, 255, 170));
                g.fillRoundRect(2, 1, w, h, arc, arc);
            }
        } else if (!b.isEnabled()) {
            g.setColor(new Color(0xEDF3F5));
            g.fillRoundRect(2, 1, w, h, arc, arc);
            g.setColor(Theme.CARD_EDGE);
            g.drawRoundRect(2, 1, w - 1, h - 1, arc, arc);
        } else if (filled(b)) {
            Card.shadow(g, 2, 1, w, h, arc);
            Color top = pressed ? Theme.MINT_DEEP : over ? Theme.MINT_TOP.brighter() : Theme.MINT_TOP;
            g.setPaint(new GradientPaint(0, 0, top, 0, h, Theme.MINT_DEEP));
            g.fillRoundRect(2, 1, w, h, arc, arc);
            g.setColor(new Color(255, 255, 255, 70));
            g.drawRoundRect(3, 2, w - 2, h - 2, arc, arc);
        } else {
            Card.shadow(g, 2, 1, w, h, arc);
            Color bottom = pressed ? Theme.MINT_SOFT : over ? Theme.MINT_PALE : new Color(0xF5FAFB);
            g.setPaint(new GradientPaint(0, 0, Color.WHITE, 0, h, bottom));
            g.fillRoundRect(2, 1, w, h, arc, arc);
            g.setColor(over || pressed ? Theme.MINT_LINE : Theme.CARD_EDGE);
            g.drawRoundRect(2, 1, w - 1, h - 1, arc, arc);
        }
        if (b.isFocusOwner() && b.isFocusPainted() == false && !nav && b.isEnabled()) {
            g.setColor(new Color(0x1F, 0xB9, 0x9C, 110));
            g.drawRoundRect(4, 3, w - 5, h - 5, arc - 4, arc - 4);
        }
        g.dispose();
        Graphics2D t = (Graphics2D) graphics;
        t.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        super.paint(graphics, c);
    }

    @Override
    protected void paintText(Graphics g, AbstractButton b, java.awt.Rectangle r, String text) {
        g.setFont(b.getFont());
        int mnemonic = b.getDisplayedMnemonicIndex();
        BasicGraphicsUtils.drawStringUnderlineCharAt(b, (Graphics2D) g, text, mnemonic, r.x, r.y + g.getFontMetrics().getAscent());
    }

    @Override
    protected void paintButtonPressed(Graphics g, AbstractButton b) {
        // 눌림은 paint에서 MINT_SELECTED로 그린다.
    }
}
