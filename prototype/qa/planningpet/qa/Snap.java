package planningpet.qa;

import planningpet.app.Main;
import planningpet.app.model.*;
import planningpet.app.view.App;
import planningpet.pet.view.PickDialog;
import planningpet.schedule.model.*;
import planningpet.schedule.view.*;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;

/** 창을 띄우지 않고 모든 화면을 PNG로 그린다. snap.sh가 -Djava.awt.headless=true로 부른다. */
public final class Snap {
    private Snap() {
    }

    public static void main(String[] args) throws Exception {
        File dir = new File(args.length > 0 ? args[0] : "screenshots");
        dir.mkdirs();
        App app = Main.create(MockData.sample());
        app.control.windowShown();
        String[][] screens = {{"홈", "1-main"}, {"일정관리", "2-schedule"}, {"나의 방", "5-room"}, {"캐릭터", "6-character"},
            {"통계", "10-stats"}, {"설정", "8-settings"}};
        for (String[] screen : screens) {
            app.show(screen[0]);
            save(app.root, new Dimension(1000, 680), new File(dir, screen[1] + ".png"));
        }
        // 예시 그림과 같은 비율(16:9)로 넓게 그린 판. 강아지를 데려와 두 마리인 상태다(D-48).
        app.pet.adopt(true, "");
        app.pet.select("CAT");
        // 넓은 판은 예시처럼 방을 채운 상태로 그린다(아이템 열 개를 가지고 놓았다).
        for (String item : new String[] {"털실 공", "뼈다귀", "러그", "액자", "걸이 화분", "조명", "책장", "소파", "캣타워", "화분"}) {
            app.store.owned.add(item);
            app.store.placed.add(item);
        }
        app.store.placed.remove("쿠션");
        app.store.changed();
        for (String[] screen : screens) {
            if (screen[0].equals("나의 방") || screen[0].equals("캐릭터") || screen[0].equals("통계")) {
                app.show(screen[0]);
                save(app.root, new Dimension(1440, 810), new File(dir, screen[1] + "-wide.png"));
            }
        }
        LocalDate monday = TaskBook.mondayOf(app.store.today());
        save(TaskDialog.content(new TaskForm(null, app.store.today(), app.store.now), new JButton("취소"), new JButton("저장"), null), null, new File(dir, "3-form.png"));
        save(AutoPlaceDialog.content(app.store, monday, Planner.place(app.store, monday), new JButton("취소"),
                new JButton("적용")), null, new File(dir, "4-autoplace.png"));
        save(new PickDialog().panel, null, new File(dir, "7-pick.png"));
        Task essay = app.store.book.dayPlan(app.store.today()).timed().get(1);
        save(new TimeEditor(essay, app.store.now).panel, null, new File(dir, "9-time-edit.png"));
        System.out.println("그렸다: " + dir.getPath());
    }

    public static void save(JComponent c, Dimension size, File file) throws Exception {
        Dimension d = size != null ? size : c.getPreferredSize();
        c.setSize(d);
        layout(c);
        layout(c);
        scrollToMorning(c);
        BufferedImage img = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        c.paint(g);
        g.dispose();
        ImageIO.write(img, "png", file);
    }

    private static void layout(Component c) {
        c.doLayout();
        if (c instanceof Container k) {
            for (Component child : k.getComponents()) {
                layout(child);
            }
        }
    }

    /** 일정관리 시간표는 8시가 맨 위에 오게 한다. */
    private static void scrollToMorning(Component c) {
        if (c instanceof JScrollPane s && s.getViewport().getView() instanceof TimetablePanel) {
            s.getViewport().setViewPosition(new java.awt.Point(0, 8 * TimetablePanel.HOUR));
        } else if (c instanceof Container k) {
            for (Component child : k.getComponents()) {
                scrollToMorning(child);
            }
        }
    }
}
