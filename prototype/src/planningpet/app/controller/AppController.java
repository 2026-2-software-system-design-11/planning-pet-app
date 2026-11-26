package planningpet.app.controller;

import planningpet.app.model.Store;
import planningpet.app.model.Visits;
import planningpet.pet.model.Talk;
import java.time.LocalDate;

/** 앱 controller(06의 15번, 22번). 도메인 controller는 Model을 바꾸는 일을 change로 감싼다. */
public final class AppController {
    private final Store store;
    private final Talk talk;

    /** 메인 달력에서 고른 날(D-106). Main이 달력에 잇는다. */
    public java.util.function.Supplier<LocalDate> homeDay;

    public AppController(Store store, Talk talk) {
        this.store = store;
        this.talk = talk;
        this.homeDay = store::today;
    }

    /**
     * 06의 22번. 변경을 적용하고 데이터 변경 알림을 한 번 보낸다. 프로토타입은 저장하지 않는다. D-106: 변경이 새 말을 하지 않았고
     * 말풍선이 수를 말하는 날(다른 말이면 고른 날)의 목록이 바뀌었으면 그 날의 수를 다시 센다.
     */
    public void change(Runnable edit) {
        int seq = store.talkSeq;
        String said = store.talk;
        Store.Recount r = store.recount;
        LocalDate day = r != null ? r.day() : homeDay.get();
        String before = store.book.listKey(day);
        edit.run();
        // 새 말은 say가 번호를 올리고, 홈에만 남기는 말(sayAtHome)은 새 글이라 같은 글인지로 알아본다.
        if (seq == store.talkSeq && said == store.talk && !before.equals(store.book.listKey(day))) {
            store.retell(r != null ? r : talk.dayLine(day));
        }
        store.changed();
    }

    /** 펫 말만 바꾼다. 저장할 데이터가 아니므로 22번을 거치지 않고 알림만 보낸다(06 읽는 법). 수를 말하면 목록을 따라간다(D-106). */
    public void say(Store.Recount line) {
        store.say(line.line().get());
        store.recount = line;
        store.changed();
    }

    /** 06의 15번. 창이 보이게 될 때. 그날 처음이면 연속 접속을 세고(12번) 브리핑한다. 펫이 한 말을 돌려준다. */
    public String windowShown() {
        LocalDate before = store.lastOpenDay;
        if (store.today().equals(before)) {
            say(talk.dayLine(store.today()));
            return store.talk;
        }
        String[] line = new String[1];
        change(() -> {
            Visits.recordOpen(store);
            line[0] = talk.opening(before);
            store.say(line[0]);
            store.recount = talk.openingRecount(before); // D-106
        });
        return line[0];
    }
}
