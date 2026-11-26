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

    /** 칸에 넣을 값과 칸 아래 글. 받지 않으면 value가 null이고 글은 빨간 글, 받으면 흐린 글이다(맞추지 않았으면 null). days는 옮길 날 수. */
    public record Read(Object value, String notice, int days) {
        public Read(Object value, String notice) {
            this(value, notice, 0);
        }

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
        } else if (text.replace(" ", "").equals("반시간")) {
            return new Read(30, "30분으로 읽었어요");
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
    private static final Pattern SPOKEN = Pattern.compile("(오전|오후|아침|낮|저녁|밤|새벽)?\\s*(\\d{1,2})\\s*(?:시|:)\\s*(?:(반)|(\\d{1,2})\\s*분?)?");

    /**
     * 시작 시각 칸. 목록 값과 그 앞부분("14"), "09:03", "930", "오후 2시 반"을 앱 하루의 5분 값으로 읽는다.
     * 다음 날 04:00을 넘으면 03:55(다음 날)로 맞추고, 반올림은 날을 넘지 않는다(23:58은 23:55). 맞춘 까닭은 값을 정한 하나만 쓴다.
     */
    public static Read start(String text, java.time.LocalDate day) {
        return start(text, day, null);
    }

    /** due가 있으면(기간 할 일) "밤 4시"로 마감 다음 날로 넘기지 않고 그날 끝 03:55(다음 날)로 맞춘다(D-98). */
    public static Read start(String text, java.time.LocalDate day, java.time.LocalDate due) {
        String t = text.strip();
        String f = fill(t);
        if (f != null) {
            return new Read(f, null);
        }
        boolean next = t.endsWith(NEXT_DAY);
        String body = next ? t.substring(0, t.length() - NEXT_DAY.length()).strip() : t;
        int m = minuteOf(body);
        if (m < 0) {
            return new Read(null, START_BAD);
        }
        m += next && m < Task.DAY ? Task.DAY : 0;
        int last = Task.DAY_END - 5;
        if (m >= Task.DAY_END && !next) {
            // D-98: "밤 4시", "밤 5시"는 다음 달력 날의 그 시각이다.
            java.time.LocalDate d = day.plusDays(1);
            if (due != null && d.isAfter(due)) {
                return new Read(Times.value(last), "마감(" + due.getDayOfMonth() + "일) 안에 두려고 03:55로 맞췄어요");
            }
            String v = Times.value(Math.min((int) Math.round((m - Task.DAY) / 5.0) * 5, Task.DAY - 5));
            // D-98: 일정의 달과 다르면 달을 붙인다("12월 1일(화)").
            String month = d.getMonthValue() == day.getMonthValue() ? "" : d.getMonthValue() + "월 ";
            return new Read(v, month + d.getDayOfMonth() + "일(" + "월화수목금토일".charAt(d.getDayOfWeek().getValue() - 1) + ") " + v
                    + planningpet.common.model.Rules.josa(v, "으로", "로") + " 읽었어요", 1);
        }
        int r = (int) Math.round(m / 5.0) * 5;
        // D-98: 까닭이 둘이면 값을 정한 까닭을 보인다. 다음 날 04:00을 넘으면 하루 끝이다.
        if (m >= Task.DAY_END || r > last && m >= Task.DAY) {
            return new Read(Times.value(last), "하루는 새벽 4시에 끝나서 " + Times.value(last) + "로 맞췄어요");
        }
        r = Math.min(r, m < Task.DAY ? Task.DAY - 5 : last);
        String v = Times.value(r);
        Matcher w = SPOKEN.matcher(body);
        String said = w.matches() ? w.group(1) : null;
        if ("오전".equals(said) && w.group(2).equals("12")) {
            return new Read(v, "오전 12시는 밤 자정" + (r == m ? ", " : "이라 ") + v + "로 읽었어요");
        }
        String as = v + planningpet.common.model.Rules.josa(v, "으로", "로") + " 읽었어요";
        if ("오전".equals(said) && Integer.parseInt(w.group(2)) > 12) {
            return new Read(v, "오전을 빼고 " + as);
        }
        if (body.matches("[1-8]:?\\d{2}")) {
            return new Read(v, as);
        }
        return new Read(v, r == m ? null : "5분 단위로 맞췄어요(" + v + ")");
    }

    /** 쳐 넣는 동안 시작 시각 칸에 채워 보일 값(D-98). 한 글자 1~8은 오후 시각, 아니면 그 앞부분으로 시작하는 첫 목록 값. */
    public static String fill(String t) {
        if (t.matches("[1-8]")) {
            return Times.value((Integer.parseInt(t) + 12) * 60);
        }
        return t.isEmpty() ? null : Times.values().stream().filter(v -> v.startsWith(t)).findFirst().orElse(null);
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
            // D-98: 앞에 0 없이 친 한 자리 1~8시("730", "7:30")는 "7시 30분"처럼 오후다.
            h += g != 5 && c.group(g).matches("[1-8]") ? 12 : 0;
        } else if (w.matches()) {
            h = hour(w.group(1), Integer.parseInt(w.group(2)));
            most = 29;
            min = w.group(3) != null ? 30 : w.group(4) != null ? Integer.parseInt(w.group(4)) : 0;
        } else {
            return -1;
        }
        return h < 0 || h > most || min > 59 ? -1 : h * 60 + min;
    }

    /** "N시"의 시(D-98). 새벽과 밤의 1~3시, 밤의 4~5시, 밤과 오전의 12시는 다음 날이라 24를 더한다. 아침 12시는 낮 12시다. */
    private static int hour(String word, int n) {
        if (n > 12) {
            return n > 23 ? -1 : n;
        }
        return switch (word == null ? "" : word) {
            case "오전" -> n == 12 ? 24 : n;
            case "아침" -> n;
            case "오후", "저녁" -> n % 12 + 12;
            case "새벽" -> n == 12 ? 24 : n <= 3 ? n + 24 : n;
            case "밤" -> n == 12 ? 24 : n <= 5 ? n + 24 : n + 12;
            default -> n == 0 || n >= 9 ? n : n + 12;
        };
    }
}
