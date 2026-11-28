package planningpet.common.view;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import planningpet.pet.model.WearId;
import planningpet.shop.model.ItemId;

/**
 * 디자인 시스템 6절의 그림. 모두 나의 방 예시(2026-11-23)의 카툰 셀 채색 한 벌이다. 만든 방법은 prototype/assets/PROMPTS.md에 있다.
 * 없으면 null을 돌려주고 부르는 쪽이 그리지 않는다. 창 바탕과 캐릭터 창의 큰 펫 무대와 장신구 카드가 쓰는 것만 옮겼고, 다른 그림은 그것을
 * 쓰는 화면의 단위가 더한다(docs/porting/character.md S8c2, S8d2).
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
    /** 장신구(D-51)와 wear 안 파일 이름. 05 Q-28대로 펫 그림과 같은 도형 코드로 그려 저장한 것이다. */
    private static final Map<WearId, String> WEAR_FILES = new EnumMap<>(Map.ofEntries(
            Map.entry(WearId.BASIC_NECKLACE, "collar-basic"), Map.entry(WearId.RIBBON_NECKLACE, "collar-ribbon"),
            Map.entry(WearId.STAR_NECKLACE, "collar-star"), Map.entry(WearId.CROWN_NECKLACE, "collar-crown"),
            Map.entry(WearId.FLOWER_PIN, "pin-flower"), Map.entry(WearId.RIBBON_PIN, "pin-ribbon"), Map.entry(WearId.GLASSES, "glasses"),
            Map.entry(WearId.STRAW_HAT, "hat-straw"), Map.entry(WearId.BERET, "hat-beret"), Map.entry(WearId.CROWN, "hat-crown"),
            Map.entry(WearId.SCARF, "scarf"), Map.entry(WearId.VEST, "vest"), Map.entry(WearId.CAPE, "cape")));
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

    /** 장신구 그림(캐릭터 창 장신구 카드). 투명한 가장자리는 잘라 두고 같은 그림을 돌려준다. null이면 null이다. */
    public static BufferedImage wear(WearId wear) {
        String file = WEAR_FILES.get(wear);
        return file == null ? null : trimmed("wear/" + file + ".png");
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

    /** 비율을 지키며 상자를 빈틈없이 덮게 그리고 넘치는 몫은 상자 가운데를 기준으로 양쪽에 나눈다. 상자 밖은 부르는 쪽이 잘라 낸다. */
    public static void cover(Graphics2D g, BufferedImage im, int x, int y, int w, int h) {
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        double scale = Math.max((double) w / im.getWidth(), (double) h / im.getHeight());
        int dw = (int) Math.ceil(im.getWidth() * scale);
        int dh = (int) Math.ceil(im.getHeight() * scale);
        g.drawImage(im, x + (w - dw) / 2, y + (h - dh) / 2, dw, dh, null);
    }

    /** 비율을 지키며 상자 안 가운데에 맞춰 그린다. */
    public static void fit(Graphics2D g, BufferedImage im, int x, int y, int w, int h) {
        place(g, im, x, y, w, h, 0.5);
    }

    /** 비율을 지키며 상자 안에 맞추고 가로는 가운데, 세로는 아래에 붙인다(바닥에 놓는 러그와 털실 공). */
    public static void fitBottom(Graphics2D g, BufferedImage im, int x, int y, int w, int h) {
        place(g, im, x, y, w, h, 1.0);
    }

    /** 상자 안에 맞춘 그림의 폭과 높이는 버림하고, 남는 높이의 vertical 몫(0은 위, 1은 아래)을 위에 둔다. */
    private static void place(Graphics2D g, BufferedImage im, int x, int y, int w, int h, double vertical) {
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        double scale = Math.min((double) w / im.getWidth(), (double) h / im.getHeight());
        int dw = (int) (im.getWidth() * scale);
        int dh = (int) (im.getHeight() * scale);
        g.drawImage(im, x + (w - dw) / 2, y + (int) ((h - dh) * vertical), dw, dh, null);
    }
}
