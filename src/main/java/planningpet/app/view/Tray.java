package planningpet.app.view;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import javax.swing.Timer;
import planningpet.app.controller.AppController;
import planningpet.app.controller.DayWatcher;
import planningpet.home.view.MainScreen;
import planningpet.schedule.model.Reminder;

/** 30초 타이머와 운영체제 알림(07). 06 23번 날 바뀜과 17번 일정 시작 알림을 본다(docs/porting/tray.md). */
public final class Tray {
    /** 06 17번과 23번의 30초. */
    static final int PERIOD_MS = 30_000;
    /** 05 그 밖의 창과 알림의 처음 창 닫기 안내(06 25번). */
    static final String NOTICE = "플래닝펫은 트레이에서 계속 알림을 보내요";
    private final AppController app;
    private final DayWatcher watcher;
    private final AppWindow window;
    private final MainScreen home;
    /** 창을 보이고 앞으로 가져온다. Main이 넣는다. */
    private final Runnable front;
    private final Timer timer;
    /** 운영체제 알림(제목, 내용). 트레이 아이콘을 달면 그 아이콘의 알림이 된다. 검사가 바꿔 끼운다. */
    BiConsumer<String, String> notify = (title, text) -> { };
    /** 마지막으로 띄운 일정 시작 알림의 할 일 날짜. 그 뒤 첫 사건을 알림 누르기로 본다. */
    private LocalDate alerted;

    public Tray(AppController app, DayWatcher watcher, Clock clock, AppWindow window, MainScreen home, Runnable front) {
        this.app = app;
        this.watcher = watcher;
        this.window = window;
        this.home = home;
        this.front = front;
        timer = new Timer(PERIOD_MS, e -> tick(LocalDateTime.now(clock)));
    }

    /** 타이머를 켠다. Main이 메인 창을 띄우고 15번을 부른 뒤 켠다(docs/porting/main.md). */
    public void start() {
        timer.start();
    }

    Timer timer() { return timer; }

    /** 날 바뀜을 먼저 보고 보낼 일정 시작 알림을 띄운다. 알림의 날은 할 일의 date다(D-77로 다음 날 시작해도 목록이 있는 날). */
    void tick(LocalDateTime now) {
        watcher.tick(now);
        for (Reminder r : app.dueReminders(now)) {
            notify.accept(r.title(), r.text());
            alerted = r.task().getDate();
        }
    }

    /**
     * 알림이나 트레이 아이콘의 사건. Java가 둘을 가르지 못해 알림 뒤 첫 사건을 알림 누르기로 보고, 창을 앞으로 가져와 홈에서 그 날을
     * 고르고 15번을 부른다(seq-uc13). 그 밖의 사건은 열기와 같다.
     */
    void clicked() {
        LocalDate day = alerted;
        alerted = null;
        if (day == null) {
            open();
            return;
        }
        front.run();
        window.show("홈");
        home.showDay(day);
        app.onWindowShown();
    }

    /** 처음 창 닫기 안내(UC-19 1a). 제목은 앱 이름이다. 남은 일정 시작 알림의 날짜를 지워 다음 사건은 열기와 같다. */
    public void notice() {
        alerted = null;
        notify.accept("플래닝펫", NOTICE);
    }

    /** 아이콘과 메뉴를 운영체제 트레이에 단다. 달면 true다. 화면에 아이콘이 생겨 검사하지 않는다(docs/porting/tray.md 3절). */
    public boolean install() {
        if (!SystemTray.isSupported()) {
            return false;
        }
        PopupMenu popup = new PopupMenu();
        menu().forEach((name, run) -> {
            MenuItem item = new MenuItem(name);
            item.addActionListener(e -> run.run());
            popup.add(item);
        });
        TrayIcon icon = new TrayIcon(AppWindow.icon(), AppWindow.TITLE, popup);
        icon.setImageAutoSize(true);
        icon.addActionListener(e -> clicked());
        try {
            SystemTray.getSystemTray().add(icon);
        } catch (AWTException e) {
            return false;
        }
        notify = (title, text) -> icon.displayMessage(title, text, TrayIcon.MessageType.INFO);
        return true;
    }

    /** 트레이 메뉴의 이름과 할 일(06 25번). 열기는 창을 보이고 15번을 부르고, 끝내기는 앱을 끝낸다. */
    public Map<String, Runnable> menu() {
        Map<String, Runnable> menu = new LinkedHashMap<>();
        menu.put("열기", this::open);
        menu.put("끝내기", app::quit);
        return menu;
    }

    private void open() {
        front.run();
        app.onWindowShown();
    }
}
