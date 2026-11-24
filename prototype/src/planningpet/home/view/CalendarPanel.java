package planningpet.home.view;

import planningpet.app.model.Store;
import planningpet.common.view.*;
import planningpet.schedule.model.TaskBook;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * 메인의 달력(05 메인, UC-09). 한 주는 일요일부터 토요일까지다. 날짜 아래에 그날 일의 수, 기간 할 일 마감 날에는 빨간 점(D-86).
 * 모양은 2026-11-24 홈 예시: 왼쪽에 달력 아이콘과 제목, 오른쪽에 원 꺾쇠, 칸 바탕 없이 숫자만, 오늘은 연민트 둥근 네모.
 */
public final class CalendarPanel extends JPanel {
    private static final String DAYS = "일월화수목금토";
    /** 마감 날 빨간 점. 목록의 "오늘 마감"과 같은 색이다. */
    private static final Color DUE = new Color(0xC0392B);
    private final Store store;
    private final Consumer<LocalDate> onSelect;
    /** 날짜 칸. 그달의 주 수만큼 줄이 있다. */
    public final JPanel grid = new JPanel(new GridLayout(0, 7, 2, 2));
    private final JLabel title = new JLabel();
    public final CircleButton prev = new CircleButton(Icons.Kind.LEFT, 32, "이전 달", false);
    public final CircleButton next = new CircleButton(Icons.Kind.RIGHT, 32, "다음 달", false);
    /** D-60: 오늘이 든 달로 옮기고 오늘을 고른다. 이미 그렇다면 흐리다. */
    public final javax.swing.JButton today = new javax.swing.JButton("오늘");
    private YearMonth month;
    private LocalDate selected;

