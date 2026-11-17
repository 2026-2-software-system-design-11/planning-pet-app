package planningpet.common.view;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.List;
import javax.imageio.ImageIO;

/** 디자인 시스템 6절의 그림. 없으면 null을 돌려주고 부르는 쪽이 임시 그림을 그린다. */
public final class Images {
    public static final BufferedImage LANDSCAPE = load("landscape.png");
    public static final BufferedImage ROOM = load("room.png");
    private static final BufferedImage CATS = load("cats.png");
    private static final BufferedImage FURNITURE = load("furniture-cutout.png");
    /** furniture-cutout.png의 칸 순서(왼쪽 위부터). */
    private static final java.util.Map<String, BufferedImage> TRIMMED = new java.util.HashMap<>();
    private static final List<String> FURNITURE_ORDER = List.of("러그", "화분", "조명", "쿠션", "액자", "책장");

    private Images() {
    }

    private static BufferedImage load(String name) {
        try (InputStream in = Images.class.getResourceAsStream("/assets/" + name)) {
            return in == null ? null : ImageIO.read(in);
        } catch (java.io.IOException e) {
            return null;
        }
    }

    /** 모습 단계(1~3)의 고양이. */
    public static BufferedImage cat(int stage) {
        return CATS == null ? null : cell(CATS, stage - 1, 0, 3, 1);
    }

    /** 아이템 그림. 책상과 소파는 그림이 없어 null이다. */
    public static BufferedImage item(String name) {
        int i = FURNITURE_ORDER.indexOf(name);
        return FURNITURE == null || i < 0 ? null
                : TRIMMED.computeIfAbsent(name, n -> trim(cell(FURNITURE, i % 3, i / 3, 3, 2)));
    }

    private static BufferedImage cell(BufferedImage atlas, int col, int row, int cols, int rows) {
        int w = atlas.getWidth() / cols;
        int h = atlas.getHeight() / rows;
        return atlas.getSubimage(col * w, row * h, w, h);
    }

    /** 투명한 가장자리를 잘라 낸다(원본 Assets.item과 같다). */
    private static BufferedImage trim(BufferedImage im) {
        int x0 = im.getWidth();
        int y0 = im.getHeight();
        int x1 = 0;
        int y1 = 0;
        for (int y = 0; y < im.getHeight(); y++) {
            for (int x = 0; x < im.getWidth(); x++) {
                if ((im.getRGB(x, y) >>> 24) > 200) {
                    x0 = Math.min(x0, x);
                    y0 = Math.min(y0, y);
                    x1 = Math.max(x1, x);
                    y1 = Math.max(y1, y);
                }
            }
        }
        return x0 < x1 && y0 < y1 ? im.getSubimage(x0, y0, x1 - x0 + 1, y1 - y0 + 1) : im;
    }

    /** 비율을 지키며 상자 안 가운데에 맞춰 그린다. */
    public static void fit(Graphics2D g, BufferedImage im, int x, int y, int w, int h) {
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        double scale = Math.min((double) w / im.getWidth(), (double) h / im.getHeight());
        int dw = (int) (im.getWidth() * scale);
        int dh = (int) (im.getHeight() * scale);
        g.drawImage(im, x + (w - dw) / 2, y + (h - dh) / 2, dw, dh, null);
    }
}
