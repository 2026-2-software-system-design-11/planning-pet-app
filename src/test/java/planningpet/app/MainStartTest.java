package planningpet.app;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import planningpet.app.model.AppData;
import planningpet.app.model.DataStore;
import planningpet.app.model.LoadResult;
import planningpet.app.model.LoadResult.Reason;
import planningpet.app.store.SqliteDataStore;
import planningpet.app.view.Tray;
import planningpet.common.view.Card;
import planningpet.common.view.Snap;
import planningpet.common.view.Theme;
import planningpet.home.view.MainScreen;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.PetState;
import planningpet.pet.view.PickDialog;
import planningpet.schedule.model.Category;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Task;
import planningpet.schedule.model.TaskDraft;
import planningpet.schedule.view.TaskRows;
import planningpet.settings.view.SettingsScreen;

/** Main.start의 갈래. 번호는 docs/porting/main.md 2절. 지금은 11-16(월) 10:00이고, 창은 띄우지 않고 부른 차례만 적는다. */
class MainStartTest {
    private static final ZoneId ZONE = ZoneId.systemDefault();
    private static final Clock CLOCK = Clock.fixed(LocalDateTime.of(2026, 11, 16, 10, 0).atZone(ZONE).toInstant(), ZONE);
    @TempDir
    Path folder;
    private final List<String> calls = new ArrayList<>();
    private JComponent root;
    private PickDialog pick;
    private Runnable closed;
    /** 메인 창이 넣은 신호 뒤의 일. */
    private Runnable signalled;
    /** 타이머를 켠 트레이. */
    private Tray tray;
    /** 트레이를 달았는지(shell.tray가 돌려줄 값). */
    private boolean trayOn;
    /** 메인 창의 닫기 버튼과 Cmd+Q에 이은 일. */
    private Runnable closing;
    private Runnable quitKey;
    private final Main.Shell shell = new Main.Shell() {
        public void unreadable(Reason r, Path f) { calls.add("못 읽음 창 " + r + " " + f); }

        public void pick(PickDialog p, Runnable c) {
            pick = p;
            closed = c;
            calls.add("고르기 창");
        }

        public void main(JComponent r, Runnable c) {
            root = r;
            closing = c;
            calls.add("메인 창");
        }

        public void hide() { calls.add("숨기기"); }

        public void quitKey(Runnable quit) {
            quitKey = quit;
            calls.add("Cmd+Q");
        }

        public boolean tray(Tray tray) {
            calls.add("트레이");
            return trayOn;
        }

        public void timer(Tray t) {
            tray = t;
            calls.add("타이머");
        }

        public void saveFailed(String text) { calls.add(text); }

        public void exit(int code) { calls.add("끝내기 " + code); }

        public void signal(Runnable shown) { signalled = shown; }

        public void front() { calls.add("앞으로"); }
    };

    /** Main.main처럼 부품을 만들기 전에 Theme를 건다. 검사마다 되돌린다. */
    @BeforeEach
    void install() {
        Theme.install();
    }

    @AfterEach
    void undoInstall() {
        for (Object key : List.copyOf(UIManager.getLookAndFeelDefaults().keySet())) {
            UIManager.put(key, null);
        }
        UIManager.put("swing.boldMetal", null);
    }

    /** 고양이 "나비", lastOpenDay 11-15, 연속 5일. */
    private static AppData saved() {
        AppData d = new AppData(CLOCK);
        d.addPet(new PetState(PetKind.CAT, "나비"));
        d.setActiveKind(PetKind.CAT);
        d.setLastOpenDay(LocalDate.of(2026, 11, 15));
        d.setStreak(5);
        return d;
    }

    /** 폴더의 SqliteDataStore로 연다. */
    private void run() {
        Main.start(new SqliteDataStore(folder, CLOCK), folder, CLOCK, shell, false);
    }

    private long files() throws IOException {
        try (var list = Files.list(folder)) {
            return list.count();
        }
    }

