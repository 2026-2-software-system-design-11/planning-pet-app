package planningpet.common.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.Dimension;
import java.io.IOException;
import org.junit.jupiter.api.Test;

/** 디자인 시스템 5절의 진행 막대. 번호는 docs/porting/home.md S3i 2절. 기준 그림은 proto/bar-*.png다. */
class BarTest {
    private static Bar ratio(double r) {
        Bar b = new Bar(0, 8);
        b.set(r);
        return b;
    }

    @Test
    void eBar1Ratio() throws IOException {
        for (double r : new double[] {0, 0.5, 1}) {
            Bar b = ratio(r);
            assertEquals(r, b.ratio());
            Snap.assertLooksLikePrototype("bar-r" + r + "-120x8", b, 120, 8);
        }
    }

    @Test
    void eBar2TinyRatio() throws IOException {
        Snap.assertLooksLikePrototype("bar-r0.03-120x8", ratio(0.03), 120, 8);
        Snap.assertLooksLikePrototype("bar-r0.0-120x8", ratio(0.004), 120, 8);
    }

    @Test
    void eBar3OutOfRange() throws IOException {
        double[][] cases = {{-1, 0}, {Double.NEGATIVE_INFINITY, 0}, {2, 1}, {Double.POSITIVE_INFINITY, 1}};
        for (double[] c : cases) {
            Bar b = ratio(c[0]);
            assertEquals(c[1], b.ratio());
            Snap.assertLooksLikePrototype("bar-r" + c[1] + "-120x8", b, 120, 8);
        }
        Bar nan = ratio(Double.NaN);
        assertEquals(Double.NaN, nan.ratio());
        Snap.assertLooksLikePrototype("bar-r0.0-120x8", nan, 120, 8);
    }

    @Test
    void eBar6HeightAndWidth() throws IOException {
        for (int[] s : new int[][] {{120, 20}, {300, 12}, {120, 4}, {0, 8}, {10, 8}, {40, 8}, {0, 0}}) {
            Snap.assertLooksLikePrototype("bar-r0.5-" + s[0] + "x" + s[1], ratio(0.5), s[0], s[1]);
        }
    }

    @Test
    void eBar7Track() throws IOException {
        Bar b = new Bar(0, 8);
        assertSame(b, b.track(Theme.TRACK_DEEP));
        b.set(0.5);
        Snap.assertLooksLikePrototype("bar-deep-r0.5-120x8", b, 120, 8);
    }

    @Test
    void eBar8NewBar() throws IOException {
        Bar room = new Bar(70, 10);
        assertEquals(new Dimension(70, 10), room.getPreferredSize());
        assertEquals(new Dimension(20, 10), room.getMinimumSize());
        assertFalse(room.isOpaque());
        Bar card = new Bar(0, 8);
        assertEquals(new Dimension(0, 8), card.getPreferredSize());
        assertEquals(new Dimension(20, 8), card.getMinimumSize());
        assertEquals(0, card.ratio());
        Snap.assertLooksLikePrototype("bar-r0.0-120x8", card, 120, 8);
    }
}
