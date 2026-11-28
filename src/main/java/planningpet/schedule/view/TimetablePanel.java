package planningpet.schedule.view;

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
    private final AppData data;
    /** 보이는 주의 첫날(D-91). 일정관리 화면(S5c)이 주를 옮길 때 정한다. */
    LocalDate monday;

    public TimetablePanel(AppData data) {
        this.data = data;
        setPreferredSize(new Dimension(300, HOUR * HOURS));
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
        g.dispose();
    }
}
