package planningpet.stats.view;

import planningpet.app.view.App;
import planningpet.common.view.Card;
import planningpet.common.view.Icons;
import planningpet.common.view.Images;
import planningpet.common.view.RoundButtonUI;
import planningpet.common.view.Theme;
import planningpet.stats.model.Stats;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

/**
 * 통계(D-55). 팀이 준 시각 예시(2026-11-23 통계)의 배치를 따른다: 위 줄은 계획 달성률 도넛과 날짜별 완료 추이,
 * 아래 줄은 일정 유형별 달성률(D-54)과 주간과 월간 요약. 기간은 처음에 이번 주이고 머리 오른쪽(D-47)의
 * headControls()로 주간과 월간을 고르고 화살표로 옮긴다. 값은 모두 Stats가 센다.
 */
public final class StatsScreen extends JPanel {
    /** 제목 옆 "?"의 설명. D-55의 문장을 옮겼다. */
    private static final String HELP_RATE = "끝낸 수 / 셀 일정 수예요. 셀 일정은 고른 기간 안에서 오늘까지인 날의 할 일과 고정 일정이고, 앞날의 일정은 세지 않아요.";
    private static final String HELP_TREND = "주간이면 날마다, 월간이면 주마다 끝낸 수(막대)와 달성률(선)이에요. 오늘 뒤의 날은 세지 않아요.";
    private static final String HELP_KIND = "셀 일정이 있는 유형만 보여요. 유형마다 끝낸 수 / 셀 일정 수예요.";
    private static final String HELP_SUM = "이번 주(설정의 한 주 시작 요일부터 7일)와 이번 달의 달성률, 그 기간에 얻은 포인트(완료 보상과 출석)예요.";
    /** 요약의 이번 달 머리 띠. 예시의 옅은 하늘색. */
    static final Color MONTH_BAND = new Color(0xE4EEFC);

    private final App app;
    private Stats.Period period;
    public final JToggleButton weekButton = new JToggleButton("주간");
    public final JToggleButton monthButton = new JToggleButton("월간");
    public final JButton prevButton = new JButton();
    public final JButton nextButton = new JButton();
    public final JLabel periodLabel = new JLabel("", SwingConstants.CENTER);
    private final JComponent head;
    private final Donut donut = new Donut();
    private final StatRow doneRow = new StatRow(StatRow.DONE, "완료한 일정");
    private final StatRow leftRow = new StatRow(StatRow.LEFT, "미완료 일정");
    private final StatRow totalRow = new StatRow(StatRow.TOTAL, "총 일정");
    private final CardLayout rateCards = new CardLayout();
    private final JPanel rateBody = new JPanel(rateCards);
    final TrendChart trend = new TrendChart();
    private final KindBars kinds = new KindBars();
    private final SummaryBox weekBox = new SummaryBox(false);
    private final SummaryBox monthBox = new SummaryBox(true);

