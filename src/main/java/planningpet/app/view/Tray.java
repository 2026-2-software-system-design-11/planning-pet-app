package planningpet.app.view;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.function.BiConsumer;
import javax.swing.Timer;
import planningpet.app.controller.AppController;
import planningpet.app.controller.DayWatcher;
import planningpet.schedule.model.Reminder;

/** 30초 타이머와 운영체제 알림(07). 06 23번 날 바뀜과 17번 일정 시작 알림을 본다(docs/porting/tray.md). */
public final class Tray {
    /** 06 17번과 23번의 30초. */
    static final int PERIOD_MS = 30_000;
    private final AppController app;
    private final DayWatcher watcher;
    private final Timer timer;
    /** 운영체제 알림(제목, 내용). 트레이 아이콘을 달면 그 아이콘의 알림이 된다. 검사가 바꿔 끼운다. */
    BiConsumer<String, String> notify = (title, text) -> { };

    public Tray(AppController app, DayWatcher watcher, Clock clock) {
        this.app = app;
        this.watcher = watcher;
        timer = new Timer(PERIOD_MS, e -> tick(LocalDateTime.now(clock)));
    }

    /** 타이머를 켠다. Main이 메인 창을 띄우고 15번을 부른 뒤 켠다(docs/porting/main.md). */
    public void start() {
        timer.start();
    }

    Timer timer() { return timer; }

    /** 날 바뀜을 먼저 보고 보낼 일정 시작 알림을 띄운다. */
    void tick(LocalDateTime now) {
        watcher.tick(now);
        for (Reminder r : app.dueReminders(now)) {
            notify.accept(r.title(), r.text());
        }
    }
}
