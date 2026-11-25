package planningpet.schedule.view;

import planningpet.schedule.model.Task;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 시작 시각과 걸리는 시간 칸에 쳐 넣은 글을 값으로 읽는다(D-98, 06의 32번). */
public final class Typed {
    /** 받지 않을 때 걸리는 시간 칸 아래의 빨간 글. */
    public static final String MINUTES_BAD = "숫자(분)로 넣어 주세요(예: 30)";
    private static final Pattern MINUTES = Pattern.compile("\\s*(\\d+)\\s*분?\\s*");
    private static final Pattern HOURS = Pattern.compile("\\s*(\\d{1,3}(?:\\.\\d{1,3})?)\\s*시간\\s*(?:(반)|(\\d{1,3})\\s*분)?\\s*");

    private Typed() {
    }

    /** 칸에 넣을 값과 칸 아래 글. 받지 않으면 value가 null이고 글은 빨간 글, 받으면 흐린 글이다(맞추지 않았으면 null). */
    public record Read(Object value, String notice) {
        public boolean rejected() {
            return value == null;
        }
    }

    /** 걸리는 시간 칸. "30", "30분", "1시간", "1시간 반", "1.5시간"을 분으로 읽고 10분부터 720분 안의 5분 값으로 맞춘다. */
    public static Read minutes(String text) {
        double n;
        Matcher m = MINUTES.matcher(text);
        Matcher h = HOURS.matcher(text);
        if (m.matches()) {
            n = m.group(1).length() > 6 ? 1e6 : Integer.parseInt(m.group(1));
        } else if (h.matches()) {
            n = Double.parseDouble(h.group(1)) * 60 + (h.group(2) != null ? 30 : 0)
                    + (h.group(3) != null ? Integer.parseInt(h.group(3)) : 0);
        } else {
            return new Read(null, MINUTES_BAD);
        }
        if (n < 10) {
            return new Read(10, "최소 10분부터 넣을 수 있어요");
        }
        if (n > 720) {
            return new Read(720, "최대 720분(12시간)까지 넣을 수 있어요");
        }
        int r = (int) Math.round(n / 5) * 5;
        return new Read(r, r == n ? null : "5분 단위로 맞췄어요(" + r + "분)");
    }

    /** 받지 않을 때 시작 시각 칸 아래의 빨간 글. */
    public static final String START_BAD = "14:00처럼 적어 주세요";
    private static final String NEXT_DAY = "(다음 날)";
    private static final Pattern CLOCK = Pattern.compile("(\\d{1,2}):(\\d{2})|(\\d{1,2}?)(\\d{2})|(\\d{1,2})");
    private static final Pattern SPOKEN = Pattern.compile("(오전|오후|아침|낮|저녁|밤|새벽)?\\s*(\\d{1,2})\\s*시\\s*(?:(반)|(\\d{1,2})\\s*분)?");

    /**
     * 시작 시각 칸. 목록 값과 그 앞부분("14"), "09:03", "930", "오후 2시 반"을 앱 하루의 5분 값으로 읽는다.
     * 다음 날 04:00을 넘으면 03:55(다음 날)로 맞추고, 반올림은 날을 넘지 않는다(23:58은 23:55).
     */
    public static Read start(String text) {
        String t = text.strip();
        for (String v : Times.values()) {
            if (!t.isEmpty() && v.startsWith(t)) {
                return new Read(v, null);
            }
        }
        boolean next = t.endsWith(NEXT_DAY);
        int m = minuteOf(next ? t.substring(0, t.length() - NEXT_DAY.length()).strip() : t);
        if (m < 0) {
            return new Read(null, START_BAD);
        }
        m += next && m < Task.DAY ? Task.DAY : 0;
        int last = Task.DAY_END - 5;
        if (m >= Task.DAY_END) {
            return new Read(Times.value(last), "하루는 새벽 4시에 넘어가요. " + Times.value(last) + "로 맞췄어요");
        }
        int r = Math.min((int) Math.round(m / 5.0) * 5, m < Task.DAY ? Task.DAY - 5 : last);
        return new Read(Times.value(r), r == m ? null : "5분 단위로 맞췄어요(" + Times.value(r) + ")");
    }

    /** 앱 하루의 분. 알아듣지 못하거나 시와 분이 범위 밖이면 -1. */
    private static int minuteOf(String s) {
        Matcher c = CLOCK.matcher(s);
        Matcher w = SPOKEN.matcher(s);
        int h;
        int min;
        int most = 23;
        if (c.matches()) {
            int g = c.group(1) != null ? 1 : c.group(3) != null ? 3 : 5;
            h = Integer.parseInt(c.group(g));
            min = g == 5 ? 0 : Integer.parseInt(c.group(g + 1));
        } else if (w.matches()) {
            h = hour(w.group(1), Integer.parseInt(w.group(2)));
            most = 27;
            min = w.group(3) != null ? 30 : w.group(4) != null ? Integer.parseInt(w.group(4)) : 0;
        } else {
            return -1;
        }
        return h < 0 || h > most || min > 59 ? -1 : h * 60 + min;
    }

    /** "N시"의 시(S54, S60~S62). 새벽과 밤의 1~3시, 밤 12시는 다음 날이라 24를 더한다. 13시부터는 그대로다. */
    private static int hour(String word, int n) {
        if (n > 12) {
            return n > 23 ? -1 : n;
        }
        return switch (word == null ? "" : word) {
            case "오전", "아침" -> n % 12;
            case "오후", "저녁" -> n % 12 + 12;
            case "새벽" -> n == 12 ? 24 : n <= 3 ? n + 24 : n;
            case "밤" -> n == 12 ? 24 : n <= 3 ? n + 24 : n <= 5 ? n : n + 12;
            default -> n == 0 || n >= 9 ? n : n + 12;
        };
    }
}
