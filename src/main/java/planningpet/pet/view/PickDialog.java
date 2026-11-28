package planningpet.pet.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import planningpet.common.view.Card;
import planningpet.common.view.Icons;
import planningpet.common.view.RoundField;
import planningpet.common.view.Theme;
import planningpet.common.view.Ui;
import planningpet.pet.model.PetKind;

/**
 * 반려동물 고르기 창의 내용(05, UC-17). 첫 실행 때 메인 창보다 먼저 뜬다.
 * 모양은 2026-11-23 고르기 예시를 옮겼다(디자인 시스템 10절 "고르기 창"): 하늘 언덕 장면 위에 창의 줄들을
 * 가운데 한 줄로 둔다. 부품 종류, 글, 기본값은 05 그대로다.
 */
public final class PickDialog {
    /** 리본 아래 부제(05, 11-23 예시 글). */
    static final String SUBTITLE = "선택한 친구와 함께 일정을 관리하고,\n계획을 실천하며 성장할 수 있어요!";
    /** 이름 칸 라벨(05, 11-23 예시 글). */
    static final String NAME_LABEL = "반려동물의 이름을 지어주세요!";
    /** 줄 폭: 카드 둘과 사이. 이름 줄 카드도 이 폭이다. */
    private static final int ROW = PickCard.W * 2 + 20;

    public final PickCard cat = new PickCard("고양이", PetKind.CAT, true, "조용하고 편안한 성격으로\n언제나 곁에서 응원해요!");
    public final PickCard dog = new PickCard("강아지", PetKind.DOG, false, "활발하고 다정한 성격으로\n항상 힘이 되어줘요!");
    public final RoundField name = new RoundField(null, "예시) 모찌, 코코, 하루").count(10);
    public final JPanel panel = new Backdrop();

