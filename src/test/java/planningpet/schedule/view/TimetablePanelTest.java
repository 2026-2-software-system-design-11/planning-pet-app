package planningpet.schedule.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static planningpet.schedule.model.Tasks.d;
import static planningpet.schedule.model.Tasks.task;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import org.junit.jupiter.api.Test;
import planningpet.app.controller.AppController;
import planningpet.app.model.AppData;
import planningpet.app.model.Apps;
import planningpet.app.model.MemoryStore;
import planningpet.common.view.Snap;
import planningpet.common.view.Theme;
import planningpet.reward.controller.RewardController;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Task;
import planningpet.schedule.model.Tasks.Spec;

/** 일정관리 시간표. 번호는 docs/porting/schedule.md S5b 2절. 오늘은 11-18(수) 10:00, 보이는 주는 11-16부터, 날 칸 폭은 100이다. */
class TimetablePanelTest {
    private static final int H = TimetablePanel.HOUR * TimetablePanel.HOURS;
    private AppData data;
    private TimetablePanel panel;
    private final MemoryStore store = new MemoryStore();

    private BufferedImage show(Task... tasks) {
        return show(752, tasks);
    }

    /** 일정 tasks가 있는 앱 데이터로 11-16 주의 시간표를 만들어 폭 w로 그린다. */
    private BufferedImage show(int w, Task... tasks) {
        data = Apps.data("2026-11-18T10:00", tasks);
        RewardController reward = new RewardController(data, new AppController(data, store, e -> { }));
        panel = new TimetablePanel(data, reward);
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

    /** (x0, y0)부터 (x1, y1) 앞까지의 픽셀에서 빨강, 초록, 파랑 합의 가장 작은 값(가장 진한 글). */
    private static int darkest(BufferedImage img, int x0, int y0, int x1, int y1) {
        int min = 765;
        for (int y = y0; y < y1; y++) {
            for (int x = x0; x < x1; x++) {
                int p = img.getRGB(x, y);
                min = Math.min(min, (p >> 16 & 255) + (p >> 8 & 255) + (p & 255));
            }
        }
        return min;
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

    /** 06 19번의 검사 줄을 11-18에 그린 블록 자리. */
    @Test
    void tTt1to7Blocks() {
        assertEquals(List.of("A 253,396,98,44"), blocks(at("A", "09:00", 60).task()));
        assertEquals(List.of("A 253,396,48,44", "B 303,418,48,44"), blocks(at("A", "09:00", 60).task(), at("B", "09:30", 60).task()));
        assertEquals(List.of("A 253,396,98,44", "B 253,440,98,44"), blocks(at("A", "09:00", 60).task(), at("B", "10:00", 60).task()));
        assertEquals(List.of("A 253,396,48,88", "B 303,418,48,22", "C 303,440,48,22"), blocks(at("A", "09:00", 120).task(),
                at("B", "09:30", 30).task(), at("C", "10:00", 30).task()));
        assertEquals(List.of("A 303,396,48,44", "B 253,396,48,66"), blocks(at("A", "09:00", 60).task(),
                at("B", "09:00", 90).seq(2).task()));
        assertEquals(List.of("A 253,1034,48,66", "B 303,1078,48,22"), blocks(at("A", "23:30", 90).task(),
                at("B", "00:30", 30).nextDay().task()));
        assertEquals(List.of("자소서 초안 253,616,48,44", "면접 준비 303,638,48,44", "운동 253,792,98,44"), blocks(
                at("자소서 초안", "14:00", 60).task(), at("면접 준비", "14:30", 60).memo("질문 목록").task(), at("운동", "18:00", 60).task()));
    }

    @Test
    void eTbl5ThreeColumns() {
        assertEquals(List.of("A 253,396,31,44", "B 286,396,31,44", "C 319,396,31,44"), blocks(at("A", "09:00", 60).task(),
                at("B", "09:00", 60).seq(2).task(), at("C", "09:00", 60).seq(3).task()));
    }

    @Test
    void eTbl6Colors() {
        Color[][] want = {{Theme.MINT_SOFT, Theme.MINT_LINE}, {Theme.TRACK, Theme.CARD_LINE}, {Theme.DAY, Theme.CARD_LINE},
            {Theme.DAY, Theme.CARD_LINE}};
        BufferedImage img = show(task("ABC", d(16)).at("09:00").minutes(60).task(),
                task("ABC", d(17)).kind(Kind.FIXED).at("09:00").minutes(60).task(), at("ABC", "09:00", 60).done(d(18)).task(),
                task("ABC", d(19)).kind(Kind.FIXED).at("09:00").minutes(60).done(d(18)).task(),
                task("    ", d(20)).at("09:00").done(d(18)).task(), task("    ", d(21)).at("09:00").task());
        for (int i = 0; i < 4; i++) {
            int x = 53 + 100 * i;
            assertEquals(List.of(want[i][0].getRGB(), want[i][1].getRGB()), List.of(img.getRGB(x + 49, 436),
                    img.getRGB(x + 49, 439)), "블록 " + i);
            int ink = darkest(img, x + 20, 398, x + 98, 406); // 취소선(406) 위의 글
            assertTrue(i < 2 ? ink < 400 : ink >= 480, "글 " + i + " " + ink);
        }
        assertTrue(ink(img, 473, 405, 480, 412, Theme.DAY), "취소선");
        assertFalse(ink(img, 573, 405, 580, 412, Theme.MINT_SOFT));
    }

    @Test
    void eTbl7CheckBox() {
        BufferedImage img = show(at("A", "09:00", 60).task(), task("B", d(19)).at("09:00").done(d(18)).task());
        assertEquals(List.of(new Rectangle(257, 400, 12, 12), new Rectangle(357, 400, 12, 12)),
                List.copyOf(panel.boxAreas().keySet()));
        assertFalse(ink(img, 259, 402, 268, 411, Theme.CARD_TOP));
        assertTrue(ink(img, 359, 402, 368, 411, Theme.CARD_TOP));
        show(192, at("A", "09:00", 60).task(), at("B", "09:30", 60).task());
        assertEquals(List.of(new Rectangle(97, 400, 4, 12), new Rectangle(107, 422, 4, 12)),
                List.copyOf(panel.boxAreas().keySet()));
    }

    private static final String DIV = "<html><div style='width:280px'>";
    private static final String END = "</div></html>";

    /** 시간표 자리 (x, y)에 마우스를 올렸을 때의 툴팁. */
    private String tipAt(int x, int y) {
        return panel.getToolTipText(new MouseEvent(panel, MouseEvent.MOUSE_MOVED, 0, 0, x, y, 0, false));
    }

    @Test
    void eTbl8Narrow() {
        Task t = at("운동", "18:00", 60).task();
        int fit = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics().getFontMetrics().stringWidth("운동") + 24;
        show(52 + 7 * fit, t);
        assertNull(panel.tip(t));
        show(52 + 7 * (fit - 1), t);
        assertEquals(DIV + "<b>운동</b> 18:00~19:00" + END, panel.tip(t));
        Task a = at("A", "09:00", 60).task();
        show(a, at("B", "09:30", 60).task());
        assertEquals(DIV + "<b>A</b> 09:00~10:00" + END, panel.tip(a));
    }

    @Test
    void tTt7Tips() {
        Task a = at("자소서 초안", "14:00", 60).task();
        Task b = at("면접 준비", "14:30", 60).memo("질문 목록").task();
        Task c = at("운동", "18:00", 60).task();
        show(a, b, c);
        assertEquals(List.of(DIV + "<b>자소서 초안</b> 14:00~15:00" + END, DIV + "<b>면접 준비</b> 14:30~15:30<br>질문 목록" + END),
                List.of(tipAt(260, 620), tipAt(330, 670)));
        assertNull(tipAt(300, 800));
    }

    @Test
    void eTbl9Tips() {
        Task memo = at("A", "09:00", 60).memo("메모").task();
        show(memo, task("B", d(19)).at("09:00").task());
        assertEquals(TaskRows.memoTip(memo), tipAt(300, 420));
        assertNull(tipAt(400, 420));
        assertNull(tipAt(30, 420));
        assertNull(tipAt(300, 500));
        String e = "\uD83D\uDE00";
        String[][] memos = {{"<a>\n줄", "&lt;a&gt;<br>줄"}, {"가".repeat(201), "가".repeat(200) + "…"},
            {"가".repeat(199) + e + "나", "가".repeat(199) + e.charAt(0) + "…"}};
        for (String[] m : memos) {
            Task t = at("C", "09:00", 60).memo(m[0]).task();
            show(t, at("D", "09:30", 60).task());
            assertEquals(DIV + "<b>C</b> 09:00~10:00<br>" + m[1] + END, tipAt(260, 420));
        }
    }

    /** 시간표 자리 (x, y)에 마우스 사건 하나를 보낸다. */
    private void press(int id, int x, int y, int button, int count, boolean popup) {
        panel.dispatchEvent(new MouseEvent(panel, id, 0, 0, x, y, count, popup, button));
    }

    @Test
    void eTbl10CheckBox() {
        Task a = at("A", "09:00", 60).task();
        show(a);
        press(MouseEvent.MOUSE_CLICKED, 260, 405, MouseEvent.BUTTON1, 1, false);
        assertEquals(List.of(true, 1), List.of(a.isDone(), store.saves));
        press(MouseEvent.MOUSE_CLICKED, 260, 405, MouseEvent.BUTTON1, 1, false);
        assertEquals(List.of(false, 2), List.of(a.isDone(), store.saves));
    }

    /** 칸을 오른쪽 버튼으로 한 번 누르고, 두 블록의 칸이 겹친 자리를 누른다. */
    @Test
    void eTbl11BoxOtherPresses() {
        Task a = at("A", "09:00", 60).task();
        show(a);
        press(MouseEvent.MOUSE_CLICKED, 260, 405, MouseEvent.BUTTON3, 1, false);
        assertEquals(List.of(false, 0), List.of(a.isDone(), store.saves));
        Task s = at("S", "09:00", 10).task();
        Task b = at("B", "09:10", 30).task();
        show(s, b);
        assertEquals(List.of(new Rectangle(257, 400, 12, 12), new Rectangle(257, 407, 12, 12)),
                List.copyOf(panel.boxAreas().keySet()));
        press(MouseEvent.MOUSE_CLICKED, 260, 409, MouseEvent.BUTTON1, 1, false);
        assertEquals(List.of(false, true), List.of(s.isDone(), b.isDone()));
    }
}
