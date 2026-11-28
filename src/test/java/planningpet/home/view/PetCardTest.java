package planningpet.home.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Dimension;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import planningpet.app.model.AppData;
import planningpet.common.view.Snap;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.PetState;

/** 홈 펫 카드 몸. 번호는 docs/porting/home.md S3m 2절. 기준 그림은 proto/petcard-*.png다. */
class PetCardTest {
    /** 2026-11-20 10:00의 앱 데이터에 기본 목걸이를 입은 펫 하나(고양이 나비, 강아지 바둑이)를 두고 고른다. */
    static AppData data(PetKind kind, int exp) {
        AppData data = new AppData(Clock.fixed(Instant.parse("2026-11-20T01:00:00Z"), ZoneId.of("Asia/Seoul")));
        PetState p = new PetState(kind, kind == PetKind.CAT ? "나비" : "바둑이");
        p.setExp(exp);
        data.addPet(p);
        data.setActiveKind(kind);
        return data;
    }

    @Test
    void ePcard1CardSizes() throws IOException {
        PetCard c = new PetCard(data(PetKind.CAT, 0));
        assertEquals(new Dimension(360, 240), c.getPreferredSize());
        assertEquals(new Dimension(300, 210), c.getMinimumSize());
        for (int[] s : new int[][] {{360, 240}, {300, 210}, {800, 500}}) {
            Snap.assertLaidOutLooksLikePrototype("petcard-size-" + s[0] + "x" + s[1], c, s[0], s[1]);
        }
    }
}
