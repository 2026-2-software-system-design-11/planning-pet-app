package planningpet.home.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import planningpet.common.view.Snap;
import planningpet.common.view.Theme;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.PetState;
import planningpet.pet.view.PetArt;

/** 홈 펫 카드의 그림 도구. 번호는 docs/porting/home.md S3j 2절. 기준 그림은 proto/scene-*.png다. */
class PetCardArtTest {
    /** 프로토타입 PetCard처럼 안티에일리어싱과 품질 우선을 켜고, 틀보다 오른쪽과 아래로 4 큰 그림에 그린다. */
    private static BufferedImage scene(Rectangle f, int ground) {
        BufferedImage img = image(f);
        Graphics2D g = hinted(img);
        PetCardArt.scene(g, f, ground);
        g.dispose();
        return img;
    }

    /** 구름 그림 없이 그린다. */
    private static BufferedImage bare(Rectangle f, int ground) {
        BufferedImage img = image(f);
        Graphics2D g = hinted(img);
        PetCardArt.scene(g, f, ground, null);
        g.dispose();
        return img;
    }

    private static BufferedImage image(Rectangle f) {
        return new BufferedImage(f.x + f.width + 4, f.y + f.height + 4, BufferedImage.TYPE_INT_ARGB);
    }

    private static Graphics2D hinted(BufferedImage img) {
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        return g;
    }

    /** 카드 w x h의 PetCard 틀과 땅 선(틀 아래에서 높이의 1/11, 최소 14). */
    private static Rectangle frame(int w, int h) {
        return new Rectangle(2, 1, w - 4, h - 8);
    }

    private static int ground(Rectangle f) {
        return f.y + f.height - Math.max(14, f.height / 11);
    }

    private static BufferedImage card(int w, int h) {
        return scene(frame(w, h), ground(frame(w, h)));
    }

    @Test
    void eScene1CardSizes() throws IOException {
        for (int[] s : new int[][] {{360, 240}, {300, 210}, {800, 500}}) {
            Snap.assertLooksLikePrototype("scene-card-" + s[0] + "x" + s[1], card(s[0], s[1]));
        }
    }

    @Test
    void eScene2MovedFrame() throws IOException {
        Snap.assertLooksLikePrototype("scene-moved", scene(new Rectangle(30, 20, 356, 232), 20 + 232 - 21));
    }

    @Test
    void eScene3GroundEnds() throws IOException {
        Snap.assertLooksLikePrototype("scene-ground-top", scene(new Rectangle(2, 1, 356, 232), 1));
        Snap.assertLooksLikePrototype("scene-ground-below", scene(new Rectangle(2, 1, 356, 232), 1 + 232 + 30));
    }

    @Test
    void eScene4TinyCards() throws IOException {
        Snap.assertLooksLikePrototype("scene-card-4x8", card(4, 8));
        Snap.assertLooksLikePrototype("scene-zero", scene(new Rectangle(0, 0, 0, 0), -14));
        Snap.assertLooksLikePrototype("scene-card-120x60", card(120, 60));
    }

    @Test
    void eScene5NoCloudAndGraphicsAfter() throws IOException {
        for (int[] s : new int[][] {{360, 240}, {300, 210}, {800, 500}, {120, 60}}) {
            Rectangle f = frame(s[0], s[1]);
            Snap.assertLooksLikePrototype("scene-no-cloud-card-" + s[0] + "x" + s[1], bare(f, ground(f)));
        }
        Snap.assertLooksLikePrototype("scene-no-cloud-moved", bare(new Rectangle(30, 20, 356, 232), 231));
        Snap.assertLooksLikePrototype("scene-no-cloud-ground-top", bare(new Rectangle(2, 1, 356, 232), 1));
        Snap.assertLooksLikePrototype("scene-no-cloud-ground-below", bare(new Rectangle(2, 1, 356, 232), 263));
        Snap.assertLooksLikePrototype("scene-card-4x8", bare(frame(4, 8), ground(frame(4, 8))));
        Snap.assertLooksLikePrototype("scene-zero", bare(new Rectangle(0, 0, 0, 0), -14));
        Rectangle f = frame(360, 240);
        Graphics2D g = hinted(image(f));
        PetCardArt.scene(g, f, ground(f));
        assertEquals(RenderingHints.VALUE_INTERPOLATION_BICUBIC, g.getRenderingHint(RenderingHints.KEY_INTERPOLATION));
        assertEquals(Theme.GRASS_DEEP, g.getColor());
        assertEquals(232 / 140f, ((BasicStroke) g.getStroke()).getLineWidth());
        assertEquals(BasicStroke.CAP_ROUND, ((BasicStroke) g.getStroke()).getEndCap());
        Graphics2D n = hinted(image(f));
        PetCardArt.scene(n, f, ground(f), null);
        assertNull(n.getRenderingHint(RenderingHints.KEY_INTERPOLATION));
    }

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

