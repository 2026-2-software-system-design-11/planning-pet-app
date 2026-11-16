package planningpet.proto;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.RenderingHints;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;

/** 디자인 시스템 5절의 버튼과 내비게이션 버튼. 흰색에서 바탕색으로 가는 둥근 버튼이다. */
public final class RoundButtonUI extends BasicButtonUI {
    /** 내비게이션 버튼에 붙이는 표시. 고르면 MINT_SELECTED, 아니면 MINT_PALE. */
    public static final String NAV = "planningpet.nav";
    /** 치운 아이템처럼 글자를 흐리게 보일 때 붙이는 표시. */
    public static final String DIM = "planningpet.dim";

    public static ComponentUI createUI(JComponent c) {
        return new RoundButtonUI();
    }

    @Override
    public void installDefaults(AbstractButton b) {
        super.installDefaults(b);
        b.setOpaque(false);
        b.setBorder(new EmptyBorder(10, 12, 10, 12));
        b.setFocusPainted(false);
        b.setRolloverEnabled(true);
    }

    @Override
    public void paint(Graphics graphics, JComponent c) {
        AbstractButton b = (AbstractButton) c;
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        boolean nav = Boolean.TRUE.equals(b.getClientProperty(NAV));
        boolean on = b.isSelected() || b.getModel().isPressed();
        java.awt.Color base = nav ? (on ? Theme.MINT_SELECTED : Theme.MINT_PALE) : on ? Theme.MINT_SELECTED : Theme.MINT_SOFT;
        if (!b.isEnabled()) {
            base = Theme.DAY;
        }
        int w = b.getWidth() - 3;
        int h = b.getHeight() - 3;
        g.setPaint(new GradientPaint(0, 0, java.awt.Color.WHITE, 0, h, base));
        g.fillRoundRect(1, 1, w, h, Theme.BUTTON_RADIUS, Theme.BUTTON_RADIUS);
        g.setColor(Theme.MINT_LINE);
        g.drawRoundRect(1, 1, w, h, Theme.BUTTON_RADIUS, Theme.BUTTON_RADIUS);
        g.dispose();
        super.paint(graphics, c);
    }

    @Override
    protected void paintText(Graphics g, AbstractButton b, java.awt.Rectangle r, String text) {
        g.setColor(b.isEnabled() && !Boolean.TRUE.equals(b.getClientProperty(DIM)) ? Theme.INK : Theme.INK_DIM);
        g.setFont(b.getFont());
        int mnemonic = b.getDisplayedMnemonicIndex();
        BasicGraphicsUtils.drawStringUnderlineCharAt(b, (Graphics2D) g, text, mnemonic, r.x, r.y + g.getFontMetrics().getAscent());
    }

    @Override
    protected void paintButtonPressed(Graphics g, AbstractButton b) {
        // 눌림은 paint에서 MINT_SELECTED로 그린다.
    }
}
