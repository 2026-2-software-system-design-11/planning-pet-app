package planningpet.schedule.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.time.LocalDate;
import javax.swing.JComponent;
import planningpet.app.model.AppData;
import planningpet.common.view.Theme;
import planningpet.schedule.model.Task;

/**
 * 일정관리의 일주일 시간표(07, 05 일정관리). 0시부터 다음 날 4시(28:00)까지 그린다(D-77). 옮기는 몫은 docs/porting/schedule.md
 * S5b. 끌어 놓기는 C2, 자동 배치 뒤 진하게 보이기는 S5d가 더한다.
 */
public final class TimetablePanel extends JComponent {
    /** 한 시간의 높이. */
    public static final int HOUR = 44;
    /** 왼쪽 시각 글 칸의 폭. */
    public static final int GUTTER = 52;
    /** 그리는 시간 수(D-77): 0시부터 다음 날 4시까지. */
    public static final int HOURS = Task.DAY_END / 60;
    /** 자정 아래 "다음 날" 칸의 바탕(D-77). */
    private static final Color NIGHT = new Color(0x1B, 0x22, 0x38, 14);
    private final AppData data;
    /** 보이는 주의 첫날(D-91). 일정관리 화면(S5c)이 주를 옮길 때 정한다. */
    LocalDate monday;

    public TimetablePanel(AppData data) {
        this.data = data;
        setPreferredSize(new Dimension(300, HOUR * HOURS));
    }

    /** h번째 시각 줄의 글. 자정 아래는 00:00부터 다시 적는다(D-77). */
    static String hourText(int h) {
        return String.format("%02d:00", h % 24);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        int day = (getWidth() - GUTTER) / 7;
        g.setColor(Theme.CARD_TOP);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int today = (int) (data.today().toEpochDay() - monday.toEpochDay());
        if (today >= 0 && today < 7) {
            g.setColor(Theme.DAY);
            g.fillRect(GUTTER + today * day, 0, day, getHeight());
        }
        // D-77: 자정 아래 네 시간은 그날 밤에 이어진 "다음 날" 칸이다. 옅게 칠하고 시각을 00:00부터 다시 적는다.
        g.setColor(NIGHT);
        g.fillRect(GUTTER, 24 * HOUR, getWidth() - GUTTER, (HOURS - 24) * HOUR);
        for (int h = 0; h < HOURS; h++) {
            g.setColor(Theme.CARD_LINE);
            g.drawLine(GUTTER, h * HOUR, getWidth(), h * HOUR);
            g.setColor(Theme.INK_DIM);
            g.drawString(hourText(h), 6, h * HOUR + 14);
        }
        Graphics2D mid = (Graphics2D) g.create();
        mid.setColor(Theme.INK_SUB);
        mid.drawLine(GUTTER, 24 * HOUR, getWidth(), 24 * HOUR);
        mid.setFont(Theme.font(11));
        mid.drawString("자정", 6, 24 * HOUR - 4);
        mid.drawString("다음 날", 6, 24 * HOUR + 28);
        mid.dispose();
        g.dispose();
    }
}
