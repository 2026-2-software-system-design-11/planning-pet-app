package planningpet.common.view;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * 디자인 시스템 6절의 그림. 모두 나의 방 예시(2026-11-23)의 카툰 셀 채색 한 벌이다. 만든 방법은 assets/PROMPTS.md에 있다.
 * 없으면 null을 돌려주고 부르는 쪽이 임시 그림을 그린다.
 */
public final class Images {
    /** 가구와 펫이 없는 방. 아이템과 펫의 자리는 이 그림에 대한 비율이다(RoomView). */
    public static final BufferedImage ROOM = load("room.png");
    /** 창 왼쪽 아래 덤불과 데이지. 예시에서 잘라 냈다. */
    public static final BufferedImage MEADOW = load("meadow-corner.png");
    /** 사이드바 아래 구름. 예시에서 잘라 냈다. */
    public static final BufferedImage CLOUD = load("cloud.png");
    /** pet 화면 상점 버튼 위의 쇼핑백. 예시에서 잘라 냈다. */
    public static final BufferedImage SHOP_BAG = load("shop-bag.png");
    /** 아이템 이름(D-28)과 파일 이름. */
    private static final Map<String, String> ITEM_FILES = Map.ofEntries(
            Map.entry("쿠션", "cushion"), Map.entry("화분", "plant"), Map.entry("러그", "rug"), Map.entry("액자", "frame"),
            Map.entry("조명", "lamp"), Map.entry("책장", "bookshelf"), Map.entry("책상", "desk"), Map.entry("소파", "sofa"),
            Map.entry("캣타워", "cat-tower"), Map.entry("우드 침대", "bed"), Map.entry("걸이 화분", "hanging-plant"),
            Map.entry("털실 공", "yarn"), Map.entry("뼈다귀", "bone"));
    /** 자세(D-50)와 파일 이름. */
    private static final Map<String, String> POSE_FILES = Map.of(
            "앉기", "sit", "자기", "sleep", "기지개", "stretch", "밥 먹기", "eat", "노래", "sing");
    /** 장신구(D-51)와 파일 이름. */
    private static final Map<String, String> WEAR_FILES = Map.ofEntries(
            Map.entry("기본 목걸이", "collar-basic"), Map.entry("리본 목걸이", "collar-ribbon"),
            Map.entry("별 목걸이", "collar-star"), Map.entry("왕관 목걸이", "collar-crown"),
            Map.entry("꽃 핀", "pin-flower"), Map.entry("리본 핀", "pin-ribbon"), Map.entry("안경", "glasses"),
            Map.entry("밀짚모자", "hat-straw"), Map.entry("베레모", "hat-beret"), Map.entry("왕관", "hat-crown"),
            Map.entry("스카프", "scarf"), Map.entry("조끼", "vest"), Map.entry("망토", "cape"));
    private static final Map<String, BufferedImage> CACHE = new HashMap<>();

    private Images() {
    }

    private static BufferedImage load(String name) {
        try (InputStream in = Images.class.getResourceAsStream("/assets/" + name)) {
            return in == null ? null : ImageIO.read(in);
        } catch (java.io.IOException e) {
            return null;
        }
    }

    /** 펫 그림. kind는 "CAT" 또는 "DOG", stage는 모습 단계 1~3(D-20). 투명한 가장자리는 잘라 둔다. */
    public static BufferedImage pet(String kind, int stage) {
        String file = "pets/" + ("DOG".equals(kind) ? "dog" : "cat") + "-" + Math.max(1, Math.min(3, stage)) + ".png";
        return trimmed(file);
    }

    /** 자세 그림(D-50). pets/{cat,dog}-{자세}.png가 없으면 앉기는 모습 1단계 그림(장신구 없음)을, 다른 자세는 앉기 그림을 쓴다. */
    public static BufferedImage pose(String kind, String pose) {
        String animal = "DOG".equals(kind) ? "dog" : "cat";
        BufferedImage im = trimmed("pets/" + animal + "-" + POSE_FILES.getOrDefault(pose, "sit") + ".png");
        if (im != null) {
            return im;
        }
        BufferedImage sit = trimmed("pets/" + animal + "-sit.png");
        return sit != null ? sit : pet(kind, 1);
    }

    /** 펫 칩의 얼굴. pets/{cat,dog}-face.png가 없으면 앉기 그림의 위쪽을 잘라 쓴다. */
    public static BufferedImage face(String kind) {
        String animal = "DOG".equals(kind) ? "dog" : "cat";
        BufferedImage im = trimmed("pets/" + animal + "-face.png");
        if (im != null) {
            return im;
        }
        synchronized (CACHE) {
            String key = "face:" + animal;
            if (!CACHE.containsKey(key)) {
                BufferedImage sit = pose(kind, "앉기");
                CACHE.put(key, sit == null ? null : sit.getSubimage(0, 0, sit.getWidth(), sit.getHeight() * 11 / 20));
            }
            return CACHE.get(key);
        }
    }

    /** 장신구 그림(D-51). 없으면 null. */
    public static BufferedImage wear(String name) {
        String file = WEAR_FILES.get(name);
        return file == null ? null : trimmed("wear/" + file + ".png");
    }

    /** 아이템 그림. 이름이 목록에 없으면 null이다. */
    public static BufferedImage item(String name) {
        String file = ITEM_FILES.get(name);
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

    /** 투명한 가장자리를 잘라 낸다. */
    private static BufferedImage trim(BufferedImage im) {
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

    /** 비율을 지키며 상자를 빈틈없이 덮게 그리고 넘치는 곳은 가운데 기준으로 잘라 낸다. */
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

    /** 비율을 지키며 상자 안에 맞추고 가로는 가운데, 세로는 아래에 붙인다(바닥에 서는 펫과 가구). */
    public static void fitBottom(Graphics2D g, BufferedImage im, int x, int y, int w, int h) {
        place(g, im, x, y, w, h, 1.0);
    }

    /** 상자 위에 붙여 맞춘다(천장에 거는 것). */
    public static void fitTop(Graphics2D g, BufferedImage im, int x, int y, int w, int h) {
        place(g, im, x, y, w, h, 0);
    }

    private static void place(Graphics2D g, BufferedImage im, int x, int y, int w, int h, double vertical) {
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        double scale = Math.min((double) w / im.getWidth(), (double) h / im.getHeight());
        int dw = (int) (im.getWidth() * scale);
        int dh = (int) (im.getHeight() * scale);
        g.drawImage(im, x + (w - dw) / 2, y + (int) ((h - dh) * vertical), dw, dh, null);
    }
}
