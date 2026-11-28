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
import java.time.LocalDate;
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
import planningpet.pet.model.Bubble;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.Pose;
import planningpet.pet.model.PetState;
import planningpet.schedule.model.Category;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Task;

/** 홈 펫 카드. 번호는 docs/porting/home.md S3m, S3n, S3o 2절. 기준 그림은 proto/petcard-*.png이고 글 띠를 덮었다. */
class PetCardTest {
    /** 2026-11-20 10:00의 앱 데이터에 기본 목걸이를 입은 펫 하나(고양이 나비, 강아지 바둑이)를 두고 고른다. */
    static AppData data(PetKind kind, int exp, Task... tasks) {
        AppData data = new AppData(Clock.fixed(Instant.parse("2026-11-20T01:00:00Z"), ZoneId.of("Asia/Seoul")), List.of(tasks),
                List.of());
        PetState p = new PetState(kind, kind == PetKind.CAT ? "나비" : "바둑이");
        p.setExp(exp);
        data.addPet(p);
        data.setActiveKind(kind);
        return data;
    }

    /** 시각 없는 할 일. due가 0보다 크면 11월 day부터 due까지의 기간 할 일이다. 끝냈으면 11/20에 끝냈다. */
    static Task task(String title, int day, int due, boolean done) {
        LocalDate d = LocalDate.of(2026, 11, day);
        LocalDate end = due > 0 ? LocalDate.of(2026, 11, due) : null;
        return new Task(title, 1, Kind.TASK, title, d, null, 30, "", false, Category.OTHER, null, null, end, end == null ? null : d,
                done, done ? LocalDate.of(2026, 11, 20) : null, 0, 0, null, null);
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
        g.fillRect(x + 6, r0 + 77, iw - 12, 24);
        if (c.periodText() != null) {
            g.fillRect(x + 6, r0 + 119, iw - 12, 19);
        }
        Rectangle b = c.bubbleBox;
        if (b != null) {
            g.fillRect(b.x + 30, b.y + 7, b.width - 34, b.height - 14);
        }
        g.dispose();
        Snap.assertLooksLikePrototype("petcard-" + name, img);
    }

    @Test
    void ePcard1CardSizes() throws IOException {
        PetCard c = new PetCard(data(PetKind.CAT, 0), new Bubble());
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
        PetCard c = new PetCard(data(PetKind.CAT, 0), new Bubble());
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
        PetCard c = new PetCard(d, new Bubble());
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
        assertCovered("size-360x240", new PetCard(d, new Bubble()), 360, 240);
    }

    @Test
    void ePcard7ActivePetAndChange() throws IOException {
        AppData d = data(PetKind.CAT, 0);
        PetState dog = new PetState(PetKind.DOG, "바둑이");
        dog.setExp(240);
        d.addPet(dog);
        d.setActiveKind(PetKind.DOG);
        PetCard c = new PetCard(d, new Bubble());
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
        assertEquals(List.of(c.expBar, c.doneBar, c), asked);
        assertCovered("size-360x240", c, 360, 240);
    }

    /** 경험치 막대는 "경험치" 알약 뒤에 온다. 알약 폭은 글꼴에 달려 테스트 때의 글꼴로 잰다(S3n E-PCOL-1). */
    @Test
    void ePcol1ExpBarPlace() {
        PetCard c = new PetCard(data(PetKind.CAT, 0), new Bubble());
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
            PetCard c = new PetCard(data(PetKind.CAT, exps[i]), new Bubble());
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
        PetCard c = new PetCard(d, new Bubble());
        d.activePet().setExp(210);
        assertEquals(0, c.expBar.ratio());
        d.changed();
        assertEquals(80 / 110.0, c.expBar.ratio(), 1e-9);
        assertEquals("Lv. 3", c.levelText());
        assertCovered("col-change-210", c, 360, 240);
    }

    private static PetCard named(PetKind kind, String name) {
        AppData d = data(kind, 0);
        d.clearPets();
        d.addPet(new PetState(kind, name));
        return new PetCard(d, new Bubble());
    }

