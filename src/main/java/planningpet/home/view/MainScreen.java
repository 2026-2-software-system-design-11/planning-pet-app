package planningpet.home.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.time.LocalDate;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import planningpet.app.controller.AppController;
import planningpet.app.model.AppData;
import planningpet.common.view.Card;
import planningpet.common.view.Icons;
import planningpet.common.view.RoundField;
import planningpet.common.view.Theme;
import planningpet.common.view.Ui;
import planningpet.home.controller.HomeController;
import planningpet.reward.controller.RewardController;
import planningpet.schedule.model.Task;
import planningpet.schedule.view.TaskRows;

/**
 * 홈(05 메인, D-59). 왼쪽 위 펫 카드, 왼쪽 아래 달력, 오른쪽 위아래 할 일 목록이다. 값은 디자인 시스템 10절 "홈"에 있다
 * (docs/porting/home.md S3ab).
 */
public final class MainScreen extends JPanel {
    private static final String WEEK = "월화수목금토일";
    private final AppData data;
    final CalendarPanel calendar;
    final TaskRows rows;
    final PetCard pet;
    final JLabel listTitle = new JLabel();
    final RoundField quick = new RoundField(Icons.Kind.PENCIL, "할 일을 쓰고 Enter");
    /** D-40: 칸 옆 "추가"는 Enter와 같다(넣는 일은 C4). "일정 추가"는 목록 제목 줄 오른쪽이다. */
    final JButton quickButton = new JButton("추가");
    final JButton addButton = new JButton("일정 추가");
    /** D-74: 예시 일정이 남았을 때만 보인다. */
    final JButton sampleButton = new JButton("예시 지우기");
    /** 예시 안내 띠(D-74). 예시 일정이 없으면 숨는다. */
    final JPanel sampleBar = new JPanel(new BorderLayout(8, 0));
    /** 예시 일정이 남았는가(D-74). 남은 예시를 세는 일은 C5가 넣는다. */
    public BooleanSupplier samples = () -> false;
    /** "일정 추가"를 누르면 고른 날을 받는다. 일정 입력 창은 S4가 넣는다. */
    public Consumer<LocalDate> openAdd = day -> { };
    /** "예시 지우기"를 누르면 부른다(D-74). 확인과 지우기는 C5가 넣는다. */
    public Runnable clearSamples = () -> { };
    /** 펫 카드를 누르면 캐릭터 창을 연다(D-58, D-59). S8이 넣는다. */
    public Runnable openCharacter = () -> { };
    /** 목록 줄의 연필, 휴지통(S4)과 시각 알약(S5). */
    public Consumer<Task> onEdit = t -> { };
    public Consumer<Task> onDelete = t -> { };
    public TaskRows.TimeOpener onTime = (t, day, at) -> { };

