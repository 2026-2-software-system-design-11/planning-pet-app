package planningpet.schedule.view;

import planningpet.schedule.model.Task;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.IntStream;

/** 시작 시각 칸(일정 입력 창 5번, 시각 고치기 창 1번)의 값. 앱 하루의 분(D-77)과 칸 글을 오간다. */
public final class Times {
    private static final String NEXT_DAY = "(다음 날)";

    private Times() {
    }

    /** 00:00부터 23:55, 이어서 "00:00(다음 날)"부터 "03:55(다음 날)"까지 5분 단위(D-38, D-77). */
    public static List<String> values() {
        return IntStream.range(0, Task.DAY_END / 5).mapToObj(i -> value(i * 5)).toList();
    }

    /** 앱 하루의 분을 "01:30", 24시간을 넘으면 "01:30(다음 날)"로. */
    public static String value(int minute) {
        return Task.hhmm(minute) + (minute >= Task.DAY ? NEXT_DAY : "");
    }

    /** 칸의 값을 앱 하루의 분으로. */
    public static int minuteOf(Object value) {
        String text = (String) value;
        boolean next = text.endsWith(NEXT_DAY);
        LocalTime t = LocalTime.parse(next ? text.substring(0, text.length() - NEXT_DAY.length()) : text);
        return (next ? Task.DAY : 0) + t.getHour() * 60 + t.getMinute();
    }

    /** 다음 날 값이면 칸 옆에 적는 실제 날과 시각 "(토 01:30)"(D-77). 아니면 빈 글. */
    public static String real(LocalDate date, int minute) {
        if (minute < Task.DAY) {
            return "";
        }
        LocalDate day = date.plusDays(1);
        return "(" + "월화수목금토일".charAt(day.getDayOfWeek().getValue() - 1) + " " + Task.hhmm(minute) + ")";
    }

    /** 끝 시각 안내 "15:30에 끝나요", 자정을 넘으면 "01:00(다음 날)에 끝나요"(D-73, D-77). */
    public static String endHint(int start, int minutes) {
        return Task.endText(start + minutes) + "에 끝나요";
    }
}
