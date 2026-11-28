package planningpet.schedule.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import planningpet.app.model.AppData;
import planningpet.common.model.Rules;
import planningpet.common.view.Card;
import planningpet.common.view.Theme;
import planningpet.common.view.Ui;
import planningpet.reward.controller.RewardController;
import planningpet.schedule.model.AutoPlacer;
import planningpet.schedule.model.DefaultDates;
import planningpet.schedule.model.Screen;
import planningpet.schedule.model.Task;

/**
 * 일정관리(05 일정관리, UC-10, UC-11). 위 줄, 가운데 시간표, 오른쪽 "시각 없는 할 일" 카드다. 옮기는 몫은
 * docs/porting/schedule.md S5c. 시간표 누르기와 처음 보이는 자리는 S5c2, 말풍선, 펫 자리, 놓인 블록 보이기, 앱 창에 붙이기는 S5d가
 * 더한다.
 */
public final class ScheduleScreen extends JPanel {
    private static final String WEEK = "월화수목금토일";
    private final AppData data;
    private final Clock clock;
    final TimetablePanel table;
    final JPanel header = new JPanel(new GridLayout(1, 7));
    final JLabel range = new JLabel("", SwingConstants.CENTER);
    final JButton prev = new JButton("<");
    final JButton next = new JButton(">");
    /** D-60: 이번 주를 보고 있으면 흐리다. */
    final JButton today = new JButton("오늘");
    final JButton autoPlace = new JButton("자동 배치");
    final JButton add = new JButton("일정 추가");
    final TaskRows untimed;
    final JScrollPane scroll;
    /** 보이는 주의 첫날(06 데이터 절의 한 주, D-91). */
    private LocalDate start;
    /** 목록 줄의 연필, 휴지통, 시계 단추. 창은 S4와 S5f가 넣는다. 시간표 블록의 누르기는 S5c2가 잇는다. */
    public Consumer<Task> onEdit = t -> { };
    public Consumer<Task> onDelete = t -> { };
    public TaskRows.TimeOpener onTime = (t, day, at) -> { };
    /** "일정 추가"를 누르면 06 14번의 날을 받는다. 일정 입력 창은 S5f가 넣는다. */
    public Consumer<LocalDate> openAdd = day -> { };
    /** "자동 배치"를 누르면 보이는 주의 첫날을 받는다. 결과 창은 S6이 넣는다. */
    public Consumer<LocalDate> openAutoPlace = start -> { };
    /** 주를 옮긴 뒤 다시 그린다. Main이 앱 창의 redraw를 넣는다(Theme.clear까지 돈다). */
    public Runnable redraw = this::refresh;

    /** clock은 자동 배치 단추가 세는 지금이다. 데이터 변경 알림에 refresh를 거는 일은 화면을 넣는 쪽(Main)이 한다. */
    public ScheduleScreen(AppData data, RewardController reward, Clock clock) {
        super(new BorderLayout(8, 8));
        this.data = data;
        this.clock = clock;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 12));
        start = thisWeekStart();
        table = new TimetablePanel(data, reward);
        untimed = new TaskRows(data, reward, t -> onEdit.accept(t), t -> onDelete.accept(t), (t, day, at) -> onTime.open(t, day, at));
        prev.addActionListener(e -> move(start.minusWeeks(1)));
        next.addActionListener(e -> move(start.plusWeeks(1)));
        today.addActionListener(e -> move(thisWeekStart()));
        autoPlace.addActionListener(e -> openAutoPlace.accept(start));
        add.addActionListener(e -> openAdd.accept(DefaultDates.forAdd(Screen.SCHEDULE, null, start, data.today())));
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
        Card side = new Card(new BorderLayout(0, 8));
        side.setPreferredSize(new Dimension(240, 0));
        side.add(new JLabel("<html><b>시각 없는 할 일</b></html>"), BorderLayout.NORTH);
        JScrollPane sideScroll = new JScrollPane(untimed, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sideScroll.setBorder(BorderFactory.createEmptyBorder());
        side.add(sideScroll, BorderLayout.CENTER);
        // 말풍선과 펫 자리(side의 SOUTH)는 S5d가 더한다.
        add(side, BorderLayout.EAST);
    }

    /** 보이는 주의 첫날. */
    public LocalDate weekStart() {
        return start;
    }

    /** 오늘이 든 주의 첫날(설정의 한 주 시작, D-91). */
    private LocalDate thisWeekStart() {
        return Rules.weekStart(data.today(), data.getWeekStart());
    }

    /** 색 뜻 하나: 12x12 색 칸과 12 크기 글. */
    private static JLabel swatch(Color color, String text) {
        JLabel l = new JLabel(text, new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
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

    /** 보이는 주를 newStart로 옮기고 다시 그린다. */
    public void move(LocalDate newStart) {
        start = newStart;
        redraw.run();
    }

    /** 기간 글, 요일 머리, "오늘"과 "자동 배치" 단추, 오른쪽 목록, 시간표를 보이는 주로 다시 그린다. */
    public void refresh() {
        // D-91: 한 주 시작을 바꾸면 보던 주의 첫날이 든 새 주로 바꾼다.
        start = Rules.weekStart(start, data.getWeekStart());
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
            if (d.equals(data.today())) {
                label.setOpaque(true);
                label.setBackground(Theme.DAY_SELECTED);
            }
            header.add(label);
        }
        untimed.show(data.getBook().untimedOfWeek(start));
        autoPlace.setEnabled(AutoPlacer.place(data.getBook(), start, LocalDateTime.now(clock)).count() > 0);
        table.repaint();
        header.revalidate();
    }
}
