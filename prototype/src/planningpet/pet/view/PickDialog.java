package planningpet.pet.view;

import planningpet.app.view.App;

import planningpet.app.controller.AppController;
import planningpet.app.model.MockData;
import planningpet.app.model.Store;
import planningpet.common.view.Ui;
import planningpet.pet.controller.PetController;
import planningpet.pet.model.Talk;
import planningpet.common.view.Card;
import planningpet.common.view.Icons;
import planningpet.common.view.Theme;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/** 반려동물 고르기 창(05, UC-17). 첫 실행 때 메인 창보다 먼저 뜬다. */
public final class PickDialog {
    /** 리본 아래 부제(05, 11-23 예시 글). 비우면 줄을 그리지 않는다. */
    static final String SUBTITLE = "선택한 친구와 함께 일정을 관리하고,\n계획을 실천하며 성장할 수 있어요!";
    /** 이름 칸 라벨(05, 11-23 예시 글). */
    static final String NAME_LABEL = "반려동물의 이름을 지어주세요!";
    /** 버튼 자리. true면 예시처럼 가운데(05), false면 오른쪽 아래. */
    static final boolean BUTTONS_CENTER = true;
    /** 줄 폭: 카드 둘과 사이. 이름 줄 카드와 버튼 줄도 이 폭이다. */
    private static final int ROW = PickCard.W * 2 + 20;


    public final JRadioButton cat = new JRadioButton("고양이", true);
    public final JRadioButton dog = new JRadioButton("강아지");
    public final JTextField name = new JTextField(12);
    /** D-74: 펫을 고른 다음 예시로 둘러볼지 바로 시작할지 고른다. 바로 시작이 주 행동이고 Enter와 같다. */
    public final JButton examples = new JButton("예시로 둘러보기");
    public final JButton start = new JButton("바로 시작");
    public final JPanel panel = new JPanel(new BorderLayout(0, 10));

