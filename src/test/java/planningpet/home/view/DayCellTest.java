package planningpet.home.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import planningpet.common.view.Snap;
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
}
