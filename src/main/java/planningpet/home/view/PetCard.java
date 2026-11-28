package planningpet.home.view;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JComponent;
import javax.swing.Timer;
import planningpet.app.model.AppData;
import planningpet.common.view.Bar;
import planningpet.common.view.Card;
import planningpet.common.view.Icons;
import planningpet.common.view.Theme;
import planningpet.common.view.Ui;
import planningpet.reward.model.Level;

/**
 * 홈의 펫 카드(05 홈 "펫 카드", D-59). 하늘과 풀밭 장면 왼쪽에 고른 펫(D-48)을 그린다. 값은 디자인 시스템 10절 "홈"에 있다.
 * 오른쪽 열 위에 이름과 레벨, 경험치 막대를 그린다(S3n). 흰 작은 카드와 말풍선은 #469, 누르기는 #467이 더한다(docs/porting/home.md S3m).
 */
public final class PetCard extends JComponent {
    /** 오른쪽 열의 위와 오른쪽 여백. */
    private static final int PAD = 14;
    private final AppData data;
    /** 경험치 막대(06 10번). 하늘 위에서도 보이게 바탕이 TRACK_DEEP이다. */
    final Bar expBar = new Bar(0, 8).track(Theme.TRACK_DEEP);
    /** 펫 숨쉬기 시계. 처음 화면에 붙을 때 만들고 붙어 보이는 동안 40밀리초마다 펫 자리를 다시 그린다. */
    Timer clock;
    /** 시계를 만든 때(System.nanoTime 값)와 그 뒤 지난 초(PetArt의 움직임 시각). 창 없이 그릴 때는 0이다. */
    long start;
    double t;

    /** 고른 펫을 그리고 데이터 변경 알림마다 다시 그린다. 메인 창은 펫을 고른 뒤 열려 고른 펫이 늘 있다(S3m). */
    public PetCard(AppData data) {
        this.data = data;
        setLayout(null);
        add(expBar);
        data.onChange(this::refresh);
        refresh();
    }

    private void refresh() {
        expBar.set(Level.of(data.activePet().getExp()).ratio());
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

    /** 이름 줄과 경험치 줄의 위. 카드가 높으면(넓은 창) 오른쪽 열을 조금 내려 위로 몰리지 않게 한다. */
    private static int[] rows(Rectangle f) {
        int name = f.y + PAD + Math.max(0, (f.height - 250) / 3);
        return new int[] {name, name + 30 + 6};
    }

    @Override
    public void doLayout() {
        Rectangle f = frame();
        int chip = chipWidth();
        expBar.setBounds(infoX(f) + chip + 8, rows(f)[1], Math.max(20, infoW(f) - chip - 8), 8);
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
        g.setColor(Theme.CARD_EDGE);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(new RoundRectangle2D.Double(f.x + 0.5, f.y + 0.5, f.width - 1, f.height - 1, r, r));
        g.dispose();
    }

    /** 오른쪽 열: 발자국과 이름, 작은 종류, 레벨을 한 줄에 아래 끝을 맞춘다. 이름이 길면 줄인다(D-81). 그 아래 경험치 알약과 수. */
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
    }
}
