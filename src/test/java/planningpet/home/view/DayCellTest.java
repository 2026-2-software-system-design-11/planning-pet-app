package planningpet.home.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import planningpet.common.view.Snap;
import planningpet.common.view.Theme;
import planningpet.schedule.model.CellPreview;

/**
 * 홈 달력의 날짜 칸. 번호는 docs/porting/home.md S3e 2절. 기준 그림 proto/cell-*.png는 숫자와 수를 빈 글로 그린 프로토타입 사본이
 * 그렸고, 글이 든 배치는 투명 바탕에 그린 잉크의 자리, 색, 굵기로 본다.
 */
class DayCellTest {
    private static final CellPreview NONE = new CellPreview(0, true, List.of());
    private static final CellPreview TWO = new CellPreview(2, true, List.of());

    private static LocalDate d(int day) {
        return LocalDate.of(2026, 11, day);
    }

    private static DayCell cell(int day, CellPreview preview, boolean today, boolean picked) {
        return new DayCell(d(day), preview, today, picked);
    }

    private static void looks(String name, DayCell c, int w, int h) throws IOException {
        c.blankText();
        Snap.assertLaidOutLooksLikePrototype("cell-" + name + "-" + w + "x" + h, c, w, h);
    }

    private static BufferedImage paint(DayCell c, int w, int h) {
        c.setSize(w, h);
        BufferedImage img = new BufferedImage(w + 4, h + 4, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        c.paint(g);
        g.dispose();
        return img;
    }

    /** 줄 from부터 to 앞까지에서 색 c에 가까운 잉크: {왼쪽, 위, 오른쪽, 아래, 수, 가장 큰 알파, 꽉 찬 수}. 없으면 수가 0이다. */
    private static int[] ink(BufferedImage img, Color c, int from, int to) {
        int[] box = {9999, 9999, -1, -1, 0, 0, 0};
        for (int y = from; y < to; y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int p = img.getRGB(x, y);
                int dist = Math.abs((p >> 16 & 255) - c.getRed()) + Math.abs((p >> 8 & 255) - c.getGreen())
                        + Math.abs((p & 255) - c.getBlue());
                if (p >>> 24 == 0 || dist > 24) {
                    continue;
                }
                box = new int[] {Math.min(box[0], x), Math.min(box[1], y), Math.max(box[2], x), Math.max(box[3], y),
                    box[4] + 1, Math.max(box[5], p >>> 24), box[6] + (p == c.getRGB() ? 1 : 0)};
            }
        }
        return box;
    }

    @Test
    void eCell1Backgrounds() throws IOException {
        for (int[] s : new int[][] {{46, 44}, {60, 56}, {20, 30}, {0, 0}}) {
            looks("plain", cell(18, NONE, false, false), s[0], s[1]);
            looks("today", cell(18, NONE, true, false), s[0], s[1]);
            looks("picked", cell(18, NONE, false, true), s[0], s[1]);
            looks("today-picked", cell(18, NONE, true, true), s[0], s[1]);
        }
    }

    @Test
    void eCell5Bold() {
        int plain = ink(paint(cell(18, NONE, false, false), 46, 44), Theme.INK, 0, 44)[6];
        int picked = ink(paint(cell(18, NONE, false, true), 46, 44), Theme.INK, 0, 44)[6];
        int today = ink(paint(cell(18, NONE, true, false), 46, 44), Theme.INK, 0, 44)[6];
        assertTrue(picked > plain && today > plain, "굵은 숫자는 꽉 찬 픽셀이 많다");
        assertEquals(picked, today);
    }

    @Test
    void eCell6Text() {
        assertEquals("<html><center>18</center></html>", cell(18, NONE, false, false).getText());
        assertEquals("<html><center><b>18</b></center></html>", cell(18, NONE, false, true).getText());
        assertEquals("<html><center>18<br><font size='2' color='#28977F'>2개</font></center></html>",
                cell(18, TWO, false, false).getText());
        assertEquals("<html><center>18<br><font size='2' color='#8A96A0'>1개</font></center></html>",
                cell(18, new CellPreview(1, false, List.of()), true, false).getText());
        assertEquals("<html><center>20<br><font size='2' color='#28977F'>2개</font> <font size='2' color='#C0392B'>마감</font>"
                + "</center></html>", cell(20, new CellPreview(2, true, List.of("E사 지원서", "F사 서류")), false, false).getText());
        assertEquals("<html><center><b>21</b><br><font size='2' color='#28977F'>3개</font> <font size='2' color='#C0392B'>마감"
                + "</font></center></html>", cell(21, new CellPreview(3, true, List.of("G사")), false, true).getText());
    }

    @Test
    void eCell8WeekdayColors() {
        BufferedImage sunday = paint(cell(15, NONE, false, false), 46, 44);
        assertTrue(ink(sunday, Theme.SUNDAY, 0, 22)[4] > 0);
        assertEquals(0, ink(sunday, Theme.INK, 0, 44)[4]);
        BufferedImage saturday = paint(cell(21, NONE, false, false), 46, 44);
        assertTrue(ink(saturday, Theme.SATURDAY, 0, 44)[4] > 0);
        assertEquals(0, ink(saturday, Theme.INK, 0, 44)[4]);
        BufferedImage wednesday = paint(cell(18, NONE, false, false), 46, 44);
        assertTrue(ink(wednesday, Theme.INK, 0, 44)[4] > 0);
        assertEquals(0, ink(wednesday, Theme.SUNDAY, 0, 44)[4] + ink(wednesday, Theme.SATURDAY, 0, 44)[4]);
    }
}
