package planningpet.home.view;

import planningpet.app.view.App;
import planningpet.common.view.*;
import planningpet.schedule.view.TaskRows;
import java.awt.BorderLayout;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * 메인(05 메인, D-59). 하루는 이 화면에서 끝난다. 왼쪽 위 펫 카드, 왼쪽 아래 달력, 오른쪽 위아래 할 일 목록이다.
 * 모양은 2026-11-24 홈 예시를 따르고, 값은 디자인 시스템 10절 "홈"에 있다.
 */
public final class MainScreen extends JPanel {
    private static final String WEEK = "월화수목금토일";
    private final App app;
    public final CalendarPanel calendar;
    public final TaskRows rows;
    public final PetCard pet;
    private final JLabel listTitle = new JLabel();
    public final RoundField quick = new RoundField(Icons.Kind.PENCIL, "할 일을 쓰고 Enter");
    /** D-40: 칸 옆 "추가"는 Enter와 같다. "일정 추가"는 목록 제목 줄 오른쪽이다. */
    public final JButton quickButton = new JButton("추가");
    public final JButton addButton = new JButton("일정 추가");
    /** 펫 카드를 누르면 캐릭터 창을 연다(D-58, D-59). Main이 나의 방의 characterOpener로 잇는다. */
    public Runnable openCharacter = () -> { };

    public MainScreen(App app) {
        super(null);
        this.app = app;
        calendar = new CalendarPanel(app.store, this::select);
        rows = new TaskRows(app.schedule);
        rows.resort = this::refresh;
        pet = new PetCard(app.store, () -> openCharacter.run());

        Card list = new Card(new BorderLayout(0, 8));
        listTitle.setFont(Theme.heavy(21));
        listTitle.setIcon(Icons.icon(Icons.Kind.CHECKLIST, 24, Theme.ICON));
        listTitle.setIconTextGap(8);
        addButton.setIcon(Icons.icon(Icons.Kind.PLUS, 16, Theme.ICON));
        addButton.setIconTextGap(6);
        addButton.addActionListener(e -> app.schedule.add(calendar.selected()));
        JPanel head = new JPanel(new BorderLayout());
        head.add(listTitle, BorderLayout.CENTER);
        head.add(addButton, BorderLayout.EAST);
        head.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.CARD_EDGE),
                BorderFactory.createEmptyBorder(0, 0, 8, 0)));
        list.add(head, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(rows, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        list.add(scroll, BorderLayout.CENTER);
        list.add(quickAdd(), BorderLayout.SOUTH);

        Card calendarCard = new Card(new BorderLayout());
        calendarCard.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        calendarCard.add(calendar);
        add(pet);
        add(calendarCard);
        add(list);
        app.watch(this::refresh);
    }

    /**
     * 왼쪽 열은 본문 폭의 0.47(360에서 760 사이), 펫 카드 높이는 본문 높이의 0.42(236에서 400 사이)이고 나머지가 달력이다.
     * 오른쪽 할 일 카드는 위아래를 다 쓴다(D-59).
     */
    @Override
    public void doLayout() {
        int w = getWidth();
        int h = getHeight();
        int gap = Theme.GAP;
        int leftW = (int) Math.max(360, Math.min(760, w * 0.47));
        int petH = (int) Math.max(236, Math.min(400, h * 0.42));
        java.awt.Component[] parts = getComponents();
        parts[0].setBounds(0, 0, leftW, petH);
        parts[1].setBounds(0, petH + gap, leftW, h - petH - gap);
        parts[2].setBounds(leftW + gap, 0, w - leftW - gap, h);
        for (java.awt.Component c : parts) {
            c.doLayout();
        }
    }

    private JPanel quickAdd() {
        JPanel panel = new JPanel(new BorderLayout(8, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
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
        hint.setForeground(Theme.INK_SUB);
        hint.setFont(Theme.font(12));
        hint.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        quickButton.setBorder(BorderFactory.createEmptyBorder(9, 18, 11, 18));
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
        rows.showPlan(app.store.book.dayPlan(date));
        calendar.refresh();
        pet.repaint();
    }
}
