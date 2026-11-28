package planningpet.home.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.ToolTipManager;
import org.junit.jupiter.api.Test;
import planningpet.app.model.AppData;
import planningpet.pet.model.Bubble;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.PetState;
import planningpet.schedule.model.Task;

/** 홈 펫 카드 화면 읽기와 툴팁. 번호는 docs/porting/home.md S3q 2절. */
class PetCardA11yTest {
    private static final String BASE = "나비 Lv. 1, 경험치 0 / 50, 오늘 끝낸 일 0 / 0";
    private static final String TEN = "가나다라마바사아자차";
    /** S3l의 열 자 낱말 여섯. 360x240 말풍선에서 3줄로 줄인다. */
    private static final String W6 = String.join(" ", TEN, TEN, TEN, TEN, TEN, TEN);
    private static final String OPEN = "캐릭터 창 열기";

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

    /** 펫 말이 talk인 w x h 카드. paint면 창 없이 한 번 그려 말풍선 자리를 남긴다. */
    private static PetCard card(String talk, int w, int h, boolean paint) {
        Bubble b = new Bubble();
        b.say(talk);
        PetCard c = PetCardTest.card(PetCardTest.data(PetKind.CAT, 0), b);
        c.setSize(w, h);
        c.doLayout();
        if (paint) {
            c.paint(new BufferedImage(w + 4, h + 4, BufferedImage.TYPE_INT_ARGB).createGraphics());
        }
        return c;
    }

    private static String tip(PetCard c, int x, int y) {
        return c.getToolTipText(new MouseEvent(c, MouseEvent.MOUSE_MOVED, 0, 0, x, y, 0, false));
    }

    private static String wide(String text) {
        return "<html><div style='width:260px'>" + text + "</div></html>";
    }

    @Test
    void ePtip1Plain() {
        PetCard none = card(null, 360, 240, true);
        assertEquals("", none.getToolTipText());
        assertTrue(List.of(none.getMouseListeners()).contains(ToolTipManager.sharedInstance()));
        assertEquals(List.of(OPEN, OPEN), List.of(tip(none, 5, 5), tip(none, 200, 160)));
        assertEquals(OPEN, tip(card("잘했어! +10P", 360, 240, true), 200, 160));
        assertEquals(OPEN, tip(card(W6, 360, 240, false), 200, 160), "그리기 전");
        PetCard big = card(W6, 800, 500, true);
        assertEquals(new Rectangle(270, 227, 514, 50), big.bubbleBox);
        assertEquals(OPEN, tip(big, 300, 240));
    }

    @Test
    void ePtip2CutBubble() {
        PetCard c = card(W6, 360, 240, true);
        assertEquals(new Rectangle(152, 147, 192, 67), c.bubbleBox);
        for (int[] in : new int[][] {{200, 160}, {152, 147}, {343, 213}}) {
            assertEquals(wide(W6), tip(c, in[0], in[1]), in[0] + "," + in[1]);
        }
        for (int[] out : new int[][] {{344, 160}, {200, 214}, {151, 160}, {200, 146}, {5, 5}}) {
            assertEquals(OPEN, tip(c, out[0], out[1]), out[0] + "," + out[1]);
        }
        assertEquals(wide("&lt;b&gt;굵게&lt;/b&gt; &amp; " + W6), tip(card("<b>굵게</b> & " + W6, 360, 240, true), 200, 160));
    }

    /** 줄였는지와 자리는 마지막으로 그린 값이다. 변경 알림 뒤 다시 그리기 전에는 새 말을 넓은 꼴로 보인다. */
    @Test
    void ePtip3TalkChangedAfterPaint() {
        AppData d = PetCardTest.data(PetKind.CAT, 0);
        Bubble b = new Bubble();
        b.say(W6);
        PetCard c = PetCardTest.card(d, b);
        c.setSize(360, 240);
        c.doLayout();
        c.paint(new BufferedImage(364, 244, BufferedImage.TYPE_INT_ARGB).createGraphics());
        b.say("짧아");
        d.changed();
        assertEquals(wide("짧아"), tip(c, 200, 160));
        b.say("");
        assertEquals(wide(""), tip(c, 200, 160));
    }
}