    /** 글꼴에 기대지 않게 UTF-16 한 단위를 폭 10으로 잰다(공백도 10, 이모지는 20). wrap은 stringWidth만 쓴다. */
    private static final FontMetrics TEN_EACH = new FontMetrics(new Font(Font.DIALOG, Font.PLAIN, 10)) {
        @Override
        public int stringWidth(String s) {
            return 10 * s.length();
        }
    };

    private static List<String> wrap(String text, int max) {
        return PetCardArt.wrap(text, TEN_EACH, max);
    }

    @Test
    void eWrap1FitsOneLine() {
        assertEquals(List.of("가나다"), wrap("가나다", 30));
        assertEquals(List.of("가나다"), wrap("가나다", 100));
        assertEquals(List.of(""), wrap("", 30));
    }

    @Test
    void eWrap2BreaksAtSpaces() {
        assertEquals(List.of("가나 다라", "마바"), wrap("가나 다라 마바", 50));
        assertEquals(List.of("가나", "다라", "마바"), wrap("가나 다라 마바", 49));
        assertEquals(List.of("가나 다라 마바"), wrap("가나 다라 마바", 80));
        assertEquals(List.of("가", "나", "다"), wrap("가 나 다", 10));
    }

    @Test
    void eWrap3Spaces() {
        assertEquals(List.of("가  나"), wrap("가  나", 100));
        assertEquals(List.of("가나 ", "다라"), wrap("가나  다라", 40));
        assertEquals(List.of("가나"), wrap(" 가나", 40));
        assertEquals(List.of("가나"), wrap("가나 ", 40));
        assertEquals(List.of(""), wrap("   ", 40));
        assertEquals(List.of("가\n나"), wrap("가\n나", 40));
    }

    @Test
    void eWrap4LongWord() {
        assertEquals(List.of("가나다라", "마바"), wrap("가나다라마바", 40));
        assertEquals(List.of("가", "나다라마", "바사"), wrap("가 나다라마바사", 40));
        assertEquals(List.of("가나다", "라마바", "사아자", "차카"), wrap("가나다라마바사아자차카", 30));
        assertEquals(List.of("가나", "다"), wrap("가나다", 29));
        assertEquals(List.of("가", "나", "다"), wrap("가나다", 15));
        assertEquals(List.of("가", "나", "다"), wrap("가나다", 10));
    }

    private static final String SMILE = "😀";

    /** 프로토타입과 일부러 다름(06 읽는 법): 코드 포인트로 잘라 이모지는 통째로 다음 줄로 간다. */
    @Test
    void eWrap5EmojiAtCut() {
        assertEquals(List.of("가", SMILE, "나"), wrap("가😀나", 20));
        assertEquals(List.of(SMILE, SMILE, SMILE), wrap("😀😀😀", 30));
        assertEquals(List.of(SMILE, SMILE, SMILE), wrap("😀😀😀", 25));
        assertEquals(List.of("가", SMILE, SMILE), wrap("가 😀😀", 30));
        assertEquals(List.of("가나", SMILE), wrap("가나😀", 30));
    }

    /**
     * TEN_EACH처럼 재되, 100000번 넘게 재면 예외를 던진다. 끝나지 않는 wrap을 멈추려는 것이다. Java 21은 도는 스레드를 멈출 수
     * 없어 시간 제한만 두면 제한 뒤에도 빈 줄을 더하며 메모리를 먹는다(home.md S3k 2절).
     */
    private static FontMetrics counting() {
        return new FontMetrics(new Font(Font.DIALOG, Font.PLAIN, 10)) {
            private int calls;

            @Override
            public int stringWidth(String s) {
                if (++calls > 100_000) {
                    throw new IllegalStateException("100000번 넘게 재었다");
                }
                return 10 * s.length();
            }
        };
    }

    /** 끝나지 않으면 재기만 되풀이하다 counting의 예외가 나서 테스트가 실패한다. */
    private static List<String> bounded(String text, int max) {
        return assertTimeoutPreemptively(Duration.ofSeconds(5), () -> PetCardArt.wrap(text, counting(), max));
    }

