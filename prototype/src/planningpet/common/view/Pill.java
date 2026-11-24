package planningpet.common.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JToggleButton;

/**
 * 알약 모양 탭(나의 방의 분류 탭과 펫 칩, 캐릭터의 상태와 꾸미기와 정보 탭). 고르면 민트로 채우고 흰 글자,
 * 아니면 흰 바탕에 옅은 테두리다. 그림(펫 얼굴)이나 아이콘을 글자 앞에 둘 수 있고, 둘째 줄(레벨)을 둘 수 있다.
 */
public class Pill extends JToggleButton {
    private final Icons.Kind kind;
    private BufferedImage picture;
    private String second;
    private final int size;
    /** 좁은 자리에서 아이콘을 빼고 글자만 보인다. */
    private boolean showIcon = true;

    public Pill(String text, Icons.Kind kind, int size) {
        super(text);
        this.kind = kind;
        this.size = size;
        setFont(Theme.bold(size));
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setRolloverEnabled(true);
        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    }

    /** 글자 앞 그림(펫 얼굴 등). */
    public Pill picture(BufferedImage im) {
        this.picture = im;
        revalidate();
        repaint();
        return this;
    }

    /** 둘째 줄 글자("Lv. 5"). null이면 한 줄이다. */
    public Pill second(String text) {
        this.second = text;
        revalidate();
        repaint();
        return this;
    }

    private int mark() {
        return picture != null ? size * 2 + 6 : kind != null && showIcon ? size + 4 : 0;
    }

    public void showIcon(boolean on) {
        if (showIcon != on) {
            showIcon = on;
            revalidate();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        int text = fm.stringWidth(getText());
        if (second != null) {
            text = Math.max(text, getFontMetrics(Theme.bold(size - 1)).stringWidth(second));
        }
        int h = second != null ? size * 3 + 10 : size + 18;
        int gap = mark() > 0 ? 8 : 0;
        return new Dimension(text + mark() + gap + size * 2 + 6, Math.max(h, picture != null ? size * 2 + 14 : 0));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        int w = getWidth() - 1;
        int h = getHeight() - 4;
        int r = second != null || picture != null ? 22 : h;
        boolean on = isSelected();
        Card.shadow(g, 0, 0, w, h, r);
        if (on) {
            g.setPaint(new java.awt.GradientPaint(0, 0, Theme.MINT_TOP, 0, h, Theme.MINT_DEEP));
        } else {
            g.setColor(getModel().isRollover() ? Theme.MINT_PALE : Theme.CARD_TOP);
        }
        g.fillRoundRect(0, 0, w, h, r, r);
        g.setColor(on ? Theme.MINT_DEEP : Theme.CARD_EDGE);
        g.drawRoundRect(0, 0, w, h, r, r);
        Color ink = on ? Color.WHITE : Theme.INK;
        if (!isEnabled()) {
            ink = Theme.INK_DIM;
        }
        FontMetrics fm = g.getFontMetrics(getFont());
        int textW = fm.stringWidth(getText());
        if (second != null) {
            textW = Math.max(textW, g.getFontMetrics(Theme.bold(size - 1)).stringWidth(second));
        }
        int gap = mark() > 0 ? 8 : 0;
        int x = (w - (mark() + gap + textW)) / 2;
        if (picture != null) {
            int ph = h - 8;
            Images.fit(g, picture, x, 4, mark(), ph);
        } else if (kind != null && showIcon) {
            Icons.draw(g, kind, x, (h - size - 2) / 2, size + 2, on ? Color.WHITE : Theme.ICON);
        }
        int tx = x + mark() + gap;
        g.setColor(ink);
        g.setFont(getFont());
        if (second == null) {
            g.drawString(getText(), tx, (h + fm.getAscent() - fm.getDescent()) / 2);
        } else {
            g.drawString(getText(), tx, h / 2 - 2);
            g.setFont(Theme.bold(size - 1));
            g.drawString(second, tx, h / 2 + size + 1);
        }
        g.dispose();
    }
}
