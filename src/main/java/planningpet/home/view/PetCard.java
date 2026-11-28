package planningpet.home.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import planningpet.app.model.AppData;
import planningpet.common.view.Bar;
import planningpet.common.view.Card;
import planningpet.common.view.Icons;
import planningpet.common.view.Theme;
import planningpet.common.view.Ui;
import planningpet.pet.model.Bubble;
import planningpet.reward.model.DailyProgress;
import planningpet.reward.model.Level;

/**
 * 홈의 펫 카드(05 홈 "펫 카드", D-59). 하늘과 풀밭 장면 왼쪽에 고른 펫(D-48)을 그린다. 값은 디자인 시스템 10절 "홈"에 있다.
 * 오른쪽 열에 이름과 레벨, 경험치 막대(S3n), 흰 작은 카드의 "오늘 끝낸 일", 그 아래 펫 말풍선(S3o)을 그린다. 누르면 생성자로 받은 함수에 알린다(S3p, docs/porting/home.md).
 */
public final class PetCard extends JComponent {
    /** 오른쪽 열의 위와 오른쪽 여백. */
    private static final int PAD = 14;
    private final AppData data;
    private final Bubble bubble;
    /** 경험치 막대(06 10번). 하늘 위에서도 보이게 바탕이 TRACK_DEEP이다. */
    final Bar expBar = new Bar(0, 8).track(Theme.TRACK_DEEP);
    /** 흰 작은 카드의 "오늘 끝낸 일" 칸 막대(06 13번). */
    final Bar doneBar = new Bar(0, 8).track(Theme.TRACK_DEEP);
    /** 펫 숨쉬기 시계. 처음 화면에 붙을 때 만들고 붙어 보이는 동안 40밀리초마다 펫 자리를 다시 그린다. */
    Timer clock;
    /** 시계를 만든 때(System.nanoTime 값)와 그 뒤 지난 초(PetArt의 움직임 시각). 창 없이 그릴 때는 0이다. */
    long start;
    double t;

    /** 마지막으로 그린 말풍선 자리(말이 없으면 null)와 줄였는지. 툴팁이 전체 글을 보일지 정한다(S3q). */
    Rectangle bubbleBox;
    boolean bubbleCut;
    /** 마우스가 카드 위에 있는지(올림 테두리). */
    private boolean over;
    /** 카드 위에서 마우스 단추를 눌러 아직 떼지 않았는지(누름 테두리). 카드 밖으로 나가면 풀린다. */
    private boolean down;

