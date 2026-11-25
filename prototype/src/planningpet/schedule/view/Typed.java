package planningpet.schedule.view;

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
}
