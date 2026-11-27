package planningpet.app.view;

import java.time.Clock;
import java.time.LocalDateTime;
import javax.swing.Timer;
import planningpet.app.controller.DayWatcher;

/** 30초 타이머(07). 지금은 06 23번 DayWatcher.tick만 부른다. 트레이 아이콘과 메뉴, 17번 알림은 S11이 더한다. */
public final class Tray {
    /** 06 17번과 23번의 30초. */
    static final int PERIOD_MS = 30_000;
    private final Timer timer;

    public Tray(DayWatcher watcher, Clock clock) {
        timer = new Timer(PERIOD_MS, e -> watcher.tick(LocalDateTime.now(clock)));
    }

    /** 타이머를 켠다. 창과 함께 켜는 일은 S2의 Main 잇기가 한다. */
    public void start() {
        timer.start();
    }

    Timer timer() { return timer; }
}
