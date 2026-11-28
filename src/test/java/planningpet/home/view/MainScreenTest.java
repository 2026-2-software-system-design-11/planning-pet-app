package planningpet.home.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.Container;
import java.awt.Insets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

    @Test
    void eScreen1Cards() {
        MainScreen m = screen();
        assertEquals(3, m.getComponentCount());
        assertSame(m.pet, m.getComponent(0));
        assertSame(m.calendar, ((Container) m.getComponent(1)).getComponent(0));
        assertEquals(new Insets(14, 16, 14, 16), ((JComponent) m.getComponent(1)).getInsets());
        assertEquals(new Insets(14, 16, 18, 16), ((JComponent) m.getComponent(2)).getInsets());
    }
}