    /** 이름은 이름 자리(열 폭 - 22 - 레벨 폭 - 10 - 종류 폭 - 5)에 맞춰 Ui.fit으로 준다. 글은 덮은 띠 안이다(S3n E-PCOL-2~4). */
    @Test
    void ePcol2To4Names() throws IOException {
        assertEquals("강아지", named(PetKind.DOG, "바둑이").kindText());
        assertCovered("col-dog-360x240", named(PetKind.DOG, "바둑이"), 360, 240);
        assertCovered("col-dog-300x210", named(PetKind.DOG, "바둑이"), 300, 210);
        assertCovered("col-blank-360x240", named(PetKind.CAT, ""), 360, 240);
        String[][] names = {{"long", "가나다라마바사아자차"}, {"emoji", "😀".repeat(10)}, {"one", "가"}};
        for (String[] n : names) {
            for (int[] s : new int[][] {{360, 240}, {300, 210}, {800, 500}}) {
                assertCovered("col-" + n[0] + "-" + s[0] + "x" + s[1], named(PetKind.CAT, n[1]), s[0], s[1]);
            }
        }
    }

    /** 카드가 250보다 높으면 오른쪽 열이 넘는 몫의 1/3만큼 내려온다. 아주 작은 카드도 예외 없이 그린다(S3n E-PCOL-6). */
    @Test
    void ePcol6TallAndTiny() throws IOException {
        PetCard c = new PetCard(data(PetKind.CAT, 0), new Bubble());
        for (int[] s : new int[][] {{360, 280}, {360, 340}, {360, 600}, {1600, 300}, {300, 100}, {120, 60}, {0, 0}}) {
            assertCovered("col-tall-" + s[0] + "x" + s[1], c, s[0], s[1]);
        }
    }

    /** 칸 막대는 작은 카드 + 34에 열 폭 - 24다. 자리는 글꼴에 달리지 않아 덮지 않는다(S3o E-PTODAY-1). */
    @Test
    void ePtoday1MiniCard() {
        PetCard c = new PetCard(data(PetKind.CAT, 0), new Bubble());
        assertEquals("오늘 끝낸 일 0 / 0", c.doneText());
        int[][] cases = {{360, 240, 164, 121, 168}, {300, 210, 140, 121, 132}, {800, 500, 282, 201, 490}, {120, 60, 140, 121, 20}};
        for (int[] k : cases) {
            Snap.layOut(c, k[0], k[1]);
            assertEquals(new Rectangle(k[2], k[3], k[4], 8), c.doneBar.getBounds());
        }
        assertEquals(List.of(0.0, 0), List.of(c.doneBar.ratio(), c.doneBar.segments()));
    }

    @Test
    void ePtoday2DoneCounts() throws IOException {
        int[][] counts = {{1, 4}, {0, 1}, {4, 4}, {3, 10}, {3, 11}, {12, 12}};
        int[] segments = {4, 1, 4, 10, 0, 0};
        for (int i = 0; i < counts.length; i++) {
            Task[] tasks = new Task[counts[i][1]];
            for (int j = 0; j < tasks.length; j++) {
                tasks[j] = task("일" + j, 20, 0, j < counts[i][0]);
            }
            PetCard c = new PetCard(data(PetKind.CAT, 0, tasks), new Bubble());
            assertEquals("오늘 끝낸 일 " + counts[i][0] + " / " + counts[i][1], c.doneText());
            assertEquals(counts[i][0] / (double) counts[i][1], c.doneBar.ratio(), 1e-9);
            assertEquals(segments[i], c.doneBar.segments());
            for (String size : new String[] {"360x240", "300x210"}) {
                String[] wh = size.split("x");
                assertCovered("today-" + counts[i][0] + "of" + counts[i][1] + "-" + size, c, Integer.parseInt(wh[0]),
                        Integer.parseInt(wh[1]));
            }
        }
    }

