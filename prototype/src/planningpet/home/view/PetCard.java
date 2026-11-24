package planningpet.home.view;

import planningpet.app.model.Store;
import planningpet.common.view.*;
import planningpet.pet.model.PetState;
import planningpet.pet.view.Pet;
import planningpet.reward.model.DailyProgress;
import planningpet.reward.model.Level;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 * 홈의 펫 카드(05 메인, D-59). 하늘과 풀밭 장면 왼쪽에 고른 펫(D-48), 오른쪽 위에 이름과 레벨, 경험치 막대,
 * 흰 작은 카드에 "오늘 끝낸 일"과 칸 막대(06의 13번), 그 아래 펫 옆 말풍선. 누르면 캐릭터 창이 뜬다(D-58).
 * 배치는 2026-11-24 홈 예시의 펫 카드를 따른다. 값은 디자인 시스템 10절 "홈"에 있다.
 */
public final class PetCard extends JComponent {
    /** 바깥 틀 안쪽 여백과 줄 사이. */
    private static final int PAD = 14;
    private static final int GAP = 8;
    private final Store store;
    private final Runnable open;
    private final Bar expBar = new Bar(0, 8).track(Theme.TRACK_DEEP);
    private final Bar doneBar = new Bar(0, 8).track(Theme.TRACK_DEEP);
    private boolean over;
    private boolean down;
    /** 마지막으로 그린 말풍선 자리와 잘렸는지(툴팁에 전체 글을 보인다). */
    private Rectangle bubbleBox;
    private boolean bubbleCut;
    /** 펫 숨쉬기(PetArt의 t). 창에 붙어 보일 때만 돈다. 창 없이 그릴 때는 0이다. */
    private javax.swing.Timer clock;
    private long start;
    private double t;

