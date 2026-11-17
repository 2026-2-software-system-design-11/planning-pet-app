package planningpet.home.view;

import planningpet.app.view.App;
import planningpet.common.model.Rules;
import planningpet.common.view.*;
import planningpet.reward.view.WalkBar;
import planningpet.schedule.view.TaskRows;
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
    public final CalendarPanel calendar;
    public final TaskRows rows;
    private final JLabel listTitle = new JLabel();
    public final JLabel levelName = new JLabel();
    private final javax.swing.JProgressBar levelBar = new javax.swing.JProgressBar(0, 1000);
    public final JLabel levelText = new JLabel();
    public final JTextField quick = new JTextField();
    /** D-40: 칸 옆 "추가"는 Enter와 같다. "일정 추가"는 목록 제목 줄 오른쪽이다. */
    public final JButton quickButton = new JButton("추가");
    public final JButton addButton = new JButton("일정 추가");

    public MainScreen(App app) {
        super(new BorderLayout(0, 8));
        this.app = app;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 12));
        calendar = new CalendarPanel(app.store, this::select);
        rows = new TaskRows(app.schedule);

        JPanel list = new Card(new BorderLayout(0, 6));
        listTitle.setFont(Theme.font(23));
        addButton.addActionListener(e -> app.schedule.add(calendar.selected()));
        JPanel head = new JPanel(new BorderLayout());
        head.add(listTitle, BorderLayout.CENTER);
        head.add(addButton, BorderLayout.EAST);
        list.add(head, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(rows, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
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
        app.watch(this::refresh);
    }

    private JPanel quickAdd() {
        JPanel panel = new JPanel(new BorderLayout(6, 2));
        Ui.limit(quick, 100);
        Runnable put = () -> {
            if (app.schedule.quickAdd(quick.getText(), calendar.selected())) {
                quick.setText("");
            }
        };
        quick.addActionListener(e -> put.run());
        quickButton.addActionListener(e -> put.run());
        quickButton.setEnabled(false);
        quick.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void sync() {
                quickButton.setEnabled(!quick.getText().isBlank());
            }

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                sync();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                sync();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                sync();
            }
        });
        JLabel hint = new JLabel("제목을 쓰고 Enter나 추가를 누르면 30분짜리 할 일로 들어가요");
        hint.setForeground(Theme.INK_DIM);
        hint.setFont(Theme.font(12));
        panel.add(quick, BorderLayout.CENTER);
        panel.add(quickButton, BorderLayout.EAST);
        panel.add(hint, BorderLayout.SOUTH);
        return panel;
    }

    /** UC-09. 오늘이 아닌 날을 고르면 펫이 그날 할 일 수를 말한다. */
    /** 달력에서 날짜를 누른 것과 같다(QA가 부른다). */
    public void selectDate(LocalDate date) {
        calendar.pick(date);
        select(date);
    }

    private void select(LocalDate date) {
        app.home.selectDay(date);
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
