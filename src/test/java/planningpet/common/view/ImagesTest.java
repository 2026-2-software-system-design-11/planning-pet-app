package planningpet.common.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import planningpet.pet.model.WearId;
import planningpet.shop.model.ItemId;

/** 방 그림과 아이템 그림, 장신구 그림, 상자에 맞춰 그리기. 번호는 docs/porting/character.md S8c2, S8d2. */
class ImagesTest {
    private static String size(BufferedImage im) {
        return im.getWidth() + "x" + im.getHeight();
    }

    /** 모두 불투명한 검정인 w x h 그림. */
    private static BufferedImage solid(int w, int h) {
        BufferedImage im = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                im.setRGB(x, y, 0xFF000000);
            }
        }
        return im;
    }

    /** 40x40 투명 그림에 draw로 그린 뒤 칠한 픽셀의 상자(왼쪽, 위, 오른쪽, 아래). */
    private static String painted(Consumer<Graphics2D> draw) {
        BufferedImage out = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        draw.accept(g);
        g.dispose();
        int[] box = {40, 40, -1, -1};
        for (int y = 0; y < 40; y++) {
            for (int x = 0; x < 40; x++) {
                if (out.getRGB(x, y) >>> 24 != 0) {
                    box = new int[] {Math.min(box[0], x), Math.min(box[1], y), Math.max(box[2], x), Math.max(box[3], y)};
                }
            }
        }
        return box[0] + " " + box[1] + " " + box[2] + " " + box[3];
    }

    /** 방 그림은 그대로, 러그와 털실 공은 투명한 가장자리를 잘라 읽고 다시 불러도 같은 그림이다. 그림 파일이 없는 아이템과 없는 파일은 null이다. */
    @Test
    void eImg1Load() {
        assertEquals("1536x1024", size(Images.ROOM));
        assertEquals("922x538", size(Images.item(ItemId.RUG)));
        assertEquals("696x705", size(Images.item(ItemId.YARN_BALL)));
        assertSame(Images.item(ItemId.RUG), Images.item(ItemId.RUG));
        assertNull(Images.item(ItemId.SOFA));
        assertNull(Images.load("items/none.png"));
    }

    /** 알파 24 넘는 픽셀을 담는 가장 작은 상자로 자른다. 알파 24는 투명으로 본다. 다 투명하거나 상자 폭이 1이면 그대로다. */
    @Test
    void eImg2Trim() {
        BufferedImage im = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        im.setRGB(2, 3, 25 << 24);
        im.setRGB(5, 7, 0xFF000000);
        im.setRGB(9, 9, 24 << 24);
        BufferedImage cut = Images.trim(im);
        assertEquals("4x5", size(cut));
        assertEquals(25 << 24, cut.getRGB(0, 0));
        BufferedImage empty = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        assertSame(empty, Images.trim(empty));
        BufferedImage column = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        column.setRGB(4, 1, 0xFF000000);
        column.setRGB(4, 8, 0xFF000000);
        assertSame(column, Images.trim(column));
    }

    /**
     * cover는 비율을 지키며 상자를 덮는 가장 작은 배율로 올림한 크기에 그리고 넘치는 몫을 양쪽에 나눈다(나누기는 0 쪽으로 버림). 상자 밖도
     * 그린다. 20x10을 (10, 5)의 10x10에 그리면 (5, 5)부터 20x10이다. 3x2를 7x5에 그리면 2.5배의 8x5라 오른쪽으로 1 넘친다.
     */
    @Test
    void eImg3Cover() {
        assertEquals("5 5 24 14", painted(g -> Images.cover(g, solid(20, 10), 10, 5, 10, 10)));
        assertEquals("0 0 7 4", painted(g -> Images.cover(g, solid(3, 2), 0, 0, 7, 5)));
    }

    /** fit은 상자 안 가운데, fitBottom은 가로 가운데에 아래로 붙인다. 맞춘 크기와 남는 몫은 버림한다. */
    @Test
    void eImg4FitAndFitBottom() {
        assertEquals("0 7 29 21", painted(g -> Images.fit(g, solid(20, 10), 0, 0, 30, 30)));
        assertEquals("0 15 29 29", painted(g -> Images.fitBottom(g, solid(20, 10), 0, 0, 30, 30)));
        assertEquals("7 0 21 29", painted(g -> Images.fit(g, solid(10, 20), 0, 0, 30, 30)));
        assertEquals("5 24 29 35", painted(g -> Images.fitBottom(g, solid(20, 10), 5, 4, 25, 32)));
    }

    /** 그림의 x번째 열(column이 참)이나 y번째 줄에 알파 24를 넘는 픽셀이 있다. */
    private static boolean edge(BufferedImage im, int at, boolean column) {
        int n = column ? im.getHeight() : im.getWidth();
        for (int i = 0; i < n; i++) {
            if ((im.getRGB(column ? at : i, column ? i : at) >>> 24) > 24) {
                return true;
            }
        }
        return false;
    }

    @Test
    void eImg5Wear() {
        for (WearId w : WearId.values()) {
            BufferedImage im = Images.wear(w);
            assertSame(im, Images.wear(w), w.label());
            assertTrue(edge(im, 0, true) && edge(im, im.getWidth() - 1, true), w.label() + " 왼쪽과 오른쪽 끝");
            assertTrue(edge(im, 0, false) && edge(im, im.getHeight() - 1, false), w.label() + " 위와 아래 끝");
        }
        assertNull(Images.wear(null));
    }
}