    /** 카드 밖 패널(Theme.clear가 투명하게 하는 것). */
    private static List<JPanel> panels(Container c, List<JPanel> into) {
        if (c instanceof JPanel p && !(p instanceof Card)) {
            into.add(p);
        }
        for (Component child : c.getComponents()) {
            if (child instanceof Container k) {
                panels(k, into);
            }
        }
        return into;
    }

    private static <T> List<T> parts(Component c, Class<T> type) {
        List<T> out = new ArrayList<>(type.isInstance(c) ? List.of(type.cast(c)) : List.of());
        if (c instanceof Container k) {
            for (Component child : k.getComponents()) {
                out.addAll(parts(child, type));
            }
        }
        return out;
    }

    private static String at(Rectangle r) {
        return r.x + "," + r.y + " " + r.width + "x" + r.height;
    }

    /** result를 돌려주고, 저장하면 "저장"을 적거나 failing이면 실패하는 저장소. */
    private DataStore store(LoadResult result, boolean failing) {
        return new DataStore() {
            public LoadResult load() { return result; }
            public void save(AppData d) throws IOException {
                if (failing) {
                    throw new IOException("디스크가 찼다");
                }
                calls.add("저장");
            }
        };
    }

    @Test
    void eMain11Loaded() {
        AppData data = saved();
        Main.start(store(LoadResult.loaded(data), false), folder, CLOCK, shell, false);
        assertEquals(List.of("Cmd+Q", "트레이", "메인 창", "앞으로", "저장", "타이머"), calls);
        assertEquals(List.of(6, LocalDate.of(2026, 11, 16)), List.of(data.getStreak(), data.getLastOpenDay()));
        List<JPanel> panels = panels(root, new ArrayList<>());
        panels.forEach(p -> assertFalse(p.isOpaque(), p.toString()));
        panels.get(1).setOpaque(true);
        data.changed();
        assertFalse(panels.get(1).isOpaque(), "알림마다 Theme.clear를 다시 건다");
    }

    @Test
    void eMain20SignalAfterMain() {
        AppData data = saved();
        Main.start(store(LoadResult.loaded(data), false), folder, CLOCK, shell, false);
        int[] heard = {0};
        data.onChange(() -> heard[0]++);
        signalled.run();
        assertEquals(List.of("Cmd+Q", "트레이", "메인 창", "앞으로", "저장", "타이머"), calls);
        assertEquals(List.of(1, 6), List.of(heard[0], data.getStreak()));
    }

    @Test
    void eTray8MenuQuitEndsApp() {
        Main.start(store(LoadResult.loaded(saved()), false), folder, CLOCK, shell, false);
        tray.menu().get("끝내기").run();
        assertEquals(List.of("Cmd+Q", "트레이", "메인 창", "앞으로", "저장", "타이머", "끝내기 0"), calls);
    }

    @Test
    void eMain21AutostartStaysInTray() {
        AppData data = saved();
        trayOn = true;
        Main.start(store(LoadResult.loaded(data), false), folder, CLOCK, shell, true);
        assertEquals(List.of("Cmd+Q", "트레이", "메인 창", "타이머"), calls);
        assertEquals(List.of(5, LocalDate.of(2026, 11, 15)), List.of(data.getStreak(), data.getLastOpenDay()));
        tray.menu().get("열기").run();
        assertEquals(List.of("Cmd+Q", "트레이", "메인 창", "타이머", "앞으로", "저장"), calls);
        assertEquals(6, data.getStreak());
    }

    @Test
    void eMain22AutostartWithoutTrayOpens() {
        AppData data = saved();
        Main.start(store(LoadResult.loaded(data), false), folder, CLOCK, shell, true);
        assertEquals(List.of("Cmd+Q", "트레이", "메인 창", "앞으로", "저장", "타이머"), calls);
        assertEquals(6, data.getStreak());
    }

    @Test
    void eMain23CloseWithoutTrayEndsApp() {
        Main.start(store(LoadResult.loaded(saved()), false), folder, CLOCK, shell, false);
        closing.run();
        assertEquals(List.of("Cmd+Q", "트레이", "메인 창", "앞으로", "저장", "타이머", "끝내기 0"), calls);
    }

