package planningpet.schedule.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import planningpet.common.model.AppDay;

/** 06 14번. 일정 입력 창과 빠른 추가, 시각 고치기 창에 미리 채울 값. */
public final class DefaultDates {
    /** 시작 칸의 마지막 값(D-77): 다음 날 03:55. */
    public static final int LAST_START = Task.DAY_END - 5;

    private DefaultDates() {
    }

    /**
     * 미리 채울 날짜. 메인이면 고른 날짜, 일정관리면 보이는 주(shownStart부터 7일)에 오늘이 있을 때 오늘, 없을 때 그 주 첫날이다.
     * 메인이면 shownStart를, 일정관리면 selected를 보지 않는다.
     */
    public static LocalDate forAdd(Screen screen, LocalDate selected, LocalDate shownStart, LocalDate today) {
        if (screen == Screen.MAIN) {
            return selected;
        }
        return !today.isBefore(shownStart) && today.isBefore(shownStart.plusDays(7)) ? today : shownStart;
    }

    /**
     * 기본 시작 시각(앱 하루의 분, D-77). 지금의 달력 날짜면 지금을 5분 단위로 올린 시각과 09:00 가운데 늦은 것, 앱의 오늘인데 지금이
     * 다음 날 새벽(달력 날짜가 다름)이면 지금을 5분 단위로 올린 다음 날 시각, 아니면 09:00이다. 다음 날 03:55까지다. 초가 있으면 그
     * 분을 지난 것으로 보고 올린다.
     */
    public static int defaultStart(LocalDate date, LocalDateTime now) {
        int m = now.getHour() * 60 + now.getMinute() + (now.getSecond() > 0 ? 1 : 0);
        m = (m + 4) / 5 * 5;
        if (date.equals(now.toLocalDate())) {
            return Math.min(Math.max(m, 9 * 60), LAST_START);
        }
        if (date.equals(now.toLocalDate().minusDays(1)) && date.equals(AppDay.of(now))) {
            return Math.min(Task.DAY + m, LAST_START);
        }
        return 9 * 60;
    }
}
