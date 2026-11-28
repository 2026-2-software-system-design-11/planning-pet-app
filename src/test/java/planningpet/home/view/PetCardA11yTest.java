package planningpet.home.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import org.junit.jupiter.api.Test;
import planningpet.app.model.AppData;
import planningpet.pet.model.Bubble;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.PetState;
import planningpet.schedule.model.Task;

/** 홈 펫 카드 화면 읽기와 툴팁. 번호는 docs/porting/home.md S3q 2절. */
class PetCardA11yTest {
    private static final String BASE = "나비 Lv. 1, 경험치 0 / 50, 오늘 끝낸 일 0 / 0";

    /** 펫 말이 talk인 카드의 화면 읽기 설명. */
    private static String described(AppData d, String talk) {
        Bubble b = new Bubble();
        b.say(talk);
        return PetCardTest.card(d, b).getAccessibleContext().getAccessibleDescription();
    }

    private static Task period() {
        return PetCardTest.task("E사 지원서", 16, 22, false);
    }

    @Test
    void ePa11y1RoleAndName() {
        PetCard c = PetCardTest.card(PetCardTest.data(PetKind.CAT, 0), new Bubble());
        AccessibleContext x = c.getAccessibleContext();
        assertEquals(AccessibleRole.PUSH_BUTTON, x.getAccessibleRole());
        assertEquals("펫 카드. 누르면 캐릭터 창이 열려요", x.getAccessibleName());
        assertSame(x, c.getAccessibleContext());
    }

    @Test
    void ePa11y2Description() {
        assertEquals(BASE, described(PetCardTest.data(PetKind.CAT, 0), null));
        assertEquals("바둑이 Lv. 4, 경험치 0 / 140, 오늘 끝낸 일 0 / 0", described(PetCardTest.data(PetKind.DOG, 240), null));
        assertEquals("나비 Lv. 10, 최고 레벨, 오늘 끝낸 일 0 / 0", described(PetCardTest.data(PetKind.CAT, 1600), null));
        AppData blank = PetCardTest.data(PetKind.CAT, 0);
        blank.clearPets();
        blank.addPet(new PetState(PetKind.CAT, ""));
        assertEquals(" Lv. 1, 경험치 0 / 50, 오늘 끝낸 일 0 / 0", described(blank, null));
        Task[] four = new Task[4];
        for (int i = 0; i < 4; i++) {
            four[i] = PetCardTest.task("일" + i, 20, 0, i < 1);
        }
        assertEquals("나비 Lv. 1, 경험치 0 / 50, 오늘 끝낸 일 1 / 4", described(PetCardTest.data(PetKind.CAT, 0, four), null));
        assertEquals(BASE + ", 기간 할 일 1개 (~11/22)", described(PetCardTest.data(PetKind.CAT, 0, period()), null));
        assertEquals(BASE + ", 잘했어! +10P", described(PetCardTest.data(PetKind.CAT, 0), "잘했어! +10P"));
        assertEquals(BASE, described(PetCardTest.data(PetKind.CAT, 0), "   "));
        assertEquals(BASE + ", 기간 할 일 1개 (~11/22), 잘했어! +10P", described(PetCardTest.data(PetKind.CAT, 0, period()), "잘했어! +10P"));
    }

    @Test
    void ePa11y3Change() {
        AppData d = PetCardTest.data(PetKind.CAT, 0);
        Bubble b = new Bubble();
        PetCard c = PetCardTest.card(d, b);
        AccessibleContext x = c.getAccessibleContext();
        d.activePet().setExp(210);
        b.say("좋아");
        assertEquals(BASE, x.getAccessibleDescription());
        d.changed();
        assertSame(x, c.getAccessibleContext());
        assertEquals("나비 Lv. 3, 경험치 80 / 110, 오늘 끝낸 일 0 / 0, 좋아", x.getAccessibleDescription());
    }
}
