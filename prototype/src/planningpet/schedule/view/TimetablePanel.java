package planningpet.schedule.view;

import planningpet.app.model.Store;
import planningpet.common.view.Theme;
import planningpet.schedule.controller.TaskController;
import planningpet.schedule.model.*;
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
import javax.swing.SwingUtilities;

/** 일정관리의 일주일 시간표(05 일정관리). 0시부터 다음 날 4시(28:00)까지 그린다(D-77). */
public final class TimetablePanel extends JComponent {
    public static final int HOUR = 44;
    public static final int GUTTER = 52;
    /** 그리는 시간 수(D-77): 0시부터 다음 날 4시까지. */
    public static final int HOURS = Task.DAY_END / 60;
    private final Store store;
    private final Map<Rectangle, Task> blocks = new LinkedHashMap<>();
    private final Map<Rectangle, Task> boxes = new LinkedHashMap<>();
    LocalDate monday;
    /** 자정 아래 "다음 날" 칸의 바탕(D-77). */
    private static final Color NIGHT = new Color(0x1B, 0x22, 0x38, 14);

    public TimetablePanel(Store store, TaskController control) {
        this.store = store;
        setPreferredSize(new Dimension(300, HOUR * HOURS));
        setToolTipText("");
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 체크박스는 왼쪽 버튼 한 번으로만 바뀐다. 나중에 그린(위에 있는) 블록의 것을 먼저 본다.
                Task boxed = null;
                for (Map.Entry<Rectangle, Task> b : boxes.entrySet()) {
                    if (b.getKey().contains(e.getPoint())) {
                        boxed = b.getValue();
                    }
                }
                if (boxed != null && SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
                    control.check(boxed);
                    return;
                }
                if (boxed != null && e.getClickCount() == 1) {
                    return;
                }
                Task t = at(e);
                if (t != null && e.getClickCount() == 2) {
                    control.edit(t);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                Task t = at(e);
                if (t != null && e.isPopupTrigger()) {
                    TaskMenu.of(control, t).show(TimetablePanel.this, e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mousePressed(e);
            }
        });
    }

    /** 마지막으로 그린 블록과 체크박스 자리. 블라인드 검사가 번호를 찍는다. */
    public Map<Rectangle, Task> blockAreas() {
        return blocks;
    }

    public Map<Rectangle, Task> boxAreas() {
        return boxes;
    }

    private Task at(MouseEvent e) {
        Task hit = null;
        for (Map.Entry<Rectangle, Task> b : blocks.entrySet()) {
            if (b.getKey().contains(e.getPoint())) {
                hit = b.getValue();
            }
        }
        return hit;
    }

    /** 메모가 있는 블록에 마우스를 올리면 메모를 보인다(D-33). */
    @Override
    public String getToolTipText(MouseEvent e) {
        Task t = at(e);
        return t == null ? null : TaskRows.memoTip(t);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        int day = (getWidth() - GUTTER) / 7;
        g.setColor(Theme.CARD_TOP);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        int today = (int) (store.today().toEpochDay() - monday.toEpochDay());
        if (today >= 0 && today < 7) {
            g.setColor(Theme.DAY);
            g.fillRect(GUTTER + today * day, 0, day, getHeight());
        }
        // D-77: 자정 아래 네 시간은 그날 밤에 이어진 "다음 날" 칸이다. 옅게 칠하고 시각을 00:00부터 다시 적는다.
        g.setColor(NIGHT);
        g.fillRect(GUTTER, 24 * HOUR, getWidth() - GUTTER, (HOURS - 24) * HOUR);
        for (int h = 0; h < HOURS; h++) {
            g.setColor(Theme.CARD_LINE);
            g.drawLine(GUTTER, h * HOUR, getWidth(), h * HOUR);
            g.setColor(Theme.INK_DIM);
            g.drawString(String.format("%02d:00", h % 24), 6, h * HOUR + 14);
        }
        Graphics2D mid = (Graphics2D) g.create();
        mid.setColor(Theme.INK_SUB);
        mid.drawLine(GUTTER, 24 * HOUR, getWidth(), 24 * HOUR);
        mid.setFont(Theme.font(11));
        mid.drawString("자정", 6, 24 * HOUR - 4);
        mid.drawString("다음 날", 6, 24 * HOUR + 28);
        mid.dispose();
        blocks.clear();
        boxes.clear();
        for (int i = 0; i < 7; i++) {
            var tasks = store.book.timedOn(monday.plusDays(i));
            var columns = Planner.columns(tasks);
            for (Task t : tasks) {
                int[] c = columns.get(t);
                int w = day / c[1];
                Rectangle r = new Rectangle(GUTTER + i * day + c[0] * w + 1, t.startMinute() * HOUR / 60,
                        w - 2, Math.max(t.minutes * HOUR / 60, 16));
                blocks.put(r, t);
                g.setColor(t.done ? Theme.DAY : t.kind == Task.Kind.FIXED ? Theme.TRACK : Theme.MINT_SOFT);
                g.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
                g.setColor(t.done ? Theme.CARD_LINE : t.kind == Task.Kind.FIXED ? Theme.CARD_LINE : Theme.MINT_LINE);
                g.drawRoundRect(r.x, r.y, r.width - 1, r.height - 1, 10, 10);
                Graphics2D text = (Graphics2D) g.create();
                text.clipRect(r.x, r.y, r.width, r.height);
                text.setColor(t.done ? Theme.INK_DIM : Theme.INK);
                int tx = r.x + 4;
                {
                    // D-35: 고정 일정 블록에도 체크박스를 둔다.
                    Rectangle box = new Rectangle(r.x + 4, r.y + 4, 12, 12).intersection(r);
                    boxes.put(box, t);
                    text.setColor(Theme.CARD_TOP);
                    text.fill(box);
                    text.setColor(Theme.MINT_LINE);
                    text.draw(box);
                    if (t.done) {
                        text.setColor(Theme.MINT);
                        text.drawPolyline(new int[] {box.x + 2, box.x + 5, box.x + 10}, new int[] {box.y + 6, box.y + 9, box.y + 3}, 3);
                    }
                    text.setColor(t.done ? Theme.INK_DIM : Theme.INK);
                    tx += 16;
                }
                text.drawString(t.title, tx, r.y + 14);
                if (t.done) {
                    text.drawLine(tx, r.y + 10, tx + text.getFontMetrics().stringWidth(t.title), r.y + 10);
                }
                text.dispose();
            }
        }
        g.dispose();
    }
}
