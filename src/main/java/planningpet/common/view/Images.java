package planningpet.common.view;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import planningpet.shop.model.ItemId;

/**
 * 디자인 시스템 6절의 그림. 모두 나의 방 예시(2026-11-23)의 카툰 셀 채색 한 벌이다. 만든 방법은 prototype/assets/PROMPTS.md에 있다.
 * 없으면 null을 돌려주고 부르는 쪽이 그리지 않는다. 창 바탕과 캐릭터 창의 큰 펫 무대가 쓰는 것만 옮겼고, 다른 그림은 그것을 쓰는 화면의
 * 단위가 더한다(docs/porting/character.md S8c2).
 */
public final class Images {
    /** 가구와 펫이 없는 방. 캐릭터 창 큰 펫 무대의 바탕이다. */
    public static final BufferedImage ROOM = load("room.png");
    /** 창 왼쪽 아래 덤불과 데이지. 예시에서 잘라 냈다. */
    public static final BufferedImage MEADOW = load("meadow-corner.png");
    /** 사이드바 아래 구름. 예시에서 잘라 냈다. */
    public static final BufferedImage CLOUD = load("cloud.png");
    /** 아이템(D-28)과 파일 이름. 캐릭터 창 무대의 러그와 털실 공만 있고 나머지 열하나는 나의 방(S7)이 더한다. */
    private static final Map<ItemId, String> ITEM_FILES = new EnumMap<>(Map.of(ItemId.RUG, "rug", ItemId.YARN_BALL, "yarn"));
    /** 가장자리를 잘라 둔 그림. 열쇠는 assets 안 파일 이름이다. */
    private static final Map<String, BufferedImage> CACHE = new HashMap<>();

    private Images() {
    }

    /** src/main/resources의 /assets/name. 없거나 읽지 못하면 null. */
    static BufferedImage load(String name) {
        try (InputStream in = Images.class.getResourceAsStream("/assets/" + name)) {
            return in == null ? null : ImageIO.read(in);
        } catch (java.io.IOException e) {
            return null;
        }
    }

    /** 아이템 그림. 투명한 가장자리는 잘라 두고 같은 그림을 돌려준다. 그림 파일이 없는 아이템이면 null이다. */
    public static BufferedImage item(ItemId item) {
        String file = ITEM_FILES.get(item);
        return file == null ? null : trimmed("items/" + file + ".png");
    }

    private static BufferedImage trimmed(String file) {
        synchronized (CACHE) {
            if (!CACHE.containsKey(file)) {
                BufferedImage im = load(file);
                CACHE.put(file, im == null ? null : trim(im));
            }
            return CACHE.get(file);
        }
    }

    /**
     * 투명한 가장자리를 잘라 낸다. 알파가 24 넘는 픽셀을 모두 담는 가장 작은 상자다. 그런 픽셀이 없거나 상자의 폭이나 높이가 1이면
     * 그대로 돌려준다(프로토타입과 같다).
     */
    static BufferedImage trim(BufferedImage im) {
        int x0 = im.getWidth();
        int y0 = im.getHeight();
        int x1 = 0;
        int y1 = 0;
        for (int y = 0; y < im.getHeight(); y++) {
            for (int x = 0; x < im.getWidth(); x++) {
                if ((im.getRGB(x, y) >>> 24) > 24) {
                    x0 = Math.min(x0, x);
                    y0 = Math.min(y0, y);
                    x1 = Math.max(x1, x);
                    y1 = Math.max(y1, y);
                }
            }
        }
        return x0 < x1 && y0 < y1 ? im.getSubimage(x0, y0, x1 - x0 + 1, y1 - y0 + 1) : im;
    }
}
