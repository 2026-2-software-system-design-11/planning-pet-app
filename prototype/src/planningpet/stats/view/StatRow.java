package planningpet.stats.view;

import planningpet.app.view.App;
import planningpet.common.view.Card;
import planningpet.common.view.Icons;
import planningpet.common.view.Images;
import planningpet.common.view.RoundButtonUI;
import planningpet.common.view.Theme;
import planningpet.stats.model.Stats;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

/** 도넛 오른쪽의 한 줄: 아이콘, 이름, 값을 담은 옅은 알약. */
final class StatRow extends JComponent {
    static final int DONE = 0;
    static final int LEFT = 1;
    static final int TOTAL = 2;
    private final int kind;
    private final String name;
    String value = "";

    StatRow(int kind, String name) {
        this.kind = kind;
        this.name = name;
        setPreferredSize(new Dimension(150, 38));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        StatsArt.smooth(g);
        int w = getWidth() - 2;
        int h = getHeight() - 3;
        g.setColor(new Color(0xF4FAFB));
        g.fill(new RoundRectangle2D.Double(1, 1, w, h, h, h));
        g.setColor(Theme.CARD_EDGE);
        g.draw(new RoundRectangle2D.Double(1, 1, w - 1, h - 1, h, h));
        int s = 20;
        int iy = 1 + (h - s) / 2;
        switch (kind) {
            case DONE -> Icons.draw(g, Icons.Kind.CHECK, 10, iy, s, Theme.MINT_DEEP);
            case LEFT -> StatsArt.draw(g, StatsArt.Kind.RING, 10, iy, s, Theme.MINT_DEEP);
            default -> Icons.draw(g, Icons.Kind.CALENDAR, 10, iy, s, Theme.SATURDAY);
        }
        g.setFont(Theme.bold(13));
        FontMetrics fm = g.getFontMetrics();
        int base = 1 + (h + fm.getAscent() - fm.getDescent()) / 2;
        g.setColor(Theme.INK);
        g.drawString(name, 10 + s + 7, base);
        g.setFont(Theme.heavy(15));
        fm = g.getFontMetrics();
        g.drawString(value, w - 10 - fm.stringWidth(value), base);
        g.dispose();
    }
}
