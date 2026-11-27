package planningpet.pet.view;

import planningpet.common.view.Card;
import planningpet.common.view.Icons;
import planningpet.common.view.Theme;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JRadioButton;

/**
 * 반려동물 고르기 창의 동물 카드(05 입력 항목 1, 디자인 시스템 10절 "고르기 창"). 부품은 05대로 `JRadioButton`이고 모양만 카드로 그린다.
 * 흰 바깥 테 안에 종류별 바탕(고양이 차가운 하늘빛, 강아지 따뜻한 크림), 발자국과 종류 이름, 설명 두 줄. 고른 카드는 민트 테두리다.
 */
public final class PickCard extends JRadioButton {
    /** 1배 크기. 창 폭 800에 두 장과 사이 20이 들어간다. */
    static final int W = 248;
    static final int H = 262;
    private static final int RADIUS = 30;
    private final PetKind kind;
    /** 이름 아래 설명 두 줄(11-23 예시 글, 줄은 \n으로 나눈다). */
    private final String about;

    public PickCard(String text, String kind, boolean selected, String about) {
        super(text, selected);
        this.about = about;
        this.kind = "DOG".equals(kind) ? PetKind.DOG : PetKind.CAT;
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setRolloverEnabled(true);
        setFont(Theme.heavy(22));
        getAccessibleContext().setAccessibleDescription(about.replace("\n", " "));
        addItemListener(e -> repaint());
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(W, H);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        PickScene.hints(g);
        boolean on = isSelected();
        boolean over = getModel().isRollover() && isEnabled();
        boolean cat = kind == PetKind.CAT;
        int x = 4;
        int y = 2;
        int w = getWidth() - 8;
        int h = getHeight() - 9;
        // 바깥: 그림자와 흰 테.
        Card.shadow(g, x, y + 2, w, h, RADIUS + 4);
        if (on) {
            Card.shadow(g, x, y + 4, w, h, RADIUS + 4);
        }
        g.setColor(Theme.CARD_TOP);
        g.fill(new RoundRectangle2D.Double(x, y, w, h, RADIUS, RADIUS));
        // 안: 종류별 바탕.
        double ring = 5;
        RoundRectangle2D inner = new RoundRectangle2D.Double(x + ring, y + ring, w - ring * 2, h - ring * 2, RADIUS - 8, RADIUS - 8);
        Graphics2D in = (Graphics2D) g.create();
        in.clip(inner);
        in.setPaint(new GradientPaint(0, (float) inner.getY(), cat ? Theme.COOL_TOP : Theme.WARM_TOP, 0,
                (float) inner.getMaxY(), cat ? Theme.COOL_BOTTOM : Theme.WARM_BOTTOM));
        in.fill(inner);
        double cx = getWidth() / 2.0;
        // 이름 줄: 발자국과 종류 이름.
        in.setFont(getFont());
        FontMetrics fm = in.getFontMetrics();
        int icon = 22;
        int gap = 6;
        int textW = fm.stringWidth(getText());
        int rowX = (int) (cx - (icon + gap + textW) / 2.0);
        int base = (int) (inner.getY() + 178);
        Icons.draw(in, Icons.Kind.PAW, rowX, base - fm.getAscent() / 2 - icon / 2 - 1, icon, cat ? Theme.ICON : Theme.WARM_ICON);
        in.setColor(Theme.INK);
        in.drawString(getText(), rowX + icon + gap, base);
        // 설명 두 줄: 중간 굵기 14, INK_SUB, 가운데 맞춤.
        in.setFont(Theme.font(14));
        FontMetrics small = in.getFontMetrics();
        in.setColor(Theme.INK_SUB);
        String[] lines = about.split("\n");
        for (int i = 0; i < lines.length; i++) {
            in.drawString(lines[i], (int) (cx - small.stringWidth(lines[i]) / 2.0), base + 25 + i * 20);
        }
        in.dispose();
        // 안쪽 테두리: 고름은 민트 3, 마우스는 MINT_LINE 2, 나머지는 종류 색 1.5.
        Shape edge = new RoundRectangle2D.Double(x + ring, y + ring, w - ring * 2, h - ring * 2, RADIUS - 8, RADIUS - 8);
        if (on) {
            g.setPaint(new GradientPaint(0, y, Theme.MINT_TOP, 0, y + h, Theme.MINT_DEEP));
            g.setStroke(new BasicStroke(3f));
        } else {
            g.setColor(over ? Theme.MINT_LINE : cat ? Theme.COOL_LINE : Theme.WARM_LINE);
            g.setStroke(new BasicStroke(over ? 2f : 1.5f));
        }
        g.draw(edge);
        g.dispose();
    }
}
