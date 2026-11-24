package planningpet.common.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 06의 작은 규칙들: 앱 날짜(1번), 한 주(데이터 절, D-91), 여러 도메인이 쓰는 글자 규칙(16번의 받침, 조사, 제목 줄임). */
public final class Rules {
    private Rules() {
    }

    public static LocalDate appDay(LocalDateTime time) {
        return time.minusHours(4).toLocalDate();
    }

    /** 06 데이터 절의 한 주(D-91): 그 날짜가 든 주의 첫날. 한 주는 first부터 7일이다. */
    public static LocalDate weekStart(LocalDate date, java.time.DayOfWeek first) {
        return date.with(java.time.temporal.TemporalAdjusters.previousOrSame(first));
    }

    /** 이름 마지막 글자가 받침 있는 한글이면 "이야", 그 밖은 "야". */
    public static String iya(String name) {
        return hasBatchim(name) ? "이야" : "야";
    }

    /** 마지막 글자를 읽을 때 받침이 있는지. 한글과 숫자(영, 일, 삼, 육, 칠, 팔)만 본다. */
    public static boolean hasBatchim(String word) {
        char last = word.charAt(word.length() - 1);
        if (last >= 0xAC00 && last <= 0xD7A3) {
            return (last - 0xAC00) % 28 != 0;
        }
        return "013678".indexOf(last) >= 0;
    }

    /** 마지막 글자에 받침이 있으면 withBatchim, 없으면 without. */
    public static String josa(String word, String withBatchim, String without) {
        return hasBatchim(word) ? withBatchim : without;
    }

    /** 15자를 넘으면 앞 14자 뒤에 "…". */
    public static String shortTitle(String title) {
        return title.length() > 15 ? title.substring(0, 14) + "…" : title;
    }
}
