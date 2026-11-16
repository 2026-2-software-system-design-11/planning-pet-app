package planningpet.proto;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/** 메인(05 메인). 하루는 이 화면에서 끝난다. */
public final class MainScreen extends JPanel {
    private static final String WEEK = "월화수목금토일";
    private final App app;
    private final CalendarPanel calendar;
    private final TaskRows rows;
    private final JLabel listTitle = new JLabel();
    private final JLabel levelName = new JLabel();
    private final javax.swing.JProgressBar levelBar = new javax.swing.JProgressBar(0, 1000);
    private final JLabel levelText = new JLabel();
    private final JTextField quick = new JTextField();

    public MainScreen(App app) {
        super(new BorderLayout(0, 8));
        this.app = app;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 12));
        calendar = new CalendarPanel(app.store, this::select);
        rows = new TaskRows(app.actions);

        JPanel list = new Card(new BorderLayout(0, 6));
        listTitle.setFont(Theme.font(23));
        list.add(listTitle, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(rows);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        list.add(scroll, BorderLayout.CENTER);
        list.add(quickAdd(), BorderLayout.SOUTH);

        JPanel body = new JPanel(new GridLayout(1, 2, 16, 0));
        Card calendarCard = new Card(new BorderLayout());
        calendarCard.add(calendar);
        body.add(calendarCard);
        body.add(list);
        add(body, BorderLayout.CENTER);

        JPanel bottom = new Card(new BorderLayout());
        JPanel top = new JPanel(new BorderLayout());
        levelBar.setPreferredSize(new java.awt.Dimension(110, 10));
        top.add(Ui.flow(levelName, levelBar, levelText), BorderLayout.EAST);
        bottom.add(top, BorderLayout.NORTH);
        bottom.add(new WalkBar(app.store), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        app.store.onChange(this::refresh);
    }

    private JPanel quickAdd() {
        JPanel panel = new JPanel(new BorderLayout(6, 2));
        JButton add = new JButton("일정 추가");
        add.addActionListener(e -> app.actions.add(calendar.selected()));
        Ui.limit(quick, 100);
        quick.addActionListener(e -> {
            String title = quick.getText().strip();
            if (title.isEmpty() || title.length() > 100) {
                return;
            }
            app.store.add(Task.Kind.TASK, title, calendar.selected(), null, 30, "", null);
            quick.setText("");
            app.store.changed();
        });
        JLabel hint = new JLabel("시간을 안 쓰면 30분으로 들어가요");
        hint.setForeground(Theme.INK_DIM);
        hint.setFont(Theme.font(12));
        panel.add(quick, BorderLayout.CENTER);
        panel.add(add, BorderLayout.EAST);
        panel.add(hint, BorderLayout.SOUTH);
        return panel;
    }

    /** UC-09. 오늘이 아닌 날을 고르면 펫이 그날 할 일 수를 말한다. */
    private void select(LocalDate date) {
        app.store.talk = date.equals(app.store.today()) ? app.talk.usual() : app.talk.otherDate(date);
        app.store.changed();
    }

    private void refresh() {
        LocalDate date = calendar.selected();
        listTitle.setText(date.getMonthValue() + "월 " + date.getDayOfMonth() + "일 ("
                + WEEK.charAt(date.getDayOfWeek().getValue() - 1) + ") 할 일");
        rows.showPlan(app.store.dayPlan(date));
        calendar.refresh();
        levelName.setText(app.store.petName + " Lv." + Rules.level(app.store.exp));
        levelName.setFont(Theme.bold(15));
        levelBar.setValue((int) (Rules.expRatio(app.store.exp) * 1000));
        levelText.setText(Rules.expText(app.store.exp));
    }
}
