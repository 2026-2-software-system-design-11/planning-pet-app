package planningpet.schedule.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static planningpet.schedule.model.Tasks.d;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.List;
import org.junit.jupiter.api.Test;
import planningpet.app.model.AppData;
import planningpet.app.model.Apps;
import planningpet.common.view.Snap;
import planningpet.common.view.Theme;
import planningpet.schedule.model.Task;

/** 일정관리 시간표. 번호는 docs/porting/schedule.md S5b 2절. 오늘은 11-18(수) 10:00, 보이는 주는 11-16부터, 날 칸 폭은 100이다. */
class TimetablePanelTest {
    private static final int H = TimetablePanel.HOUR * TimetablePanel.HOURS;
    private AppData data;
    private TimetablePanel panel;

    /** 일정 tasks가 있는 앱 데이터로 11-16 주의 시간표를 만들어 폭 752로 그린다. */
    private BufferedImage show(Task... tasks) {
        data = Apps.data("2026-11-18T10:00", tasks);
        panel = new TimetablePanel(data);
        panel.monday = d(16);
        return Snap.paint(panel, 752, H);
    }

    @Test
    void eTbl1SizeAndBack() {
        BufferedImage img = show();
        assertEquals(new Dimension(300, 1232), panel.getPreferredSize());
        int white = Theme.CARD_TOP.getRGB();
        assertEquals(List.of(white, white, Theme.DAY.getRGB(), white), List.of(img.getRGB(102, 20), img.getRGB(40, 20),
                img.getRGB(302, 20), img.getRGB(402, 20)));
        panel.monday = d(23);
        img = Snap.paint(panel, 752, H);
        assertEquals(List.of(white, white), List.of(img.getRGB(302, 20), img.getRGB(702, 20)));
    }
}
