package planningpet.proto;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;

/** 펫 임시 그림을 가운데에 앉혀 보이는 부품. scale만큼 크게 그린다. */
public final class PetView extends JComponent {
    private final Store store;
    private final int scale;

    public PetView(Store store, int scale) {
        this.store = store;
        this.scale = scale;
        setPreferredSize(new Dimension(80 * scale, 60 * scale));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.translate(getWidth() / 2, getHeight() - 6);
        g.scale(scale, scale);
        Pet.draw(g, store.petKind, 0, 0, Rules.stage(Rules.level(store.exp)), false);
        g.dispose();
    }
}
