package planningpet.schedule.view;

import planningpet.app.model.Store;
import planningpet.app.view.App;
import planningpet.common.view.*;
import planningpet.pet.view.PetView;
import planningpet.schedule.model.Planner;
import planningpet.schedule.model.TaskBook;
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
    public final JButton autoPlace = new JButton("자동 배치");
    public final TaskRows untimed;
    public final JLabel bubble = Ui.bubble();
    private LocalDate monday;
    private int lastTalk;
    private boolean wasVisible;
    private boolean showHint = true;

    public ScheduleScreen(App app) {
        super(new BorderLayout(8, 8));
        this.app = app;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 12));
        monday = TaskBook.mondayOf(app.store.today());
        table = new TimetablePanel(app.store, app.schedule);
        untimed = new TaskRows(app.schedule);
        JButton prev = new JButton("<");
        JButton next = new JButton(">");
        JButton today = new JButton("오늘");
        JButton add = new JButton("일정 추가");
        prev.addActionListener(e -> move(monday.minusWeeks(1)));
        next.addActionListener(e -> move(monday.plusWeeks(1)));
        today.addActionListener(e -> move(TaskBook.mondayOf(app.store.today())));
        autoPlace.addActionListener(e -> AutoPlaceDialog.open(app, monday));
        add.addActionListener(e -> app.schedule.add(defaultDate()));
        JPanel bar = new JPanel(new BorderLayout());
        bar.add(Ui.flow(prev, range, next, today), BorderLayout.WEST);
        bar.add(Ui.flow(autoPlace, add), BorderLayout.EAST);
        // D-44: 시간표 블록 색의 뜻.
        JPanel top = new JPanel(new BorderLayout(0, 4));
        top.add(bar, BorderLayout.NORTH);
        top.add(Ui.flow(swatch(Theme.TRACK, "고정 일정"), swatch(Theme.MINT_SOFT, "할 일"), swatch(Theme.DAY, "끝냄")),
                BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setColumnHeaderView(header);
        scroll.getViewport().setViewPosition(new Point(0, 8 * TimetablePanel.HOUR));
        Card tableCard = new Card(new BorderLayout());
        tableCard.add(scroll);
        add(tableCard, BorderLayout.CENTER);
        JPanel side = new Card(new BorderLayout(0, 8));
        side.setPreferredSize(new Dimension(240, 0));
        side.add(new JLabel("<html><b>시각 없는 할 일</b></html>"), BorderLayout.NORTH);
        JScrollPane sideScroll = new JScrollPane(untimed, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sideScroll.setBorder(BorderFactory.createEmptyBorder());
        side.add(sideScroll, BorderLayout.CENTER);
        JPanel petCorner = new JPanel(new BorderLayout());
        petCorner.add(bubble, BorderLayout.NORTH);
        petCorner.add(new PetView(app.store, 1), BorderLayout.CENTER);
        side.add(petCorner, BorderLayout.SOUTH);
        add(side, BorderLayout.EAST);
        app.watch(this::refresh);
    }

    /** 06의 14번: 보이는 주에 오늘이 있으면 오늘, 없으면 그 주 월요일. */
    public LocalDate defaultDate() {
        LocalDate today = app.store.today();
        return TaskBook.mondayOf(today).equals(monday) ? today : monday;
    }

    public LocalDate monday() {
        return monday;
    }

    private static JLabel swatch(java.awt.Color color, String text) {
        JLabel l = new JLabel(text, new javax.swing.Icon() {
            @Override
            public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
                g.setColor(color);
                g.fillRect(x, y, 12, 12);
                g.setColor(Theme.CARD_LINE);
                g.drawRect(x, y, 11, 11);
            }

            @Override
            public int getIconWidth() {
                return 12;
            }

            @Override
            public int getIconHeight() {
                return 12;
            }
        }, JLabel.LEFT);
        l.setFont(Theme.font(12));
        return l;
    }

    public void move(LocalDate newMonday) {
        monday = newMonday;
        showHint = true;
        app.redraw();
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
        untimed.show(app.store.book.untimedOfWeek(monday), true);
        int n = Planner.targets(app.store, monday).size();
        autoPlace.setEnabled(n > 0);
        // 화면을 열거나 주를 옮기면 SCHEDULE_HINT. 보이는 중에 바뀌면 새 펫 말(완료, 취소, 상한, 레벨 업)이
        // 있을 때 그 말, 없으면 SCHEDULE_HINT를 다시 정한다(D-39).
        boolean visible = "일정관리".equals(app.current());
        boolean newTalk = app.store.talkSeq != lastTalk;
        lastTalk = app.store.talkSeq;
        String text = null;
        if (visible) {
            text = !wasVisible || showHint || !newTalk
                    ? (n > 0 ? "이번 주 남은 날의 시각 없는 할 일이 " + n + "개 있어. 자동 배치를 눌러 볼래?" : "지금 자동 배치할 할 일은 없어.")
                    : app.store.talk;
            showHint = false;
        }
        wasVisible = visible;
        if (text != null) {
            bubble.setText("<html><div style='width:150px'>" + Ui.esc(text) + "</div></html>");
        }
        table.repaint();
        header.revalidate();
    }
}
