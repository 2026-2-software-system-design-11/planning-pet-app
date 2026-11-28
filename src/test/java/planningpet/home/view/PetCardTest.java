package planningpet.home.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.RepaintManager;
import javax.swing.Timer;
import org.junit.jupiter.api.Test;
import planningpet.app.model.AppData;
import planningpet.common.view.Snap;
import planningpet.common.view.Theme;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.Pose;
import planningpet.pet.model.PetState;

/** 홈 펫 카드. 번호는 docs/porting/home.md S3m, S3n 2절. 기준 그림은 proto/petcard-*.png이고 글 띠를 덮었다. */
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

    /** w x h 카드를 그리고 이름 줄과 경험치 줄의 글 띠를 자홍색으로 덮어 기준 그림과 견준다(S3n). */
    static void assertCovered(String name, PetCard c, int w, int h) throws IOException {
        Snap.layOut(c, w, h);
        BufferedImage img = new BufferedImage(w + 4, h + 4, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        c.paint(g);
        Rectangle f = new Rectangle(2, 1, w - 4, h - 8);
        int x = f.x + PetCardArt.petWidth(f) + PetCardArt.GAP;
        int iw = f.x + f.width - 14 - x;
        int r0 = f.y + 14 + Math.max(0, (f.height - 250) / 3);
        g.setColor(new Color(0xFF00FF));
        g.fillRect(x + 19, r0 - 4, iw - 17, 37);
        g.fillRect(x - 1, r0 + 30, iw + 3, 37);
        g.dispose();
        Snap.assertLooksLikePrototype("petcard-" + name, img);
    }

    @Test
    void ePcard1CardSizes() throws IOException {
        PetCard c = new PetCard(data(PetKind.CAT, 0));
        assertEquals(new Dimension(360, 240), c.getPreferredSize());
        assertEquals(new Dimension(300, 210), c.getMinimumSize());
        // 이름 줄의 글은 덮은 띠 안이라 수로 본다(S3n E-PCOL-1).
        assertEquals(List.of("나비", "고양이", "Lv. 1"), List.of(c.nameText(), c.kindText(), c.levelText()));
        for (int[] s : new int[][] {{360, 240}, {300, 210}, {800, 500}}) {
            assertCovered("size-" + s[0] + "x" + s[1], c, s[0], s[1]);
        }
    }

    /** 창 없이 그리면 시각 0이다(E-PCARD-1). 시계는 처음 붙일 때 만든 40밀리초 시계이고 떼면 멈춘다. */
    @Test
    void ePcard4Clock() throws IOException {
        PetCard c = new PetCard(data(PetKind.CAT, 0));
        assertNull(c.clock);
        c.addNotify();
        try {
            Timer clock = c.clock;
            assertTrue(clock.isRunning() && clock.isRepeats());
            assertEquals(40, clock.getDelay());
            for (double t : new double[] {1.3, 1e6}) {
                c.tick(c.start + (long) (t * 1e9));
                assertEquals(t, c.t);
                assertCovered("time-" + t, c, 360, 240);
            }
            c.removeNotify();
            assertFalse(clock.isRunning());
            c.addNotify();
            assertSame(clock, c.clock);
            assertTrue(clock.isRunning());
        } finally {
            c.removeNotify();
        }
    }

    @Test
    void ePcard5OddSizes() throws IOException {
        AppData d = data(PetKind.CAT, 0);
        PetCard c = new PetCard(d);
        int[][] sizes = {{0, 0}, {4, 8}, {20, 20}, {120, 60}, {300, 100}, {1600, 300}, {360, 600}};
        double[] fill = {0, 1.6429, 6.5, 0.5, 0.6795, 0.8045, 0.3692};
        for (int i = 0; i < sizes.length; i++) {
            int w = sizes[i][0];
            int h = sizes[i][1];
            assertCovered("odd-" + w + "x" + h, c, w, h);
            if (w > 0) {
                assertEquals(fill[i], PetCardArt.petFill(d.activePet(), new Rectangle(2, 1, w - 4, h - 8)), 0.00005);
            }
        }
    }

    /** 본 구현 PetArt는 앉기만 그려 다른 자세도 앉기다. 프로토타입은 누운 그림이다(S3m E-PCARD-6). */
    @Test
    void ePcard6PoseDrawsSitting() throws IOException {
        AppData d = data(PetKind.CAT, 0);
        d.activePet().setPose(Pose.SLEEP);
        assertCovered("size-360x240", new PetCard(d), 360, 240);
    }

    @Test
    void ePcard7ActivePetAndChange() throws IOException {
        AppData d = data(PetKind.CAT, 0);
        PetState dog = new PetState(PetKind.DOG, "바둑이");
        dog.setExp(240);
        d.addPet(dog);
        d.setActiveKind(PetKind.DOG);
        PetCard c = new PetCard(d);
        assertCovered("two-dog", c, 360, 240);
        List<JComponent> asked = new ArrayList<>();
        RepaintManager before = RepaintManager.currentManager(c);
        RepaintManager.setCurrentManager(new RepaintManager() {
            @Override
            public void addDirtyRegion(JComponent x, int a, int b, int w, int h) {
                asked.add(x);
            }
        });
        try {
            d.setActiveKind(PetKind.CAT);
            d.changed();
        } finally {
            RepaintManager.setCurrentManager(before);
        }
        assertEquals(List.of(c.expBar, c), asked);
        assertCovered("size-360x240", c, 360, 240);
    }

    /** 경험치 막대는 "경험치" 알약 뒤에 온다. 알약 폭은 글꼴에 달려 테스트 때의 글꼴로 잰다(S3n E-PCOL-1). */
    @Test
    void ePcol1ExpBarPlace() {
        PetCard c = new PetCard(data(PetKind.CAT, 0));
        int chip = c.getFontMetrics(Theme.bold(11)).stringWidth("경험치") + 16;
        int[][] cases = {{360, 240, 152, 192, 51}, {300, 210, 128, 156, 51}, {800, 500, 270, 514, 131}, {120, 60, 128, -24, 51}};
        for (int[] k : cases) {
            Snap.layOut(c, k[0], k[1]);
            assertEquals(new Rectangle(k[2] + chip + 8, k[4], Math.max(20, k[3] - chip - 8), 8), c.expBar.getBounds());
        }
    }

    @Test
    void ePcol5LevelAndExp() throws IOException {
        int[] exps = {49, 50, 210, 1599, 1600, 9999, -5};
        String[] texts = {"Lv. 1 경험치 49 / 50", "Lv. 2 경험치 0 / 80", "Lv. 3 경험치 80 / 110", "Lv. 9 경험치 329 / 330",
            "Lv. 10 최고 레벨", "Lv. 10 최고 레벨", "Lv. 1 경험치 -5 / 50"};
        double[] ratios = {0.98, 0, 80 / 110.0, 329 / 330.0, 1, 1, 0};
        for (int i = 0; i < exps.length; i++) {
            PetCard c = new PetCard(data(PetKind.CAT, exps[i]));
            assertEquals(texts[i], c.levelText() + " " + c.expText());
            assertEquals(ratios[i], c.expBar.ratio(), 1e-9);
            for (String size : new String[] {"360x240", "300x210"}) {
                String[] wh = size.split("x");
                assertCovered("col-exp" + exps[i] + "-" + size, c, Integer.parseInt(wh[0]), Integer.parseInt(wh[1]));
            }
        }
    }

    @Test
    void ePcol7Change() throws IOException {
        AppData d = data(PetKind.CAT, 0);
        PetCard c = new PetCard(d);
        d.activePet().setExp(210);
        assertEquals(0, c.expBar.ratio());
        d.changed();
        assertEquals(80 / 110.0, c.expBar.ratio(), 1e-9);
        assertEquals("Lv. 3", c.levelText());
        assertCovered("col-change-210", c, 360, 240);
    }
}
