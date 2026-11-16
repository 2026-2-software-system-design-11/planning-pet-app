package planningpet.proto;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;

/** 성취도 막대(D-22, 06의 13번). 오늘 할 일을 끝낸 비율만큼 펫이 걸어가 있다. */
public final class WalkBar extends JComponent {
    private final Store store;

    public WalkBar(Store store) {
        this.store = store;
        store.onChange(this::repaint);
        setPreferredSize(new Dimension(600, 72));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        var today = store.forDate(store.today());
        long done = today.stream().filter(t -> t.done).count();
        int left = 70;
        int right = getWidth() - 60;
        int y = getHeight() - 16;
        g.setColor(Theme.INK);
        g.setFont(Theme.bold(14));
        g.drawString("성취도", 8, y + 5);
        g.drawString(today.isEmpty() ? "-" : done + "/" + today.size(), right + 14, y + 5);
        g.setStroke(new BasicStroke(3));
        g.setColor(Theme.TRACK);
        g.drawLine(left, y, right, y);
        double ratio = today.isEmpty() ? 0 : done / (double) today.size();
        int catX = left + (int) ((right - left) * ratio);
        g.setColor(Theme.MINT);
        g.drawLine(left, y, catX, y);
        for (int i = 1; i <= today.size(); i++) {
            int x = left + (right - left) * i / today.size();
            g.setColor(i <= done ? Theme.MINT : Color.WHITE);
            g.fillOval(x - 5, y - 5, 10, 10);
            g.setColor(Theme.MINT);
            g.drawOval(x - 5, y - 5, 10, 10);
        }
        Pet.drawAt(g, store, catX, y - 6, 56);
        g.dispose();
    }
}
