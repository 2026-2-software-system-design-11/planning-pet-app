package planningpet.pet.view;

import planningpet.app.model.Store;
import planningpet.common.view.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JComponent;

/** pet 화면의 방. 아이템마다 자리가 정해져 있다(D-13). 자리와 그림은 디자인 시스템 6절을 따른다. */
public final class RoomView extends JComponent {
    /** 아이템 자리: 방 너비와 높이에 대한 비율 {x, y, w, h}. 넣은 순서대로 그린다. 앞 여섯은 원본 프로토타입의 자리다. */
    private static final Map<String, double[]> BEHIND = new LinkedHashMap<>();
    private static final double[] PLANT = {0.79, 0.63, 0.20, 0.34};

    static {
        BEHIND.put("액자", new double[] {0.77, 0.13, 0.19, 0.23});
        BEHIND.put("책장", new double[] {0.015, 0.44, 0.25, 0.42});
        BEHIND.put("조명", new double[] {0.73, 0.33, 0.25, 0.51});
        BEHIND.put("책상", new double[] {0.56, 0.52, 0.17, 0.22});
        BEHIND.put("소파", new double[] {0.38, 0.56, 0.26, 0.22});
        BEHIND.put("러그", new double[] {0.13, 0.70, 0.72, 0.29});
        BEHIND.put("쿠션", new double[] {0.19, 0.66, 0.25, 0.22});
    }

    private final Store store;
    public String bubble = "";

    public RoomView(Store store) {
        this.store = store;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        g.clip(new RoundRectangle2D.Double(0, 0, w, h, Theme.IMAGE_RADIUS, Theme.IMAGE_RADIUS));
        if (Images.ROOM != null) {
            g.drawImage(Images.ROOM, 0, 0, w, h, null);
        }
        BEHIND.forEach((name, slot) -> item(g, name, slot, w, h));
        Pet.drawAt(g, store.pet(), (int) (w * 0.495), (int) (h * 0.93), (int) (Math.min(w * 0.43, h * 0.53)));
        item(g, "화분", PLANT, w, h);
        g.setFont(Theme.font(15));
        int textW = g.getFontMetrics().stringWidth(bubble);
        int bx = w / 2 - textW / 2 - 14;
        int by = (int) (h * 0.30);
        g.setColor(Theme.CARD_TOP);
        g.fillRoundRect(bx, by, textW + 28, 34, Theme.BUBBLE_RADIUS, Theme.BUBBLE_RADIUS);
        g.setColor(Theme.CARD_LINE);
        g.drawRoundRect(bx, by, textW + 28, 34, Theme.BUBBLE_RADIUS, Theme.BUBBLE_RADIUS);
        g.setColor(Theme.INK);
        g.drawString(bubble, bx + 14, by + 23);
        g.dispose();
    }

    private void item(Graphics2D g, String name, double[] s, int w, int h) {
        if (!store.placed.contains(name)) {
            return;
        }
        int x = (int) (s[0] * w);
        int y = (int) (s[1] * h);
        int iw = (int) (s[2] * w);
        int ih = (int) (s[3] * h);
        BufferedImage im = Images.item(name);
        if (im != null) {
            Images.fit(g, im, x, y, iw, ih);
            return;
        }
        g.setColor(Theme.MINT_PALE);
        g.fillRoundRect(x, y, iw, ih, 14, 14);
        g.setColor(Theme.MINT_LINE);
        g.drawRoundRect(x, y, iw, ih, 14, 14);
        g.setColor(Theme.INK);
        g.drawString(name + " (임시)", x + 8, y + 20);
    }
}