    public PickDialog() {
        ButtonGroup group = new ButtonGroup();
        group.add(cat);
        group.add(dog);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 0, 22, 0));
        // 창을 키우면 위아래 빈 곳만 늘고 내용은 가운데에 모여 있다.
        panel.add(Box.createVerticalGlue());
        panel.add(new Logo("플래닝펫", "Planning Pet"));
        panel.add(Box.createVerticalStrut(4));
        panel.add(new Ribbon("함께할 반려동물을 골라 주세요"));
        panel.add(Box.createVerticalStrut(2));
        JLabel sub = new JLabel("<html><div style='text-align:center'>" + SUBTITLE.replace("\n", "<br>") + "</div></html>",
                SwingConstants.CENTER);
        sub.setFont(Theme.font(16));
        sub.setForeground(Theme.INK_SUB);
        panel.add(center(sub));
        panel.add(Box.createVerticalStrut(12));
        JPanel animals = clear(new FlowLayout(FlowLayout.CENTER, 20, 0));
        animals.add(cat);
        animals.add(dog);
        animals.setMaximumSize(new Dimension(Integer.MAX_VALUE, PickCard.H));
        panel.add(animals);
        panel.add(Box.createVerticalGlue());
    }

    /** 이름 줄 카드: 연필, 라벨, 둥근 이름 칸, 칸 아래 안내(05). 테스트가 카드만 견주도록 패키지 안에 연다. */
    JPanel nameBar() {
        JPanel bar = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                PickScene.hints(g);
                int w = getWidth() - 4;
                int h = getHeight() - 6;
                Card.shadow(g, 2, 1, w, h, 26);
                g.setColor(PickScene.alpha(Theme.CARD_TOP, 225));
                g.fillRoundRect(2, 1, w, h, 26, 26);
                g.setColor(Theme.CARD_EDGE);
                g.drawRoundRect(2, 1, w - 1, h - 1, 26, 26);
                g.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(10, 20, 14, 22));
        JLabel label = new JLabel(NAME_LABEL, Icons.icon(Icons.Kind.PENCIL, 22, Theme.ICON), SwingConstants.LEFT);
        label.setFont(Theme.bold(15));
        label.setIconTextGap(8);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 6));
        label.setLabelFor(name);
        name.setFont(Theme.font(14));
        name.setColumns(12);
        name.getAccessibleContext().setAccessibleName("반려동물 이름");
        JLabel hint = Ui.hint("비워 두면 나비, 바둑이로 불러요");
        // 오른쪽 빈 테: 배율이 바뀌면 글 폭이 조금 늘어 끝 글자가 잘린다(2배 그림에서 "불러요"가 잘림).
        hint.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 6));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 0, 16);
        bar.add(label, c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 0, 0);
        bar.add(name, c);
        c.gridy = 1;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(4, 0, 0, 0);
        bar.add(hint, c);
        bar.setPreferredSize(new Dimension(ROW, bar.getPreferredSize().height));
        bar.setMaximumSize(bar.getPreferredSize());
        return bar;
    }

    private static JPanel clear(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setOpaque(false);
        p.setAlignmentX(0.5f);
        return p;
    }

    private static JPanel center(JComponent c) {
        JPanel p = clear(new FlowLayout(FlowLayout.CENTER, 0, 0));
        p.add(c);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        return p;
    }

    /** 창 바탕(PickScene.backdrop). 폭은 800 이상이다. */
    private static final class Backdrop extends JPanel {
        Backdrop() {
            setOpaque(true);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(Math.max(800, d.width), d.height);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            PickScene.backdrop(g, getWidth(), getHeight());
            g.dispose();
        }
    }

    /**
     * 로고: 사이드바와 같은 PetArt 고양이 얼굴, 아주 굵은 word("플래닝펫"), english("Planning Pet"), 양옆 강조선.
     * 두 글은 라벨 글이 아니라 그림이라, 글 없는 기준 그림을 그리려고 생성자로 받는다(docs/porting/pick.md).
     */
    static final class Logo extends JLabel {
        private final String word;
        private final String english;

        Logo(String word, String english) {
            super(word + " " + english);
            this.word = word;
            this.english = english;
            setAlignmentX(0.5f);
            setPreferredSize(new Dimension(800, 60));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            PickScene.hints(g);
            int faceW = 74;
            int faceH = 51;
            g.setFont(Theme.heavy(30));
            FontMetrics big = g.getFontMetrics();
            int wordW = big.stringWidth(word);
            int gap = 10;
            int total = faceW + gap + wordW;
            int x = (getWidth() - total) / 2;
            int y = (getHeight() - faceH) / 2;
            PetArt.drawLogo(g, x, y, faceW, faceH);
            int tx = x + faceW + gap;
            g.setColor(Theme.INK);
            g.drawString(word, tx, y + big.getAscent() - 4);
            g.setFont(Theme.bold(14));
            FontMetrics small = g.getFontMetrics();
            g.setColor(Theme.INK_SUB);
            g.drawString(english, tx + (wordW - small.stringWidth(english)) / 2, y + faceH);
            int s = 24;
            Graphics2D left = (Graphics2D) g.create();
            left.translate(x - 14, 0);
            left.scale(-1, 1);
            Icons.draw(left, Icons.Kind.SPARK, 0, y + 2, s, Theme.SPARK);
            left.dispose();
            Icons.draw(g, Icons.Kind.SPARK, tx + wordW + 12, y + 2, s, Theme.SPARK);
            g.dispose();
        }
    }

    /** 리본 제목. 글은 라벨 글이라 화면 읽기와 QA가 그대로 읽는다. 하늘 발자국 둘이 양옆에 붙는다. */
    private static final class Ribbon extends JLabel {
        Ribbon(String text) {
            super(text, SwingConstants.CENTER);
            setFont(Theme.heavy(21));
            setForeground(Color.WHITE);
            setAlignmentX(0.5f);
            setPreferredSize(new Dimension(800, 62));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            PickScene.hints(g);
            FontMetrics fm = g.getFontMetrics(getFont());
            int bandW = Math.max(380, fm.stringWidth(getText()) + 120);
            int bandH = 40;
            double bx = (getWidth() - bandW) / 2.0;
            double by = 8;
            PickScene.ribbon(g, bx, by, bandW, bandH);
            PickScene.skyPaw(g, bx - 78, by + 32, 28, -0.25);
            PickScene.skyPaw(g, bx + bandW + 78, by + 32, 28, 0.25);
            g.setFont(getFont());
            int tw = fm.stringWidth(getText());
            int tx = (getWidth() - tw) / 2;
            int ty = (int) (by + bandH / 2.0 + (fm.getAscent() - fm.getDescent()) / 2.0);
            g.setColor(PickScene.alpha(Theme.RIBBON_DEEP.darker(), 120));
            g.drawString(getText(), tx, ty + 1);
            g.setColor(getForeground());
            g.drawString(getText(), tx, ty);
            g.dispose();
        }
    }
}
