package planningpet.home.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.Color;
import org.junit.jupiter.api.Test;
import planningpet.common.view.Theme;

/** 홈 펫 카드의 그림 도구. 번호는 docs/porting/home.md S3j 2절. */
class PetCardArtTest {
    @Test
    void eMix1() {
        Color a = Theme.GRASS;
        Color b = Theme.GRASS_FAR;
        assertEquals(a, PetCardArt.mix(a, b, 0));
        assertEquals(b, PetCardArt.mix(a, b, 1));
        assertEquals(new Color(0xD6ECAF), PetCardArt.mix(a, b, 0.35));
        assertEquals(new Color(212, 236, 181), PetCardArt.mix(a, b, 0.5));
        assertEquals(new Color(228, 238, 143), PetCardArt.mix(a, b, -0.5));
        assertEquals(new Color(188, 233, 238), PetCardArt.mix(a, b, 2));
        assertEquals(new Color(127, 127, 127), PetCardArt.mix(Color.BLACK, Color.WHITE, 0.5));
        assertEquals(new Color(254, 254, 254), PetCardArt.mix(Color.BLACK, Color.WHITE, 0.999));
        assertEquals(255, PetCardArt.mix(new Color(10, 20, 30, 40), new Color(10, 20, 30, 90), 0.5).getAlpha());
        assertThrows(IllegalArgumentException.class, () -> PetCardArt.mix(Color.WHITE, Color.BLACK, -1));
    }

    @Test
    void eAlpha1() {
        for (int a : new int[] {0, 120, 200, 255}) {
            assertEquals(new Color(0xCC, 0xEB, 0xC8, a), PetCardArt.withAlpha(Theme.GRASS_FAR, a));
        }
        assertEquals(new Color(1, 2, 3, 200), PetCardArt.withAlpha(new Color(1, 2, 3, 9), 200));
        assertThrows(IllegalArgumentException.class, () -> PetCardArt.withAlpha(Theme.GRASS_FAR, 256));
        assertThrows(IllegalArgumentException.class, () -> PetCardArt.withAlpha(Theme.GRASS_FAR, -1));
    }
}
