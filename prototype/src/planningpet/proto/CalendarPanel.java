package planningpet.proto;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** 메인의 달력(05 메인, UC-09). 한 주는 일요일부터 토요일까지다. */
public final class CalendarPanel extends JPanel {
    private static final String DAYS = "일월화수목금토";
    private final Store store;
    private final Consumer<LocalDate> onSelect;
    private final JPanel grid = new JPanel(new GridLayout(7, 7, 2, 2));
    private final JLabel title = new JLabel("", SwingConstants.CENTER);
    private YearMonth month;
    private LocalDate selected;

    public CalendarPanel(Store store, Consumer<LocalDate> onSelect) {
        super(new BorderLayout(0, 8));
        this.store = store;
        this.onSelect = onSelect;
        this.selected = store.today();
        this.month = YearMonth.from(selected);
        JButton prev = new JButton("<");
        JButton next = new JButton(">");
        prev.addActionListener(e -> move(-1));
        next.addActionListener(e -> move(1));
        JPanel head = new JPanel(new BorderLayout());
        head.add(prev, BorderLayout.WEST);
        head.add(title, BorderLayout.CENTER);
        head.add(next, BorderLayout.EAST);
        add(head, BorderLayout.NORTH);
        add(grid, BorderLayout.CENTER);
    }

    public LocalDate selected() {
        return selected;
    }

    private void move(int months) {
        month = month.plusMonths(months);
        refresh();
    }

    public void refresh() {
        title.setText(month.getYear() + "년 " + month.getMonthValue() + "월");
        title.setFont(Theme.font(23));
        grid.removeAll();
        for (int i = 0; i < 7; i++) {
            JLabel day = new JLabel(String.valueOf(DAYS.charAt(i)), SwingConstants.CENTER);
            day.setForeground(i == 0 ? Theme.SUNDAY : i == 6 ? Theme.SATURDAY : Theme.INK);
            day.setFont(Theme.font(16));
            grid.add(day);
        }
        int offset = month.atDay(1).getDayOfWeek().getValue() % 7;
        for (int cell = 0; cell < 42; cell++) {
            int dayNumber = cell - offset + 1;
            grid.add(dayNumber < 1 || dayNumber > month.lengthOfMonth() ? new JLabel() : cell(month.atDay(dayNumber)));
        }
        grid.revalidate();
        grid.repaint();
    }

    private JLabel cell(LocalDate date) {
        Store.CellPreview preview = store.cellPreview(date);
        String day = date.equals(selected) ? "<b>" + date.getDayOfMonth() + "</b>" : String.valueOf(date.getDayOfMonth());
        String text = "<center>" + day + (preview == null ? "" : "<br><font size='2' color='"
                + (preview.open() ? "#28977F" : "#8A96A0") + "'>" + Ui.esc(preview.title()) + "</font>") + "</center>";
        boolean today = date.equals(store.today());
        boolean picked = date.equals(selected);
        JLabel label = new JLabel("<html>" + text + "</html>", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(java.awt.Graphics graphics) {
                java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
                g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(today ? Theme.DAY_SELECTED : Theme.DAY);
                g.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 18, 18);
                if (picked) {
                    g.setColor(Theme.MINT);
                    g.setStroke(new java.awt.BasicStroke(2));
                    g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 18, 18);
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
