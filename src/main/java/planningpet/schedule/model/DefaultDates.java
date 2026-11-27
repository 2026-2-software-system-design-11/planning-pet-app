package planningpet.schedule.model;

import java.time.LocalDate;

/** 06 14번. 일정 입력 창과 빠른 추가, 시각 고치기 창에 미리 채울 값. */
public final class DefaultDates {
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
}
