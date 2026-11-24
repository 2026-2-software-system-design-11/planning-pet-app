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
    /** 물음 창(D-61). 돌려주는 값은 고른 버튼의 번호이고 창을 닫으면 -1이다. QA는 창 대신 답을 준다. */
    public interface Ask {
        int choose(String title, String message, String[] options);
    }

    public Ask ask;
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
    /** 큰 펫 양옆 화살표. 고른 펫을 바꾼다(D-80). 펫이 하나면 흐리다. */
    public final CircleButton prevPet = new CircleButton(Icons.Kind.LEFT, 40, "앞 펫", false);
    public final CircleButton nextPet = new CircleButton(Icons.Kind.RIGHT, 40, "다음 펫", false);

    public CharacterScreen(App app) {
        super(new BorderLayout());
        this.app = app;
        this.ask = (title, message, options) -> JOptionPane.showOptionDialog(this, message, title, JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        Card main = new Card(new BorderLayout(Theme.GAP + 4, 0), 24);
        main.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        stageView = big();
        stages = growth();
        main.add(left(), BorderLayout.CENTER);
        main.add(right(), BorderLayout.EAST);
        add(main);
        app.watch(this::refresh);
        // 창(D-58)은 메인 창 밖이라 앱 틀의 투명 처리가 닿지 않는다. 다시 그린 부품도 투명하게 한다.
        app.watch(() -> Theme.clear(this));
        Theme.clear(this);
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

    /** 방 그림 위 큰 펫. 입혀 보는 장신구(draft)로 그린다. 양옆 화살표는 펫을 바꾼다(D-80). 자세는 아래 자세 칸에서 고른다. */
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
        CircleButton prev = prevPet;
        CircleButton next = nextPet;
        prev.addActionListener(e -> stepPet(-1));
        next.addActionListener(e -> stepPet(1));
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

    /** D-80: 앞이나 다음 펫을 고른다. 입혀 본 장신구가 있으면 먼저 묻는다(D-61). */
    private void stepPet(int delta) {
        if (app.store.pets.size() > 1 && confirmLeave()) {
            app.pet.step(delta);
        }
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
        tabs[0].setSelected(true);
        tabBody.add(text(stateText), "상태");
        tabBody.add(dress(), "꾸미기");
        tabBody.add(text(infoText), "정보");
        tabCards.show(tabBody, "상태");
        apply.setVisible(false);
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
        b.setToolTipText(open ? (on ? "누르면 벗겨 봐요" : "누르면 입혀 봐요") : lockedTip(w.level()));
        b.addActionListener(e -> {
            if (on) {
                draft.remove(w.slot());
            } else {
                draft.put(w.slot(), w.name());
            }
            refresh();
        });
        return b;
    }

    private static Icons.Kind slotIcon(String slot) {
        return switch (slot) {
            case Wardrobe.NECK -> Icons.Kind.COLLAR;
            case Wardrobe.HAT -> Icons.Kind.HAT;
            case Wardrobe.ACCESSORY -> Icons.Kind.BOW;
            default -> Icons.Kind.SHIRT;
        };
    }

    /** 자세 칸 하나. 고른 자세는 민트 테두리다. 누르면 바로 바뀐다(D-50). */
    private JComponent poseCard(PetState p, String pose) {
        JToggleButton b = new JToggleButton(pose, pose.equals(p.pose)) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth() - 5;
                int h = getHeight() - 6;
                Card.shadow(g, 2, 2, w, h, 16);
                g.setColor(isSelected() ? Theme.MINT_PALE : Theme.CARD_TOP);
                g.fillRoundRect(2, 2, w, h, 16, 16);
                g.setColor(isSelected() ? Theme.MINT : Theme.CARD_EDGE);
                g.setStroke(new java.awt.BasicStroke(isSelected() ? 2.2f : 1f));
                g.drawRoundRect(2, 2, w, h, 16, 16);
                int size = Math.min(w, h) - 14;
                Pet.drawStage(g, p.kind, pose, p.wear, 3, 2 + w / 2, h - 4, size);
                g.dispose();
            }
        };
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setToolTipText(pose);
        b.addActionListener(e -> app.pet.pose(p.kind, pose));
        return b;
    }

    private void askRename() {
        PetState p = app.store.pet();
        JTextField field = new JTextField(p.name, 12);
        Ui.limit(field, 10);
        Ui.focusWhenShown(field);
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.add(field, BorderLayout.NORTH);
        panel.add(Ui.hint("10자까지. 비워 두면 " + PetState.defaultName(p.kind) + "로 불러요"), BorderLayout.SOUTH);
        int answer = JOptionPane.showOptionDialog(this, panel, "이름 변경", JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, new String[] {"취소", "저장"}, "저장");
        if (answer == 1) {
            app.pet.rename(p.kind, field.getText());
        }
    }

    /** 잠긴 장신구 카드의 툴팁(05 캐릭터 창 7번). 조사는 레벨 숫자의 받침을 따른다. */
    public static String lockedTip(int level) {
        return "레벨 " + level + planningpet.common.model.Rules.josa(String.valueOf(level), "이", "가") + " 되면 열려요";
    }

    /**
     * D-61: 입혀 본 장신구가 저장된 것과 다르면 적용, 버리기, 취소를 묻는다. 창을 닫거나 다른 펫을 고르기 전에 부른다.
     * 하던 일을 이어도 되면 true다. 취소, 물음 창 닫기, 적용의 저장 실패면 false이고 아무것도 바꾸지 않는다.
     */
    public boolean confirmLeave() {
        PetState p = app.store.pet();
        if (draft.equals(p.wear)) {
            return true;
        }
        int answer = ask.choose("꾸미기", "입혀 본 장신구를 적용할까요?", new String[] {"적용", "버리기", "취소"});
        if (answer == 0) {
            app.pet.applyWear(p.kind, new LinkedHashMap<>(draft));
            return draft.equals(app.store.pet().wear);
        }
        if (answer == 1) {
            draft.clear();
            draft.putAll(p.wear);
            refresh();
            return true;
        }
        return false;
    }

    /**
     * 캐릭터 창을 열 때 부른다(D-58). 입혀 보던 장신구를 버리고 저장된 것에서 시작한다.
     * 늘 상태 탭으로 열고, dress(장신구 칸, "{이름} 꾸미기")면 꾸미기 탭으로 연다(D-93).
     */
    public void opened(boolean dress) {
        draftKind = null;
        Pill tab = tabs[dress ? 1 : 0];
        if (!tab.isSelected()) {
            tab.doClick();
        }
        refresh();
        Theme.clear(this);
    }

    /** 지금 고른 탭 이름(상태, 꾸미기, 정보). 검사용. */
    public String tab() {
        for (Pill t : tabs) {
            if (t.isSelected()) {
                return t.getText();
            }
        }
        return "";
    }

    public void refresh() {
        Store s = app.store;
        PetState p = s.pet();
        if (!p.kind.equals(draftKind)) {
            draftKind = p.kind;
            draft.clear();
            draft.putAll(p.wear);
        }
        petTabs.removeAll();
        ButtonGroup g = new ButtonGroup();
        for (String kind : new String[] {"CAT", "DOG"}) {
            PetState q = s.pet(kind);
            if (q == null) {
                continue;
            }
            Pill tab = new Pill(q.kindName(), null, 18).picture(Pet.face(q));
            tab.setPreferredSize(new Dimension(200, 58));
            tab.setSelected(q == p);
            tab.addActionListener(e -> {
                // D-61: 입혀 본 것이 있으면 묻고, 취소면 고른 펫과 탭을 그대로 둔다.
                if (!kind.equals(app.store.activeKind) && confirmLeave()) {
                    app.pet.select(kind);
                } else {
                    refresh();
                }
            });
            g.add(tab);
            petTabs.add(tab);
        }
        petTabs.revalidate();
        prevPet.setEnabled(s.pets.size() > 1);
        nextPet.setEnabled(s.pets.size() > 1);
        poses.removeAll();
        for (String pose : Poses.ALL) {
            poses.add(poseCard(p, pose));
        }
        poses.revalidate();
        int lv = Level.level(p.exp);
        name.setText(p.name);
        level.setText("Lv. " + lv);
        bond.setText("친밀도 " + Bond.percent(s.book.rewardHistory(), p.kind) + "%");
        expText.setText(Level.expText(p.exp));
        expBar.set(Level.expRatio(p.exp));
        for (int i = 0; i < slotButtons.length; i++) {
            slotButtons[i].setSelected(Wardrobe.SLOTS.get(i).equals(slot));
        }
        wears.removeAll();
        for (Wardrobe.Wear w : Wardrobe.inSlot(slot)) {
            wears.add(wearCard(w, lv));
        }
        for (int i = wears.getComponentCount(); i < 4; i++) {
            wears.add(new JPanel());
        }
        wears.revalidate();
        wears.repaint();
        apply.setEnabled(!draft.equals(p.wear));
        long done = s.book.checkables(s.today()).stream().filter(t -> t.done).count();
        stateText.setText("<html>오늘 끝낸 일 <b>" + done + "개</b><br><br>친밀도 <b>" + Bond.percent(s.book.rewardHistory(), p.kind)
                + "%</b> (함께한 날 " + Bond.days(s.book.rewardHistory(), p.kind) + "일)<br><br>누적 경험치 <b>" + p.exp + "</b></html>");
        StringBuilder worn = new StringBuilder();
        p.wear.values().forEach(v -> worn.append(worn.length() > 0 ? ", " : "").append(v));
        infoText.setText("<html>종류 <b>" + p.kindName() + "</b><br><br>이름 <b>" + Ui.esc(p.name) + "</b><br><br>자세 <b>" + p.pose
                + "</b><br><br>입은 장신구 <b>" + (worn.length() == 0 ? "없음" : worn) + "</b></html>");
        stageView.repaint();
        stages.repaint();
    }
}