    /**
     * 고른 펫과 bubble의 펫 말(앱에서는 AppController.getBubble())을 그리고 데이터 변경 알림마다 다시 그린다. 메인 창은 펫을 고른 뒤
     * 열려 고른 펫이 늘 있다(S3m). 왼쪽 단추로 카드 안에서 누르고 떼거나, 초점이 있을 때 Enter나 Space를 누르면 open을 부른다
     * (05 입력 항목 9, S3p). 앱에서는 캐릭터 창(S8)이 open에 잇는다(D-58).
     */
    public PetCard(AppData data, Bubble bubble, Runnable open) {
        this.data = data;
        this.bubble = bubble;
        setLayout(null);
        add(expBar);
        add(doneBar);
        setFocusable(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText("");
        getAccessibleContext().setAccessibleName("펫 카드. 누르면 캐릭터 창이 열려요");
        addMouseListener(new MouseAdapter() {
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
                if (click && SwingUtilities.isLeftMouseButton(e)) {
                    open.run();
                }
            }
        });
        // 초점을 얻고 잃을 때 포커스 고리를 다시 그린다.
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "open");
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "open");
        getActionMap().put("open", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                open.run();
            }
        });
        data.onChange(this::refresh);
        refresh();
    }

    /** 화면 읽기 프로그램에는 누르는 단추로 알린다. 이름은 생성자가, 설명(펫, 레벨, 오늘 끝낸 일, 말)은 refresh가 채운다(S3q). */
    @Override
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJComponent() {
                @Override
                public AccessibleRole getAccessibleRole() {
                    return AccessibleRole.PUSH_BUTTON;
                }
            };
        }
        return accessibleContext;
    }

    private void refresh() {
        expBar.set(Level.of(data.activePet().getExp()).ratio());
        DailyProgress d = progress();
        doneBar.set(d.getDone(), d.getTotal());
        getAccessibleContext().setAccessibleDescription(nameText() + " " + levelText() + ", " + expText() + ", " + doneText()
                + (periodText() == null ? "" : ", " + periodText()) + (bubbleText().isBlank() ? "" : ", " + bubbleText()));
        revalidate();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(360, 240);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(300, 210);
    }

    /** 마지막으로 그린 말풍선을 줄였고 마우스가 그 위면 펫 말 전체, 아니면 "캐릭터 창 열기"(S3q). */
    @Override
    public String getToolTipText(MouseEvent e) {
        if (bubbleCut && bubbleBox != null && bubbleBox.contains(e.getPoint())) {
            return "<html><div style='width:260px'>" + Ui.esc(bubbleText()) + "</div></html>";
        }
        return "캐릭터 창 열기";
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (clock == null) {
            start = System.nanoTime();
            clock = new Timer(40, e -> {
                if (isShowing()) {
                    tick(System.nanoTime());
                    Rectangle f = frame();
                    repaint(f.x, f.y, PetCardArt.petWidth(f) + 24, f.height);
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

    /** now(System.nanoTime 값) 때의 움직임 시각을 센다. */
    void tick(long now) {
        t = (now - start) / 1e9;
    }

    /** 카드 틀(그림자 자리를 뺀 것). Card와 같다. */
    private Rectangle frame() {
        return new Rectangle(2, 1, getWidth() - 4, getHeight() - 8);
    }

    /** 오른쪽 열(이름부터 말풍선까지)의 왼쪽과 폭. */
    private static int infoX(Rectangle f) {
        return f.x + PetCardArt.petWidth(f) + PetCardArt.GAP;
    }

    private static int infoW(Rectangle f) {
        return f.x + f.width - PAD - infoX(f);
    }

    /** 이름 줄, 경험치 줄, 흰 작은 카드, 말풍선의 위. 카드가 높으면(넓은 창) 오른쪽 열을 조금 내려 위로 몰리지 않게 한다. */
    private int[] rows(Rectangle f) {
        int name = f.y + PAD + Math.max(0, (f.height - 250) / 3);
        int mini = name + 36 + 8 + 4 + 16 + PetCardArt.GAP;
        return new int[] {name, name + 30 + 6, mini, mini + miniHeight() + PetCardArt.GAP};
    }

    @Override
    public void doLayout() {
        Rectangle f = frame();
        int chip = chipWidth();
        expBar.setBounds(infoX(f) + chip + 8, rows(f)[1], Math.max(20, infoW(f) - chip - 8), 8);
        doneBar.setBounds(infoX(f) + 12, rows(f)[2] + 34, Math.max(20, infoW(f) - 24), 8);
    }

    /** "경험치" 알약의 폭. 막대가 그 뒤에 온다. */
    private int chipWidth() {
        return getFontMetrics(Theme.bold(11)).stringWidth("경험치") + 16;
    }

    /** "나비". */
    String nameText() {
        return data.activePet().getName();
    }

    /** 이름 옆에 작게 적는 종류 "고양이"(D-81). */
    String kindText() {
        return data.activePet().getKind().label();
    }

    /** "Lv. 3". */
    String levelText() {
        return "Lv. " + Level.of(data.activePet().getExp()).getLevel();
    }

    /** "경험치 80 / 110", 레벨 10이면 "최고 레벨"(06 10번). */
    String expText() {
        return Level.of(data.activePet().getExp()).text();
    }

    /** 오늘의 끝낸 수와 할 일 수(06 13번). 고른 날이 아니라 오늘이다. */
    private DailyProgress progress() {
        return DailyProgress.of(data.getBook(), data.today());
    }

    /** "오늘 끝낸 일 1 / 4". 오늘 일정이 없으면 "0 / 0"이다(06 13번). */
    String doneText() {
        DailyProgress d = progress();
        return "오늘 끝낸 일 " + d.getDone() + " / " + d.getTotal();
    }

    /** 막대 아래 "기간 할 일 1개 (~11/22)"(D-70). 오늘 기간 안에 할 일이 없으면 null. */
    String periodText() {
        return data.getBook().periodNote(data.today());
    }

    /** 펫 말. 없으면 빈 글이다. */
    String bubbleText() {
        return bubble.getLine() == null ? "" : bubble.getLine();
    }

    /** 흰 작은 카드의 높이. 기간 할 일 글이 있으면 한 줄 늘린다. */
    private int miniHeight() {
        return periodText() == null ? 52 : 70;
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
        Graphics2D s = (Graphics2D) g.create();
        s.clip(new RoundRectangle2D.Double(f.x, f.y, f.width, f.height, r, r));
        PetCardArt.scene(s, f, PetCardArt.ground(f));
        PetCardArt.pet(s, data.activePet(), f, t);
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

    /** 오른쪽 열: 발자국과 이름, 작은 종류, 레벨을 한 줄에 아래 끝을 맞춘다. 이름이 길면 줄인다(D-81). 그 아래 경험치 알약과 수, 흰 작은 카드. */
    private void info(Graphics2D g, Rectangle f) {
        int[] r = rows(f);
        int x = infoX(f);
        int w = infoW(f);
        String level = levelText();
        g.setFont(Theme.heavy(24));
        int lw = g.getFontMetrics().stringWidth(level);
        int base = r[0] + 24;
        Icons.draw(g, Icons.Kind.PAW, x, base - 15, 15, Theme.ICON);
        String kind = kindText();
        g.setFont(Theme.font(11));
        int kw = g.getFontMetrics().stringWidth(kind);
        g.setFont(Theme.bold(15));
        String name = Ui.fit(nameText(), g.getFontMetrics(), w - 22 - lw - 10 - kw - 5);
        g.setColor(Theme.INK);
        g.drawString(name, x + 21, base);
        int kx = x + 21 + g.getFontMetrics().stringWidth(name) + 5;
        g.setFont(Theme.font(11));
        g.setColor(Theme.INK_SUB);
        g.drawString(kind, kx, base);
        g.setColor(Theme.INK);
        g.setFont(Theme.heavy(24));
        g.drawString(level, kx + kw + 10, base);
        // 경험치: 민트빛 알약, 막대(자식 Bar), 아래 오른쪽 수.
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
        // 흰 작은 카드: 제목과 발자국 수, 아래 칸 막대(자식 Bar).
        int my = r[2];
        int mh = miniHeight();
        g.setColor(new Color(255, 255, 255, 215));
        g.fillRoundRect(x, my, w, mh, 14, 14);
        g.setColor(new Color(255, 255, 255, 240));
        g.drawRoundRect(x, my, w - 1, mh - 1, 14, 14);
        g.setFont(Theme.bold(12));
        g.setColor(Theme.INK_SUB);
        g.drawString("오늘 끝낸 일", x + 12, my + 22);
        DailyProgress d = progress();
        String count = d.getDone() + " / " + d.getTotal();
        g.setFont(Theme.heavy(16));
        int cw = g.getFontMetrics().stringWidth(count);
        g.setColor(Theme.INK);
        g.drawString(count, x + w - 12 - cw, my + 23);
        Icons.draw(g, Icons.Kind.PAW, x + w - 12 - cw - 20, my + 9, 15, Theme.ICON);
        String period = periodText();
        if (period != null) {
            // D-70: 막대 아래에 기간 할 일 수와 가장 가까운 마감.
            g.setFont(Theme.font(11));
            g.setColor(Theme.INK_SUB);
            g.drawString(Ui.fit(period, g.getFontMetrics(), w - 24), x + 12, my + 60);
        }
    }
}
