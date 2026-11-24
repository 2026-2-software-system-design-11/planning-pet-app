package planningpet.pet.view;

import planningpet.app.model.Store;
import planningpet.app.view.App;
import planningpet.common.view.*;
import planningpet.pet.model.Bond;
import planningpet.pet.model.PetState;
import planningpet.pet.model.Poses;
import planningpet.pet.model.Wardrobe;
import planningpet.reward.model.Level;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

/**
 * 캐릭터(05 캐릭터, D-47~D-51). 배치는 2026-11-23 캐릭터 창 예시를 따른다. 왼쪽은 펫 탭, 방 속 큰 펫(자세와 입혀 볼 장신구),
 * 자세 다섯 칸. 오른쪽은 이름과 이름 변경, 레벨과 친밀도와 경험치, 성장 단계, 상태와 꾸미기와 정보 탭, 꾸미기 적용이다.
 */
public final class CharacterScreen extends JPanel {
    private final App app;
    private final JPanel petTabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    private final JPanel poses = new JPanel(new GridLayout(1, Poses.ALL.size(), 10, 0));
    public final JLabel name = new JLabel();
    public final JButton rename = new JButton("이름 변경");
    public final JLabel level = new JLabel();
    public final JLabel bond = new JLabel();
    private final JLabel expText = new JLabel();
    private final Bar expBar = new Bar(0, 12);
    private final JComponent stages;
    private final CardLayout tabCards = new CardLayout();
    private final JPanel tabBody = new JPanel(tabCards);
    private final Pill[] tabs = {new Pill("상태", Icons.Kind.CHART, 14), new Pill("꾸미기", Icons.Kind.SHIRT, 14),
        new Pill("정보", Icons.Kind.INFO, 14)};
    private final JToggleButton[] slotButtons = new JToggleButton[Wardrobe.SLOTS.size()];
    public final JPanel wears = new JPanel(new GridLayout(1, 4, 10, 0));
    public final JButton apply = new JButton("꾸미기 적용");
    private final JLabel stateText = new JLabel();
    private final JLabel infoText = new JLabel();
    private final JComponent stageView;
    /** 아직 적용하지 않은 장신구(D-51). draftKind의 펫 것이다. */
    public final Map<String, String> draft = new LinkedHashMap<>();
    private String draftKind;
    private String slot = Wardrobe.NECK;

    public CharacterScreen(App app) {
        super(new BorderLayout());
        this.app = app;
        Card main = new Card(new BorderLayout(Theme.GAP + 4, 0), 24);
        main.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        stageView = big();
        stages = growth();
        main.add(left(), BorderLayout.CENTER);
        main.add(right(), BorderLayout.EAST);
        add(main);
        app.watch(this::refresh);
    }

    private JComponent left() {
        JPanel col = new JPanel(new BorderLayout(0, 10));
        col.add(petTabs, BorderLayout.NORTH);
        Card roomCard = new Card(new BorderLayout(), 18);
        roomCard.setBorder(BorderFactory.createEmptyBorder(5, 5, 7, 5));
        roomCard.add(stageView);
        col.add(roomCard, BorderLayout.CENTER);
        poses.setPreferredSize(new Dimension(0, 92));
        col.add(poses, BorderLayout.SOUTH);
        return col;
    }