    @Test
    void eMain24CmdQEndsApp() {
        Main.start(store(LoadResult.loaded(saved()), false), folder, CLOCK, shell, false);
        quitKey.run();
        assertEquals(List.of("Cmd+Q", "트레이", "메인 창", "앞으로", "저장", "타이머", "끝내기 0"), calls);
    }

    @Test
    void eOne5SignalDuringPick() {
        run();
        assertEquals(List.of("고르기 창"), calls);
        assertNull(signalled);
    }

    @Test
    void eMain12LoadedSaveFails() {
        AppData data = saved();
        Main.start(store(LoadResult.loaded(data), true), folder, CLOCK, shell, false);
        assertEquals(List.of("Cmd+Q", "트레이", "메인 창", "앞으로", "연속 접속 저장에 실패해서 바꾸기 전으로 돌렸어요.", "타이머"), calls);
        assertEquals(List.of(5, LocalDate.of(2026, 11, 15)), List.of(data.getStreak(), data.getLastOpenDay()));
    }

    @Test
    void eMain13FirstRun() throws IOException {
        run();
        assertEquals(List.of("고르기 창"), calls);
        assertEquals(0, files());
    }

    @Test
    void eMain14FirstRunStart() throws IOException {
        run();
        pick.dog.doClick();
        pick.start.doClick();
        assertEquals(List.of("고르기 창", "Cmd+Q", "트레이", "메인 창", "앞으로", "타이머"), calls);
        AppData read = new SqliteDataStore(folder, CLOCK).load().data();
        assertEquals(List.of("DOG 바둑이"), read.getPets().stream().map(p -> p.getKind() + " " + p.getName()).toList());
        assertEquals(List.of(1, LocalDate.of(2026, 11, 16)), List.of(read.getStreak(), read.getLastOpenDay()));
    }

    @Test
    void eMain15FirstRunClose() throws IOException {
        run();
        closed.run();
        assertEquals(List.of("고르기 창", "끝내기 0"), calls);
        assertEquals(0, files());
    }

    @Test
    void eMain16FirstRunSaveFails() {
        Main.start(store(LoadResult.firstRun(), true), folder, CLOCK, shell, false);
        pick.start.doClick();
        assertEquals(List.of("고르기 창", "반려동물 고르기 저장에 실패해서 바꾸기 전으로 돌렸어요."), calls);
    }

    @Test
    void eMain17FirstRunExamples() throws IOException {
        run();
        pick.examples.doClick();
        assertEquals(List.of("고르기 창"), calls);
        assertEquals(0, files());
    }

    @Test
    void eMain18Unreadable() throws IOException {
        Path file = folder.resolve(SqliteDataStore.FILE);
        Files.writeString(file, "not a db");
        run();
        assertEquals(List.of("못 읽음 창 NOT_DATA_FILE " + file, "끝내기 1"), calls);
        assertEquals("not a db", Files.readString(file));
        assertEquals(1, files());
    }

    @Test
    void eMain19UnreadableNewer() throws IOException, SQLException {
        Path file = folder.resolve(SqliteDataStore.FILE);
        try (Connection db = DriverManager.getConnection("jdbc:sqlite:" + file); Statement s = db.createStatement()) {
            s.execute("PRAGMA user_version = 99");
        }
        byte[] before = Files.readAllBytes(file);
        run();
        assertEquals(List.of("못 읽음 창 NEWER_VERSION " + file, "끝내기 1"), calls);
        assertArrayEquals(before, Files.readAllBytes(file));
        assertEquals(1, files());
    }

