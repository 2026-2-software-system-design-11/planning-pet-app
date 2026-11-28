package planningpet.home.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import planningpet.app.controller.AppController;
import planningpet.app.model.AppData;
import planningpet.common.view.Card;
import planningpet.common.view.Icons;
import planningpet.common.view.Theme;
import planningpet.home.controller.HomeController;

/**
 * 홈(05 메인, D-59). 왼쪽 위 펫 카드, 왼쪽 아래 달력, 오른쪽 위아래 할 일 목록이다. 값은 디자인 시스템 10절 "홈"에 있다
 * (docs/porting/home.md S3ab).
 */
public final class MainScreen extends JPanel {
    private static final String WEEK = "월화수목금토일";
    private final AppData data;
    final CalendarPanel calendar;
    final PetCard pet;
    final JLabel listTitle = new JLabel();
    /** "일정 추가"는 목록 제목 줄 오른쪽이다(D-40). */
    final JButton addButton = new JButton("일정 추가");

    /** 펫 카드, 달력 카드, 할 일 카드를 이 차례로 갖는다. 데이터 변경 알림에 refresh를 거는 일은 화면을 넣는 쪽(Main)이 한다. */
    public MainScreen(AppData data, AppController app, HomeController home) {
        super(null);
        this.data = data;
        calendar = new CalendarPanel(data, home, app);
        pet = new PetCard(data, app.getBubble(), () -> { });

        Card list = new Card(new BorderLayout(0, 8));
        listTitle.setFont(Theme.heavy(21));
        listTitle.setIcon(Icons.icon(Icons.Kind.CHECKLIST, 24, Theme.ICON));
        listTitle.setIconTextGap(8);
        addButton.setIcon(Icons.icon(Icons.Kind.PLUS, 16, Theme.ICON));
        addButton.setIconTextGap(6);
        JPanel head = new JPanel(new BorderLayout());
        head.add(listTitle, BorderLayout.CENTER);
        head.add(addButton, BorderLayout.EAST);
        head.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.CARD_EDGE),
                BorderFactory.createEmptyBorder(0, 0, 8, 0)));
        JPanel top = new JPanel(new BorderLayout());
        top.add(head, BorderLayout.NORTH);
        list.add(top, BorderLayout.NORTH);

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

    /** 고른 날의 목록 제목, 달력, 펫 카드를 다시 그린다. */
    public void refresh() {
        LocalDate date = calendar.selected();
        listTitle.setText(date.getMonthValue() + "월 " + date.getDayOfMonth() + "일 ("
                + WEEK.charAt(date.getDayOfWeek().getValue() - 1) + ") 할 일");
        calendar.refresh();
        pet.repaint();
    }
}