    public CalendarPanel(Store store, Consumer<LocalDate> onSelect) {
        super(new BorderLayout(0, 4));
        this.store = store;
        this.onSelect = onSelect;
        this.selected = store.today();
        this.month = YearMonth.from(selected);
        prev.addActionListener(e -> move(-1));
        next.addActionListener(e -> move(1));
        today.setFont(Theme.bold(12));
        today.setBorder(BorderFactory.createEmptyBorder(5, 12, 7, 12));
        today.addActionListener(e -> {
            selected = store.today();
            month = YearMonth.from(selected);
            onSelect.accept(selected);
        });
        title.setIcon(Icons.icon(Icons.Kind.CALENDAR, 24, Theme.ICON));
        title.setIconTextGap(8);
        title.setFont(Theme.heavy(21));
        JPanel arrows = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 4, 0));
        arrows.add(today);
        arrows.add(prev);
        arrows.add(next);
        JPanel head = new JPanel(new BorderLayout());
        head.add(title, BorderLayout.CENTER);
        head.add(arrows, BorderLayout.EAST);
        head.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.CARD_EDGE),
                BorderFactory.createEmptyBorder(0, 0, 8, 0)));
        JPanel week = new JPanel(new GridLayout(1, 7, 2, 0));
        for (int i = 0; i < 7; i++) {
            JLabel day = new JLabel(String.valueOf(DAYS.charAt(i)), SwingConstants.CENTER);
            day.setForeground(i == 0 ? Theme.SUNDAY : i == 6 ? Theme.SATURDAY : Theme.INK_SUB);
            day.setFont(Theme.bold(13));
            day.setBorder(BorderFactory.createEmptyBorder(6, 0, 2, 0));
            week.add(day);
        }
        JPanel top = new JPanel(new BorderLayout());
        top.add(head, BorderLayout.NORTH);
        top.add(week, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);
        add(grid, BorderLayout.CENTER);
    }

    public LocalDate selected() {
        return selected;
    }

    public void pick(LocalDate date) {
        selected = date;
        month = YearMonth.from(date);
    }

    private void move(int months) {
        month = month.plusMonths(months);
        refresh();
    }

    public void refresh() {
        title.setText(month.getYear() + "년 " + month.getMonthValue() + "월");
        boolean atToday = month.equals(YearMonth.from(store.today())) && store.today().equals(selected);
        today.setEnabled(!atToday);
        today.setToolTipText(atToday ? "오늘을 보고 있어요" : null);
        grid.removeAll();
        int offset = month.atDay(1).getDayOfWeek().getValue() % 7;
        int cells = (offset + month.lengthOfMonth() + 6) / 7 * 7;
        for (int cell = 0; cell < cells; cell++) {
            int dayNumber = cell - offset + 1;
            grid.add(dayNumber < 1 || dayNumber > month.lengthOfMonth() ? new JLabel() : cell(month.atDay(dayNumber)));
        }
        grid.revalidate();
        grid.repaint();
    }

    /**
     * 날짜 칸. 글(getText)은 숫자, 일의 수, 마감 표시의 HTML이고(QA와 화면 읽기 프로그램이 읽는다), 그림은 손수 그린다:
     * 숫자, 그 아래 "{수}개", 마감 날이면 숫자 오른쪽 위 빨간 점(D-86). 오늘은 DAY_TODAY 둥근 네모, 고른 날은 MINT 테두리,
     * 마우스를 올리면 DAY.
     */
    private JLabel cell(LocalDate date) {
        TaskBook.CellPreview preview = store.book.cellPreview(date, store.calendarFixed);
        boolean due = preview != null && !preview.due().isEmpty();
        String day = date.equals(selected) ? "<b>" + date.getDayOfMonth() + "</b>" : String.valueOf(date.getDayOfMonth());
        String text = "<center>" + day + (preview == null ? "" : "<br><font size='2' color='"
                + (preview.open() ? "#28977F" : "#8A96A0") + "'>" + preview.text() + "</font>")
                + (due ? " <font size='2' color='#C0392B'>마감</font>" : "") + "</center>";
        boolean today = date.equals(store.today());
        boolean picked = date.equals(selected);
        int dow = date.getDayOfWeek().getValue() % 7;
        Color ink = dow == 0 ? Theme.SUNDAY : dow == 6 ? Theme.SATURDAY : Theme.INK;
        boolean[] over = {false};
        JLabel label = new JLabel("<html>" + text + "</html>", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                int r = 12;
                if (today || over[0]) {
                    g.setColor(today ? Theme.DAY_TODAY : Theme.DAY);
                    g.fillRoundRect(2, 1, w - 4, h - 2, r, r);
                }
                if (picked) {
                    g.setColor(Theme.MINT);
                    g.setStroke(new BasicStroke(2f));
                    g.drawRoundRect(2, 1, w - 5, h - 3, r, r);
                }
                g.setFont(today || picked ? Theme.bold(14) : Theme.font(14));
                FontMetrics fm = g.getFontMetrics();
                String n = String.valueOf(date.getDayOfMonth());
                int numY = preview == null ? (h + fm.getAscent() - fm.getDescent()) / 2 : h / 2 - 1;
                g.setColor(ink);
                g.drawString(n, (w - fm.stringWidth(n)) / 2, numY);
                if (due) {
                    // D-86: 기간 할 일의 마감 날은 숫자 오른쪽 위에 빨간 점.
                    g.setColor(DUE);
                    g.fillOval((w + fm.stringWidth(n)) / 2 + 1, numY - fm.getAscent() + 1, 6, 6);
                }
                if (preview != null) {
                    g.setFont(Theme.font(11));
                    FontMetrics pf = g.getFontMetrics();
                    String p = PetCardArt.fit(preview.text(), pf, w - 6);
                    g.setColor(preview.open() ? Theme.TASK_TEXT : Theme.INK_DIM);
                    g.drawString(p, (w - pf.stringWidth(p)) / 2, numY + 3 + pf.getAscent());
                }
                g.dispose();
            }
        };
        label.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        if (due) {
            label.setToolTipText(String.join(", ", preview.due().stream().map(t -> t + " 마감").toList()));
        }
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                over[0] = true;
                label.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                over[0] = false;
                label.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                selected = date;
                onSelect.accept(date);
            }
        });
        return label;
    }
}
