package planningpet.home.controller;

import planningpet.app.controller.AppController;
import planningpet.app.model.Store;
import planningpet.pet.model.Talk;
import java.time.LocalDate;

/** 홈 controller. 메인 달력에서 날짜를 고르는 동작(UC-09)을 받는다. */
public final class HomeController {
    private final AppController control;
    private final Store store;
    private final Talk talk;

    public HomeController(AppController control, Store store, Talk talk) {
        this.control = control;
        this.store = store;
        this.talk = talk;
    }

    /** 날짜를 고르면 펫이 그 날을 말한다. 펫 말만 바꾸므로 알림만 보낸다. 그날 목록이 바뀌면 다시 센다(D-106). */
    public void selectDay(LocalDate date) {
        control.say(talk.dayLine(date));
    }
}
