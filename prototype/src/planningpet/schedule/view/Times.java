package planningpet.schedule.view;

import planningpet.schedule.model.Task;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;

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

    /** "09:03"이나 "01:33(다음 날)"처럼 시각 꼴인 글을 가까운 5분 값의 앱 하루 분으로(D-95). 끝은 03:55(다음 날)다. 시각 꼴이 아니면 null. */
    public static Integer rounded(String text) {
        try {
            return Math.min(Math.round(minuteOf(text.strip()) / 5f) * 5, Task.DAY_END - 5);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * 시작 시각 칸이 쳐 넣은 글을 읽는 법(D-95). 목록에 있는 글은 그대로, 목록에 없는 시각 꼴은 가까운 5분 값으로 맞춰 rounded에
     * 알리고, "14"처럼 앞부분만 친 글은 그것으로 시작하는 첫 값이다. 그 밖에는 받지 않아 이전 값으로 돌아간다.
     */
    public static void roundTyped(JSpinner field, Consumer<String> rounded) {
        List<String> list = values();
        JFormattedTextField text = ((JSpinner.DefaultEditor) field.getEditor()).getTextField();
        text.setFormatterFactory(new DefaultFormatterFactory(new DefaultFormatter() {
            {
                setOverwriteMode(false);
            }

            @Override
            public Object stringToValue(String s) throws java.text.ParseException {
                if (list.contains(s)) {
                    return s;
                }
                Integer r = rounded(s);
                if (r != null) {
                    rounded.accept(value(r));
                    return value(r);
                }
                for (String v : list) {
                    if (!s.isEmpty() && v.startsWith(s)) {
                        return v;
                    }
                }
                throw new java.text.ParseException(s, 0);
            }
        }));
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
