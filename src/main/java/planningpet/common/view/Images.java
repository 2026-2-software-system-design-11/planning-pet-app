package planningpet.common.view;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 * 디자인 시스템 6절의 그림. 모두 나의 방 예시(2026-11-23)의 카툰 셀 채색 한 벌이다. 만든 방법은 prototype/assets/PROMPTS.md에 있다.
 * 없으면 null을 돌려주고 부르는 쪽이 그리지 않는다. 창 바탕이 쓰는 둘만 옮겼고, 다른 그림은 그것을 쓰는 화면의 단위가 더한다.
 */
public final class Images {
    /** 창 왼쪽 아래 덤불과 데이지. 예시에서 잘라 냈다. */
    public static final BufferedImage MEADOW = load("meadow-corner.png");
    /** 사이드바 아래 구름. 예시에서 잘라 냈다. */
    public static final BufferedImage CLOUD = load("cloud.png");

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
}
