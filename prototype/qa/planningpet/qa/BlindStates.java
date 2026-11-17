package planningpet.qa;

import planningpet.app.*;
import planningpet.app.model.Store;
import planningpet.app.view.App;
import planningpet.common.model.Rules;
import planningpet.schedule.model.*;
import planningpet.schedule.view.*;
import planningpet.shop.view.ShopDialog;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

/** 블라인드 예측 2차: 1차 그림(screen-01~11)에 없던 상태의 화면을 screen-12부터 그린다. */
public final class BlindStates {
    private BlindStates() {
    }

    public static void main(String[] args) throws Exception {
        File images = new File(args[0]);
        File key = new File(args[1]);
        images.mkdirs();
        try (PrintWriter out = new PrintWriter(key, StandardCharsets.UTF_8)) {
            out.println("# 블라인드 그림 열쇠 2차");
            out.println();
            out.println("예측자에게 주지 않는다. 그림 번호와 부품을 잇는다.");
            int n = 12;
            Store none = MockData.empty();
            App first = Main.create(none);
            first.store.say(first.talk.onWindowShown(first.game));
            first.show("홈");
            first.store.changed();
            BlindShots.shoot(first.root, new Dimension(1000, 680), null, images, key, n++, out, "홈, 일정이 하나도 없는 처음");
            App app = Main.create(MockData.sample());
            app.store.say(app.talk.onWindowShown(app.game));
            LocalDate today = app.store.today();
            TaskForm bad = new TaskForm(null, today, app.store.now);
            bad.timed.setSelected(true);
            bad.sync();
            bad.start.setValue("23:30");
            bad.minutes.setValue(40);
            bad.showErrors(TaskCheck.check(bad.draft(), bad.editing()));
            BlindShots.shoot(TaskDialog.content(bad, new JButton("취소"), new JButton("저장"), null), null, "일정 추가",
                    images, key, n++, out, "일정 입력 창(추가), 저장을 누른 뒤 오류");
            Task exercise = app.store.dayPlan(today).untimed().get(1);
            String[] two = {"삭제", "취소"};
            BlindShots.shoot(new JOptionPane("'" + exercise.title + "'" + Rules.josa(exercise.title, "을", "를") + " 삭제할까요?",
                    JOptionPane.QUESTION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, two, two[1]), null, "일정 삭제",
                    images, key, n++, out, "일정 삭제 확인 창(반복 아님)");
            Task fixed = app.store.tasks.stream().filter(x -> x.kind == Task.Kind.FIXED).findFirst().orElseThrow();
            TaskForm fixedForm = new TaskForm(fixed, fixed.date, app.store.now);
            fixedForm.showSeries(app.store.seriesEnd(fixed));
            BlindShots.shoot(TaskDialog.content(fixedForm, new JButton("취소"),
                    new JButton("저장"), new JButton("삭제")), null, "일정 수정", images, key, n++, out, "일정 입력 창(고정 일정 수정)");
            Store rich = MockData.sample();
            rich.points = 300;
            BlindShots.shoot(ShopDialog.content(rich, item -> { }, new JButton("닫기")), null, "상점", images, key, n++, out,
                    "상점 창(300P, 살 수 있는 것 있음)");
            app.store.add(Task.Kind.TASK, "기말 프로젝트", today, null, 600, "", null);
            LocalDate monday = Store.mondayOf(today);
            BlindShots.shoot(AutoPlaceDialog.content(app.store, monday, Planner.place(app.store, monday), new JButton("취소"),
                    new JButton("적용")), null, "자동 배치 결과", images, key, n++, out, "자동 배치 결과 창(놓지 못한 할 일 있음)");
            BlindShots.shoot(menuOver(MockData.sample()), new Dimension(1000, 680), null, images, key, n++, out,
                    "홈, 할 일 줄에서 오른쪽 버튼을 누른 뒤");
            Task untimed = app.store.dayPlan(today).untimed().get(0);
            BlindShots.shoot(new TimeEditor(untimed, app.store.now).panel, null, null, images, key, n++, out,
                    "시각 고치기 창(시각 없는 할 일)");
        }
        System.out.println("그렸다: " + images.getPath() + ", 열쇠: " + key.getPath());
        System.exit(0);
    }

    /** 홈 그림 위, 둘째 할 일 줄 자리에 오른쪽 버튼 메뉴를 얹는다. 홈 그림은 눌러지지 않는 그림이다. */
    private static JLayeredPane menuOver(Store s) throws Exception {
        App app = Main.create(s);
        app.store.say(app.talk.onWindowShown(app.game));
        app.show("홈");
        app.store.changed();
        Dimension size = new Dimension(1000, 680);
        File tmp = File.createTempFile("home", ".png");
        Snap.save(app.root, size, tmp);
        BufferedImage home = javax.imageio.ImageIO.read(tmp);
        tmp.delete();
        JLayeredPane pane = new JLayeredPane();
        pane.setPreferredSize(size);
        JLabel picture = new JLabel(new ImageIcon(home));
        picture.setBounds(0, 0, size.width, size.height);
        pane.add(picture, JLayeredPane.DEFAULT_LAYER);
        Task essay = s.dayPlan(s.today()).timed().get(1);
        // 창 없이는 팝업을 띄울 수 없어서, 메뉴의 항목(같은 부품, 같은 동작)을 메뉴 모양 판에 옮겨 담는다.
        JPopupMenu menu = TaskMenu.of(app.actions, essay);
        javax.swing.JPanel look = new javax.swing.JPanel();
        look.setLayout(new javax.swing.BoxLayout(look, javax.swing.BoxLayout.Y_AXIS));
        look.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.GRAY));
        for (java.awt.Component item : menu.getComponents()) {
            look.add(item);
        }
        look.setSize(look.getPreferredSize().width + 40, look.getPreferredSize().height);
        look.setLocation(720, 200);
        pane.add(look, JLayeredPane.POPUP_LAYER);
        return pane;
    }
}
