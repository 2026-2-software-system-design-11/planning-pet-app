package planningpet.proto;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

/** 일정관리(05 일정관리, UC-10, UC-11). */
public final class ScheduleScreen extends JPanel {
    private static final String WEEK = "월화수목금토일";
    private final App app;
    private final TimetablePanel table;
    private final JPanel header = new JPanel(new GridLayout(1, 7));
    private final JLabel range = new JLabel("", SwingConstants.CENTER);
    private final JButton autoPlace = new JButton("자동 배치");
    private final TaskRows untimed;
    private final JLabel bubble = Ui.bubble();
    private LocalDate monday;

    public ScheduleScreen(App app) {
        super(new BorderLayout(8, 8));
        this.app = app;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 12));
        monday = Store.mondayOf(app.store.today());
        table = new TimetablePanel(app.store, app.actions);
        untimed = new TaskRows(app.actions);
        JButton prev = new JButton("<");
        JButton next = new JButton(">");
        JButton today = new JButton("오늘");
        JButton add = new JButton("일정 추가");
        prev.addActionListener(e -> move(monday.minusWeeks(1)));
        next.addActionListener(e -> move(monday.plusWeeks(1)));
        today.addActionListener(e -> move(Store.mondayOf(app.store.today())));
        autoPlace.addActionListener(e -> AutoPlaceDialog.open(app, monday));
        add.addActionListener(e -> app.actions.add(defaultDate()));
        JPanel bar = new JPanel(new BorderLayout());
        bar.add(Ui.flow(prev, range, next, today), BorderLayout.WEST);
        bar.add(Ui.flow(autoPlace, add), BorderLayout.EAST);
        add(bar, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setColumnHeaderView(header);
        scroll.getViewport().setViewPosition(new Point(0, 8 * TimetablePanel.HOUR));
        Card tableCard = new Card(new BorderLayout());
        tableCard.add(scroll);
        add(tableCard, BorderLayout.CENTER);
        JPanel side = new Card(new BorderLayout(0, 8));
        side.setPreferredSize(new Dimension(240, 0));
        side.add(new JLabel("<html><b>시각 없는 할 일</b></html>"), BorderLayout.NORTH);
        side.add(untimed, BorderLayout.CENTER);
        JPanel petCorner = new JPanel(new BorderLayout());
        petCorner.add(bubble, BorderLayout.NORTH);
        petCorner.add(new PetView(app.store, 1), BorderLayout.CENTER);
        side.add(petCorner, BorderLayout.SOUTH);
        add(side, BorderLayout.EAST);
        app.store.onChange(this::refresh);
    }

    /** 06의 14번: 보이는 주에 오늘이 있으면 오늘, 없으면 그 주 월요일. */
    private LocalDate defaultDate() {
        LocalDate today = app.store.today();
        return Store.mondayOf(today).equals(monday) ? today : monday;
    }

    private void move(LocalDate newMonday) {
        monday = newMonday;
        app.store.changed();
    }

    private void refresh() {
        table.monday = monday;
        LocalDate sunday = monday.plusDays(6);
        range.setText("<html><b>" + monday.getMonthValue() + "월 " + monday.getDayOfMonth() + "일 ~ "
                + sunday.getMonthValue() + "월 " + sunday.getDayOfMonth() + "일</b></html>");
        header.removeAll();
        header.setBorder(BorderFactory.createEmptyBorder(0, TimetablePanel.GUTTER, 0, 0));
        for (int i = 0; i < 7; i++) {
            LocalDate d = monday.plusDays(i);
            JLabel label = new JLabel(WEEK.charAt(i) + " " + d.getDayOfMonth(), SwingConstants.CENTER);
            if (d.equals(app.store.today())) {
                label.setOpaque(true);
                label.setBackground(Theme.DAY_SELECTED);
            }
            header.add(label);
        }
        untimed.show(app.store.untimedOfWeek(monday), true);
        int n = Planner.targets(app.store, monday).size();
        autoPlace.setEnabled(n > 0);
        bubble.setText("<html><div style='width:150px'>" + (n > 0 ? "시간이 안 정해진 할 일이 " + n + "개 있어. 자동 배치를 눌러 볼래?"
                : "이번 주 할 일은 모두 시간이 정해졌어.") + "</div></html>");
        table.repaint();
        header.revalidate();
    }
}
