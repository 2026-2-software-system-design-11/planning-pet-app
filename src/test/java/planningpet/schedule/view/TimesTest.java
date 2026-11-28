package planningpet.schedule.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

/** 시작 시각 칸의 값과 끝 시각 글. 번호는 docs/porting/task-dialog.md S4b 2절. */
class TimesTest {
    @Test
    void eTimes1Values() {
        List<String> v = Times.values();
        assertEquals(336, v.size());
        assertEquals(List.of("00:00", "00:05", "23:55", "00:00(다음 날)", "03:55(다음 날)"),
                List.of(v.get(0), v.get(1), v.get(287), v.get(288), v.get(335)));
    }

    @Test
    void eTimes2ValueAndMinute() {
        int[] minutes = {0, 1435, 1440, 1530, 1675};
        assertEquals(List.of("00:00", "23:55", "00:00(다음 날)", "01:30(다음 날)", "03:55(다음 날)"),
                IntStream.of(minutes).mapToObj(Times::value).toList());
        for (int m : minutes) {
            assertEquals(m, Times.minuteOf(Times.value(m)));
        }
        assertThrows(DateTimeParseException.class, () -> Times.minuteOf("24:00"));
        assertThrows(DateTimeParseException.class, () -> Times.minuteOf("03:55(다음날)"));
        assertThrows(ClassCastException.class, () -> Times.minuteOf(30));
    }

    @Test
    void eTimes3Real() {
        LocalDate fri = LocalDate.of(2026, 11, 20);
        assertEquals(List.of("", "(토 00:00)", "(토 01:30)"), IntStream.of(1439, 1440, 1530).mapToObj(m -> Times.real(fri, m)).toList());
        assertEquals("(월 01:00)", Times.real(LocalDate.of(2026, 11, 22), 1500));
        assertEquals("(금 01:00)", Times.real(LocalDate.of(2026, 12, 31), 1500));
    }

    @Test
    void eTimes4EndHint() {
        int[][] in = {{900, 30}, {600, 45}, {1410, 30}, {1430, 10}, {1410, 90}, {1440, 10}, {1530, 30}, {960, 720}};
        assertEquals(List.of("15:30에 끝나요", "10:45에 끝나요", "24:00에 끝나요", "24:00에 끝나요", "01:00(다음 날)에 끝나요",
                "00:10(다음 날)에 끝나요", "02:00(다음 날)에 끝나요", "04:00(다음 날)에 끝나요"),
                Arrays.stream(in).map(h -> Times.endHint(h[0], h[1])).toList());
    }
}
