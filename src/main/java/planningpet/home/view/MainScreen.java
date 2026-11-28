package planningpet.home.view;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import planningpet.app.controller.AppController;
import planningpet.app.model.AppData;
import planningpet.common.view.Card;
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
}
