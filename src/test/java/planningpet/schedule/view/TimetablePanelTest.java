package planningpet.schedule.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static planningpet.schedule.model.Tasks.d;
import static planningpet.schedule.model.Tasks.task;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import org.junit.jupiter.api.Test;
import planningpet.app.model.AppData;
import planningpet.app.model.Apps;
import planningpet.common.view.Snap;
import planningpet.common.view.Theme;
import planningpet.schedule.model.Task;
import planningpet.schedule.model.Tasks.Spec;

/** 일정관리 시간표. 번호는 docs/porting/schedule.md S5b 2절. 오늘은 11-18(수) 10:00, 보이는 주는 11-16부터, 날 칸 폭은 100이다. */
class TimetablePanelTest {
    private static final int H = TimetablePanel.HOUR * TimetablePanel.HOURS;
    private AppData data;
    private TimetablePanel panel;

    private BufferedImage show(Task... tasks) {
        return show(752, tasks);
    }

    /** 일정 tasks가 있는 앱 데이터로 11-16 주의 시간표를 만들어 폭 w로 그린다. */
    private BufferedImage show(int w, Task... tasks) {
        data = Apps.data("2026-11-18T10:00", tasks);
        panel = new TimetablePanel(data);
        panel.monday = d(16);
        return Snap.paint(panel, w, H);
    }

    /** 11-18 시각 at부터 minutes분인 할 일. */
    private static Spec at(String title, String at, int minutes) {
        return task(title, d(18)).at(at).minutes(minutes);
    }

    /** tasks를 그린 블록 자리 "제목 x,y,폭,높이"를 그린 차례로. */
    private List<String> blocks(Task... tasks) {
        show(tasks);
        return panel.blockAreas().entrySet().stream().map(e -> e.getValue().getTitle() + " " + e.getKey().x + ","
                + e.getKey().y + "," + e.getKey().width + "," + e.getKey().height).toList();
    }

    /** bottom 위에 top을 덧칠한 색. */
    private static int over(Color bottom, Color top) {
        BufferedImage one = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = one.createGraphics();
        g.setColor(bottom);
        g.fillRect(0, 0, 1, 1);
        g.setColor(top);
        g.fillRect(0, 0, 1, 1);
        g.dispose();
        return one.getRGB(0, 0);
    }

    /** (x0, y0)부터 (x1, y1) 앞까지에 bg와 다른 픽셀이 있는가. */
    private static boolean ink(BufferedImage img, int x0, int y0, int x1, int y1, Color bg) {
        for (int y = y0; y < y1; y++) {
            for (int x = x0; x < x1; x++) {
                if (img.getRGB(x, y) != bg.getRGB()) {
                    return true;
                }
            }
        }
        return false;
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

    @Test
    void eTbl1Night() {
        BufferedImage img = show();
        Color night = new Color(0x1B, 0x22, 0x38, 14);
        assertEquals(List.of(over(Theme.CARD_TOP, night), over(Theme.DAY, night), Theme.CARD_TOP.getRGB()),
                List.of(img.getRGB(102, 1110), img.getRGB(302, 1110), img.getRGB(48, 1110)));
    }

    @Test
    void eTbl2HourLines() {
        assertEquals(List.of("00:00", "09:00", "23:00", "00:00", "03:00"), List.of(TimetablePanel.hourText(0),
                TimetablePanel.hourText(9), TimetablePanel.hourText(23), TimetablePanel.hourText(24), TimetablePanel.hourText(27)));
        BufferedImage img = show();
        for (int h = 0; h < 28; h++) {
            int line = h == 24 ? Theme.INK_SUB.getRGB() : Theme.CARD_LINE.getRGB();
            assertEquals(List.of(line, line, Theme.CARD_TOP.getRGB()), List.of(img.getRGB(52, h * 44), img.getRGB(750, h * 44),
                    img.getRGB(51, h * 44)), "줄 " + h);
            assertTrue(ink(img, 6, h * 44 + 3, 52, h * 44 + 16, Theme.CARD_TOP), "글 " + h);
        }
        assertTrue(ink(img, 6, 1040, 52, 1055, Theme.CARD_TOP), "자정");
        assertTrue(ink(img, 6, 1073, 52, 1090, Theme.CARD_TOP), "다음 날");
        assertFalse(ink(img, 6, 1028, 52, 1040, Theme.CARD_TOP));
    }

    @Test
    void eTbl3OnTimetable() {
        Task a = at("A", "09:00", 60).task();
        Task b = at("B", "11:00", 30).done(d(18)).task();
        show(a, b, task("C", d(18)).task(), task("P", d(16)).at("09:00").period(d(16), d(20)).task(),
                task("Q", d(17)).at("09:00").period(d(17), d(20)).done(d(17)).task(), task("W", d(23)).at("09:00").task());
        assertEquals(List.of(a, b), List.copyOf(panel.blockAreas().values()));
    }

    @Test
    void eTbl4BlockRects() {
        assertEquals(List.of("M 153,1078,98,22", "A 253,396,98,44", "N 253,1034,98,66", "S 353,396,98,16"), blocks(
                at("A", "09:00", 60).task(), at("N", "23:30", 90).task(), task("M", d(17)).at("00:30").nextDay().task(),
                task("S", d(19)).at("09:00").minutes(10).task()));
    }

    @Test
    void eTbl5ThreeColumns() {
        assertEquals(List.of("A 253,396,31,44", "B 286,396,31,44", "C 319,396,31,44"), blocks(at("A", "09:00", 60).task(),
                at("B", "09:00", 60).seq(2).task(), at("C", "09:00", 60).seq(3).task()));
    }
}
