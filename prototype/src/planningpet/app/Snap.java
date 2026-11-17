package planningpet.app;

import planningpet.app.model.Store;
import planningpet.app.view.App;
import planningpet.pet.view.PickDialog;
import planningpet.schedule.model.*;
import planningpet.schedule.view.*;
import planningpet.shop.view.ShopDialog;
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
        app.store.say(app.talk.onWindowShown(app.game));
        for (String[] screen : new String[][] {{"홈", "1-main"}, {"일정관리", "2-schedule"}, {"pet", "5-pet"},
                {"설정", "8-settings"}}) {
            app.show(screen[0]);
            save(app.root, new Dimension(1000, 680), new File(dir, screen[1] + ".png"));
        }
        LocalDate monday = Store.mondayOf(app.store.today());
        save(TaskDialog.content(new TaskForm(null, app.store.today(), app.store.now), new JButton("취소"), new JButton("저장"), null), null, new File(dir, "3-form.png"));
        save(AutoPlaceDialog.content(app.store, monday, Planner.place(app.store, monday), new JButton("취소"),
                new JButton("적용")), null, new File(dir, "4-autoplace.png"));
        save(ShopDialog.content(app.store, item -> { }, new JButton("닫기")), null, new File(dir, "6-shop.png"));
        save(new PickDialog().panel, null, new File(dir, "7-pick.png"));
        Task essay = app.store.dayPlan(app.store.today()).timed().get(1);
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
