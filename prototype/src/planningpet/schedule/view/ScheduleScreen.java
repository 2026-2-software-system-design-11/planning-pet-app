package planningpet.schedule.view;

import planningpet.app.view.App;
import planningpet.common.view.*;
import planningpet.pet.view.PetView;
import planningpet.schedule.model.*;
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
    /** 보이는 주의 첫날(06 데이터 절의 한 주, D-91). */
    private LocalDate start;
    /** D-60: 이번 주를 보고 있으면 흐리다. */
    public JButton today;
    private int lastTalk;
    private boolean wasVisible;
    private boolean showHint = true;
    /** 주를 옮겨 처음 보이는 자리로 내려야 한다(D-89). */
    private boolean scrollPending;
    private final JScrollPane scroll;

    public ScheduleScreen(App app) {
        super(new BorderLayout(8, 8));
        this.app = app;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 12));
        start = thisWeekStart();
        table = new TimetablePanel(app.store, app.schedule);
        untimed = new TaskRows(app.schedule);
        untimed.dropOn = table;
        JButton prev = new JButton("<");
        JButton next = new JButton(">");
        today = new JButton("오늘");
        JButton add = new JButton("일정 추가");
        prev.addActionListener(e -> move(start.minusWeeks(1)));
        next.addActionListener(e -> move(start.plusWeeks(1)));
        today.addActionListener(e -> move(thisWeekStart()));
        autoPlace.addActionListener(e -> AutoPlaceDialog.open(app, start));
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
        scroll = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
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

    /** 06의 14번: 보이는 주에 오늘이 있으면 오늘, 없으면 그 주 첫날. */
    public LocalDate defaultDate() {
        return DefaultDates.forSchedule(start, app.store.today());
    }

    /** 보이는 주의 첫날. */
    public LocalDate weekStart() {
        return start;
    }

    /** 오늘이 든 주의 첫날(설정의 한 주 시작, D-91). */
    private LocalDate thisWeekStart() {
        return planningpet.common.model.Rules.weekStart(app.store.today(), app.store.weekStart);
    }

    /** 시간표 맨 위가 앱 하루의 분 minute이 되게 내린다. 끝을 넘지 않는다. */
    public void scrollTo(int minute) {
        int view = scroll.getViewport().getExtentSize().height;
        int max = Math.max(0, table.getPreferredSize().height - view);
        int y = Math.min(Math.max(0, minute * TimetablePanel.HOUR / 60), max);
        scroll.getViewport().setViewPosition(new Point(0, y));
    }

    /** 시간표 맨 위의 앱 하루의 분. */
    public int topMinute() {
        return (int) Math.round(scroll.getViewport().getViewPosition().y * 60.0 / TimetablePanel.HOUR);
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

    public void move(LocalDate newStart) {
        start = newStart;
        showHint = true;
        table.note.close();
        scrollPending = true;
        app.redraw();
    }

    private void refresh() {
        // D-91: 한 주 시작을 바꾸면 보던 주의 첫날이 든 새 주로 바꾼다.
        start = planningpet.common.model.Rules.weekStart(start, app.store.weekStart);
        table.monday = start;
        boolean thisWeek = start.equals(thisWeekStart());
        today.setEnabled(!thisWeek);
        today.setToolTipText(thisWeek ? "이번 주를 보고 있어요" : null);
        LocalDate last = start.plusDays(6);
        range.setText("<html><b>" + start.getMonthValue() + "월 " + start.getDayOfMonth() + "일 ~ "
                + last.getMonthValue() + "월 " + last.getDayOfMonth() + "일</b></html>");
        header.removeAll();
        header.setBorder(BorderFactory.createEmptyBorder(0, TimetablePanel.GUTTER, 0, 0));
        for (int i = 0; i < 7; i++) {
            LocalDate d = start.plusDays(i);
            JLabel label = new JLabel(WEEK.charAt(d.getDayOfWeek().getValue() - 1) + " " + d.getDayOfMonth(), SwingConstants.CENTER);
            if (d.equals(app.store.today())) {
                label.setOpaque(true);
                label.setBackground(Theme.DAY_SELECTED);
            }
            header.add(label);
        }
        untimed.show(app.store.book.untimedOfWeek(start));
        int n = Planner.targets(app.store, start).size();
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
        // D-89: 자동 배치 뒤에는 놓인 블록으로, 화면을 열거나 주를 옮기면 처음 보이는 자리로 내린다. 데이터만 바뀌면 그대로다.
        java.util.List<Task> placed = app.schedule.takePlaced();
        if (!placed.isEmpty()) {
            scrollTo(placed.stream().mapToInt(Task::startMinute).min().getAsInt() - 60);
            table.flash(placed);
        } else if (visible && (!wasVisible || scrollPending)) {
            scrollTo(Planner.top(start, app.store.now));
        }
        if (visible) {
            scrollPending = false;
        } else {
            table.note.close();
        }
        wasVisible = visible;
        if (text != null) {
            bubble.setText("<html><div style='width:150px'>" + Ui.esc(text) + "</div></html>");
        }
        table.repaint();
        header.revalidate();
    }
}
