package planningpet.home.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import java.awt.Rectangle;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import org.junit.jupiter.api.Test;
import planningpet.app.controller.AppController;
import planningpet.app.model.AppData;
import planningpet.home.controller.HomeController;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.PetState;

/** 홈 화면. 번호는 docs/porting/home.md S3ab 2절. */
class MainScreenTest {
    private static final ZoneId ZONE = ZoneId.systemDefault();
    private AppData data;

    /** 오늘 11-14 15:23, 고른 고양이 나비의 홈 화면. */
    private MainScreen screen() {
        data = new AppData(Clock.fixed(LocalDateTime.of(2026, 11, 14, 15, 23).atZone(ZONE).toInstant(), ZONE), List.of(), List.of());
        data.addPet(new PetState(PetKind.CAT, "나비"));
        data.setActiveKind(PetKind.CAT);
        AppController app = new AppController(data, null, e -> { });
        return new MainScreen(data, app, new HomeController(data, app));
    }

    private static String parts(MainScreen m, int w, int h) {
        m.setSize(w, h);
        m.doLayout();
        List<String> out = new ArrayList<>();
        for (Component c : m.getComponents()) {
            out.add(at(c.getBounds()));
        }
        return String.join(" | ", out);
    }

    private static String at(Rectangle r) {
        return r.x + "," + r.y + " " + r.width + "x" + r.height;
    }

    @Test
    void eScreen1Cards() {
        MainScreen m = screen();
        assertEquals(3, m.getComponentCount());
        assertSame(m.pet, m.getComponent(0));
        assertSame(m.calendar, ((Container) m.getComponent(1)).getComponent(0));
        assertEquals(new Insets(14, 16, 14, 16), ((JComponent) m.getComponent(1)).getInsets());
        assertEquals(new Insets(14, 16, 18, 16), ((JComponent) m.getComponent(2)).getInsets());
    }

    @Test
    void eScreen1Layout() {
        assertEquals("0,0 377x236 | 0,248 377x306 | 389,0 415x554", parts(screen(), 804, 554));
    }

    @Test
    void eScreen2Edges() {
        MainScreen m = screen();
        assertEquals("0,0 360x236 | 0,248 360x313 | 372,0 393x561", parts(m, 765, 561));
        assertEquals("0,0 360x236 | 0,248 360x314 | 372,0 394x562", parts(m, 766, 562));
        assertEquals("0,0 360x236 | 0,248 360x315 | 372,0 395x563", parts(m, 767, 563));
        assertEquals("0,0 759x399 | 0,411 759x541 | 771,0 846x952", parts(m, 1617, 952));
        assertEquals("0,0 760x400 | 0,412 760x541 | 772,0 846x953", parts(m, 1618, 953));
        assertEquals("0,0 760x400 | 0,412 760x788 | 772,0 1228x1200", parts(m, 2000, 1200));
    }

    @Test
    void eScreen3TinyPane() {
        MainScreen m = screen();
        assertEquals("0,0 360x236 | 0,248 360x-48 | 372,0 -72x200", parts(m, 300, 200));
        assertEquals("0,0 360x236 | 0,248 360x-248 | 372,0 -372x0", parts(m, 0, 0));
    }
}
