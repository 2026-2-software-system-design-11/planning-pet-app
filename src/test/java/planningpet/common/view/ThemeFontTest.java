package planningpet.common.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Font;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Theme의 글꼴 함수. 번호는 docs/porting/theme.md 2절. */
class ThemeFontTest {
    private static String look(Font f) {
        return f.getFontName() + " " + f.getSize2D() + " " + f.getStyle();
    }

    @Test
    void eFont1ReadsPretendardFiles() {
        assertEquals(List.of("Pretendard Medium 14.0 0", "Pretendard Bold 14.0 0", "Pretendard ExtraBold 14.0 0",
                "Pretendard Regular 12.0 0"),
                List.of(look(Theme.font(14)), look(Theme.bold(14)), look(Theme.heavy(14)), look(Theme.regular(12))));
    }

    @Test
    void eFont2HeavyFromSize20() {
        assertEquals(List.of("Pretendard Medium 19.0 0", "Pretendard ExtraBold 20.0 0"),
                List.of(look(Theme.font(19)), look(Theme.font(20))));
    }
}
