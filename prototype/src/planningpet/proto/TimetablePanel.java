package planningpet.proto;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JComponent;

/** 일정관리의 일주일 시간표(05 일정관리). 0시부터 24시까지 그린다. */
public final class TimetablePanel extends JComponent {
    public static final int HOUR = 44;
    public static final int GUTTER = 52;
    private final Store store;
    private final Map<Rectangle, Task> blocks = new LinkedHashMap<>();
    LocalDate monday;

    public TimetablePanel(Store store, Actions actions) {
        this.store = store;
        setPreferredSize(new Dimension(300, HOUR * 24));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Task t = at(e);
                if (t != null && e.getClickCount() == 2) {
                    actions.edit(t);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                Task t = at(e);
                if (t != null && e.isPopupTrigger()) {
                    actions.menu(t).show(TimetablePanel.this, e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mousePressed(e);
            }
        });
    }

    private Task at(MouseEvent e) {
        return blocks.entrySet().stream().filter(b -> b.getKey().contains(e.getPoint()))
                .map(Map.Entry::getValue).findFirst().orElse(null);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        int day = (getWidth() - GUTTER) / 7;
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        int today = (int) (store.today().toEpochDay() - monday.toEpochDay());
        if (today >= 0 && today < 7) {
            g.setColor(new Color(0xFFFBE6));
            g.fillRect(GUTTER + today * day, 0, day, getHeight());
        }
        for (int h = 0; h < 24; h++) {
            g.setColor(new Color(0xE6E6E6));
            g.drawLine(GUTTER, h * HOUR, getWidth(), h * HOUR);
            g.setColor(Color.GRAY);
            g.drawString(String.format("%02d:00", h), 6, h * HOUR + 14);
        }
        blocks.clear();
        for (int i = 0; i < 7; i++) {
            var tasks = store.timedOn(monday.plusDays(i));
            var columns = Planner.columns(tasks);
            for (Task t : tasks) {
                int[] c = columns.get(t);
                int w = day / c[1];
                Rectangle r = new Rectangle(GUTTER + i * day + c[0] * w + 1, t.startMinute() * HOUR / 60,
                        w - 2, Math.max(t.minutes * HOUR / 60, 16));
                blocks.put(r, t);
                g.setColor(t.done ? new Color(0xE6E6E6) : t.kind == Task.Kind.FIXED ? new Color(0xD6E4F5) : new Color(0xFFF1C9));
                g.fill(r);
                g.setColor(t.done ? Color.GRAY : Color.DARK_GRAY);
                g.setClip(r);
                g.drawString(t.title, r.x + 4, r.y + 14);
                if (t.done) {
                    g.drawLine(r.x + 4, r.y + 10, r.x + 4 + g.getFontMetrics().stringWidth(t.title), r.y + 10);
                }
                g.setClip(null);
            }
        }
        g.dispose();
    }
}
