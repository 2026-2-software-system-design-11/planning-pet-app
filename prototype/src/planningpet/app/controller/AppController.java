package planningpet.app.controller;

import planningpet.app.model.Store;
import planningpet.app.model.Visits;
import planningpet.pet.model.Talk;
import java.time.LocalDate;

/** 앱 controller(06의 15번, 22번). 도메인 controller는 Model을 바꾸는 일을 change로 감싼다. */
public final class AppController {
    private final Store store;
    private final Talk talk;

    public AppController(Store store, Talk talk) {
        this.store = store;
        this.talk = talk;
    }

    /** 06의 22번. 변경을 적용하고 데이터 변경 알림을 한 번 보낸다. 프로토타입은 저장하지 않는다. */
    public void change(Runnable edit) {
        edit.run();
        store.changed();
    }

    /** 06의 15번. 창이 보이게 될 때. 그날 처음이면 연속 접속을 세고(12번) 브리핑한다. 펫이 한 말을 돌려준다. */
    public String windowShown() {
        LocalDate before = store.lastOpenDay;
        String line;
        if (store.today().equals(before)) {
            line = talk.usual();
        } else {
            Visits.recordOpen(store);
            line = talk.opening(before);
        }
        change(() -> store.say(line));
        return line;
    }
}
