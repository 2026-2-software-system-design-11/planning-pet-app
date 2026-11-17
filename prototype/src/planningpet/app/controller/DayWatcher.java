package planningpet.app.controller;

import planningpet.app.view.App;
import planningpet.home.view.MainScreen;
import java.time.LocalDate;

/** 06의 23번. 켜 둔 채 앱 날짜가 바뀌면 화면을 새 날에 맞춘다. 프로토타입 시계는 멈춰 있어서 QA가 시계를 옮긴 뒤 부른다. */
public final class DayWatcher {
    private final App app;
    private final MainScreen main;
    private LocalDate last;

    public DayWatcher(App app, MainScreen main) {
        this.app = app;
        this.main = main;
        last = app.store.today();
    }

    /** 30초 타이머가 부른다. 마지막 확인 때와 앱 날짜가 다르면 한 번 알리고 true. */
    public boolean tick() {
        LocalDate today = app.store.today();
        if (today.equals(last)) {
            return false;
        }
        // 고른 날짜가 옛 오늘이었으면 새 오늘로, 다른 날을 보고 있었으면 그대로 둔다(T-DAYW-4).
        if (last.equals(main.calendar.selected())) {
            main.calendar.pick(today);
        }
        last = today;
        // 프로토타입 창은 늘 보이므로 브리핑한다(D-23).
        app.store.say(app.talk.onWindowShown(app.game));
        app.store.changed();
        return true;
    }
}
