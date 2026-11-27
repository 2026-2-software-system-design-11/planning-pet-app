package planningpet.reward.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** 06 10번과 docs/porting/rewards.md 2절의 E-LVL 경계. */
class LevelTest {
    private static String of(int exp) {
        Level l = Level.of(exp);
        return l.getLevel() + " " + l.getStage() + " " + l.text();
    }

    @Test
    void T_LVL_1_start() {
        assertEquals("1 1 경험치 0 / 50", of(0));
        assertEquals(0.0, Level.of(0).ratio());
    }

    @Test
    void T_LVL_2_justBeforeLevel2() {
        assertEquals("1 1 경험치 49 / 50", of(49));
        assertEquals(0.98, Level.of(49).ratio(), 1e-9);
    }

    @Test
    void T_LVL_3_level2() {
        assertEquals("2 1 경험치 0 / 80", of(50));
    }

    @Test
    void E_LVL_1_aroundLevel3() {
        assertEquals("2 1 경험치 79 / 80", of(129));
        assertEquals("3 1 경험치 0 / 110", of(130));
    }

    @Test
    void E_LVL_2_justBeforeStage2() {
        assertEquals("3 1 경험치 109 / 110", of(239));
    }

    @Test
    void T_LVL_4_stage2() {
        assertEquals("4 2 경험치 0 / 140", of(240));
    }

    @Test
    void E_LVL_3_justBeforeStage3() {
        assertEquals("6 2 경험치 199 / 200", of(749));
    }

    @Test
    void T_LVL_5_stage3() {
        assertEquals("7 3 경험치 0 / 240", of(750));
    }

    @Test
    void E_LVL_4_aroundLevel9() {
        assertEquals("8 3 경험치 279 / 280", of(1269));
        assertEquals("9 3 경험치 0 / 330", of(1270));
    }

    @Test
    void T_LVL_6_justBeforeTop() {
        assertEquals("9 3 경험치 329 / 330", of(1599));
    }

    @Test
    void T_LVL_7_top() {
        for (int exp : new int[] {1600, 5000}) {
            assertEquals("10 3 최고 레벨", of(exp));
            assertEquals(0, Level.of(exp).getSpan());
            assertEquals(1.0, Level.of(exp).ratio());
        }
    }

    @Test
    void E_LVL_5_belowZero() {
        assertEquals("1 1 경험치 -1 / 50", of(-1));
    }
}
