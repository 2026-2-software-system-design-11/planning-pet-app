package planningpet.shop.view;

import planningpet.common.view.*;
import planningpet.shop.model.Shop;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JToggleButton;

/**
 * 나의 방 상점 칸의 아이템 카드(D-53). 그림, 이름, 아래에 가격(동전과 "{가격}P") 또는 민트 알약 "보유 중".
 * 고르면 민트 테두리와 오른쪽 위 체크 배지가 붙는다. 글자(getText)는 "책장 200P"나 "쿠션 보유 중"처럼 한 줄이다.
 */
public final class ShopCard extends JToggleButton {
    public static final int W = 104;
    public static final int H = 150;
    public final Shop.Item item;
    private final boolean owned;

    public ShopCard(Shop.Item item, boolean owned, boolean placed) {
        super(item.name() + " " + (owned ? "보유 중" + (placed ? " ✓" : "") : item.price() + "P"));
        this.item = item;
        this.owned = owned;
        setPreferredSize(new Dimension(W, H));
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setRolloverEnabled(true);
        setToolTipText(owned ? (placed ? "방에 놓여 있어요" : "가지고 있어요. 방에 놓지 않았어요") : "누르면 미리보기에서 볼 수 있어요");
        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        int x = 2;
        int y = 8;
        int w = getWidth() - 5;
        int h = getHeight() - 12;
        int r = 16;
        boolean on = isSelected();
        Card.shadow(g, x, y, w, h, r);
        g.setColor(on ? Theme.MINT_PALE : getModel().isRollover() ? Theme.CARD_BOTTOM : Theme.CARD_TOP);
        g.fillRoundRect(x, y, w, h, r, r);
        g.setColor(on ? Theme.MINT : Theme.CARD_EDGE);
        g.setStroke(new java.awt.BasicStroke(on ? 2.2f : 1f));
        g.drawRoundRect(x, y, w, h, r, r);
        BufferedImage im = Images.item(item.name());
        int pic = Math.min(w - 24, h - 66);
        if (im != null) {
            Images.fit(g, im, x + (w - pic) / 2, y + 10, pic, pic);
        } else {
            Icons.draw(g, Icons.Kind.BOX, x + (w - pic / 2) / 2, y + 10 + pic / 4, pic / 2, Theme.ICON);
        }
        g.setFont(Theme.bold(13));
        java.awt.FontMetrics fm = g.getFontMetrics();
        g.setColor(Theme.INK);
        g.drawString(item.name(), x + (w - fm.stringWidth(item.name())) / 2, y + h - 36);
        int py = y + h - 26;
        if (owned) {
            String t = "보유 중";
            g.setFont(Theme.bold(12));
            int tw = g.getFontMetrics().stringWidth(t) + 24;
            g.setColor(Theme.MINT_SOFT);
            g.fillRoundRect(x + (w - tw) / 2, py, tw, 20, 20, 20);
            g.setColor(Theme.ICON);
            g.drawString(t, x + (w - tw) / 2 + 12, py + 15);
        } else {
            String t = item.price() + "P";
            g.setFont(Theme.heavy(14));
            int tw = g.getFontMetrics().stringWidth(t);
            int left = x + (w - tw - 20) / 2;
            Icons.draw(g, Icons.Kind.COIN, left, py + 2, 16, Theme.COIN);
            g.setColor(Theme.INK);
            g.drawString(t, left + 20, py + 16);
        }
        if (on) {
            int b = 22;
            int bx = x + w - b + 6;
            int by = y - 6;
            g.setColor(java.awt.Color.WHITE);
            g.fillOval(bx - 2, by - 2, b + 4, b + 4);
            Icons.draw(g, Icons.Kind.CHECK, bx, by, b, Theme.MINT_DEEP);
        }