    /** 홈이 메뉴 "홈"으로 붙고 알림마다 다시 그리기가 Theme.clear보다 먼저 돈다(docs/porting/home.md S3ac E-SCREEN-15). */
    @Test
    void eScreen15HomeAttached() {
        AppData data = saved();
        data.getBook().add(new TaskDraft(Kind.TASK, "보고서", Category.OTHER, LocalDate.of(2026, 11, 16), null, null, 30, "", null, null,
                false, null, null, false));
        Main.start(store(LoadResult.loaded(data), false), folder, CLOCK, shell, false);
        data.changed();
        Snap.layOut(root, 1000, 680);
        MainScreen home = parts(root, MainScreen.class).get(0);
        assertEquals("[홈, 설정] 174,92 804x564 [오늘도 계획을 세우고, 조금씩 성장해요!] 0",
                parts(root, JToggleButton.class).stream().filter(b -> b.getClass() == JToggleButton.class).map(AbstractButton::getText).toList() + " "
                        + at(SwingUtilities.convertRectangle(home.getParent(), home.getBounds(), root)) + " "
                        + parts(root, JLabel.class).stream().map(JLabel::getText).filter(t -> t != null && t.startsWith("오늘도")).toList() + " "
                        + panels(parts(home, TaskRows.class).get(0), new ArrayList<>()).stream().filter(JPanel::isOpaque).count());
    }

    /** 설정이 메뉴 "설정"으로 붙고 알림마다 다시 그리기가 Theme.clear보다 먼저 돈다(docs/porting/settings.md S10b E-SET-14). */
    @Test
    void eSet14SettingsAttached() {
        AppData data = saved();
        Main.start(store(LoadResult.loaded(data), false), folder, CLOCK, shell, false);
        data.setWeekStart(java.time.DayOfWeek.SUNDAY);
        data.changed();
        SettingsScreen settings = parts(root, SettingsScreen.class).get(0);
        assertEquals("[플래닝펫을 내게 맞게 바꿔요.] 일요일 0", parts(root, JLabel.class).stream().map(JLabel::getText).filter("플래닝펫을 내게 맞게 바꿔요."::equals).toList()
                + " " + parts(settings, javax.swing.JRadioButton.class).stream().filter(AbstractButton::isSelected).map(AbstractButton::getText).findFirst().orElse("")
                + " " + panels(settings, new ArrayList<>()).stream().filter(JPanel::isOpaque).count());
    }

    /** 06 T-LIST-9(D-94)를 창 1000x680의 홈 목록으로 본다. 오늘은 11-19 09:00이다. 화면이 홈뿐이라 목록이 10 높다(S3ac 2절). */
    @Test
    void tList9TitlesInWindow() {
        Clock at = Clock.fixed(LocalDateTime.of(2026, 11, 19, 9, 0).atZone(ZONE).toInstant(), ZONE);
        LocalDate d12 = LocalDate.of(2026, 11, 12);
        AppData data = new AppData(at, new ArrayList<>(List.of(
                new Task("a", 1, Kind.TASK, "졸업 작품 기획서", d12, null, 60, "", false, Category.OTHER, null, null, LocalDate.of(2026, 11, 19),
                        d12, false, null, 0, 0, null, null),
                new Task("b", 2, Kind.TASK, "E사 지원서", LocalDate.of(2026, 11, 18), LocalTime.of(9, 0), 120, "", false, Category.OTHER,
                        null, null, LocalDate.of(2026, 11, 22), LocalDate.of(2026, 11, 16), false, null, 0, 0, null, null))), List.of());
        data.addPet(new PetState(PetKind.CAT, "나비"));
        data.setActiveKind(PetKind.CAT);
        Main.start(store(LoadResult.loaded(data), false), folder, at, shell, false);
        Snap.layOut(root, 1000, 680);
        TaskRows rows = parts(root, TaskRows.class).get(0);
        List<String> got = new ArrayList<>(List.of(at(rows.getParent().getParent().getBounds())));
        for (int i : new int[] {0, 2}) {
            JPanel row = (JPanel) rows.getComponent(i);
            got.add(((TaskRows.TitleLabel) row.getComponent(1)).shown() + " "
                    + ((JLabel) row.getComponent(3)).getText().replaceAll("<[^>]+>", "") + " " + at(row.getComponent(3).getBounds()));
        }
        assertEquals(List.of("16,67 383x402", "졸업 작품 기획서 ~11/19까지 오늘 마감 6,33 373x15", "E사 지원서 ~11/22까지 18일에 하려던 일 6,33 373x15"),
                got);
    }
}