    /** 펫 카드, 달력 카드, 할 일 카드를 이 차례로 갖는다. 데이터 변경 알림에 refresh를 거는 일은 화면을 넣는 쪽(Main)이 한다. */
    public MainScreen(AppData data, AppController app, HomeController home, RewardController reward) {
        super(null);
        this.data = data;
        calendar = new CalendarPanel(data, home, app);
        rows = new TaskRows(data, reward, t -> onEdit.accept(t), t -> onDelete.accept(t), (t, day, at) -> onTime.open(t, day, at));
        rows.resort = this::refresh;
        pet = new PetCard(data, app.getBubble(), () -> openCharacter.run());

        Card list = new Card(new BorderLayout(0, 8));
        listTitle.setFont(Theme.heavy(21));
        listTitle.setIcon(Icons.icon(Icons.Kind.CHECKLIST, 24, Theme.ICON));
        listTitle.setIconTextGap(8);
        addButton.setIcon(Icons.icon(Icons.Kind.PLUS, 16, Theme.ICON));
        addButton.setIconTextGap(6);
        addButton.addActionListener(e -> openAdd.accept(calendar.selected()));
        JPanel head = new JPanel(new BorderLayout());
        head.add(listTitle, BorderLayout.CENTER);
        head.add(addButton, BorderLayout.EAST);
        head.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.CARD_EDGE),
                BorderFactory.createEmptyBorder(0, 0, 8, 0)));
        sampleButton.addActionListener(e -> clearSamples.run());
        sampleButton.setFont(Theme.bold(12));
        sampleButton.setBorder(BorderFactory.createEmptyBorder(5, 12, 7, 12));
        JLabel sampleNote = new JLabel("예시 일정이 들어 있어요. 다 봤으면 지워도 돼요");
        sampleNote.setFont(Theme.font(12));
        sampleNote.setForeground(Theme.INK_SUB);
        sampleBar.add(sampleNote, BorderLayout.CENTER);
        sampleBar.add(sampleButton, BorderLayout.EAST);
        sampleBar.setBorder(BorderFactory.createEmptyBorder(6, 4, 0, 0));
        JPanel top = new JPanel(new BorderLayout());
        top.add(head, BorderLayout.NORTH);
        top.add(sampleBar, BorderLayout.SOUTH);
        list.add(top, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(rows, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        list.add(scroll, BorderLayout.CENTER);
        list.add(quickAdd(), BorderLayout.SOUTH);

        Card calendarCard = new Card(new BorderLayout());
        calendarCard.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        calendarCard.add(calendar);
        add(pet);
        add(calendarCard);
        add(list);
    }

    /**
     * 왼쪽 열은 본문 폭의 0.47(360에서 760 사이), 펫 카드 높이는 본문 높이의 0.42(236에서 400 사이)이고 나머지가 달력이다.
     * 오른쪽 할 일 카드는 위아래를 다 쓴다(D-59).
     */
    @Override
    public void doLayout() {
        int w = getWidth();
        int h = getHeight();
        int gap = Theme.GAP;
        int leftW = (int) Math.max(360, Math.min(760, w * 0.47));
        int petH = (int) Math.max(236, Math.min(400, h * 0.42));
        Component[] parts = getComponents();
        parts[0].setBounds(0, 0, leftW, petH);
        parts[1].setBounds(0, petH + gap, leftW, h - petH - gap);
        parts[2].setBounds(leftW + gap, 0, w - leftW - gap, h);
        for (Component c : parts) {
            c.doLayout();
        }
    }

    /** 빠른 추가 줄(05 입력 항목 5, D-40): 100자까지 받는 둥근 칸, 옆 "추가"(칸이 비면 흐림), 아래 안내. Enter와 "추가"는 C4가 잇는다. */
    private JPanel quickAdd() {
        JPanel panel = new JPanel(new BorderLayout(8, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        Ui.limit(quick, 100);
        quickButton.setEnabled(false);
        quick.getDocument().addDocumentListener(new DocumentListener() {
            private void sync() {
                quickButton.setEnabled(!quick.getText().isBlank());
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                sync();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                sync();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                sync();
            }
        });
        JLabel hint = new JLabel("제목을 쓰고 Enter나 추가를 누르면 30분짜리 할 일로 들어가요");
        hint.setForeground(Theme.INK_SUB);
        hint.setFont(Theme.font(12));
        hint.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        quickButton.setBorder(BorderFactory.createEmptyBorder(9, 18, 11, 18));
        panel.add(quick, BorderLayout.CENTER);
        panel.add(quickButton, BorderLayout.EAST);
        panel.add(hint, BorderLayout.SOUTH);
        return panel;
    }

    /** 달력에서 date를 고른다. 일정 시작 알림을 누르면 Tray가 부른다(07 seq-uc13). */
    public void showDay(LocalDate date) {
        calendar.pick(date);
    }

    /** 고른 날의 목록 제목과 목록, 예시 띠, 달력, 펫 카드를 다시 그린다. 목록이 다시 놓일 때(resort)도 부른다. */
    public void refresh() {
        LocalDate date = calendar.selected();
        listTitle.setText(date.getMonthValue() + "월 " + date.getDayOfMonth() + "일 ("
                + WEEK.charAt(date.getDayOfWeek().getValue() - 1) + ") 할 일");
        rows.showPlan(data.getBook().dayPlan(date));
        boolean shown = samples.getAsBoolean();
        sampleBar.setVisible(shown);
        sampleButton.setVisible(shown);
        calendar.refresh();
        pet.repaint();
    }
}
