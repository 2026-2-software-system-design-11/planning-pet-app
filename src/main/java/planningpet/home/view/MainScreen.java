package planningpet.home.view;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import planningpet.app.controller.AppController;
import planningpet.app.model.AppData;
import planningpet.common.view.Card;
import planningpet.common.view.Theme;
import planningpet.home.controller.HomeController;

/**
 * 홈(05 메인, D-59). 왼쪽 위 펫 카드, 왼쪽 아래 달력, 오른쪽 위아래 할 일 목록이다. 값은 디자인 시스템 10절 "홈"에 있다
 * (docs/porting/home.md S3ab).
 */
public final class MainScreen extends JPanel {
    final CalendarPanel calendar;
    final PetCard pet;

    /** 펫 카드, 달력 카드, 할 일 카드를 이 차례로 갖는다. */
    public MainScreen(AppData data, AppController app, HomeController home) {
        super(null);
        calendar = new CalendarPanel(data, home, app);
        pet = new PetCard(data, app.getBubble(), () -> { });

        Card list = new Card(new BorderLayout(0, 8));

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
}
