package planningpet.pet.view;

import planningpet.app.view.App;
import planningpet.common.view.Icons;
import planningpet.common.view.RoundButtonUI;
import planningpet.common.view.RoundField;
import planningpet.common.view.Theme;
import planningpet.common.view.Ui;
import planningpet.pet.model.PetState;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.geom.Ellipse2D;
import java.util.HashSet;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * 펫 데려오기 창(05 "펫 데려오기 창", UC-20, D-48). 나의 방의 "+"로 연다.
 * 모양은 2026-11-23 상점 예시의 "캐릭터 해금" 창을 옮겼다(디자인 시스템 10절 "펫 데려오기 창"): 위에 발자국과 제목의 하늘빛 머리 띠,
 * 흰 본문 가운데 큰 펫과 강조선, 굵은 물음, 예시의 가격 알약 자리에 이름 줄, 아래에 같은 높이의 취소와 데려오기 큰 알약.
 * 예시의 가격(1,000P)은 명세에 없어 넣지 않는다. 창은 05대로 `JOptionPane`이고 닫기와 Esc는 운영체제 창 틀과 `JOptionPane`이 맡는다.
 */
public final class AdoptDialog {
    /** 창 안 폭. 예시 창(470)을 앱 글자 크기에 맞춰 줄이되, 글자와 펫이 창에서 차지하는 몫은 예시와 비슷하게 둔다. */
    static final int W = 356;
    private static final int HEAD = 52;
    private static final int PET_BOX = 156;
    /** 본문 아래 색: 흰색에 `DAY`를 0.75 섞은 아주 옅은 하늘빛. */
    private static final java.awt.Color BOTTOM = PickScene.mix(Theme.CARD_TOP, Theme.DAY, 0.75);

    private AdoptDialog() {
    }

    /** "+"가 부른다. 남은 종류 하나를 이름을 붙여 데려온다. 둘 다 있으면 아무것도 하지 않는다. */
    public static void show(App app) {
        boolean dog = app.store.pet("DOG") == null;
        if (app.store.pet(dog ? "DOG" : "CAT") != null) {
            return;
        }
        String kind = dog ? "강아지" : "고양이";
        JTextField field = new RoundField(null, null);
        JButton cancel = new JButton("취소");
        JButton ok = new JButton("데려오기");
        JOptionPane pane = pane(dog, field, cancel, ok);
        cancel.addActionListener(e -> pane.setValue(cancel));
        ok.addActionListener(e -> pane.setValue(ok));
        Ui.focusWhenShown(field);
        JDialog d = pane.createDialog(app.root, kind + " 데려오기");
        // 이름 칸에서 Enter는 데려오기다(바꾸기 전 JOptionPane의 처음 값과 같다).
        d.getRootPane().setDefaultButton(ok);
        d.setVisible(true);
        d.dispose();
        if (pane.getValue() == ok) {
            app.pet.adopt(dog, field.getText());
        }
    }

    /**
     * 창 안을 담은 `JOptionPane`. 버튼은 본문 안에 있으므로 JOptionPane의 빈 버튼 줄을 없애고, 본문 둘레의 틈은 본문 아래 색으로 칠한다.
     * 창 없이 그리는 Snap도 이것을 쓴다.
     */
    public static JOptionPane pane(boolean dog, JTextField field, JButton cancel, JButton ok) {
        JComponent content = content(dog, field, cancel, ok);
        JOptionPane pane = new JOptionPane(content, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[0]);
        pane.setBorder(BorderFactory.createEmptyBorder());
        pane.setBackground(BOTTOM);
        for (java.awt.Component c : pane.getComponents()) {
            if (c instanceof JPanel p && p.getComponentCount() == 0) {
                pane.remove(c);
            }
        }
        clearAround(pane, content);
        return pane;
    }

    private static void clearAround(java.awt.Container c, JComponent content) {
        for (java.awt.Component child : c.getComponents()) {
            if (child == content) {
                continue;
            }
            if (child instanceof JPanel p) {
                p.setOpaque(false);
            }
            if (child instanceof java.awt.Container k) {
                clearAround(k, content);
            }
        }
    }

    /** 창 안. field, cancel, ok는 부르는 쪽이 동작을 단다. */
    public static JComponent content(boolean dog, JTextField field, JButton cancel, JButton ok) {
        String kind = dog ? "강아지" : "고양이";
        JPanel root = new Backdrop();
        root.setLayout(new BorderLayout());
        root.add(new Head(kind + " 데려오기"), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(6, 22, 22, 22));
        body.add(new Stage(dog));
        body.add(Box.createVerticalStrut(6));
        JLabel ask = new JLabel(kind + "를 데려올까요?", SwingConstants.CENTER);
        ask.setFont(Theme.heavy(22));
        ask.setForeground(Theme.INK);
        // 배율이 1이 아닌 화면에서 글자 폭이 조금 늘어도 잘리지 않게 양옆에 여유를 둔다.
        ask.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
        body.add(center(ask));
        body.add(Box.createVerticalStrut(14));
        body.add(center(nameRow(dog, field)));
        body.add(Box.createVerticalStrut(18));

        JPanel buttons = new JPanel(new GridBagLayout());
        buttons.setOpaque(false);
        for (JButton b : new JButton[] {cancel, ok}) {
            b.setFont(Theme.bold(18));
            b.setBorder(BorderFactory.createEmptyBorder(12, 20, 13, 20));
        }
        ok.putClientProperty(RoundButtonUI.PRIMARY, Boolean.TRUE);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.weightx = 0.4;
        c.insets = new Insets(0, 0, 0, 6);
        buttons.add(cancel, c);
        c.gridx = 1;
        c.weightx = 0.6;
        c.insets = new Insets(0, 6, 0, 0);
        buttons.add(ok, c);
        // 예시처럼 취소가 좁고 주 행동이 넓다(4 대 6).
        int inner = W - 44;
        cancel.setPreferredSize(new Dimension((int) ((inner - 12) * 0.4), 50));
        ok.setPreferredSize(new Dimension((int) ((inner - 12) * 0.6), 50));
        buttons.setAlignmentX(0.5f);
        buttons.setMaximumSize(new Dimension(inner, 50));
        body.add(buttons);
        root.add(body, BorderLayout.CENTER);
        return root;
    }

