package planningpet.schedule.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.time.Clock;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import planningpet.app.model.AppData;
import planningpet.common.view.Card;
import planningpet.common.view.Ui;
import planningpet.reward.controller.RewardController;
import planningpet.schedule.model.Task;

/**
 * 일정관리(05 일정관리, UC-10, UC-11). 위 줄, 가운데 시간표, 오른쪽 "시각 없는 할 일" 카드다. 옮기는 몫은
 * docs/porting/schedule.md S5c. 시간표 누르기와 처음 보이는 자리는 S5c2, 말풍선, 펫 자리, 놓인 블록 보이기, 앱 창에 붙이기는 S5d가
 * 더한다.
 */
public final class ScheduleScreen extends JPanel {
    private final AppData data;
    private final Clock clock;
    final TimetablePanel table;
    final JPanel header = new JPanel(new GridLayout(1, 7));
    final JLabel range = new JLabel("", SwingConstants.CENTER);
    final JButton prev = new JButton("<");
    final JButton next = new JButton(">");
    /** D-60: 이번 주를 보고 있으면 흐리다. */
    final JButton today = new JButton("오늘");
    final JButton autoPlace = new JButton("자동 배치");
    final JButton add = new JButton("일정 추가");
    final TaskRows untimed;
    final JScrollPane scroll;
    /** 목록 줄의 연필, 휴지통, 시계 단추. 창은 S4와 S5f가 넣는다. 시간표 블록의 누르기는 S5c2가 잇는다. */
    public Consumer<Task> onEdit = t -> { };
    public Consumer<Task> onDelete = t -> { };
    public TaskRows.TimeOpener onTime = (t, day, at) -> { };

    /** clock은 자동 배치 단추가 세는 지금이다. 데이터 변경 알림에 refresh를 거는 일은 화면을 넣는 쪽(Main)이 한다. */
    public ScheduleScreen(AppData data, RewardController reward, Clock clock) {
        super(new BorderLayout(8, 8));
        this.data = data;
        this.clock = clock;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 12));
        table = new TimetablePanel(data);
        untimed = new TaskRows(data, reward, t -> onEdit.accept(t), t -> onDelete.accept(t), (t, day, at) -> onTime.open(t, day, at));
        JPanel bar = new JPanel(new BorderLayout());
        bar.add(Ui.flow(prev, range, next, today), BorderLayout.WEST);
        bar.add(Ui.flow(autoPlace, add), BorderLayout.EAST);
        JPanel top = new JPanel(new BorderLayout(0, 4));
        top.add(bar, BorderLayout.NORTH);
        add(top, BorderLayout.NORTH);
        scroll = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setColumnHeaderView(header);
        scroll.getViewport().setViewPosition(new Point(0, 8 * TimetablePanel.HOUR));
        Card tableCard = new Card(new BorderLayout());
        tableCard.add(scroll);
        add(tableCard, BorderLayout.CENTER);
        Card side = new Card(new BorderLayout(0, 8));
        side.setPreferredSize(new Dimension(240, 0));
        side.add(new JLabel("<html><b>시각 없는 할 일</b></html>"), BorderLayout.NORTH);
        JScrollPane sideScroll = new JScrollPane(untimed, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sideScroll.setBorder(BorderFactory.createEmptyBorder());
        side.add(sideScroll, BorderLayout.CENTER);
        // 말풍선과 펫 자리(side의 SOUTH)는 S5d가 더한다.
        add(side, BorderLayout.EAST);
    }
}
