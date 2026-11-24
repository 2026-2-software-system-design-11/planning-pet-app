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
    private static final String HELP_SUM = "이번 주(월요일부터 일요일)와 이번 달의 달성률, 그 기간에 얻은 포인트(완료 보상과 출석)예요.";
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
        this.period = Stats.Period.week(app.store.today());
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
