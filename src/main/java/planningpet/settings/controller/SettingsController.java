package planningpet.settings.controller;

import java.io.IOException;
import java.time.DayOfWeek;
import planningpet.app.controller.AppController;
import planningpet.app.model.AppData;
import planningpet.settings.model.Autostart;

/** 설정 controller(05 설정, 07). 설정 화면의 칸을 받아 설정 값과 운영체제 등록을 바꾼다(docs/porting/settings.md S10a). */
public final class SettingsController {
    private final AppData data;
    private final AppController app;
    private final Autostart autostart;

    public SettingsController(AppData data, AppController app, Autostart autostart) {
        this.data = data;
        this.app = app;
        this.autostart = autostart;
    }

    /** D-86. "달력에 고정 일정 보이기". 홈 달력 칸의 수에 고정 일정도 센다. */
    public void setCalendarFixed(boolean on) {
        app.change("달력에 고정 일정 보이기", () -> data.setCalendarFixed(on));
    }

    /** D-91. "한 주 시작". 홈 달력, 일정관리의 주, 자동 배치 범위, 통계의 주, 마감 기본값이 따른다. */
    public void setWeekStart(DayOfWeek day) {
        app.change("한 주 시작", () -> data.setWeekStart(day));
    }

    /** 06 25번. 운영체제 등록만 바꾸고 저장할 데이터가 아니라 알림을 보내지 않는다. 거절되면 그 IOException을 화면이 받는다. */
    public void setAutostart(boolean on) throws IOException {
        autostart.set(on);
    }
}
