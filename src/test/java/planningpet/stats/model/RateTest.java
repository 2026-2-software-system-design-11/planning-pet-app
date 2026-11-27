package planningpet.stats.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

/** 06 29번 Rate와 docs/porting/stats.md 2절의 E-STAT 경계. */
class RateTest {
    @Test
    void E_STAT_1_roundsHalfUp() {
        List<Integer> got = List.of(new Rate(1, 3), new Rate(2, 3), new Rate(1, 8), new Rate(3, 8), new Rate(0, 1))
                .stream().map(Rate::percent).toList();
        assertEquals(List.of(33, 67, 13, 38, 0), got);
    }

    @Test
    void zeroWhenNothingToCount() {
        assertEquals(0, new Rate(0, 0).percent());
    }
}
