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
        this.preview = preview;
        setLayout(null);
    }

    private PetState petOf(int i, int n) {
        return n > 1 ? store.pet(i == 0 ? "CAT" : "DOG") : store.pets.get(0);
    }

    /** i번째 펫의 {가운데 x, 바닥 y, 상자 한 변}(방 그림 좌표). */
    private int[] spot(int i, int n, int w, int h, int visibleH) {
        // 디자이너 제안(README 3절): 바닥 y 0.82, 상자 한 변 min(칸 폭 0.165, 칸 높이 0.62), 두 마리면 x 0.42와 0.58.
        int ground = Math.min((int) (h * 0.82), visibleH - 8);
        int size = (int) Math.min(getWidth() * 0.165, getHeight() * 0.62);
        int px = (int) (w * (n > 1 ? (i == 0 ? 0.42 : 0.58) : 0.50));
        return new int[] {px, ground, size};
    }

    /** 방 그림이 칸을 덮을 때의 그림 상자 {x0, y0, w, h}. */
    private int[] frame() {
        int cw = getWidth();
        int ch = getHeight();
        if (Images.ROOM == null) {
            return new int[] {0, 0, cw, ch};
        }
        double scale = Math.max((double) cw / Images.ROOM.getWidth(), (double) ch / Images.ROOM.getHeight());
        int w = (int) Math.ceil(Images.ROOM.getWidth() * scale);
        int h = (int) Math.ceil(Images.ROOM.getHeight() * scale);
        // 넘치는 높이는 위에서 더 많이 자른다(바닥 쪽에 펫과 아이템이 있다).
        return new int[] {(cw - w) / 2, (int) ((ch - h) * 0.65), w, h};
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        int cw = getWidth();
        int ch = getHeight();
        g.clip(new RoundRectangle2D.Double(0, 0, cw, ch, Theme.IMAGE_RADIUS, Theme.IMAGE_RADIUS));
        int[] f = frame();
        if (Images.ROOM != null) {
            g.drawImage(Images.ROOM, f[0], f[1], f[2], f[3], null);
        }
        g.translate(f[0], f[1]);
        int w = f[2];
        int h = f[3];
        BEHIND.forEach((name, slot) -> item(g, name, slot, w, h));
        if (!preview) {
            pets(g, w, h, ch - f[1]);
        }
        FRONT.forEach((name, slot) -> item(g, name, slot, w, h));
        g.dispose();
    }

    /** 펫 한 마리면 가운데, 두 마리면 고양이 왼쪽, 강아지 오른쪽이다. 고른 펫 위에 말풍선을 둔다. */
    private void pets(Graphics2D g, int w, int h, int visibleH) {
        int n = store.pets.size();
        for (int i = 0; i < n; i++) {
            PetState p = petOf(i, n);
            int[] at = spot(i, n, w, h, visibleH);
            int px = at[0];
            int ground = at[1];
            int size = at[2];
            g.setColor(new java.awt.Color(0x9A, 0x6A, 0x45, 40));
            g.fillOval(px - size * 3 / 10, ground - size / 22, size * 3 / 5, size / 11);
            Pet.drawAt(g, p, px, ground, size);
            if (p == store.pet()) {
                int top = ground - Pet.drawnHeight(p, size) * 9 / 10;
                Icons.draw(g, Icons.Kind.HEART, px - size * 11 / 20, top + size / 14, size / 6, Theme.HEART);
                Icons.draw(g, Icons.Kind.HEART, px - size * 7 / 16, top - size / 20, size / 9, Theme.HEART);
                Icons.draw(g, Icons.Kind.SPARK, px + size * 3 / 10, top - size / 30, size / 5, Theme.SPARK);
                bubble(g, px + size / 5, top - 4, w);
            }
        }
    }

    /** 말풍선(디자인 시스템 5절): 흰 바탕, 옅은 그림자, 꼬리가 아래의 펫을 가리킨다. 글이 없으면 하트 하나다(D-56). */
    private void bubble(Graphics2D g, int tipX, int tipY, int roomW) {
        g.setFont(Theme.bold(14));
        java.awt.FontMetrics fm = g.getFontMetrics();
        boolean words = bubble != null && !bubble.isEmpty();
        int heart = words ? 14 : 22;
        int bw = words ? fm.stringWidth(bubble) + heart + 8 + 32 : 44;
        int bh = words ? 36 : 40;
        int bx = Math.max(10, Math.min(roomW - bw - 10, tipX - (words ? bw / 2 : 8)));
        int by = tipY - 10 - bh;
        int r = words ? Theme.BUBBLE_RADIUS : bh;
        Card.shadow(g, bx, by, bw, bh, r);
        java.awt.Polygon tail = new java.awt.Polygon(new int[] {tipX - 7, tipX + 7, tipX}, new int[] {by + bh - 3, by + bh - 3, tipY}, 3);
        g.setColor(Theme.CARD_TOP);
        g.fillRoundRect(bx, by, bw, bh, r, r);
        g.fill(tail);
        g.setColor(Theme.CARD_EDGE);
        g.drawRoundRect(bx, by, bw, bh, r, r);
        if (words) {
            Icons.draw(g, Icons.Kind.HEART, bx + 16, by + (bh - heart) / 2, heart, Theme.HEART);
            g.setColor(Theme.INK);
            g.drawString(bubble, bx + 16 + heart + 8, by + (bh + fm.getAscent() - fm.getDescent()) / 2);
        } else {
            Icons.draw(g, Icons.Kind.HEART, bx + (bw - heart) / 2, by + (bh - heart) / 2 + 1, heart, Theme.HEART);
        }
    }

    private void item(Graphics2D g, String name, double[] s, int w, int h) {
        if (!store.placed.contains(name) && !name.equals(previewItem)) {
            return;
        }
        int x = (int) (s[0] * w);
        int y = (int) (s[1] * h);
        int iw = (int) (s[2] * w);
        int ih = (int) (s[3] * h);
        BufferedImage im = Images.item(name);
        if (im != null) {
            if ("걸이 화분".equals(name)) {
                Images.fitTop(g, im, x, y, iw, ih);
            } else if (HANGING.contains(name)) {
                Images.fit(g, im, x, y, iw, ih);
            } else {
                Images.fitBottom(g, im, x, y, iw, ih);
            }
            return;
        }
        g.setColor(new java.awt.Color(255, 255, 255, 200));
        g.fillRoundRect(x, y, iw, ih, 14, 14);
        g.setColor(Theme.MINT_LINE);
        g.drawRoundRect(x, y, iw, ih, 14, 14);
        int s2 = Math.min(iw, ih) / 2;
        Icons.draw(g, Icons.Kind.BOX, x + (iw - s2) / 2, y + (ih - s2) / 2 - 6, s2, Theme.ICON);
        g.setFont(Theme.bold(12));
        g.setColor(Theme.INK);
        g.drawString(name, x + (iw - g.getFontMetrics().stringWidth(name)) / 2, y + ih - 8);
    }
}
