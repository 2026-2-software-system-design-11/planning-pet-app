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
 * 메인의 달력(05 메인, UC-09). 한 주는 일요일부터 토요일까지다.
 * 모양은 2026-11-24 홈 예시: 왼쪽에 달력 아이콘과 제목, 오른쪽에 원 꺾쇠, 칸 바탕 없이 숫자만, 오늘은 연민트 둥근 네모.
 */
public final class CalendarPanel extends JPanel {
    private static final String DAYS = "일월화수목금토";
    private final Store store;
    private final Consumer<LocalDate> onSelect;
    /** 날짜 칸. 그달의 주 수만큼 줄이 있다. */
    public final JPanel grid = new JPanel(new GridLayout(0, 7, 2, 2));
    private final JLabel title = new JLabel();
    public final CircleButton prev = new CircleButton(Icons.Kind.LEFT, 32, "이전 달", false);
    public final CircleButton next = new CircleButton(Icons.Kind.RIGHT, 32, "다음 달", false);
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
        title.setIcon(Icons.icon(Icons.Kind.CALENDAR, 24, Theme.ICON));
        title.setIconTextGap(8);
        title.setFont(Theme.heavy(21));
        JPanel arrows = new JPanel(new GridLayout(1, 2, 4, 0));
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
     * 날짜 칸. 글(getText)은 숫자와 첫 할 일 제목의 HTML이고(QA와 화면 읽기 프로그램이 읽는다), 그림은 손수 그린다:
     * 숫자, 그 아래 제목을 칸 폭에 들어가는 만큼(D-59). 오늘은 DAY_TODAY 둥근 네모, 고른 날은 MINT 테두리, 마우스를 올리면 DAY.
     */
    private JLabel cell(LocalDate date) {
        TaskBook.CellPreview preview = store.book.cellPreview(date);
        String day = date.equals(selected) ? "<b>" + date.getDayOfMonth() + "</b>" : String.valueOf(date.getDayOfMonth());
        String text = "<center>" + day + (preview == null ? "" : "<br><font size='2' color='"
                + (preview.open() ? "#28977F" : "#8A96A0") + "'>" + Ui.esc(preview.title()) + "</font>") + "</center>";
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
                }
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        int dow = date.getDayOfWeek().getValue() % 7;
        label.setForeground(dow == 0 ? Theme.SUNDAY : dow == 6 ? Theme.SATURDAY : Theme.INK);
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selected = date;
                onSelect.accept(date);
            }
        });
        return label;
    }
}
