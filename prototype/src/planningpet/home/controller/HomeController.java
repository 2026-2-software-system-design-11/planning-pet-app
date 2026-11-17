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

    /** 날짜를 고르면 펫이 그 날을 말한다. */
    public void selectDay(LocalDate date) {
        control.change(() -> store.say(date.equals(store.today()) ? talk.usual() : talk.otherDate(date)));
    }
}
