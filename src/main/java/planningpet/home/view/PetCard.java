package planningpet.home.view;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JComponent;
import planningpet.app.model.AppData;
import planningpet.common.view.Card;
import planningpet.common.view.Theme;

/**
 * 홈의 펫 카드(05 홈 "펫 카드", D-59). 하늘과 풀밭 장면 왼쪽에 고른 펫(D-48)을 그린다. 값은 디자인 시스템 10절 "홈"에 있다.
 * 오른쪽 열은 #466, 누르기는 #467이 더한다(docs/porting/home.md S3m).
 */
public final class PetCard extends JComponent {
    private final AppData data;

    /** 고른 펫을 그리고 데이터 변경 알림마다 다시 그린다. 메인 창은 펫을 고른 뒤 열려 고른 펫이 늘 있다(S3m). */
    public PetCard(AppData data) {
        this.data = data;
        data.onChange(this::repaint);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(360, 240);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(300, 210);
    }

    /** 카드 틀(그림자 자리를 뺀 것). Card와 같다. */
    private Rectangle frame() {
        return new Rectangle(2, 1, getWidth() - 4, getHeight() - 8);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        Rectangle f = frame();
        int r = Theme.CARD_RADIUS;
        Card.shadow(g, f.x, f.y, f.width, f.height, r);
        Graphics2D s = (Graphics2D) g.create();
        s.clip(new RoundRectangle2D.Double(f.x, f.y, f.width, f.height, r, r));
        PetCardArt.scene(s, f, PetCardArt.ground(f));
        PetCardArt.pet(s, data.activePet(), f, 0);
        s.dispose();
        g.setColor(Theme.CARD_EDGE);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(new RoundRectangle2D.Double(f.x + 0.5, f.y + 0.5, f.width - 1, f.height - 1, r, r));
        g.dispose();
    }
}