    /** 기간 할 일 줄은 덮은 띠 안이라 글을 수로 보고, 작은 카드가 70으로 길어진 것을 그림으로 본다(S3o E-PTODAY-3). */
    @Test
    void ePtoday3PeriodLine() throws IOException {
        PetCard one = new PetCard(data(PetKind.CAT, 0, task("할 일", 20, 0, true), task("E사 지원서", 16, 22, false)), new Bubble());
        assertEquals(List.of("오늘 끝낸 일 1 / 1", "기간 할 일 1개 (~11/22)"), List.of(one.doneText(), one.periodText()));
        for (int[] s : new int[][] {{360, 240}, {300, 210}, {800, 500}}) {
            assertCovered("period-1-" + s[0] + "x" + s[1], one, s[0], s[1]);
        }
        PetCard two = new PetCard(data(PetKind.CAT, 0, task("멀리", 18, 25, false), task("가까이", 19, 21, false),
                task("끝냄", 16, 24, true)), new Bubble());
        assertEquals(List.of("오늘 끝낸 일 1 / 1", "기간 할 일 2개 (~11/21)"), List.of(two.doneText(), two.periodText()));
        assertCovered("period-2-360x240", two, 360, 240);
        PetCard due = new PetCard(data(PetKind.CAT, 0, task("오늘 마감", 16, 20, false)), new Bubble());
        assertEquals("오늘 끝낸 일 0 / 1", due.doneText());
        assertEquals(null, due.periodText());
        assertCovered("period-dueday-360x240", due, 360, 240);
    }

    /** 말풍선은 작은 카드 아래 8에서 틀 아래 - 12까지 놓인다. 글 자리는 덮고 자리와 줄였는지를 본다(S3o E-PTODAY-4~6). */
    @Test
    void ePtoday4To6Bubble() throws IOException {
        String w6 = String.join(" ", java.util.Collections.nCopies(6, "가나다라마바사아자차"));
        String[][] talks = {{"one", "잘했어! +10P"}, {"six", w6}, {"blank", "   "}};
        int[][] heights = {{33, 33, 33, 33, 33}, {67, 33, 50, 84, 50}, null};
        boolean[][] cuts = {{false, false, false, false, false}, {true, true, false, true, true}, {false, false, false, false, false}};
        int[][] sizes = {{360, 240, 152, 147, 192}, {300, 210, 128, 147, 156}, {800, 500, 270, 227, 514}, {360, 600, 152, 261, 192},
            {360, 240, 152, 165, 192}};
        for (int t = 0; t < talks.length; t++) {
            for (int i = 0; i < sizes.length; i++) {
                Bubble said = new Bubble();
                said.say(talks[t][1]);
                boolean period = i == 4; // 마지막은 기간 할 일 줄이 있어 작은 카드가 70이다.
                PetCard c = new PetCard(period ? data(PetKind.CAT, 0, task("E사 지원서", 16, 22, false)) : data(PetKind.CAT, 0), said);
                int[] s = sizes[i];
                assertCovered("talk-" + talks[t][0] + (period ? "-period-" : "-") + s[0] + "x" + s[1], c, s[0], s[1]);
                assertEquals(heights[t] == null ? null : new Rectangle(s[2], s[3], s[4], heights[t][i]), c.bubbleBox);
                assertEquals(cuts[t][i], c.bubbleCut);
            }
        }
    }

    /** 할 일을 끝내고 펫이 말하면 변경 알림 한 번에 수, 칸 막대, 말풍선이 함께 바뀐다(S3o E-PTODAY-7). */
    @Test
    void ePtoday7Change() throws IOException {
        AppData d = data(PetKind.CAT, 0, task("일", 20, 0, false), task("이", 20, 0, false));
        Bubble said = new Bubble();
        PetCard c = new PetCard(d, said);
        new planningpet.reward.model.Rewards(d).complete(d.getBook().getTasks().get(0), d.today());
        said.say("잘했어! +10P");
        assertEquals(0, c.doneBar.ratio());
        d.changed();
        assertEquals(List.of("오늘 끝낸 일 1 / 2", 0.5, 2), List.of(c.doneText(), c.doneBar.ratio(), c.doneBar.segments()));
        assertCovered("today-change", c, 360, 240);
        assertEquals(new Rectangle(152, 147, 192, 33), c.bubbleBox);
    }
}
