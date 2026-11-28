package planningpet.home.view;

import java.io.IOException;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import planningpet.common.view.Snap;

/**
 * 홈 달력의 날짜 칸. 번호는 docs/porting/home.md S3e 2절. 기준 그림 proto/cell-*.png는 숫자와 수를 빈 글로 그린 프로토타입 사본이
 * 그렸고, 글이 든 배치는 투명 바탕에 그린 잉크의 자리, 색, 굵기로 본다.
 */
class DayCellTest {
    private static LocalDate d(int day) {
        return LocalDate.of(2026, 11, day);
    }

    private static DayCell cell(int day, boolean today, boolean picked) {
        return new DayCell(d(day), today, picked);
    }

    private static void looks(String name, DayCell c, int w, int h) throws IOException {
        c.blankText();
        Snap.assertLaidOutLooksLikePrototype("cell-" + name + "-" + w + "x" + h, c, w, h);
    }

    @Test
    void eCell1Backgrounds() throws IOException {
        for (int[] s : new int[][] {{46, 44}, {60, 56}, {20, 30}, {0, 0}}) {
            looks("plain", cell(18, false, false), s[0], s[1]);
            looks("today", cell(18, true, false), s[0], s[1]);
            looks("picked", cell(18, false, true), s[0], s[1]);
            looks("today-picked", cell(18, true, true), s[0], s[1]);
        }
    }
}
