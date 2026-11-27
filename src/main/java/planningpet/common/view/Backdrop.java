package planningpet.common.view;

import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * 창 바탕(디자인 시스템 1절): 아쿠아 그라데이션, 왼쪽 아래에 구름과 덤불. 프로토타입 App의 root 패널이 그리던 것이다.
 * 구름과 덤불의 크기와 자리는 사이드바의 오른쪽 끝으로 잰다(docs/porting/icons.md).
 */
public class Backdrop extends JPanel {
    private final JComponent side;
    private final BufferedImage cloud;
    private final BufferedImage meadow;

    /** side는 이 패널 안의 사이드바다. */
    public Backdrop(LayoutManager layout, JComponent side) {
        this(layout, side, Images.CLOUD, Images.MEADOW);
    }

    /** 그림 파일이 없을 때(null)를 검사하는 입구. */
    Backdrop(LayoutManager layout, JComponent side, BufferedImage cloud, BufferedImage meadow) {
        super(layout);
        this.side = side;
        this.cloud = cloud;
        this.meadow = meadow;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setPaint(new GradientPaint(0, 0, Theme.BG_TOP, 0, getHeight(), Theme.BG_BOTTOM));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.dispose();
    }

    @Override
    protected void paintChildren(Graphics graphics) {
        super.paintChildren(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        int right = side.getX() + side.getWidth();
        g.clipRect(0, 0, right + 4, getHeight());
        if (cloud != null) {
            int cw = (int) (right * 0.62);
            int ch = cw * cloud.getHeight() / cloud.getWidth();
            g.drawImage(cloud, right - cw - 4, getHeight() - (int) (right * 1.05) - ch, cw, ch, null);
        }
        if (meadow != null) {
            // 예시에서 잘라 낸 덤불은 왼쪽 아래가 창 모서리처럼 둥글다. 그 모서리가 창 밖으로 나가게 조금 내려 그린다.
            int mw = (int) (right * 0.86);
            int mh = mw * meadow.getHeight() / meadow.getWidth();
            g.drawImage(meadow, -mw / 14, getHeight() - mh + mh / 14, mw, mh, null);
        }
        g.dispose();
    }
}
