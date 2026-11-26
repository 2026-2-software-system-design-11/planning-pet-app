package planningpet.schedule.view;

import planningpet.app.model.Store;
import planningpet.common.view.Theme;
import planningpet.common.view.Ui;
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
    /** 자동 배치로 방금 놓여 진하게 그리는 블록(D-89). */
    private final java.util.Set<Task> flashing = new java.util.HashSet<>();
    /** 진하게 보이는 시간(D-89). */
    public static final int FLASH_MILLIS = 2000;
    private javax.swing.Timer flashTimer;
    /** 열을 나눴거나 제목이 잘린 블록(D-87). */
    private final java.util.Set<Task> narrow = new java.util.HashSet<>();
    LocalDate monday;
    /** 자정 아래 "다음 날" 칸의 바탕(D-77). */
    private static final Color NIGHT = new Color(0x1B, 0x22, 0x38, 14);

    /** D-99: 끄는 중인 일(끌지 않으면 null)과 따라오는 상자의 자리(시간표 밖이면 null). */
    private Task dragging;
    private Spot dragSpot;
    private final TaskController control;
    /** D-99: 놓은 자리 옆 글 상자. */
    public final DropNote note = new DropNote();

    /** 시간표의 한 자리: 날짜와 앱 하루의 분(D-77). */
    public record Spot(LocalDate day, int minute) { }

    public TimetablePanel(Store store, TaskController control) {
        this.store = store;
        this.control = control;
        add(note);
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

    /** 시간표 좌표 p의 날짜와 분. 날 칸 밖이면 null. */
    public Spot spotAt(java.awt.Point p) {
        int w = (getWidth() - GUTTER) / 7;
        boolean out = w <= 0 || p.x < GUTTER || p.x >= GUTTER + 7 * w || p.y < 0 || p.y >= getHeight();
        return out ? null : new Spot(monday.plusDays((p.x - GUTTER) / w), p.y * 60 / HOUR);
    }

    /** D-99: 끌기를 시작하거나(t) 끝낸다(null). 끄는 동안 놓을 수 없는 날을 흐린다. */
    public void drag(Task t) {
        dragging = t;
        dragSpot = null;
        if (t != null) {
            note.close();
        }
        repaint();
    }

    /** D-99: 상자를 시간표 좌표 p로 옮긴다(시간표 밖이면 상자가 없다). 그 자리에 놓을 수 없으면 true. */
    public boolean dragTo(java.awt.Point p) {
        dragSpot = p == null ? null : spotAt(p);
        repaint();
        return dragSpot != null && control.blocked(dragging, dragSpot.day()) != null;
    }

    /** 따라오는 상자의 글: 놓을 시각("14:00~14:30")이나 까닭("지난 날"). 상자가 없으면 null. */
    public String dragText() {
        if (dragging == null || dragSpot == null) {
            return null;
        }
        String why = control.blocked(dragging, dragSpot.day());
        int start = TaskBook.dropStart(dragSpot.minute(), dragging.minutes);
        return why != null ? why : TaskBook.spanText(start, start + dragging.minutes);
    }

    /** D-99: 놓은 결과 글을 놓인 블록의 날 칸 옆에, 놓지 못했으면 손을 놓은 자리 at 옆에 보인다. 물음에서 취소했으면 없다. */
    void dropped(TaskController.Drop r, Task t, java.awt.Point at) {
        int day = (getWidth() - GUTTER) / 7;
        int x = GUTTER + (int) (t.date.toEpochDay() - monday.toEpochDay()) * day;
        if (r.text() != null && r.undo() == null) {
            note.open(r.text(), null, at.x + 12, at.y + 12, at.x - 12);
        } else if (r.text() != null) {
            int top = t.startMinute() * HOUR / 60;
            // S148: 당겨진 블록의 위 끝이 보이는 칸 위면 시간표를 굴려 블록 위 끝과 글 상자를 함께 보인다.
            java.awt.Rectangle seen = getVisibleRect();
            if (top < seen.y) {
                scrollRectToVisible(new java.awt.Rectangle(0, top, 1, seen.height));
            }
            note.open(r.text(), r.undo(), x + day + 2, top, x - 2);
        }
    }

    /** 끄는 동안 굴리는 보이는 칸 위, 아래 끝 띠의 폭(px). */
    public static final int EDGE = 30;
    /** 굴리다 남은 1px 아래 양. */
    private double rollRest;

    /**
     * D-99: 끄는 동안 보이는 칸 높이 height에서 마우스 높이 y일 때 굴리는 빠르기(1초에 몇 분, 아래로 +, 위로 -).
     * 끝 띠 안쪽 경계에서 60분, 끝에서 240분이고 그 사이는 끝에 가까울수록 빠르다(S149). 끝 밖(머리줄 위, 시간표 아래 밖)은 240분(S152).
     */
    public static double rollSpeed(int y, int height) {
        int d = y < EDGE ? EDGE - y : y > height - EDGE ? y - (height - EDGE) : 0;
        return d == 0 ? 0 : (y < EDGE ? -1 : 1) * (60 + 180.0 * Math.min(d, EDGE) / EDGE);
    }

    /**
     * 끄는 동안 마우스가 보이는 칸 좌표 inView에 millis 동안 있었으면 굴리고, 그 자리의 시간표 좌표(보이는 칸 밖이면 null)를 돌려준다.
     * 마우스가 시간표(시각 글 칸 포함)의 가로 범위 밖(목록 위)이면 굴리지 않는다(S152).
     */
    public java.awt.Point roll(java.awt.Point inView, int millis) {
        javax.swing.JViewport view = (javax.swing.JViewport) getParent();
        if (inView.x >= 0 && inView.x < view.getWidth()) {
            rollRest += rollSpeed(inView.y, view.getHeight()) * HOUR / 60 * millis / 1000;
        } else {
            rollRest = 0;
        }
        int step = (int) rollRest;
        rollRest -= step;
        int y = view.getViewPosition().y + step;
        view.setViewPosition(new java.awt.Point(0, Math.max(0, Math.min(getHeight() - view.getHeight(), y))));
        java.awt.Point p = new java.awt.Point(inView.x + view.getViewPosition().x, inView.y + view.getViewPosition().y);
        return getVisibleRect().contains(p) ? p : null;
    }

    /** 따라오는 상자의 크기: 날 칸 폭과 걸리는 시간만 한 높이. */
    public java.awt.Dimension boxSize(Task t) {
        return new java.awt.Dimension((getWidth() - GUTTER) / 7 - 2, Math.max(t.minutes * HOUR / 60, 16));
    }

    /** 끄는 동안 흐리게 그리는 날(놓을 수 없는 날). */
    public java.util.List<LocalDate> dimmed() {
        return dragging == null ? java.util.List.of()
                : java.util.stream.IntStream.range(0, 7).mapToObj(monday::plusDays).filter(d -> control.blocked(dragging, d) != null).toList();
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

    /** 메모가 있는 블록에 마우스를 올리면 메모를 보인다(D-33). 좁은 블록은 전체 제목과 시각을 먼저 보인다(D-87). */
    @Override
    public String getToolTipText(MouseEvent e) {
        Task t = at(e);
        return t == null ? null : tip(t);
    }

    /** 놓인 블록을 2초 동안 진하게 그린다(D-89). */
    public void flash(java.util.Collection<Task> placed) {
        flashing.clear();
        flashing.addAll(placed);
        if (flashTimer != null) {
            flashTimer.stop();
        }
        flashTimer = new javax.swing.Timer(FLASH_MILLIS, e -> {
            flashing.clear();
            repaint();
        });
        flashTimer.setRepeats(false);
        flashTimer.start();
        repaint();
    }

    /** 진하게 보이기를 끝내는 타이머. 아직 없으면 null. */
    public javax.swing.Timer flashTimer() {
        return flashTimer;
    }

    /** 지금 진하게 그리는 블록. */
    public java.util.Set<Task> flashing() {
        return java.util.Collections.unmodifiableSet(flashing);
    }

    /** 블록의 툴팁. 마지막으로 그린 모양을 따른다. */
    public String tip(Task t) {
        String memo = TaskRows.memoTip(t);
        if (!narrow.contains(t)) {
            return memo;
        }
        String head = "<b>" + Ui.esc(t.title) + "</b> " + TaskBook.timeText(t);
        String body = t.memo.isBlank() ? "" : "<br>" + Ui.esc(t.memo.length() > 200 ? t.memo.substring(0, 200) + "…" : t.memo)
                .replace("\n", "<br>");
        return "<html><div style='width:280px'>" + head + body + "</div></html>";
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
        narrow.clear();
        for (int i = 0; i < 7; i++) {
            var tasks = store.book.timedOn(monday.plusDays(i));
            var columns = Planner.columns(tasks);
            for (Task t : tasks) {
                int[] c = columns.get(t);
                int w = day / c[1];
                Rectangle r = new Rectangle(GUTTER + i * day + c[0] * w + 1, t.startMinute() * HOUR / 60,
                        w - 2, Math.max(t.minutes * HOUR / 60, 16));
                blocks.put(r, t);
                boolean lit = flashing.contains(t);
                g.setColor(lit ? Theme.MINT_SELECTED : t.done ? Theme.DAY : t.kind == Task.Kind.FIXED ? Theme.TRACK : Theme.MINT_SOFT);
                g.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
                g.setColor(lit ? Theme.MINT_DEEP : t.done ? Theme.CARD_LINE : t.kind == Task.Kind.FIXED ? Theme.CARD_LINE : Theme.MINT_LINE);
                if (lit) {
                    g.setStroke(new java.awt.BasicStroke(2.5f));
                }
                g.drawRoundRect(r.x, r.y, r.width - 1, r.height - 1, 10, 10);
                g.setStroke(new java.awt.BasicStroke(1f));
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
                if (c[1] > 1 || text.getFontMetrics().stringWidth(t.title) > r.x + r.width - tx - 2) {
                    narrow.add(t);
                }
                text.drawString(t.title, tx, r.y + 14);
                if (t.done) {
                    text.drawLine(tx, r.y + 10, tx + text.getFontMetrics().stringWidth(t.title), r.y + 10);
                }
                text.dispose();
            }
        }
        paintDrag(g, day);
        g.dispose();
    }

    /** D-99: 놓을 수 없는 날을 회색으로 흐리고, 놓일 자리에 반투명 상자와 글을 그린다(놓을 수 없으면 회색 상자와 까닭). */
    private void paintDrag(Graphics2D g, int day) {
        if (dragging == null) {
            return;
        }
        g.setColor(new Color(0x8A, 0x96, 0xA0, 70));
        for (LocalDate d : dimmed()) {
            g.fillRect(GUTTER + (int) (d.toEpochDay() - monday.toEpochDay()) * day, 0, day, getHeight());
        }
        String text = dragText();
        if (text == null) {
            return;
        }
        boolean blocked = control.blocked(dragging, dragSpot.day()) != null;
        int start = TaskBook.dropStart(dragSpot.minute(), dragging.minutes);
        Rectangle r = new Rectangle(GUTTER + (int) (dragSpot.day().toEpochDay() - monday.toEpochDay()) * day + 1,
                start * HOUR / 60, day - 2, Math.max(dragging.minutes * HOUR / 60, 16));
        g.setColor(blocked ? new Color(0xB0, 0xB8, 0xC0, 150) : new Color(0xC7, 0xF0, 0xE4, 170));
        g.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
        g.setColor(blocked ? Theme.INK_DIM : Theme.MINT_DEEP);
        g.drawRoundRect(r.x, r.y, r.width - 1, r.height - 1, 10, 10);
        g.setColor(blocked ? Theme.INK_SUB : Theme.INK);
        g.setFont(Theme.font(11));
        g.drawString(text, r.x + 4, r.y + 13);
    }
}
