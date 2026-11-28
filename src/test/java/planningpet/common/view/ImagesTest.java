package planningpet.common.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;
import planningpet.shop.model.ItemId;

/** 방 그림과 아이템 그림. 번호는 docs/porting/character.md S8c2. */
class ImagesTest {
    private static String size(BufferedImage im) {
        return im.getWidth() + "x" + im.getHeight();
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
}
