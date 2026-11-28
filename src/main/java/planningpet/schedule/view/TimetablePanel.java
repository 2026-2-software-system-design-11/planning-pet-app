package planningpet.schedule.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import planningpet.app.model.AppData;
import planningpet.common.view.Theme;
import planningpet.common.view.Ui;
import planningpet.reward.controller.RewardController;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Slot;
import planningpet.schedule.model.Task;
import planningpet.schedule.model.TimetableLayout;

/**
 * 일정관리의 일주일 시간표(07, 05 일정관리). 0시부터 다음 날 4시(28:00)까지 그린다(D-77). 옮기는 몫은 docs/porting/schedule.md
 * S5b와 S5c2(누르기). 끌어 놓기는 C2, 자동 배치 뒤 진하게 보이기는 S5d가 더한다.
 */
public final class TimetablePanel extends JComponent {
    /** 한 시간의 높이. */
    public static final int HOUR = 44;
    /** 왼쪽 시각 글 칸의 폭. */
    public static final int GUTTER = 52;
    /** 그리는 시간 수(D-77): 0시부터 다음 날 4시까지. */
    public static final int HOURS = Task.DAY_END / 60;
    /** 자정 아래 "다음 날" 칸의 바탕(D-77). */
    private static final Color NIGHT = new Color(0x1B, 0x22, 0x38, 14);
    private final AppData data;
    /** 보이는 주의 첫날(D-91). 일정관리 화면(S5c)이 주를 옮길 때 정한다. */
    LocalDate monday;
    /** 마지막으로 그린 블록과 체크 칸의 자리. 누르기와 툴팁이 쓴다. */
    private final Map<Rectangle, Task> blocks = new LinkedHashMap<>();
    private final Map<Rectangle, Task> boxes = new LinkedHashMap<>();
    /** 열을 나눴거나 제목이 잘린 블록(D-87). 마지막으로 그린 모양이다. */
    private final Set<Task> narrow = new HashSet<>();

