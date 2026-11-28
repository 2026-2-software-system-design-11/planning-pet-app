package planningpet.schedule.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static planningpet.schedule.model.Tasks.d;
import static planningpet.schedule.model.Tasks.task;

import java.awt.Dimension;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;
import planningpet.app.controller.AppController;
import planningpet.app.model.AppData;
import planningpet.app.model.Apps;
import planningpet.app.model.MemoryStore;
import planningpet.common.view.Snap;
import planningpet.common.view.Theme;
import planningpet.reward.controller.RewardController;
import planningpet.schedule.model.Task;

/** 일정관리 목록의 할 일 한 줄. 번호는 docs/porting/schedule.md S5a2 2절. 줄은 앱처럼 Theme.clear 뒤 폭 200으로 배치한다. */
class WeekRowTest {
    /** 수정, 삭제, 시각 함수가 받은 것. */
    private final List<String> heard = new ArrayList<>();

    /** now의 앱 데이터에 tasks를 둔 목록. 누름은 heard에 적는다. */
    private TaskRows rows(String now, Task... tasks) {
        AppData data = Apps.data(now, tasks);
        RewardController reward = new RewardController(data, new AppController(data, new MemoryStore(), e -> { }));
        return new TaskRows(data, reward, t -> heard.add("수정 " + t.getTitle()), t -> heard.add("삭제 " + t.getTitle()),
                (t, day, at) -> heard.add("시각 " + t.getTitle() + " " + day));
    }

    /** 오늘 11-18 10:00의 목록에서 t의 day 줄. */
    private JPanel row(Task t, LocalDate day) {
        return row(rows("2026-11-18T10:00", t), t, day);
    }

    /** 줄 앞 날짜와 목록의 날이 day인 줄을 만들어 Theme.clear 뒤 폭 200, 알맞은 높이로 배치한다. */
    private static JPanel row(TaskRows r, Task t, LocalDate day) {
        JPanel p = r.row(t, null, day, day);
        Theme.clear(p);
        Snap.layOut(p, 200, p.getPreferredSize().height);
        return p;
    }

    private static String title(JPanel row) {
        return ((JLabel) row.getComponent(1)).getText();
    }

    /** 시각 단추(오른쪽 칸의 첫째). */
    private static TimePill clock(JPanel row) {
        return (TimePill) ((JPanel) row.getComponent(2)).getComponent(0);
    }

    @Test
    void eWrow8RowDateAndClock() {
        JPanel p = row(task("과제", d(18)).task(), d(18));
        assertEquals("<html>11/18 과제</html>", title(p));
        assertEquals("", clock(p).getText());
        assertEquals(new Dimension(26, 26), clock(p).getPreferredSize());
        assertEquals("시각 정하기", clock(p).getToolTipText());
        assertEquals("<html>12/1 과제</html>", title(row(task("과제", d(31)).task(), d(31))));
        Task t = task("과제", d(18)).task();
        assertEquals("<html>과제</html>", title(rows("2026-11-18T10:00", t).row(t, "", null, d(18))));
    }
}
