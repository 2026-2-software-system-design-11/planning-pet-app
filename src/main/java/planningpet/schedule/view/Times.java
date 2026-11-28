package planningpet.schedule.view;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.IntStream;
import planningpet.schedule.model.Task;

/** 시작 시각 칸(05 일정 입력 창 5번, 시각 고치기 창 1번)의 값. 앱 하루의 분(D-77)과 칸 글을 오간다(docs/porting/task-dialog.md S4b). */
final class Times {
    private static final String NEXT_DAY = "(다음 날)";

    private Times() {
    }

    /** 00:00부터 23:55, 이어서 "00:00(다음 날)"부터 "03:55(다음 날)"까지 5분 단위(D-38, D-77). */
    static List<String> values() {
        return IntStream.range(0, Task.DAY_END / 5).mapToObj(i -> value(i * 5)).toList();
    }

    /** 앱 하루의 분을 "01:30", 24시간을 넘으면 "01:30(다음 날)"로. */
    static String value(int minute) {
        return Task.hhmm(minute) + (minute >= Task.DAY ? NEXT_DAY : "");
    }

    /** 칸의 값을 앱 하루의 분으로. */
    static int minuteOf(Object value) {
        String text = (String) value;
        boolean next = text.endsWith(NEXT_DAY);
        LocalTime t = LocalTime.parse(next ? text.substring(0, text.length() - NEXT_DAY.length()) : text);
        return (next ? Task.DAY : 0) + t.getHour() * 60 + t.getMinute();
    }
}
