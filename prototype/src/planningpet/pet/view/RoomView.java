package planningpet.pet.view;

import planningpet.app.model.Store;
import planningpet.common.view.*;
import planningpet.pet.model.PetState;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JComponent;

/**
 * 나의 방의 방(D-13, D-52). 아이템마다 자리가 정해져 있다. 자리와 그림은 디자인 시스템 6절을 따른다.
 * 펫은 함께 키우는 펫 모두를 그리고(D-48), 고른 펫 머리 위에 하트 말풍선을 둔다(D-56).
 * 펫 칩과 화살표처럼 방 위에 얹는 부품은 이 부품의 자식으로 넣고 layout이 자리를 잡는다.
 */
public final class RoomView extends JComponent {
    /**
     * 아이템 자리: 방 그림(room.png) 너비와 높이에 대한 비율 {x, y, w, h}. 넣은 순서대로 그린다(뒤 벽부터).
     * 바닥에 서는 것은 상자 아래에 붙이고(fitBottom), 벽에 거는 것(액자, 걸이 화분)은 상자 가운데에 맞춘다.
     */
    static final Map<String, double[]> BEHIND = new LinkedHashMap<>();
    /** 펫보다 앞에 그리는 것. */
    static final Map<String, double[]> FRONT = new LinkedHashMap<>();
    private static final java.util.Set<String> HANGING = java.util.Set.of("액자", "걸이 화분");

    static {
        // 디자이너 제안(ppa-design-work/art-room/README.md 3절): 모두 펫보다 뒤, 적힌 순서로 그리면 서로 가리지 않는다.
        BEHIND.put("액자", new double[] {0.735, 0.30, 0.07, 0.12});
        BEHIND.put("걸이 화분", new double[] {0.60, 0.26, 0.075, 0.17});
        BEHIND.put("책장", new double[] {0.01, 0.36, 0.15, 0.26});
        BEHIND.put("조명", new double[] {0.165, 0.40, 0.055, 0.23});
        BEHIND.put("책상", new double[] {0.225, 0.49, 0.11, 0.14});
        BEHIND.put("소파", new double[] {0.655, 0.47, 0.17, 0.17});
        BEHIND.put("캣타워", new double[] {0.86, 0.39, 0.13, 0.27});
        BEHIND.put("우드 침대", new double[] {0.00, 0.60, 0.19, 0.20});
        BEHIND.put("화분", new double[] {0.80, 0.56, 0.08, 0.15});
        BEHIND.put("러그", new double[] {0.22, 0.63, 0.56, 0.22});
        BEHIND.put("쿠션", new double[] {0.79, 0.70, 0.085, 0.10});
        BEHIND.put("털실 공", new double[] {0.235, 0.74, 0.05, 0.07});
        BEHIND.put("뼈다귀", new double[] {0.70, 0.78, 0.06, 0.04});
    }

    private final Store store;
    /** 미리보기(D-53)면 펫과 말풍선을 그리지 않고 이 아이템을 놓은 것처럼 그린다. */
    private final boolean preview;
    public String previewItem;
    /** 아이템을 사거나 펫을 데려온 직후 한 번만 하는 말(D-56). 비면 하트 말풍선이다. */
    public String bubble = "";

    public RoomView(Store store) {
        this(store, false);
    }

    public RoomView(Store store, boolean preview) {
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
