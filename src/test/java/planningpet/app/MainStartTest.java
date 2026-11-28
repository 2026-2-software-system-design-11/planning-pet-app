package planningpet.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.awt.Component;
import java.awt.Container;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import planningpet.app.model.AppData;
import planningpet.app.model.DataStore;
import planningpet.app.model.LoadResult;
import planningpet.app.view.Tray;
import planningpet.common.view.Card;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.PetState;

/** Main.start의 갈래. 번호는 docs/porting/main.md 2절. 지금은 11-16(월) 10:00이고, 창은 띄우지 않고 부른 차례만 적는다. */
class MainStartTest {
    private static final ZoneId ZONE = ZoneId.systemDefault();
    private static final Clock CLOCK = Clock.fixed(LocalDateTime.of(2026, 11, 16, 10, 0).atZone(ZONE).toInstant(), ZONE);
    @TempDir
    Path folder;
    private final List<String> calls = new ArrayList<>();
    private JComponent root;
    private final Main.Shell shell = new Main.Shell() {
        public void main(JComponent r) {
            root = r;
            calls.add("메인 창");
        }

        public void timer(Tray tray) { calls.add("타이머"); }

        public void saveFailed(String text) { calls.add(text); }
    };

    /** 고양이 "나비", lastOpenDay 11-15, 연속 5일. */
    private static AppData saved() {
        AppData d = new AppData(CLOCK);
        d.addPet(new PetState(PetKind.CAT, "나비"));
        d.setActiveKind(PetKind.CAT);
        d.setLastOpenDay(LocalDate.of(2026, 11, 15));
        d.setStreak(5);
        return d;
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
        Main.start(store(LoadResult.loaded(data), false), folder, CLOCK, shell);
        assertEquals(List.of("메인 창", "저장", "타이머"), calls);
        assertEquals(List.of(6, LocalDate.of(2026, 11, 16)), List.of(data.getStreak(), data.getLastOpenDay()));
        List<JPanel> panels = panels(root, new ArrayList<>());
        panels.forEach(p -> assertFalse(p.isOpaque(), p.toString()));
        panels.get(1).setOpaque(true);
        data.changed();
        assertFalse(panels.get(1).isOpaque(), "알림마다 Theme.clear를 다시 건다");
    }

    @Test
    void eMain12LoadedSaveFails() {
        AppData data = saved();
        Main.start(store(LoadResult.loaded(data), true), folder, CLOCK, shell);
        assertEquals(List.of("메인 창", "연속 접속 저장에 실패해서 바꾸기 전으로 돌렸어요.", "타이머"), calls);
        assertEquals(List.of(5, LocalDate.of(2026, 11, 15)), List.of(data.getStreak(), data.getLastOpenDay()));
    }
}
