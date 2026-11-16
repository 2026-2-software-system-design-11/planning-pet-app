package planningpet.proto;

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
            bad.showErrors(TaskCheck.check(bad));
            BlindShots.shoot(TaskDialog.content(bad, new JButton("취소"), new JButton("저장"), null), null, "일정 추가",
                    images, key, n++, out, "일정 입력 창(추가), 저장을 누른 뒤 오류");
            Task exercise = app.store.dayPlan(today).untimed().get(1);
            String[] two = {"지우기", "취소"};
            BlindShots.shoot(new JOptionPane("'" + exercise.title + "'" + Actions.josa(exercise.title, "을", "를") + " 지울까요?",
                    JOptionPane.QUESTION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, two, two[1]), null, "일정 삭제",
                    images, key, n++, out, "일정 삭제 확인 창(반복 아님)");
            Task fixed = app.store.tasks.stream().filter(x -> x.kind == Task.Kind.FIXED).findFirst().orElseThrow();
            BlindShots.shoot(TaskDialog.content(new TaskForm(fixed, fixed.date, app.store.now), new JButton("취소"),
                    new JButton("저장"), new JButton("삭제")), null, "일정 수정", images, key, n++, out, "일정 입력 창(고정 일정 수정)");
            Store rich = MockData.sample();
            rich.points = 300;
            BlindShots.shoot(ShopDialog.content(rich, item -> { }, new JButton("닫기")), null, "상점", images, key, n++, out,
                    "상점 창(300P, 살 수 있는 것 있음)");
            app.store.add(Task.Kind.TASK, "기말 프로젝트", today, null, 600, "", null);
            LocalDate monday = Store.mondayOf(today);
