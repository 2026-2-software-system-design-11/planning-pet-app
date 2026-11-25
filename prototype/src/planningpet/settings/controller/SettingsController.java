package planningpet.settings.controller;

import planningpet.app.controller.AppController;
import planningpet.app.model.Store;

/** 설정 controller(05 설정). 설정 화면의 칸을 받아 앱 데이터의 설정 값을 바꾼다. */
public final class SettingsController {
    private final AppController control;
    private final Store store;

    public SettingsController(AppController control, Store store) {
        this.control = control;
        this.store = store;
    }

    /** D-86. "달력에 고정 일정 보이기". 홈 달력 칸의 수에 고정 일정도 센다. */
    public void showFixedOnCalendar(boolean on) {
        control.change(() -> store.calendarFixed = on);
    }
}
