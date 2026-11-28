package planningpet.home.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import planningpet.app.model.AppData;
import planningpet.common.view.CircleButton;
import planningpet.common.view.Icons;
import planningpet.common.view.Theme;
import planningpet.schedule.model.CalendarCell;

/**
 * 홈 달력(05 홈 "달력", UC-09). 한 주는 설정의 한 주 시작(처음은 월요일)부터 7일이다(D-91). 제목 줄 왼쪽에 달 제목, 오른쪽에
 * "오늘" 버튼과 이전 달, 다음 달 꺾쇠가 있다(D-60). 날짜 칸은 그날 일의 수와 마감 점을 보인다(D-86, 06 26번).
 * 날짜 칸을 누르면 그날을 고른다(UC-09, docs/porting/home.md S3f~S3h).
 */
public final class CalendarPanel extends JPanel {
    private static final String DAYS = "일월화수목금토";
    private final AppData data;
    private final Consumer<LocalDate> onPick;
    /** 날짜 칸. 그달의 주 수만큼 줄이 있다. */
    final JPanel grid = new JPanel(new GridLayout(0, 7, 2, 2));
    /** 요일 줄. 한 주 시작(D-91)이 바뀌면 다시 놓는다. */
    final JPanel week = new JPanel(new GridLayout(1, 7, 2, 0));
    final JLabel title = new JLabel();
    final CircleButton prev = new CircleButton(Icons.Kind.LEFT, 32, "이전 달", false);
    final CircleButton next = new CircleButton(Icons.Kind.RIGHT, 32, "다음 달", false);
    /** D-60: 오늘이 든 달로 옮기고 오늘을 고른다. 이미 그렇다면 흐리다. */
    final JButton today = new JButton("오늘");
    private DayOfWeek shownStart;
    private YearMonth month;
    private LocalDate selected;

    /** onPick은 날짜 칸을 누르면 그날을 받는다. */
    public CalendarPanel(AppData data, Consumer<LocalDate> onPick) {
        super(new BorderLayout(0, 4));
        this.data = data;
        this.onPick = onPick;
        this.selected = data.today();
        this.month = YearMonth.from(selected);
        prev.addActionListener(e -> move(-1));
        next.addActionListener(e -> move(1));
        today.setFont(Theme.bold(12));
        today.setBorder(BorderFactory.createEmptyBorder(5, 12, 7, 12));
        today.addActionListener(e -> {
            selected = data.today();
            month = YearMonth.from(selected);
            onPick.accept(selected);
        });
        title.setIcon(Icons.icon(Icons.Kind.CALENDAR, 24, Theme.ICON));
        title.setIconTextGap(8);
        title.setFont(Theme.heavy(21));
        JPanel arrows = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        arrows.add(today);
        arrows.add(prev);
        arrows.add(next);
        JPanel head = new JPanel(new BorderLayout());
        head.add(title, BorderLayout.CENTER);
        head.add(arrows, BorderLayout.EAST);
        head.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.CARD_EDGE),
                BorderFactory.createEmptyBorder(0, 0, 8, 0)));
        JPanel top = new JPanel(new BorderLayout());
        top.add(head, BorderLayout.NORTH);
        top.add(week, BorderLayout.SOUTH);
        weekRow();
        add(top, BorderLayout.NORTH);
        add(grid, BorderLayout.CENTER);
    }

    /** 고른 날. 처음은 오늘이다. */
    public LocalDate selected() {
        return selected;
    }

    /** 보이는 달을 months만큼 옮긴다. 고른 날은 그대로다(05 입력 항목 2). */
    private void move(int months) {
        month = month.plusMonths(months);
        refresh();
    }

    /** 날짜 칸을 누르면 그날을 고르고 받은 함수에 넘긴다. 다시 그리기는 그 뒤의 알림이 한다(프로토타입과 같다). */
    private void choose(LocalDate date) {
        selected = date;
        onPick.accept(date);
    }

    /** 요일 줄을 한 주 시작부터 놓는다(D-91). */
    private void weekRow() {
        if (data.getWeekStart() == shownStart) {
            return;
        }
        shownStart = data.getWeekStart();
        week.removeAll();
        for (int i = 0; i < 7; i++) {
            int dow = shownStart.plus(i).getValue() % 7;
            JLabel day = new JLabel(String.valueOf(DAYS.charAt(dow)), SwingConstants.CENTER);
            day.setForeground(dow == 0 ? Theme.SUNDAY : dow == 6 ? Theme.SATURDAY : Theme.INK_SUB);
            day.setFont(Theme.bold(13));
            day.setBorder(BorderFactory.createEmptyBorder(6, 0, 2, 0));
            week.add(day);
        }
        week.revalidate();
    }

    /** 요일 줄의 글(첫 요일부터). */
    public String weekText() {
        StringBuilder b = new StringBuilder();
        for (java.awt.Component c : week.getComponents()) {
            b.append(((JLabel) c).getText());
        }
        return b.toString();
    }

    /** 한 주 시작, 고정 일정 설정, 일정이 바뀌면 부른다. 보이는 달의 칸을 다시 놓는다. 다른 달 자리는 빈 칸이다. */
    public void refresh() {
        weekRow();
        title.setText(month.getYear() + "년 " + month.getMonthValue() + "월");
        LocalDate today = data.today();
        boolean atToday = month.equals(YearMonth.from(today)) && today.equals(selected);
        this.today.setEnabled(!atToday);
        this.today.setToolTipText(atToday ? "오늘을 보고 있어요" : null);
        grid.removeAll();
        int offset = Math.floorMod(month.atDay(1).getDayOfWeek().getValue() - data.getWeekStart().getValue(), 7);
        int cells = (offset + month.lengthOfMonth() + 6) / 7 * 7;
        for (int cell = 0; cell < cells; cell++) {
            int day = cell - offset + 1;
            if (day < 1 || day > month.lengthOfMonth()) {
                grid.add(new JLabel());
                continue;
            }
            LocalDate date = month.atDay(day);
            grid.add(new DayCell(date, CalendarCell.preview(data.getBook(), date, data.isCalendarFixed()), date.equals(today),
                    date.equals(selected), this::choose));
        }
        grid.revalidate();
        grid.repaint();
    }
}