    public PickDialog() {
        Ui.limit(name, 10);
        ButtonGroup group = new ButtonGroup();
        group.add(cat);
        group.add(dog);
        JPanel animals = new JPanel(new GridLayout(2, 2, 16, 4));
        animals.add(new PetView(MockData.look("CAT"), 2));
        animals.add(new PetView(MockData.look("DOG"), 2));
        animals.add(cat);
        animals.add(dog);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        panel.add(new JLabel("<html><b>함께할 반려동물을 골라 주세요</b></html>"), BorderLayout.NORTH);
        panel.add(animals, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout(0, 2));
        bottom.add(Ui.flow(new JLabel("이름"), name), BorderLayout.NORTH);
        JLabel hint = Ui.hint("비워 두면 나비, 바둑이로 불러요");
        hint.setBorder(BorderFactory.createEmptyBorder(0, 44, 0, 0));
        bottom.add(hint, BorderLayout.CENTER);
        start.putClientProperty(planningpet.common.view.RoundButtonUI.PRIMARY, Boolean.TRUE);
        JPanel choose = new JPanel(new BorderLayout(0, 4));
        choose.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        choose.add(Ui.hint("예시는 나중에 '예시 지우기'로 한 번에 지울 수 있어요"), BorderLayout.NORTH);
        JPanel buttons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 6, 0));
        buttons.add(examples);
        buttons.add(start);
        choose.add(buttons, BorderLayout.SOUTH);
        bottom.add(choose, BorderLayout.SOUTH);
        panel.add(bottom, BorderLayout.SOUTH);
    }

    /** 이름 줄 카드: 연필, 라벨, 둥근 이름 칸, 칸 아래 안내(05). */
    private JPanel nameBar() {
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
        c.insets = new java.awt.Insets(0, 0, 0, 16);
        bar.add(label, c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new java.awt.Insets(0, 0, 0, 0);
        bar.add(name, c);
        c.gridy = 1;
        c.fill = GridBagConstraints.NONE;
        c.insets = new java.awt.Insets(4, 0, 0, 0);
        bar.add(hint, c);
        bar.setPreferredSize(new Dimension(ROW, bar.getPreferredSize().height));
        bar.setMaximumSize(bar.getPreferredSize());
        return bar;
    }

    /** 큰 알약 버튼(예시의 시작 버튼 크기를 창 크기에 맞춤). */
    private static void big(JButton b) {
        b.setFont(Theme.bold(16));
        b.setBorder(BorderFactory.createEmptyBorder(12, 26, 13, 26));
    }

    private static JPanel clear(java.awt.LayoutManager layout) {
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

    /** 로고: 사이드바와 같은 PetArt 고양이 얼굴, 아주 굵은 "플래닝펫", "Planning Pet", 양옆 강조선. */
    private static final class Logo extends JLabel {
        Logo() {
            super("플래닝펫 Planning Pet");
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
            String word = "플래닝펫";
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
            g.drawString("Planning Pet", tx + (wordW - small.stringWidth("Planning Pet")) / 2, y + faceH);
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
            setForeground(java.awt.Color.WHITE);
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

    /** 바로 시작 버튼과 같다. 고른 동물과 이름(비우면 나비, 바둑이)을 넣는다. */
    public void apply(Store s) {
        apply(s, false);
    }

    /** withExamples면 예시로 둘러보기이고 예시 일정도 넣는다(D-74). */
    public void apply(Store s, boolean withExamples) {
        // 첫 실행 창은 앱 창보다 먼저 뜬다. 앱 controller를 이 데이터로 따로 만든다.
        new PetController(new AppController(s, new Talk(s)), s).choose(dog.isSelected(), name.getText(), withExamples);
    }

    /** D-48. 나의 방의 "+": 남은 종류 하나를 이름을 붙여 데려온다. 창 위 문구와 버튼만 다르고 이름 칸은 처음 고르기와 같다. */
    public static void adopt(App app) {
        boolean dog = app.store.pet("DOG") == null;
        if (app.store.pet(dog ? "DOG" : "CAT") != null) {
            return;
        }
        String kind = dog ? "강아지" : "고양이";
        JTextField field = new JTextField(12);
        Ui.limit(field, 10);
        Ui.focusWhenShown(field);
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBorder(BorderFactory.createEmptyBorder(10, 12, 6, 12));
        p.add(new JLabel("<html><b>" + kind + "를 데려올까요?</b></html>"), BorderLayout.NORTH);
        p.add(new PetView(MockData.look(dog ? "DOG" : "CAT"), 2), BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout(0, 2));
        bottom.add(Ui.flow(new JLabel("이름"), field), BorderLayout.NORTH);
        bottom.add(Ui.hint("비워 두면 " + (dog ? "바둑이" : "나비") + "로 불러요"), BorderLayout.SOUTH);
        p.add(bottom, BorderLayout.SOUTH);
        int answer = javax.swing.JOptionPane.showOptionDialog(app.root, p, kind + " 데려오기", javax.swing.JOptionPane.DEFAULT_OPTION,
                javax.swing.JOptionPane.PLAIN_MESSAGE, null, new String[] {"취소", "데려오기"}, "데려오기");
        if (answer == 1) {
            app.pet.adopt(dog, field.getText());
        }
    }

    /** 고르면 true. 닫기 버튼으로 닫으면 false이고 앱은 아무것도 저장하지 않고 끝난다. */
    public boolean ask(Store s) {
        JDialog d = new JDialog((java.awt.Frame) null, "플래닝펫 시작", true);
        boolean[] picked = {false};
        start.addActionListener(e -> {
            apply(s, false);
            picked[0] = true;
            d.dispose();
        });
        examples.addActionListener(e -> {
            apply(s, true);
            picked[0] = true;
            d.dispose();
        });
        d.setContentPane(panel);
        d.getRootPane().setDefaultButton(start);
        d.pack();
        d.setLocationRelativeTo(null);
        d.setVisible(true);
        return picked[0];
    }
}
