package planningpet.proto;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
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
        title.setText("<html><b>" + month.getYear() + "년 " + month.getMonthValue() + "월</b></html>");
        grid.removeAll();
        for (int i = 0; i < 7; i++) {
            JLabel day = new JLabel(String.valueOf(DAYS.charAt(i)), SwingConstants.CENTER);
            day.setForeground(i == 0 ? new Color(0xC0392B) : i == 6 ? new Color(0x2E64C8) : Color.DARK_GRAY);
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
        boolean open = store.forDate(date).stream().anyMatch(t -> !t.done);
        String text = date.getDayOfMonth() + (open ? " <font color='#4A90D9' size='2'>●</font>" : "");
        if (date.equals(selected)) {
            text = "<b>" + text + "</b>";
        }
        JLabel label = new JLabel("<html>" + text + "</html>", SwingConstants.CENTER);
        label.setBorder(BorderFactory.createLineBorder(new Color(0xE0E0E0)));
        if (date.equals(store.today())) {
            label.setOpaque(true);
            label.setBackground(new Color(0xFFE58F));
        }
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