    public PetCard(Store store, Runnable open) {
        this.store = store;
        this.open = open;
        setLayout(null);
        add(expBar);
        add(doneBar);
        setFocusable(true);
        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        setToolTipText("");
        getAccessibleContext().setAccessibleName("펫 카드. 누르면 캐릭터 창이 열려요");
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                over = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                over = false;
                down = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                down = true;
                requestFocusInWindow();
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                boolean click = down && contains(e.getPoint());
                down = false;
                repaint();
                if (click && javax.swing.SwingUtilities.isLeftMouseButton(e)) {
                    open.run();
                }
            }
        };
        addMouseListener(mouse);
        addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                repaint();
            }
        });
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "open");
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "open");
        getActionMap().put("open", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                open.run();
            }
        });
        store.onChange(this::refresh);
        refresh();
    }

    /** 화면 읽기 프로그램에는 누르는 버튼으로 알린다. 이름과 설명(펫, 레벨, 오늘 끝낸 일, 말)은 refresh가 채운다. */
    @Override
    public javax.accessibility.AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJComponent() {
                @Override
                public javax.accessibility.AccessibleRole getAccessibleRole() {
                    return javax.accessibility.AccessibleRole.PUSH_BUTTON;
                }
            };
        }
        return accessibleContext;
    }

    /** 누른 것과 같다(QA가 부른다). */
    public void click() {
        open.run();
    }

    /** "나비". */
    public String nameText() {
        return store.pet().name;
    }

    /** 이름 옆에 작게 적는 종류 "고양이"(D-81). */
    public String kindText() {
        return store.pet().kindName();
    }

    /** "Lv. 3". */
    public String levelText() {
        return "Lv. " + Level.level(store.pet().exp);
    }

    /** "경험치 80 / 110", 레벨 10이면 "최고 레벨"(06의 10번). */
    public String expText() {
        return Level.expText(store.pet().exp);
    }

    /** "오늘 끝낸 일 1 / 4". 오늘 일정이 없으면 "0 / 0"이다(06의 13번). */
    public String doneText() {
        DailyProgress p = DailyProgress.of(store.book, store.today());
        return "오늘 끝낸 일 " + p.done() + " / " + p.total();
    }

    /** 칸 막대의 켜진 칸 수와 칸 수(이어진 막대면 칸 수 0). 검사용. */
    public String doneBarText() {
        return Math.round(doneBar.ratio() * 100) + "% " + doneBar.segments() + "칸";
    }

    public String bubbleText() {
        return store.talk == null ? "" : store.talk;
    }

    private void refresh() {
        PetState p = store.pet();
        expBar.set(Level.expRatio(p.exp));
        DailyProgress d = DailyProgress.of(store.book, store.today());
        doneBar.set((int) d.done(), d.total());
        getAccessibleContext().setAccessibleDescription(nameText() + " " + levelText() + ", " + expText() + ", "
                + doneText() + (bubbleText().isBlank() ? "" : ", " + bubbleText()));
        revalidate();
        repaint();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (clock == null) {
            start = System.nanoTime();
            clock = new javax.swing.Timer(40, e -> {
                if (isShowing()) {
                    t = (System.nanoTime() - start) / 1e9;
                    repaint(petArea());
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

    /** 카드 틀(그림자 자리를 뺀 것). Card와 같다. */
    private Rectangle frame() {
        return new Rectangle(2, 1, getWidth() - 4, getHeight() - 8);
    }

    private int petWidth(Rectangle f) {
        return (int) Math.max(118, Math.min(f.width * 0.40, 260));
    }

    private Rectangle petArea() {
        Rectangle f = frame();
        return new Rectangle(f.x, f.y, petWidth(f) + 24, f.height);
    }

    private int ground(Rectangle f) {
        return f.y + f.height - Math.max(14, f.height / 11);
    }

    /** 오른쪽 열(이름부터 말풍선까지)의 왼쪽과 폭. */
    private int infoX(Rectangle f) {
        return f.x + petWidth(f) + GAP;
    }

    private int infoW(Rectangle f) {
        return f.x + f.width - PAD - infoX(f);
    }

    /** 경험치 줄과 작은 카드의 세로 자리. 막대 둘을 그 자리에 둔다. */
    private int[] rows(Rectangle f) {
        // 카드가 높으면(넓은 창) 오른쪽 열을 조금 내려 위로 몰리지 않게 한다.
        int y = f.y + PAD + Math.max(0, (f.height - 250) / 3);
        int nameRow = y;
        y += 30 + 6;
        int expRow = y;
        y += 8 + 4 + 16 + GAP;
        int mini = y;
        y += 52 + GAP;
        int bubble = y;
        return new int[] {nameRow, expRow, mini, bubble};
    }

    @Override
    public void doLayout() {
        Rectangle f = frame();
        int[] r = rows(f);
        int x = infoX(f);
        int w = infoW(f);
        int chip = chipWidth();
        expBar.setBounds(x + chip + 8, r[1], Math.max(20, w - chip - 8), 8);
        doneBar.setBounds(x + 12, r[2] + 34, Math.max(20, w - 24), 8);
    }

    private int chipWidth() {
        return getFontMetrics(Theme.bold(11)).stringWidth("경험치") + 16;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(360, 240);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(300, 210);
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        if (bubbleCut && bubbleBox != null && bubbleBox.contains(e.getPoint())) {
            return "<html><div style='width:260px'>" + Ui.esc(bubbleText()) + "</div></html>";
        }
        return "캐릭터 창 열기";
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
        Shape clip = new RoundRectangle2D.Double(f.x, f.y, f.width, f.height, r, r);
        Graphics2D s = (Graphics2D) g.create();
        s.clip(clip);
        PetCardArt.scene(s, f, ground(f));
        pet(s, f);
        s.dispose();
        info(g, f);
        boolean[] cut = {false};
        bubbleBox = PetCardArt.bubble(g, bubbleText(), infoX(f), rows(f)[3], infoW(f), f.y + f.height - 12, cut);
        bubbleCut = cut[0];
        g.setColor(down ? Theme.MINT : over ? Theme.MINT_LINE : Theme.CARD_EDGE);
        g.setStroke(new BasicStroke(down ? 2f : 1.2f));
        g.draw(new RoundRectangle2D.Double(f.x + 0.5, f.y + 0.5, f.width - 1, f.height - 1, r, r));
        if (isFocusOwner()) {
            g.setColor(new Color(0x1F, 0xB9, 0x9C, 110));
            g.setStroke(new BasicStroke(2f));
            g.draw(new RoundRectangle2D.Double(f.x + 3, f.y + 3, f.width - 6, f.height - 6, r - 4, r - 4));
        }
        g.dispose();
    }

    /** 펫: 카드 왼쪽에 발을 풀밭에 붙인다. 크기는 3단계 그림이 위쪽 여백을 빼고 꽉 차는 상자이고, 단계는 그 안의 크기로 보인다(D-50). */
    private void pet(Graphics2D g, Rectangle f) {
        PetState p = store.pet();
        int ground = ground(f);
        int pw = petWidth(f);
        int cx = f.x + pw / 2 + 6;
        int size = (int) Math.min(pw * 1.7, (ground - f.y - 14) / 0.97);
        int drawn = Pet.drawnHeight(p, size);
        g.setColor(new Color(0x5E, 0x8A, 0x3C, 45));
        g.fillOval(cx - size * 3 / 10, ground - size / 26, size * 3 / 5, size / 13);
        Pet.drawAt(g, p, cx, ground, size, t);
        // 강조선은 오른쪽 귀 옆에 작게 둔다(나의 방보다 작다). 더 오른쪽은 이름과 흰 작은 카드 자리다.
        int sp = Math.max(14, size / 8);
        Icons.draw(g, Icons.Kind.SPARK, Math.min(cx + size / 6, infoX(f) - sp - 4), Math.max(f.y + 4, ground - drawn - sp / 3), sp, Theme.SPARK);
    }

    /** 오른쪽 열: 발자국과 이름, Lv, 경험치 라벨과 막대와 숫자, 흰 작은 카드(오늘 끝낸 일). */
    private void info(Graphics2D g, Rectangle f) {
        int[] r = rows(f);
        int x = infoX(f);
        int w = infoW(f);
        // 이름, 작은 종류, 레벨: 한 줄, 아래 끝을 맞춘다. 이름이 길면 줄인다(D-81).
        String level = levelText();
        g.setFont(Theme.heavy(24));
        FontMetrics lf = g.getFontMetrics();
        int lw = lf.stringWidth(level);
        int base = r[0] + 24;
        Icons.draw(g, Icons.Kind.PAW, x, base - 15, 15, Theme.ICON);
        String kind = kindText();
        g.setFont(Theme.font(11));
        int kw = g.getFontMetrics().stringWidth(kind);
        g.setFont(Theme.bold(15));
        String name = PetCardArt.fit(nameText(), g.getFontMetrics(), w - 22 - lw - 10 - kw - 5);
        g.setColor(Theme.INK);
        g.drawString(name, x + 21, base);
        int kx = x + 21 + g.getFontMetrics().stringWidth(name) + 5;
        g.setFont(Theme.font(11));
        g.setColor(Theme.INK_SUB);
        g.drawString(kind, kx, base);
        g.setColor(Theme.INK);
        int nx = kx + kw + 10;
        g.setFont(Theme.heavy(24));
        g.drawString(level, nx, base);
        // 경험치: 민트빛 라벨, 막대(자식 Bar), 아래 오른쪽 숫자.
        int chip = chipWidth();
        g.setColor(Theme.MINT_SOFT);
        g.fillRoundRect(x, r[1] - 5, chip, 18, 18, 18);
        g.setFont(Theme.bold(11));
        g.setColor(Theme.ICON);
        g.drawString("경험치", x + 8, r[1] + 8);
        String exp = expText();
        String nums = exp.startsWith("경험치 ") ? exp.substring(4) : exp;
        g.setFont(Theme.bold(13));
        g.setColor(Theme.INK);
        FontMetrics nf = g.getFontMetrics();
        g.drawString(nums, x + w - nf.stringWidth(nums), r[1] + 8 + 4 + nf.getAscent());
        // 흰 작은 카드: 제목과 발자국 숫자, 아래 칸 막대(자식 Bar).
        int my = r[2];
        g.setColor(new Color(255, 255, 255, 215));
        g.fillRoundRect(x, my, w, 52, 14, 14);
        g.setColor(new Color(255, 255, 255, 240));
        g.drawRoundRect(x, my, w - 1, 51, 14, 14);
        g.setFont(Theme.bold(12));
        g.setColor(Theme.INK_SUB);
        g.drawString("오늘 끝낸 일", x + 12, my + 22);
        DailyProgress d = DailyProgress.of(store.book, store.today());
        String count = d.done() + " / " + d.total();
        g.setFont(Theme.heavy(16));
        FontMetrics cf = g.getFontMetrics();
        int cw = cf.stringWidth(count);
        g.setColor(Theme.INK);
        g.drawString(count, x + w - 12 - cw, my + 23);
        Icons.draw(g, Icons.Kind.PAW, x + w - 12 - cw - 20, my + 9, 15, Theme.ICON);
    }
}
