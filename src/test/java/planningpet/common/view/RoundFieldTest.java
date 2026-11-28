package planningpet.common.view;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

/**
 * 둥근 입력칸. 번호는 docs/porting/pick.md 2절. 기준 그림은 글이 없는 칸만 두고(예시 글 없음, 빈 글), 글이 든 그림은
 * 운영체제마다 픽셀이 달라 테스트에 두지 않는다.
 */
class RoundFieldTest {
    private static RoundField pencil() {
        return new RoundField(Icons.Kind.PENCIL, null);
    }

    private static void looks(String name, RoundField f, int w, int h) throws IOException {
        Snap.assertLooksLikePrototype("field-" + name, f, w, h);
    }

    /** text를 친 칸을 300x40으로 그린 픽셀. */
    private static int[] pixels(RoundField f, String text) {
        f.setText(text);
        BufferedImage img = Snap.paint(f, 300, 40);
        return img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
    }

    @Test
    void eRound1Plain() throws IOException {
        RoundField f = pencil();
        assertEquals(new Insets(8, 40, 8, 16), f.getInsets());
        looks("pencil", f, 300, 40);
    }

    @Test
    void eRound2NoIcon() throws IOException {
        RoundField f = new RoundField(null, null);
        assertEquals(new Insets(8, 18, 8, 16), f.getInsets());
        assertFalse(f.isOpaque());
        assertEquals(Theme.font(14), f.getFont());
        looks("none", f, 300, 40);
    }

    @Test
    void eRound7Placeholder() {
        RoundField p = new RoundField(Icons.Kind.PENCIL, "할 일을 쓰고 Enter");
        assertFalse(Arrays.equals(pixels(pencil(), ""), pixels(p, "")), "빈 칸은 예시 글을 그린다");
        assertArrayEquals(pixels(pencil(), " "), pixels(p, " "), "공백 하나면 그리지 않는다");
        assertFalse(Arrays.equals(pixels(pencil(), ""), pixels(p, "")), "비우면 다시 그린다");
        assertArrayEquals(pixels(pencil(), ""), pixels(new RoundField(Icons.Kind.PENCIL, ""), ""));
    }
}