    /** 방 그림 위 큰 펫. 입혀 보는 장신구(draft)로 그린다. 양옆 화살표는 자세를 바꾼다. */
    private JComponent big() {
        JComponent v = new JComponent() {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.clip(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), Theme.IMAGE_RADIUS, Theme.IMAGE_RADIUS));
                if (Images.ROOM != null) {
                    Images.cover(g, Images.ROOM, 0, 0, getWidth(), getHeight());
                }
                PetState p = app.store.pet();
                int ground = getHeight() * 9 / 10;
                int size = (int) Math.min(getWidth() * 0.62, getHeight() * 0.74);
                java.awt.image.BufferedImage rug = Images.item("러그");
                if (rug != null) {
                    Images.fitBottom(g, rug, getWidth() / 2 - size * 3 / 5, ground - size / 5, size * 6 / 5, size / 4);
                }
                g.setColor(new Color(0x9A, 0x6A, 0x45, 40));
                g.fillOval(getWidth() / 2 - size * 3 / 10, ground - size / 22, size * 3 / 5, size / 11);
                Pet.drawAt(g, p, p.pose, draft, getWidth() / 2, ground, size);
                java.awt.image.BufferedImage yarn = Images.item("털실 공");
                if (yarn != null) {
                    Images.fitBottom(g, yarn, getWidth() / 2 - size * 3 / 5, ground - size / 6, size / 5, size / 5);
                }
                int top = ground - Pet.drawnHeight(p, size);
                Icons.draw(g, Icons.Kind.HEART, getWidth() / 2 - size * 2 / 5, top + size / 8, size / 7, Theme.HEART);
                Icons.draw(g, Icons.Kind.HEART, getWidth() / 2 - size * 3 / 10, top + size / 30, size / 10, Theme.HEART);
                Icons.draw(g, Icons.Kind.SPARK, getWidth() / 2 + size / 4, top + size / 6, size / 6, Theme.SPARK);
                g.dispose();
            }
        };
        v.setLayout(null);
        CircleButton prev = new CircleButton(Icons.Kind.LEFT, 40, "앞 자세", false);
        CircleButton next = new CircleButton(Icons.Kind.RIGHT, 40, "다음 자세", false);
        prev.addActionListener(e -> stepPose(-1));
        next.addActionListener(e -> stepPose(1));
        v.add(prev);
        v.add(next);
        v.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                prev.setBounds(12, (v.getHeight() - 40) / 2, 40, 40);
                next.setBounds(v.getWidth() - 52, (v.getHeight() - 40) / 2, 40, 40);
            }
        });
        return v;
    }

    private void stepPose(int delta) {
        PetState p = app.store.pet();
        int i = Poses.ALL.indexOf(p.pose);
        app.pet.pose(p.kind, Poses.ALL.get(Math.floorMod(i + delta, Poses.ALL.size())));
    }

    /** 성장 단계(D-50): 같은 그림의 세 크기, 지금 단계에 연민트 바탕. */
    private JComponent growth() {
        JComponent v = new JComponent() {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                PetState p = app.store.pet();
                int now = Level.stage(Level.level(p.exp));
                int cell = getWidth() / 3;
                int size = Math.min(cell - 30, getHeight() - 8);
                for (int i = 1; i <= 3; i++) {
                    int cx = cell * (i - 1) + cell / 2;
                    if (i == now) {
                        g.setColor(Theme.MINT_PALE);
                        g.fillRoundRect(cx - cell / 2 + 12, 0, cell - 24, getHeight(), 16, 16);
                    }
                    Pet.drawStage(g, p.kind, Poses.SIT, p.wear, i, cx, getHeight() - 6, size);
                    if (i < 3) {
                        Icons.draw(g, Icons.Kind.RIGHT, cell * i - 9, getHeight() / 2 - 9, 18, Theme.MINT_DEEP);
                    }
                }
                g.dispose();
            }
        };
        v.setPreferredSize(new Dimension(0, 84));
        v.setToolTipText("레벨 1~3, 4~6, 7~10에서 자라요");
        return v;
    }

    private JComponent right() {
        Card c = new Card(new BorderLayout(0, 10), 20);
        c.setBorder(BorderFactory.createEmptyBorder(12, 16, 14, 16));
        name.setFont(Theme.heavy(22));
        name.setIcon(Icons.icon(Icons.Kind.PAW, 24, Theme.ICON));
        name.setIconTextGap(8);
        rename.setFont(Theme.bold(13));
        rename.setIcon(Icons.icon(Icons.Kind.PENCIL, 16, Theme.ICON));
        rename.setIconTextGap(6);
        rename.setBorder(BorderFactory.createEmptyBorder(6, 12, 8, 14));
        rename.addActionListener(e -> askRename());
        JPanel top = new JPanel(new BorderLayout());
        top.add(name, BorderLayout.WEST);
        top.add(rename, BorderLayout.EAST);
        level.setFont(Theme.heavy(30));
        bond.setFont(Theme.bold(14));
        bond.setForeground(new Color(0xD8414E));
        bond.setIcon(Icons.icon(Icons.Kind.HEART, 16, Theme.HEART));
        bond.setHorizontalTextPosition(SwingConstants.LEFT);
        bond.setIconTextGap(6);
        bond.setToolTipText("경험치를 받은 날이 30일이면 100%예요. 쉬어도 줄지 않아요");
        JPanel bondPill = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(new Color(0xFFECEE));
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                g.dispose();
            }
        };
        bondPill.setBorder(BorderFactory.createEmptyBorder(4, 14, 6, 14));
        bondPill.add(bond);
        JPanel lv = new JPanel(new BorderLayout());
        lv.add(level, BorderLayout.WEST);
        JPanel pillWrap = new JPanel(new java.awt.GridBagLayout());
        pillWrap.add(bondPill);
        lv.add(pillWrap, BorderLayout.EAST);
        expText.setFont(Theme.bold(13));
        JPanel exp = new JPanel(new BorderLayout(12, 0));
        exp.add(expText, BorderLayout.WEST);
        JPanel barWrap = new JPanel(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints k = new java.awt.GridBagConstraints();
        k.weightx = 1;
        k.fill = java.awt.GridBagConstraints.HORIZONTAL;
        barWrap.add(expBar, k);
        exp.add(barWrap, BorderLayout.CENTER);
        JPanel head = new JPanel(new GridLayout(0, 1, 0, 2));
        head.add(top);
        head.add(lv);
        head.add(exp);
        Card growthCard = new Card(new BorderLayout(0, 4), 16);
        growthCard.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        JLabel gt = new JLabel("성장 단계");
        gt.setFont(Theme.bold(15));
        growthCard.add(gt, BorderLayout.NORTH);
        growthCard.add(stages, BorderLayout.CENTER);
        JPanel upper = new JPanel(new BorderLayout(0, 10));
        upper.add(head, BorderLayout.NORTH);
        upper.add(growthCard, BorderLayout.CENTER);
        ButtonGroup tg = new ButtonGroup();
        JPanel tabRow = new JPanel(new GridLayout(1, 3, 8, 0));
        String[] names = {"상태", "꾸미기", "정보"};
        for (int i = 0; i < tabs.length; i++) {
            String n = names[i];
            tg.add(tabs[i]);
            tabRow.add(tabs[i]);
            tabs[i].addActionListener(e -> {
                tabCards.show(tabBody, n);
                apply.setVisible("꾸미기".equals(n));
            });
        }
        tabs[1].setSelected(true);
        tabBody.add(text(stateText), "상태");
        tabBody.add(dress(), "꾸미기");
        tabBody.add(text(infoText), "정보");
        tabCards.show(tabBody, "꾸미기");
        apply.putClientProperty(RoundButtonUI.PRIMARY, Boolean.TRUE);
        apply.setFont(Theme.bold(16));
        apply.setIcon(Icons.icon(Icons.Kind.PAW, 20, Color.WHITE));
        apply.setIconTextGap(8);
        apply.setBorder(BorderFactory.createEmptyBorder(10, 60, 12, 60));
        apply.addActionListener(e -> app.pet.applyWear(app.store.pet().kind, new LinkedHashMap<>(draft)));
        JPanel applyRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        applyRow.add(apply);
        JPanel lower = new JPanel(new BorderLayout(0, 10));
        lower.add(tabRow, BorderLayout.NORTH);
        lower.add(tabBody, BorderLayout.CENTER);
        lower.add(applyRow, BorderLayout.SOUTH);
        c.add(upper, BorderLayout.NORTH);
        c.add(lower, BorderLayout.CENTER);
        c.setPreferredSize(new Dimension(420, 0));
        return c;
    }

    private static JComponent text(JLabel l) {
        l.setFont(Theme.font(14));
        l.setVerticalAlignment(SwingConstants.TOP);
        l.setBorder(BorderFactory.createEmptyBorder(6, 8, 0, 8));
        return l;
    }

    /** 꾸미기 탭: 왼쪽 칸 목록, 오른쪽 그 칸의 장신구 카드. */
    private JComponent dress() {
        JPanel list = new JPanel(new GridLayout(0, 1, 0, 4));
        ButtonGroup g = new ButtonGroup();
        Icons.Kind[] icons = {Icons.Kind.COLLAR, Icons.Kind.BOW, Icons.Kind.HAT, Icons.Kind.SHIRT};
        for (int i = 0; i < slotButtons.length; i++) {
            String s = Wardrobe.SLOTS.get(i);
            JToggleButton b = new JToggleButton(s, Icons.icon(icons[i], 18, Theme.ICON), i == 0);
            b.putClientProperty(RoundButtonUI.NAV, Boolean.TRUE);
            b.setFont(Theme.bold(13));
            b.setHorizontalAlignment(SwingConstants.LEFT);
            b.setIconTextGap(8);
            b.setBorder(BorderFactory.createEmptyBorder(6, 10, 8, 10));
            b.addActionListener(e -> {
                slot = s;
                refresh();
            });
            g.add(b);
            slotButtons[i] = b;
            list.add(b);
        }
        JPanel listWrap = new JPanel(new BorderLayout());
        listWrap.add(list, BorderLayout.NORTH);
        listWrap.setPreferredSize(new Dimension(112, 0));
        wears.setPreferredSize(new Dimension(0, 116));
        JPanel wearWrap = new JPanel(new BorderLayout());
        wearWrap.add(wears, BorderLayout.NORTH);
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.add(listWrap, BorderLayout.WEST);
        p.add(wearWrap, BorderLayout.CENTER);
        return p;
    }

    /** 장신구 카드 하나. 열리지 않았으면 자물쇠와 "Lv.{레벨} 해금". 입혀 보는 중이면 체크 배지. */
    private JComponent wearCard(Wardrobe.Wear w, int petLevel) {
        boolean open = w.level() <= petLevel;
        boolean on = w.name().equals(draft.get(w.slot()));
        JButton b = new JButton(open ? w.name() : "Lv." + w.level() + " 해금") {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int x = 2;
                int y = 8;
                int cw = getWidth() - 5;
                int ch = getHeight() - 12;
                Card.shadow(g, x, y, cw, ch, 14);
                g.setColor(!open ? new Color(0xEEF2F5) : on ? Theme.MINT_PALE : Theme.CARD_TOP);
                g.fillRoundRect(x, y, cw, ch, 14, 14);
                g.setColor(on ? Theme.MINT : Theme.CARD_EDGE);
                g.setStroke(new java.awt.BasicStroke(on ? 2.2f : 1f));
                g.drawRoundRect(x, y, cw, ch, 14, 14);
                int pic = Math.min(cw - 20, ch - 34);
                java.awt.image.BufferedImage im = Images.wear(w.name());
                if (!open) {
                    Icons.draw(g, Icons.Kind.LOCK, x + (cw - pic * 3 / 4) / 2, y + 8, pic * 3 / 4, Theme.INK_SUB);
                } else if (im != null) {
                    Images.fit(g, im, x + (cw - pic) / 2, y + 6, pic, pic);
                } else {
                    Icons.draw(g, slotIcon(w.slot()), x + (cw - pic * 3 / 4) / 2, y + 8, pic * 3 / 4, Theme.ICON);
                }
                g.setFont(Theme.bold(12));
                g.setColor(open ? Theme.INK : Theme.INK_SUB);
                String t = getText();
                g.drawString(t, x + (cw - g.getFontMetrics().stringWidth(t)) / 2, y + ch - 10);
                if (on) {
                    g.setColor(Color.WHITE);
                    g.fillOval(x + cw - 18, y - 8, 24, 24);
                    Icons.draw(g, Icons.Kind.CHECK, x + cw - 16, y - 6, 20, Theme.MINT_DEEP);
                }
                g.dispose();
            }
        };
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setEnabled(open);
        b.setToolTipText(open ? (on ? "누르면 벗겨 봐요" : "누르면 입혀 봐요") : "레벨 " + w.level() + "이 되면 열려요");
        b.addActionListener(e -> {
            if (on) {
                draft.remove(w.slot());
