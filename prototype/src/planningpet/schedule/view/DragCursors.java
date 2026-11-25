package planningpet.schedule.view;

import planningpet.common.view.Theme;
import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/** D-99 끌어 놓기의 마우스 모양: 줄 위의 펼친 손, 끄는 동안 쥔 손, 놓을 수 없는 날 위의 금지. 창이 없는 환경이면 비슷한 기본 모양이다. */
public final class DragCursors {
    public static final Cursor OPEN = make("펼친 손", Cursor.HAND_CURSOR, 0);
    public static final Cursor GRAB = make("쥔 손", Cursor.MOVE_CURSOR, 1);
    public static final Cursor NO = make("금지", Cursor.DEFAULT_CURSOR, 2);

    private DragCursors() {
    }

    private static Cursor make(String name, int fallback, int shape) {
        try {
            BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setStroke(new BasicStroke(1.5f));
            if (shape == 2) {
                g.setColor(java.awt.Color.WHITE);
                g.fillOval(4, 4, 22, 22);
                g.setColor(Theme.INK_SUB);
                g.setStroke(new BasicStroke(3f));
                g.drawOval(5, 5, 20, 20);
                g.drawLine(9, 21, 21, 9);
            } else {
                int finger = shape == 0 ? 10 : 4;
                for (int i = 0; i < 4; i++) {
                    hand(g, 8 + i * 4, 14 - finger, 4, finger + 4);
                }
                if (shape == 0) {
                    hand(g, 3, 15, 6, 4);
                }
                hand(g, 7, 13, 17, 12);
            }
            g.dispose();
            return java.awt.Toolkit.getDefaultToolkit().createCustomCursor(img, new Point(15, 15), name);
        } catch (java.awt.HeadlessException | IndexOutOfBoundsException e) {
            return Cursor.getPredefinedCursor(fallback);
        }
    }

    /** 흰 바탕에 진한 테두리의 둥근 손 조각. */
    private static void hand(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(java.awt.Color.WHITE);
        g.fillRoundRect(x, y, w, h, 4, 4);
        g.setColor(Theme.INK);
        g.drawRoundRect(x, y, w, h, 4, 4);
    }
}
