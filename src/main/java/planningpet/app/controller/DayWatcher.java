package planningpet.app.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import planningpet.app.model.AppData;
import planningpet.common.model.AppDay;

/** 06 23번. 켜 둔 채 앱 날짜가 바뀌면 화면을 새 날에 맞춘다. 30초 타이머(app.view.Tray)가 부른다. */
public final class DayWatcher {
    private final AppData data;
    private final AppController app;
    /** 마지막 확인 때의 앱 날짜. 처음은 만들 때의 앱 날짜다. */
    private LocalDate lastDay;

    public DayWatcher(AppData data, AppController app) {
        this.data = data;
        this.app = app;
        lastDay = data.today();
    }

    /** 앱 날짜가 바뀌면 한 번 알린다. 창이 보이면 15번이 브리핑과 함께 알린다. 고른 날짜는 S3 달력이 옮긴다(app-window.md). */
    public void tick(LocalDateTime now) {
        LocalDate day = AppDay.of(now);
        if (day.equals(lastDay)) {
            return;
        }
        lastDay = day;
        if (app.isWindowVisible()) {
            app.onWindowShown();
        } else {
            data.changed();
        }
    }
}
