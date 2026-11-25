package planningpet.schedule.view;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 시작 시각과 걸리는 시간 칸에 쳐 넣은 글을 값으로 읽는다(D-98, 06의 32번). */
public final class Typed {
    /** 받지 않을 때 걸리는 시간 칸 아래의 빨간 글. */
    public static final String MINUTES_BAD = "숫자(분)로 넣어 주세요(예: 30)";
    private static final Pattern LEADING_NUMBER = Pattern.compile("\\s*(\\d+).*", Pattern.DOTALL);

    private Typed() {
    }

    /** 칸에 넣을 값과 칸 아래 글. 받지 않으면 value가 null이고 글은 빨간 글, 받으면 흐린 글이다(맞추지 않았으면 null). */
    public record Read(Object value, String notice) {
        public boolean rejected() {
            return value == null;
        }
    }

    /** 걸리는 시간 칸. 글 앞의 숫자를 분으로 읽고 10분부터 720분 안의 5분 값으로 맞춘다. */
    public static Read minutes(String text) {
        Matcher m = LEADING_NUMBER.matcher(text);
        if (!m.matches()) {
            return new Read(null, MINUTES_BAD);
        }
        String digits = m.group(1).replaceFirst("^0+(?=\\d)", "");
        int n = digits.length() > 6 ? Integer.MAX_VALUE : Integer.parseInt(digits);
        if (n < 10) {
            return new Read(10, "최소 10분부터 넣을 수 있어요");
        }
        if (n > 720) {
            return new Read(720, "최대 720분(12시간)까지 넣을 수 있어요");
        }
        int r = Math.round(n / 5f) * 5;
        return new Read(r, r == n ? null : "5분 단위로 맞췄어요(" + r + "분)");
    }
}