    /**
     * 체크 칸은 reward의 complete와 uncomplete를 부르고, 두 번 누르기와 오른쪽 버튼 메뉴는 onEdit, onDelete에 알린다. 여는 창은
     * S4와 삭제 확인이 잇는다(docs/porting/schedule.md S5c2).
     */
    public TimetablePanel(AppData data, RewardController reward, Consumer<Task> onEdit, Consumer<Task> onDelete) {
        this.data = data;
        setPreferredSize(new Dimension(300, HOUR * HOURS));
        setToolTipText("");
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 체크 칸은 왼쪽 버튼 한 번으로만 바뀐다. 나중에 그린(위에 있는) 블록의 것을 먼저 본다.
                Task boxed = null;
                for (Map.Entry<Rectangle, Task> b : boxes.entrySet()) {
                    if (b.getKey().contains(e.getPoint())) {
                        boxed = b.getValue();
                    }
                }
                if (boxed != null && e.getClickCount() == 1) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        // UC-07, UC-08: 끝내지 않은 일은 끝내고 끝낸 일은 체크를 푼다.
                        if (boxed.isDone()) {
                            reward.uncomplete(boxed);
                        } else {
                            reward.complete(boxed);
                        }
                    }
                    return;
                }
                Task t = at(e);
                if (t != null && e.getClickCount() == 2) {
                    onEdit.accept(t);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                // 운영체제마다 누를 때나 뗄 때 팝업 깃발이 서서 둘 다 본다(목록 줄과 같다).
                Task t = at(e);
                if (t != null && e.isPopupTrigger()) {
                    TaskMenu.of(onEdit, onDelete, t).show(TimetablePanel.this, e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mousePressed(e);
            }
        });
    }

    /** h번째 시각 줄의 글. 자정 아래는 00:00부터 다시 적는다(D-77). */
    static String hourText(int h) {
        return String.format("%02d:00", h % 24);
    }

    /** 마지막으로 그린 블록의 자리와 일정. 그린 차례다. */
    Map<Rectangle, Task> blockAreas() {
        return blocks;
    }

    /** 마지막으로 그린 체크 칸의 자리와 일정. 그린 차례다. */
    Map<Rectangle, Task> boxAreas() {
        return boxes;
    }

    /** 자리 e의 블록. 겹치면 나중에 그린(위에 있는) 것이다. 없으면 null. */
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

    /** 블록의 툴팁. 마지막으로 그린 모양을 따른다. 좁지 않으면 목록 줄과 같은 메모 툴팁이다. */
    String tip(Task t) {
        String memo = TaskRows.memoTip(t);
        if (!narrow.contains(t)) {
            return memo;
        }
        String head = "<b>" + Ui.esc(t.getTitle()) + "</b> " + data.getBook().timeText(t);
        String m = t.getMemo();
        String body = m.isBlank() ? "" : "<br>" + Ui.esc(m.length() > 200 ? m.substring(0, 200) + "…" : m).replace("\n", "<br>");
        return "<html><div style='width:280px'>" + head + body + "</div></html>";
    }

    /** 07 seq-uc10: 그날 시간표에 놓이는 일정(끝낸 일 포함). 일정 목록의 차례라 뒤의 일이 위에 그려진다. */
    private List<Task> timedOn(LocalDate date) {
        LocalDate today = data.today();
        return data.getBook().getTasks().stream().filter(t -> t.onTimetable(today) && t.getDate().equals(date)).toList();
    }

    /** 왼쪽 끝이 x인 날 칸(폭 day)에서 열 s에 그리는 일정 t의 블록 자리. 높이는 16보다 작지 않다. */
    private static Rectangle blockRect(int x, Task t, Slot s, int day) {
        int w = day / s.columns();
        return new Rectangle(x + s.column() * w + 1, t.startMinute() * HOUR / 60, w - 2,
                Math.max(t.getDurationMinutes() * HOUR / 60, 16));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        int day = (getWidth() - GUTTER) / 7;
        g.setColor(Theme.CARD_TOP);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int today = (int) (data.today().toEpochDay() - monday.toEpochDay());
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
            g.drawString(hourText(h), 6, h * HOUR + 14);
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
            List<Task> tasks = timedOn(monday.plusDays(i));
            Map<Task, Slot> slots = new HashMap<>();
            TimetableLayout.of(tasks).forEach(s -> slots.put(s.task(), s));
            for (Task t : tasks) {
                Rectangle r = blockRect(GUTTER + i * day, t, slots.get(t), day);
                blocks.put(r, t);
                block(g, r, t, slots.get(t));
            }
        }
        g.dispose();
    }

    /** 블록 하나: 바탕과 테두리(D-44), 체크 칸(D-35), 제목과 끝낸 일의 취소선. 좁은 블록을 적는다(D-87). */
    private void block(Graphics2D g, Rectangle r, Task t, Slot s) {
        boolean plain = t.isDone() || t.getKind() == Kind.FIXED;
        g.setColor(t.isDone() ? Theme.DAY : plain ? Theme.TRACK : Theme.MINT_SOFT);
        g.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
        g.setColor(plain ? Theme.CARD_LINE : Theme.MINT_LINE);
        g.drawRoundRect(r.x, r.y, r.width - 1, r.height - 1, 10, 10);
        Graphics2D text = (Graphics2D) g.create();
        text.clipRect(r.x, r.y, r.width, r.height);
        // D-35: 고정 일정 블록에도 체크 칸을 둔다. 좁은 블록에서는 블록 안으로 잘린다.
        Rectangle box = new Rectangle(r.x + 4, r.y + 4, 12, 12).intersection(r);
        boxes.put(box, t);
        text.setColor(Theme.CARD_TOP);
        text.fill(box);
        text.setColor(Theme.MINT_LINE);
        text.draw(box);
        if (t.isDone()) {
            text.setColor(Theme.MINT);
            text.drawPolyline(new int[] {box.x + 2, box.x + 5, box.x + 10}, new int[] {box.y + 6, box.y + 9, box.y + 3}, 3);
        }
        text.setColor(t.isDone() ? Theme.INK_DIM : Theme.INK);
        int tx = r.x + 20;
        if (s.columns() > 1 || text.getFontMetrics().stringWidth(t.getTitle()) > r.x + r.width - tx - 2) {
            narrow.add(t);
        }
        text.drawString(t.getTitle(), tx, r.y + 14);
        if (t.isDone()) {
            text.drawLine(tx, r.y + 10, tx + text.getFontMetrics().stringWidth(t.getTitle()), r.y + 10);
        }
        text.dispose();
    }
}
