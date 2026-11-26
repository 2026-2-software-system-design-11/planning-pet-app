package planningpet.schedule.view;

import planningpet.common.view.Theme;
import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/** D-99: 시간표 밖에 놓으면 상자가 손을 놓은 자리에서 목록의 그 줄 자리로 짧게 돌아가며 사라진다(S65). 창의 끄는 층에 그린다. */
public final class DragReturn extends JComponent {
    public static final int MILLIS = 200;
    /** 창의 끄는 층 좌표에서 처음 자리(손을 놓은 자리)와 끝 자리(목록의 그 줄). */
    public final Rectangle from;
    public final Rectangle to;
    public final Timer timer = new Timer(15, e -> step());
    private float done;

    private DragReturn(Rectangle from, Rectangle to) {
        this.from = from;
        this.to = to;
    }

    /** row 좌표 at에서 box 크기의 상자를 row 자리로 돌려보낸다. 창이 없으면(끄는 층이 없으면) 하지 않고 null. */
    static DragReturn start(JComponent row, Point at, Dimension box) {
        JRootPane root = SwingUtilities.getRootPane(row);
        if (root == null) {
            return null;
        }
        JLayeredPane layer = root.getLayeredPane();
        Point p = SwingUtilities.convertPoint(row, at, layer);
        DragReturn r = new DragReturn(new Rectangle(p.x - box.width / 2, p.y - box.height / 2, box.width, box.height),
                SwingUtilities.convertRectangle(row.getParent(), row.getBounds(), layer));
        r.setBounds(0, 0, layer.getWidth(), layer.getHeight());
        layer.add(r, JLayeredPane.DRAG_LAYER);
        r.timer.start();
        return r;
    }

    private void step() {
        done = Math.min(1f, done + 15f / MILLIS);
        if (done >= 1f) {
            timer.stop();
            java.awt.Container layer = getParent();
            if (layer != null) {
                layer.remove(this);
                layer.repaint();
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f - done * 0.7f));
        int x = Math.round(from.x + (to.x - from.x) * done);
        int y = Math.round(from.y + (to.y - from.y) * done);
        int w = Math.round(from.width + (to.width - from.width) * done);
        int h = Math.round(from.height + (to.height - from.height) * done);
        g.setColor(new java.awt.Color(0xC7, 0xF0, 0xE4, 170));
        g.fillRoundRect(x, y, w, h, 10, 10);
        g.setColor(Theme.MINT_DEEP);
        g.drawRoundRect(x, y, w - 1, h - 1, 10, 10);
        g.dispose();
    }
}
