package planningpet.schedule.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.format.DateTimeParseException;
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
}
