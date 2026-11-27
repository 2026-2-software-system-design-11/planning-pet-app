package planningpet.pet.view;

import planningpet.common.view.Card;
import planningpet.common.view.Icons;
import planningpet.common.view.Theme;
import planningpet.pet.model.PetState;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.HashSet;
import javax.swing.JRadioButton;

/**
 * 반려동물 고르기 창의 동물 카드(05 입력 항목 1, 디자인 시스템 10절 "고르기 창"). 부품은 05대로 `JRadioButton`이고 모양만 카드로 그린다.
 * 흰 바깥 테 안에 종류별 바탕(고양이 차가운 하늘빛, 강아지 따뜻한 크림), 작은 언덕 위의 PetArt 펫, 발자국과 종류 이름.
 * 고른 카드는 민트 테두리와 체크 배지, 펫 옆 강조선이고 고를 때 펫이 살짝 튄다. 펫은 화면에 보일 때만 숨쉰다.
 */
public final class PickCard extends JRadioButton {
    /** 1배 크기. 창 폭 800에 두 장과 사이 20이 들어간다. */
    static final int W = 248;
    static final int H = 262;
    private static final int RADIUS = 30;
    private final PetKind kind;
    private final PetState look;
    /** 이름 아래 설명 두 줄(11-23 예시 글, 줄은 \n으로 나눈다). */
    private final String about;
    private double t;
    private double since = 99;
    private long start = System.nanoTime();
    private long pickedAt;
    private javax.swing.Timer clock;

    public PickCard(String text, String kind, boolean selected, String about) {
        super(text, selected);
        this.about = about;
        this.kind = "DOG".equals(kind) ? PetKind.DOG : PetKind.CAT;
        this.look = new PetState(kind, PetState.defaultName(kind));
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setRolloverEnabled(true);
        setFont(Theme.heavy(22));
        getAccessibleContext().setAccessibleDescription(about.replace("\n", " "));
        addItemListener(e -> {
            if (isSelected()) {
                pickedAt = System.nanoTime();
            }
            repaint();
        });
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
    public void addNotify() {
        super.addNotify();
        if (clock == null) {
            clock = new javax.swing.Timer(40, e -> {
                if (isShowing()) {
                    tick();
                    repaint();
                }
            });
        }
        clock.start();
    }

    @Override
    public void removeNotify() {
        if (clock != null) {
            clock.stop();
        }
        super.removeNotify();
    }

    private void tick() {
        long now = System.nanoTime();
        t = (now - start) / 1e9;
        since = pickedAt == 0 ? 99 : (now - pickedAt) / 1e9;
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
        // 안: 종류별 바탕과 언덕, 펫.
        double ring = 5;
        RoundRectangle2D inner = new RoundRectangle2D.Double(x + ring, y + ring, w - ring * 2, h - ring * 2, RADIUS - 8, RADIUS - 8);
        Graphics2D in = (Graphics2D) g.create();
        in.clip(inner);
        in.setPaint(new GradientPaint(0, (float) inner.getY(), cat ? Theme.COOL_TOP : Theme.WARM_TOP, 0,
                (float) inner.getMaxY(), cat ? Theme.COOL_BOTTOM : Theme.WARM_BOTTOM));
        in.fill(inner);
        double ix = inner.getX();
        double iw = inner.getWidth();
        double hillTop = inner.getY() + 120;
        PickScene.bush(in, ix + iw * 0.10, hillTop + 22, 46);
        PickScene.bush(in, ix + iw * 0.92, hillTop + 20, 40);
        Color hill = cat ? Theme.COOL_HILL : Theme.WARM_HILL;
        in.setPaint(new GradientPaint(0, (float) hillTop, hill, 0, (float) inner.getMaxY(), PickScene.mix(hill, Color.WHITE, 0.45)));
        in.fill(new Ellipse2D.Double(ix - iw * 0.18, hillTop, iw * 1.36, inner.getHeight()));
        PickScene.daisy(in, ix + iw * 0.12, hillTop + 6, 7, 0.2);
        PickScene.daisy(in, ix + iw * 0.88, hillTop + 4, 6.5, -0.3);
        double cx = getWidth() / 2.0;
        double ground = hillTop + 16;
        double size = 140;
        PetArt.drawAt(in, kind, PetPose.SIT, new HashSet<>(look.wear.values()), t, since, cx, ground, size);
        if (on) {
            double petTop = ground - size * PetArt.heightRatio(kind, PetPose.SIT);
            Icons.draw(in, Icons.Kind.SPARK, (int) (cx - size * 0.48), (int) (petTop + 2), 26, Theme.SPARK);
        }
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
        if (isFocusOwner()) {
            g.setColor(PickScene.alpha(Theme.MINT_DEEP, 150));
            g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[] {4f, 4f}, 0f));
            g.draw(new RoundRectangle2D.Double(x + ring + 4, y + ring + 4, w - ring * 2 - 8, h - ring * 2 - 8, RADIUS - 14, RADIUS - 14));
        }
        // 체크 배지: 색이 아니라 모양으로도 고름을 보인다(ICON 바탕은 카드 바탕과 5.7:1).
        if (on) {
            int b = 26;
            int bx = x + w - b - 10;
            int by = y + 10;
            g.setColor(Theme.CARD_TOP);
            g.fill(new Ellipse2D.Double(bx - 2.5, by - 2.5, b + 5, b + 5));
            Icons.draw(g, Icons.Kind.CHECK, bx, by, b, Theme.ICON);
        }
        g.dispose();
    }
}
