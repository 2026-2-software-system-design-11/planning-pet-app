package planningpet.common.view;

import java.io.IOException;
import org.junit.jupiter.api.Test;

/** 원 아이콘 버튼. 번호는 docs/porting/home.md S3c 2절. 기준 그림은 proto/cbtn-*.png이고 글이 없다. */
class CircleButtonTest {
    private static CircleButton button(Icons.Kind kind, int size, boolean plain) {
        return new CircleButton(kind, size, "팁", plain);
    }

    private static void looks(String name, CircleButton b, int w, int h) throws IOException {
        Snap.assertLooksLikePrototype("cbtn-" + name, b, w, h);
    }

    @Test
    void eCbtn1WhiteButton() throws IOException {
        looks("left-32", button(Icons.Kind.LEFT, 32, false), 32, 32);
        looks("right-32", button(Icons.Kind.RIGHT, 32, false), 32, 32);
        looks("paw-40", button(Icons.Kind.PAW, 40, false), 40, 40);
    }

    @Test
    void eCbtn2OverAndDown() throws IOException {
        CircleButton b = button(Icons.Kind.RIGHT, 32, false);
        b.getModel().setRollover(true);
        looks("over", b, 32, 32);
        b.getModel().setArmed(true);
        b.getModel().setPressed(true);
        looks("down", b, 32, 32);
    }
}
