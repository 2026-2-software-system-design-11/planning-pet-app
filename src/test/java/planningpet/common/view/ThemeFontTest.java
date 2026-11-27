package planningpet.common.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Theme의 글꼴 함수. 번호는 docs/porting/theme.md 2절. */
class ThemeFontTest {
    private static String look(Font f) {
        return f.getFontName() + " " + f.getSize2D() + " " + f.getStyle();
    }

    /** 글꼴 파일을 못 읽을 때 가는 시스템 글꼴(디자인 시스템 3절). */
    private static String fallbackFamily() {
        List<String> names = List.of(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        return names.contains("Malgun Gothic") ? "Malgun Gothic"
                : names.contains("Apple SD Gothic Neo") ? "Apple SD Gothic Neo" : Font.DIALOG;
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

    @Test
    void eFont3MissingFileFallsBackToSystemFont() {
        String family = fallbackFamily();
        for (String weight : List.of("Regular", "Medium", "Bold", "ExtraBold")) {
            Font f = Theme.load("/assets/fonts/Pretendard-없음.otf", weight);
            int style = weight.equals("Regular") || weight.equals("Medium") ? Font.PLAIN : Font.BOLD;
            assertEquals(family + " 12.0 " + style, f.getFamily() + " " + f.getSize2D() + " " + f.getStyle(), weight);
        }
    }

    @Test
    void eFont4BrokenFileFallsBackToSystemFont() {
        Font f = Theme.load("/assets/fonts/Pretendard-LICENSE.txt", "Bold");
        assertEquals(fallbackFamily() + " 12.0 " + Font.BOLD, f.getFamily() + " " + f.getSize2D() + " " + f.getStyle());
        assertEquals(Font.PLAIN, Theme.load("/assets/fonts/Pretendard-LICENSE.txt", "Medium").getStyle());
    }

    @Test
    void eFont5EmojiAndHanjaAreNotInPretendard() {
        Font f = Theme.font(14);
        assertEquals(List.of(0, 0, -1, -1, -1, -1, -1, -1), List.of("😀", "漢字", "플래닝펫 할 일", "Planning Pet 0123", "…", "★",
                "♥", "→").stream().map(f::canDisplayUpTo).toList());
    }
}
