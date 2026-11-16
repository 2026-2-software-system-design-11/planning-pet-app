package planningpet.proto;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Map;
import javax.swing.JComponent;

/** pet 화면의 방. 아이템마다 자리가 정해져 있다(D-13). 그림은 임시이고 Q-28에서 정한다. */
public final class RoomView extends JComponent {
    /** 아이템 자리: 방 너비와 높이에 대한 비율 {x, y, w, h}. 러그를 먼저 그린다. */
    private static final Map<String, double[]> SLOTS = Map.of(
            "러그", new double[] {0.30, 0.80, 0.36, 0.10},
            "액자", new double[] {0.14, 0.10, 0.10, 0.16},
            "조명", new double[] {0.91, 0.36, 0.05, 0.34},
            "책장", new double[] {0.78, 0.38, 0.11, 0.42},
            "책상", new double[] {0.04, 0.50, 0.18, 0.26},
            "화분", new double[] {0.24, 0.60, 0.06, 0.16},
            "소파", new double[] {0.56, 0.56, 0.20, 0.20},
            "쿠션", new double[] {0.34, 0.72, 0.08, 0.08});
    private final Store store;
    String bubble = "";

    public RoomView(Store store) {
        this.store = store;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        g.setColor(new Color(0xFBF3E4));
        g.fillRect(0, 0, w, h);
        g.setColor(new Color(0xE8D5B5));
        g.fillRect(0, h * 7 / 10, w, h * 3 / 10);
        g.setColor(new Color(0xCFE6F5));
        g.fillRect(w * 62 / 100, h * 8 / 100, w * 16 / 100, h * 24 / 100);
        g.setColor(Color.GRAY);
        g.drawRect(w * 62 / 100, h * 8 / 100, w * 16 / 100, h * 24 / 100);
        g.drawString("창문", w * 62 / 100 + 6, h * 8 / 100 + 16);
        if (store.placed.contains("러그")) {
            item(g, "러그", w, h);
        }
        store.placed.stream().filter(i -> !i.equals("러그")).forEach(i -> item(g, i, w, h));
        int petX = w * 48 / 100;
        int ground = h * 84 / 100;
        g.translate(petX, ground);
        g.scale(2, 2);
        Pet.draw(g, store.petKind, 0, 0, Rules.stage(Rules.level(store.exp)), false);
        g.scale(0.5, 0.5);
        g.translate(-petX, -ground);
        int textW = g.getFontMetrics().stringWidth(bubble);
        g.setColor(Color.WHITE);
        g.fillRoundRect(petX - textW / 2 - 10, ground - 150, textW + 20, 28, 12, 12);
        g.setColor(Color.GRAY);
        g.drawRoundRect(petX - textW / 2 - 10, ground - 150, textW + 20, 28, 12, 12);
        g.setColor(Color.DARK_GRAY);
        g.drawString(bubble, petX - textW / 2, ground - 131);
        g.dispose();
    }

    private static void item(Graphics2D g, String name, int w, int h) {
        double[] s = SLOTS.get(name);
        int x = (int) (s[0] * w);
        int y = (int) (s[1] * h);
        g.setColor(new Color(0xD9C4F0));
        g.fillRoundRect(x, y, (int) (s[2] * w), (int) (s[3] * h), 10, 10);
        g.setColor(new Color(0x6B5B95));
        g.drawRoundRect(x, y, (int) (s[2] * w), (int) (s[3] * h), 10, 10);
        g.drawString(name, x + 4, y + 15);
    }
}
