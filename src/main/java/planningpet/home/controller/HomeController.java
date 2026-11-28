package planningpet.home.controller;

import java.time.LocalDate;
import planningpet.app.controller.AppController;
import planningpet.app.model.AppData;
import planningpet.pet.model.PetTalk;

/** 홈 controller(07). 메인 달력에서 날짜를 고르는 동작(UC-09)을 받는다(docs/porting/home.md S3b). */
public final class HomeController {
    private final AppController app;
    private final PetTalk talk;

    public HomeController(AppData data, AppController app) {
        this.app = app;
        this.talk = new PetTalk(data);
    }

    /**
     * 고른 날의 수 말(오늘이면 USUAL, 아니면 OTHER_DATE)을 그날을 세는 날로 말한다. 그날 목록이 바뀌면 다시 센다(D-106). 펫 말만
     * 바꾸므로 change를 거치지 않고 알림만 보낸다(06 읽는 법). 고른 날은 달력이 setHomeDay로 잇는다.
     */
    public void pick(LocalDate date) {
        app.say(talk.dayLine(date).get(), date);
    }
}
