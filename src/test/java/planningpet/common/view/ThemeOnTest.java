package planningpet.common.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import javax.swing.JButton;
import org.junit.jupiter.api.Test;

/** 공용 테마 걸고 되돌리기 ThemeOn(#518). */
class ThemeOnTest {
    private static String ui() {
        return new JButton().getUI().getClass().getSimpleName();
    }

    @Test
    void installsBeforeAndUndoesAfter() {
        String plain = ui();
        ThemeOn on = new ThemeOn();
        on.beforeEach(null);
        String during = ui();
        on.afterEach(null);
        assertEquals(List.of("RoundButtonUI", plain), List.of(during, ui()));
    }
}
