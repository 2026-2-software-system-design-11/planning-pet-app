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

    private static Bar cells(int done, int total) {
        Bar b = new Bar(0, 8);
        b.set(done, total);
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
    void eBar4Cells() throws IOException {
        int[][] cases = {{1, 4}, {0, 4}, {4, 4}, {3, 10}, {1, 1}, {5, 4}, {-1, 3}};
        double[] ratios = {0.25, 0, 1, 0.3, 1, 1, 0};
        for (int i = 0; i < cases.length; i++) {
            Bar b = cells(cases[i][0], cases[i][1]);
            assertEquals(ratios[i], b.ratio());
            assertEquals(cases[i][1], b.segments());
            Snap.assertLooksLikePrototype("bar-s" + cases[i][0] + "of" + cases[i][1] + "-120x8", b, 120, 8);
        }
    }

    @Test
    void eBar5CellEnds() throws IOException {
        int[][] cases = {{0, 0}, {5, 11}, {15, 11}, {2, -3}};
        double[] ratios = {0, 5 / 11.0, 1, 0};
        int[] segments = {0, 0, 0, -3};
        String[] looks = {"bar-r0.0-120x8", "bar-s5of11-120x8", "bar-r1.0-120x8", "bar-r0.0-120x8"};
        for (int i = 0; i < cases.length; i++) {
            Bar b = cells(cases[i][0], cases[i][1]);
            assertEquals(ratios[i], b.ratio());
            assertEquals(segments[i], b.segments());
            Snap.assertLooksLikePrototype(looks[i], b, 120, 8);
        }
        Bar back = cells(2, 4);
        back.set(0.25);
        assertEquals(0, back.segments());
        Snap.assertLooksLikePrototype("bar-reset-120x8", back, 120, 8);
        Bar to = ratio(0.7);
        to.set(1, 3);
        assertEquals(1 / 3.0, to.ratio());
        assertEquals(3, to.segments());
    }

    @Test
    void eBar6HeightAndWidth() throws IOException {
        for (int[] s : new int[][] {{120, 20}, {300, 12}, {120, 4}, {0, 8}, {10, 8}, {40, 8}, {0, 0}}) {
            Snap.assertLooksLikePrototype("bar-r0.5-" + s[0] + "x" + s[1], ratio(0.5), s[0], s[1]);
            Snap.assertLooksLikePrototype("bar-s3of10-" + s[0] + "x" + s[1], cells(3, 10), s[0], s[1]);
        }
        Bar low = cells(3, 10);
        low.setPreferredSize(new Dimension(0, 4));
        Snap.assertLooksLikePrototype("bar-pref4-s3of10-120x8", low, 120, 8);
    }

    @Test
    void eBar7Track() throws IOException {
        Bar b = new Bar(0, 8);
        assertSame(b, b.track(Theme.TRACK_DEEP));
        b.set(0.5);
        Snap.assertLooksLikePrototype("bar-deep-r0.5-120x8", b, 120, 8);
        b.set(1, 4);
        Snap.assertLooksLikePrototype("bar-deep-s1of4-120x8", b, 120, 8);
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
        assertEquals(0, card.segments());
        assertEquals(10, Bar.MAX_SEGMENTS);
        Snap.assertLooksLikePrototype("bar-r0.0-120x8", card, 120, 8);
    }
}