    public StatsScreen(App app) {
        super(new GridBagLayout());
        this.app = app;
        this.period = Stats.Period.week(app.store.today(), app.store.weekStart);
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 12));
        head = buildHead();
        place(rateCard(), 0, 0, 0.44, 0.55);
        place(trendCard(), 1, 0, 0.56, 0.55);
        place(kindCard(), 0, 1, 0.44, 0.45);
        place(summaryCard(), 1, 1, 0.56, 0.45);
        app.watch(this::refresh);
    }

    /** 화면 머리 오른쪽(D-47): 내다보는 고양이, 주간과 월간 알약, "< 기간 >". App이 머리에 붙인다. */
    public JComponent headControls() {
        return head;
    }

    /** 지금 고른 기간. */
    public Stats.Period period() {
        return period;
    }

    /** 주간(false)이나 월간(true)으로 바꾼다. 오늘이 든 기간이면 오늘을, 아니면 지금 기간의 첫날을 기준으로 잡는다. */
    public void setMonthly(boolean monthly) {
        if (monthly != period.monthly()) {
            LocalDate base = period.contains(app.store.today()) ? app.store.today() : period.from();
            period = monthly ? Stats.Period.month(base) : Stats.Period.week(base, app.store.weekStart);
        }
        refresh();
    }

    /** 앞(-1)이나 뒤(+1) 기간으로 옮긴다. */
    public void step(int delta) {
        period = period.step(delta);
        refresh();
    }

    /** 사이드바 아래에 그릴 자는 고양이(예시의 왼쪽 아래). 파일이 없으면 null이다. App이 통계 화면일 때 그린다. */
    public static BufferedImage sleepingCat() {
        return StatsArt.SLEEP_CAT;
    }

    private void place(JComponent c, int x, int y, double wx, double wy) {
        GridBagConstraints k = new GridBagConstraints();
        k.gridx = x;
        k.gridy = y;
        k.weightx = wx;
        k.weighty = wy;
        k.fill = GridBagConstraints.BOTH;
        int g = Theme.GAP;
        k.insets = new Insets(y == 0 ? 0 : g / 2, x == 0 ? 0 : g / 2, y == 0 ? g / 2 : 0, x == 0 ? g / 2 : 0);
        c.setPreferredSize(new Dimension(100, 100));
        c.setMinimumSize(new Dimension(100, 100));
        add(c, k);
    }

    // 머리 오른쪽

    private JComponent buildHead() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        BufferedImage cat = StatsArt.PEEK_CAT;
        if (cat != null) {
            JComponent peek = new JComponent() {
                @Override
                protected void paintComponent(Graphics graphics) {
                    Graphics2D g = (Graphics2D) graphics.create();
                    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    Images.fitBottom(g, cat, 0, 0, getWidth(), getHeight());
                    g.dispose();
                }
            };
            peek.setPreferredSize(new Dimension(64 * cat.getWidth() / Math.max(1, cat.getHeight()), 64));
            row.add(peek);
        }
        ButtonGroup group = new ButtonGroup();
        JPanel modes = new Pill(new FlowLayout(FlowLayout.CENTER, 2, 0));
        for (JToggleButton b : new JToggleButton[] {weekButton, monthButton}) {
            b.setFont(Theme.bold(15));
            b.setIcon(Icons.icon(Icons.Kind.CALENDAR, 18, Theme.ICON));
            b.setSelectedIcon(Icons.icon(Icons.Kind.CALENDAR, 18, Color.WHITE));
            b.setIconTextGap(7);
            b.setBorder(BorderFactory.createEmptyBorder(8, 16, 11, 18));
            group.add(b);
            modes.add(b);
        }
        weekButton.setToolTipText("한 주씩 봐요. 한 주는 설정의 한 주 시작 요일부터예요");
        monthButton.setToolTipText("한 달씩 봐요");
        weekButton.addActionListener(e -> setMonthly(false));
        monthButton.addActionListener(e -> setMonthly(true));
        row.add(modes);

        JPanel nav = new Pill(new BorderLayout(2, 0));
        arrow(prevButton, StatsArt.Kind.LEFT, "이전 기간");
        arrow(nextButton, StatsArt.Kind.RIGHT, "다음 기간");
        prevButton.addActionListener(e -> step(-1));
        nextButton.addActionListener(e -> step(1));
        periodLabel.setFont(Theme.heavy(17));
        periodLabel.setForeground(Theme.INK);
        FontMetrics fm = periodLabel.getFontMetrics(periodLabel.getFont());
        periodLabel.setPreferredSize(new Dimension(Math.max(fm.stringWidth("2026년 12월"), fm.stringWidth("12/21 ~ 12/27")) + 20, 38));
        nav.add(prevButton, BorderLayout.WEST);
        nav.add(periodLabel, BorderLayout.CENTER);
        nav.add(nextButton, BorderLayout.EAST);
        row.add(nav);
        return row;
    }

    private static void arrow(JButton b, StatsArt.Kind kind, String name) {
        b.setIcon(StatsArt.icon(kind, 18, Theme.ICON));
        b.setBorder(BorderFactory.createEmptyBorder(8, 9, 11, 9));
        b.setToolTipText(name);
        b.getAccessibleContext().setAccessibleName(name);
    }

    /** 흰 알약 판(예시의 주간과 월간 묶음, 기간 옮기기 묶음). */
    private static final class Pill extends JPanel {
        Pill(java.awt.LayoutManager layout) {
            super(layout);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(4, 5, 6, 5));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth() - 4;
            int h = getHeight() - 5;
            Card.shadow(g, 2, 1, w, h, h);
            g.setColor(new Color(255, 255, 255, 235));
            g.fillRoundRect(2, 1, w, h, h, h);
            g.setColor(Theme.CARD_EDGE);
            g.drawRoundRect(2, 1, w - 1, h - 1, h, h);
            g.dispose();
        }
    }

    // 카드

    /** 카드 제목 줄: 아이콘, 아주 굵은 제목, "?" 설명, 오른쪽 장식. */
    private static JPanel title(StatsArt.Kind kind, javax.swing.Icon icon, String text, String help, JComponent east) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        JLabel label = new JLabel(text, icon != null ? icon : StatsArt.icon(kind, 30, Theme.ICON), SwingConstants.LEFT);
        label.setIconTextGap(10);
        label.setFont(Theme.heavy(21));
        JLabel q = new JLabel(StatsArt.icon(StatsArt.Kind.HELP, 20, Theme.ICON));
        q.setToolTipText("<html><div style='width:250px'>" + help + "</div></html>");
        q.getAccessibleContext().setAccessibleName(text + " 설명");
        q.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.add(label);
        left.add(q);
        row.add(left, BorderLayout.WEST);
        if (east != null) {
            row.add(east, BorderLayout.EAST);
        }
        return row;
    }

    /** 카드 오른쪽 위의 발자국 장식. */
    private static JComponent paws() {
        JComponent c = new JComponent() {
            @Override
            protected void paintComponent(Graphics graphics) {
                StatsArt.paws((Graphics2D) graphics, getWidth() - 42, 0);
            }
        };
        c.setPreferredSize(new Dimension(46, 34));
        return c;
    }

    private static Card card() {
        Card c = new Card(new BorderLayout(0, 10));
        c.setBorder(BorderFactory.createEmptyBorder(14, 18, 18, 16));
        return c;
    }

    private Card rateCard() {
        Card c = card();
        c.add(title(StatsArt.Kind.TARGET, null, "계획 달성률", HELP_RATE, paws()), BorderLayout.NORTH);
        JPanel rows = new JPanel(new GridLayout(3, 1, 0, 10));
        rows.add(doneRow);
        rows.add(leftRow);
        rows.add(totalRow);
        // 왼쪽은 높이에 맞춘 정사각형 도넛, 오른쪽은 남은 폭의 세 줄을 세로 가운데에 둔다.
        JPanel data = new JPanel(null) {
            @Override
            public void doLayout() {
                int w = getWidth();
                int h = getHeight();
                int s = Math.max(100, Math.min(h, (int) (w * 0.5)));
                donut.setBounds(0, (h - s) / 2, s, s);
                int rh = Math.min(rows.getPreferredSize().height, h);
                rows.setBounds(s + 10, (h - rh) / 2, Math.max(0, w - s - 10), rh);
            }
        };
        data.add(donut);
        data.add(rows);
        rateBody.add(data, "data");
        rateBody.add(empty(), "empty");
        c.add(rateBody, BorderLayout.CENTER);
        return c;
    }

    private Card trendCard() {
        Card c = card();
        c.add(title(null, Icons.icon(Icons.Kind.CALENDAR, 30, Theme.ICON), "날짜별 일정 완료 추이", HELP_TREND, null),
                BorderLayout.NORTH);
        c.add(trend, BorderLayout.CENTER);
        return c;
    }

    private Card kindCard() {
        Card c = card();
        c.add(title(StatsArt.Kind.PIE, null, "일정 유형별 달성률", HELP_KIND, paws()), BorderLayout.NORTH);
        c.add(kinds, BorderLayout.CENTER);
        return c;
    }

    private Card summaryCard() {
        Card c = card();
        c.add(title(StatsArt.Kind.SWAP, null, "주간과 월간 요약", HELP_SUM, paws()), BorderLayout.NORTH);
        JPanel two = new JPanel(new GridLayout(1, 2, 14, 0));
        two.add(weekBox);
        two.add(monthBox);
        c.add(two, BorderLayout.CENTER);
        return c;
    }

    private static JLabel empty() {
        JLabel l = new JLabel(StatsArt.NONE, SwingConstants.CENTER);
        l.setFont(Theme.font(14));
        l.setForeground(Theme.INK_SUB);
        return l;
    }

    // 다시 그리기

    private void refresh() {
        LocalDate today = app.store.today();
        if (!period.monthly() && period.from().getDayOfWeek() != app.store.weekStart) {
            // D-91: 한 주 시작을 바꾸면 보던 주의 첫날이 든 새 주로 바꾼다.
            period = Stats.Period.week(period.from(), app.store.weekStart);
        }
        weekButton.setSelected(!period.monthly());
        monthButton.setSelected(period.monthly());
        periodLabel.setText(periodText(period));
        periodLabel.getAccessibleContext().setAccessibleName("기간 " + periodText(period));

        Stats.Rate rate = Stats.rate(app.store, period);
        donut.set(rate);
        doneRow.value = rate.done() + "개";
        leftRow.value = (rate.total() - rate.done()) + "개";
        totalRow.value = rate.total() + "개";
        rateCards.show(rateBody, rate.total() == 0 ? "empty" : "data");
        trend.set(Stats.trend(app.store, period), today, period.monthly());
        kinds.set(Stats.byCategory(app.store, period));

        Stats.Period week = Stats.Period.week(today, app.store.weekStart);
        Stats.Period month = Stats.Period.month(today);
        weekBox.set("이번 주", "(" + md(week.from()) + " ~ " + md(week.to()) + ")",
                Stats.rate(app.store, week), Stats.points(app.store, week));
        monthBox.set("이번 달", "(" + month.from().getYear() + "년 " + month.from().getMonthValue() + "월)",
                Stats.rate(app.store, month), Stats.points(app.store, month));
        revalidate();
        repaint();
        head.repaint();
    }

    /** 머리의 기간 글자. 월간 "2026년 11월", 주간 "11/16 ~ 11/22". */
    static String periodText(Stats.Period p) {
        return p.monthly() ? p.from().getYear() + "년 " + p.from().getMonthValue() + "월" : md(p.from()) + " ~ " + md(p.to());
    }

    static String md(LocalDate d) {
        return d.getMonthValue() + "/" + d.getDayOfMonth();
    }

    // 작은 부품

}