    /** 이름 줄: 연필과 "이름", 둥근 이름 칸, 칸 아래 그 종류의 기본 이름 안내(05). 고르기 창의 이름 줄과 같은 글과 부품이다. */
    private static JPanel nameRow(boolean dog, JTextField field) {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        Ui.limit(field, 10);
        field.setFont(Theme.font(15));
        field.setColumns(12);
        field.getAccessibleContext().setAccessibleName("반려동물 이름");
        JLabel label = new JLabel("이름", Icons.icon(Icons.Kind.PENCIL, 20, Theme.ICON), SwingConstants.LEFT);
        label.setFont(Theme.bold(15));
        label.setIconTextGap(6);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
        label.setLabelFor(field);
        JLabel hint = Ui.hint("비워 두면 " + (dog ? "바둑이" : "나비") + "로 불러요");
        hint.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 8));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 0, 12);
        row.add(label, c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 0, 0);
        row.add(field, c);
        c.gridy = 1;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(4, 0, 0, 0);
        row.add(hint, c);
        row.setPreferredSize(new Dimension(W - 64, row.getPreferredSize().height));
        return row;
    }

    private static JPanel center(JComponent c) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        p.setOpaque(false);
        p.add(c);
        p.setAlignmentX(0.5f);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        return p;
    }

    /** 창 바탕: 흰색에서 아래로 아주 옅은 하늘빛(예시 본문 `#FCFCFC`에서 `#F5FAFE`). */
    private static final class Backdrop extends JPanel {
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(W, super.getPreferredSize().height);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setPaint(new GradientPaint(0, HEAD, Theme.CARD_TOP, 0, getHeight(), BOTTOM));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.dispose();
        }
    }

    /** 머리 띠: 하늘빛 바탕(`COOL_BOTTOM`, 예시 `#DDF1FE`), 발자국 24와 아주 굵은 19 제목, 아래 `CARD_EDGE` 선. 닫기는 운영체제 창 틀이 맡아 그리지 않는다. */
    private static final class Head extends JLabel {
        Head(String title) {
            super(title, Icons.icon(Icons.Kind.PAW, 24, Theme.ICON), SwingConstants.LEFT);
            setFont(Theme.heavy(19));
            setForeground(Theme.INK);
            setIconTextGap(10);
            setBorder(BorderFactory.createEmptyBorder(0, 20, 1, 20));
            setPreferredSize(new Dimension(W, HEAD));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setColor(Theme.COOL_BOTTOM);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Theme.CARD_EDGE);
            g.fillRect(0, getHeight() - 1, getWidth(), 1);
            g.dispose();
            PickScene.hints((Graphics2D) graphics);
            super.paintComponent(graphics);
        }
    }

    /**
     * 큰 펫 자리: 데려올 펫이 앉기 자세와 기본 목걸이로 앉아 있고(05: 데려온 펫은 앉기 자세, 기본 목걸이) 왼쪽 위에 강조선, 발밑에 옅은 그림자다.
     * 창이 뜰 때 펫이 한 번 살짝 튀고, 보이는 동안 숨쉰다(고르기 창 카드와 같은 PetArt 시간 값).
     */
    private static final class Stage extends JComponent {
        private final PetKind kind;
        private final PetState look;
        private final long start = System.nanoTime();
        private double t;
        private double since = 99;
        private javax.swing.Timer clock;

        Stage(boolean dog) {
            String k = dog ? "DOG" : "CAT";
            kind = dog ? PetKind.DOG : PetKind.CAT;
            look = new PetState(k, PetState.defaultName(k));
            setAlignmentX(0.5f);
            // 높이는 더 큰 고양이 앉기 그림(0.88)에 맞춰 강아지 창과 고양이 창의 크기가 같다. 위 18은 강조선, 아래 6은 그림자 자리다.
            setPreferredSize(new Dimension(W - 44, (int) Math.ceil(PET_BOX * PetArt.heightRatio(PetKind.CAT, PetPose.SIT)) + 24));
            setMaximumSize(getPreferredSize());
        }

        @Override
        public void addNotify() {
            super.addNotify();
            if (clock == null) {
                clock = new javax.swing.Timer(40, e -> {
                    if (isShowing()) {
                        double now = (System.nanoTime() - start) / 1e9;
                        t = now;
                        since = now;
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

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            PickScene.hints(g);
            double cx = getWidth() / 2.0;
            double ground = getHeight() - 6;
            g.setColor(PickScene.alpha(Theme.CARD_SHADOW, 40));
            g.fill(new Ellipse2D.Double(cx - PET_BOX * 0.36, ground - 7, PET_BOX * 0.72, 12));
            PetArt.drawAt(g, kind, PetPose.SIT, new HashSet<>(look.wear.values()), t, since, cx, ground, PET_BOX);
            double petTop = ground - PET_BOX * PetArt.heightRatio(kind, PetPose.SIT);
            Icons.draw(g, Icons.Kind.SPARK, (int) (cx - PET_BOX * 0.72), (int) (petTop - 8), 40, Theme.SPARK);
            g.dispose();
        }
    }
}
