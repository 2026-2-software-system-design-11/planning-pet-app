package planningpet.home.view;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import planningpet.app.model.AppData;
import planningpet.common.view.Theme;
import planningpet.schedule.model.CalendarCell;

/**
 * 홈 달력(05 홈 "달력", UC-09). 한 주는 설정의 한 주 시작(처음은 월요일)부터 7일이고 요일 줄과 칸이 그 요일부터 놓인다(D-91).
 * 날짜 칸은 그날 일의 수와 마감 점을 보인다(D-86, 06 26번). 제목 줄과 달 옮기기는 S3g, 누른 날 고르기와 다시 그리기 알림은
 * S3h가 더한다(docs/porting/home.md S3f, S3g).
 */
public final class CalendarPanel extends JPanel {
    private static final String DAYS = "일월화수목금토";
    private final AppData data;
    private final Consumer<LocalDate> onPick;
    /** 날짜 칸. 그달의 주 수만큼 줄이 있다. */
    final JPanel grid = new JPanel(new GridLayout(0, 7, 2, 2));
    /** 요일 줄. 한 주 시작(D-91)이 바뀌면 다시 놓는다. */
    final JPanel week = new JPanel(new GridLayout(1, 7, 2, 0));
    private DayOfWeek shownStart;
    private final YearMonth month;
    private final LocalDate selected;

    /** onPick은 날짜 칸을 누르면 그날을 받는다. */
    public CalendarPanel(AppData data, Consumer<LocalDate> onPick) {
        super(new BorderLayout(0, 4));
        this.data = data;
        this.onPick = onPick;
        this.selected = data.today();
        this.month = YearMonth.from(selected);
        JPanel top = new JPanel(new BorderLayout());
        top.add(week, BorderLayout.SOUTH);
        weekRow();
        add(top, BorderLayout.NORTH);
        add(grid, BorderLayout.CENTER);
    }

    /** 고른 날. 처음은 오늘이다. */
    public LocalDate selected() {
        return selected;
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
        grid.removeAll();
        LocalDate today = data.today();
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
                    date.equals(selected), onPick));
        }
        grid.revalidate();
        grid.repaint();
    }
}