    /** 프로토타입과 일부러 다름(끝나지 않음): 한 글자는 폭을 넘어도 한 줄에 둔다. */
    @Test
    void eWrap6NarrowerThanOneLetter() {
        assertEquals(List.of("가"), bounded("가", 5));
        assertEquals(List.of("가"), bounded("가", 0));
        assertEquals(List.of("가"), bounded("가", -5));
        assertEquals(List.of("가", "나"), bounded("가나", 9));
        assertEquals(List.of("가", "나"), bounded("가 나", 5));
        assertEquals(List.of("가"), bounded(" 가", -5));
        assertEquals(List.of(SMILE), bounded(SMILE, 10));
    }

    /** 그린 말풍선: 그림, 돌려받은 자리, 줄였는지, 그린 뒤 그래픽의 글꼴과 색. */
    private record Drawn(BufferedImage img, Rectangle box, boolean cut, Font font, Color color) {
    }

    /** 프로토타입 PetCard처럼 글 안티에일리어싱도 켜고 cw x ch 그림에 그린다. 부르기 전 줄였는지는 참이다. */
    private static Drawn bubble(String text, int x, int top, int w, int bottom, int cw, int ch) {
        BufferedImage img = new BufferedImage(cw, ch, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = hinted(img);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        boolean[] cut = {true};
        Rectangle box = PetCardArt.bubble(g, text, x, top, w, bottom, cut);
        Drawn d = new Drawn(img, box, cut[0], g.getFont(), g.getColor());
        g.dispose();
        return d;
    }

    /** 글 자리 [x+30, x+폭-4) x [y+7, y+높이-7)를 CARD_TOP으로 덮고 기준 그림과 맞춘다(home.md S3l). */
    private static void looks(String name, Drawn d) throws IOException {
        Graphics2D g = d.img().createGraphics();
        g.setColor(Theme.CARD_TOP);
        g.fillRect(d.box().x + 30, d.box().y + 7, d.box().width - 34, d.box().height - 14);
        g.dispose();
        Snap.assertLooksLikePrototype("bubble-" + name, d.img());
    }

    /** 줄 line(위 y+8에서 17씩)의 글 자리에 INK 잉크가 있는지. 운영체제마다 다른 글자 픽셀 대신 줄이 있는지만 본다. */
    private static boolean inked(Drawn d, int line) {
        return inked(d, line, d.box().x + 30, d.box().x + d.box().width - 4);
    }

    private static boolean inked(Drawn d, int line, int fromX, int toX) {
        for (int y = d.box().y + 8 + 17 * line; y < d.box().y + 25 + 17 * line; y++) {
            for (int x = fromX; x < toX; x++) {
                int p = d.img().getRGB(x, y);
                int dist = Math.abs((p >> 16 & 255) - Theme.INK.getRed()) + Math.abs((p >> 8 & 255) - Theme.INK.getGreen())
                        + Math.abs((p & 255) - Theme.INK.getBlue());
                if (p >>> 24 == 255 && dist < 60) {
                    return true;
                }
            }
        }
        return false;
    }

    @Test
    void eBub1Blank() throws IOException {
        Font usual = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics().getFont();
        for (String text : new String[] {"", "   "}) {
            Drawn d = bubble(text, 152, 147, 192, 221, 360, 240);
            assertNull(d.box());
            assertFalse(d.cut());
            assertEquals(usual, d.font());
            Snap.assertLooksLikePrototype("bubble-blank", d.img());
        }
    }

    @Test
    void eBub2OneLine() throws IOException {
        int[][] cards = {{360, 240, 152, 147, 192, 221}, {300, 210, 128, 147, 156, 191}, {800, 500, 270, 227, 514, 481}};
        for (int[] c : cards) {
            Drawn d = bubble("잘했어! +10P", c[2], c[3], c[4], c[5], c[0], c[1]);
            assertEquals(new Rectangle(c[2], c[3], c[4], 33), d.box());
            assertFalse(d.cut());
            assertTrue(inked(d, 0));
            assertFalse(inked(d, 1));
            assertEquals(Theme.bold(13), d.font());
            assertEquals(Theme.INK, d.color());
            looks("one-" + c[0] + "x" + c[1], d);
        }
    }

    private static final String W2 = "가나다라마 바사아자차 카타파하거너";
    private static final String W3 = W2 + " 저처커터퍼허고노도로";

    @Test
    void eBub3Lines() throws IOException {
        Drawn two = bubble(W2, 152, 147, 192, 221, 360, 240);
        assertEquals(new Rectangle(152, 147, 192, 50), two.box());
        assertTrue(inked(two, 1));
        assertFalse(inked(two, 2));
        looks("two", two);
        Drawn three = bubble(W3, 152, 147, 192, 221, 360, 240);
        assertEquals(new Rectangle(152, 147, 192, 67), three.box());
        assertTrue(inked(three, 2));
        assertFalse(three.cut());
        looks("three", three);
    }

    private static final String TEN = "가나다라마바사아자차";
    private static final String W6 = String.join(" ", TEN, TEN, TEN, TEN, TEN, TEN);

    @Test
    void eBub4TooManyLines() throws IOException {
        Drawn card = bubble(W6, 152, 147, 192, 221, 360, 240);
        assertEquals(new Rectangle(152, 147, 192, 67), card.box());
        assertTrue(card.cut());
        assertFalse(inked(card, 2, 152 + 32 + 154, 360));
        // 마지막 줄은 남은 줄을 이어 붙여 줄이므로 열 자 낱말(110)보다 길다.
        assertTrue(inked(card, 2, 152 + 32 + 115, 152 + 32 + 154));
        looks("six-room3", card);
        Drawn tall = bubble(W6, 152, 147, 192, 400, 360, 412);
        assertEquals(new Rectangle(152, 147, 192, 84), tall.box());
        assertTrue(tall.cut());
        assertTrue(inked(tall, 3));
        assertFalse(inked(tall, 3, 152 + 32 + 154, 360));
        assertTrue(inked(tall, 3, 152 + 32 + 115, 152 + 32 + 154));
        looks("six-tall", tall);
    }

    @Test
    void eBub5LittleRoomBelow() throws IOException {
        Object[][] cases = {{"six-room2", W6, 200, 50}, {"three-room2", W3, 197, 50}, {"three-room1", W3, 196, 33}, {"six-below-top", W6, 100, 33}};
        for (Object[] c : cases) {
            Drawn d = bubble((String) c[1], 152, 147, 192, (int) c[2], 360, 240);
            assertEquals(new Rectangle(152, 147, 192, (int) c[3]), d.box());
            assertTrue(d.cut());
            looks((String) c[0], d);
        }
    }

    @Test
    void eBub6Moved() throws IOException {
        Drawn d = bubble(W2, 30, 20, 192, 221, 360, 240);
        assertEquals(new Rectangle(30, 20, 192, 50), d.box());
        looks("moved", d);
    }

    /** 프로토타입은 wrap이 끝나지 않아 결과가 없다. S3k E-WRAP-6의 고침대로 한 줄에 한 글자씩 놓인다. */
    @Test
    void eBub7NarrowerThanOneLetter() {
        Drawn narrow = assertTimeoutPreemptively(Duration.ofSeconds(5), () -> bubble("잘했어! +10P", 152, 147, 40, 400, 360, 412));
        assertEquals(new Rectangle(152, 147, 40, 84), narrow.box());
        assertTrue(narrow.cut());
        Drawn zero = assertTimeoutPreemptively(Duration.ofSeconds(5), () -> bubble("잘했어", 152, 147, 0, 400, 360, 412));
        assertEquals(new Rectangle(152, 147, 0, 67), zero.box());
        assertFalse(zero.cut());
    }

    /** 프로토타입 PetCard처럼 힌트를 켜고 카드 w x h의 틀에 펫만 투명 그림에 그린다. 기준 그림은 proto/petcard-pet-*.png다(S3m). */
    static BufferedImage pet(PetState p, int w, int h, double t) {
        BufferedImage img = new BufferedImage(w + 4, h + 4, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = hinted(img);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        PetCardArt.pet(g, p, frame(w, h), t);
        g.dispose();
        return img;
    }

    @Test
    void ePcard1PetInCardSizes() throws IOException {
        PetState cat = new PetState(PetKind.CAT, "나비");
        int[][] sizes = {{360, 240}, {300, 210}, {800, 500}};
        double[] fill = {0.7915, 0.7826, 0.8147};
        for (int i = 0; i < sizes.length; i++) {
            int w = sizes[i][0];
            int h = sizes[i][1];
            Snap.assertLooksLikePrototype("petcard-pet-size-" + w + "x" + h, pet(cat, w, h, 0));
            assertEquals(fill[i], PetCardArt.petFill(cat, frame(w, h)), 0.00005);
        }
        double[] scale = {0.94, 0.94, 0.97, 1.0, 1.0};
        for (int stage = 0; stage < scale.length; stage++) {
            assertEquals(scale[stage], PetArt.cardScale(stage));
        }
    }
}
