package planningpet.proto;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollBar;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

/**
 * 블라인드 예측 검사용 그림. 화면을 창 없이 그리고, 눌러지거나 써지는 부품마다 번호만 찍는다.
 * 부품 이름은 그림에 쓰지 않는다. 번호와 부품의 대응은 그림 폴더 밖의 열쇠 파일에만 둔다.
 * 같은 모양이 되풀이되는 부품(목록 줄, 달력 칸)은 처음 하나에만 번호를 찍는다.
 */
public final class BlindShots {
    /** low가 참이면 번호를 오른쪽 아래 모서리에 찍는다(블록 안 체크박스 번호와 겹치지 않게). */
    private record Mark(int no, Rectangle at, String what, boolean low) {
        Mark(int no, Rectangle at, String what) {
            this(no, at, what, false);
        }
    }

    private final List<Mark> marks = new ArrayList<>();
    private final Set<String> seen = new HashSet<>();

    public static void main(String[] args) throws Exception {
        File images = new File(args[0]);
        File key = new File(args[1]);
        images.mkdirs();
        try (PrintWriter out = new PrintWriter(key, StandardCharsets.UTF_8)) {
            out.println("# 블라인드 그림 열쇠");
            out.println();
            out.println("예측자에게 주지 않는다. 그림 번호와 부품을 잇는다.");
            App app = Main.create(MockData.sample());
            app.store.say(app.talk.onWindowShown(app.game));
            int n = 1;
            for (String screen : new String[] {"홈", "일정관리", "pet", "설정"}) {
                app.show(screen);
                shoot(app.root, new Dimension(1000, 680), null, images, key, n++, out, screen);
            }
            LocalDate monday = Store.mondayOf(app.store.today());
            shoot(TaskDialog.content(new TaskForm(null, app.store.today(), app.store.now), new JButton("취소"),
                    new JButton("저장"), null), null, "일정 추가", images, key, n++, out, "일정 입력 창(추가)");
            Task essay = app.store.dayPlan(app.store.today()).timed().get(1);
            shoot(TaskDialog.content(new TaskForm(essay, essay.date, app.store.now), new JButton("취소"),
                    new JButton("저장"), new JButton("삭제")), null, "일정 수정", images, key, n++, out, "일정 입력 창(수정)");
            shoot(new TimeEditor(essay, app.store.now).panel, null, null, images, key, n++, out, "시각 고치기 창");
            shoot(AutoPlaceDialog.content(app.store, monday, Planner.place(app.store, monday), new JButton("취소"),
                    new JButton("적용")), null, "자동 배치 결과", images, key, n++, out, "자동 배치 결과 창");
            shoot(ShopDialog.content(app.store, item -> { }, new JButton("닫기")), null, "상점", images, key, n++, out,
                    "상점 창");
            shoot(new PickDialog().panel, null, "플래닝펫 시작", images, key, n++, out, "반려동물 고르기 창");
            // Actions.delete가 반복 일정에 띄우는 확인 창과 같은 글과 버튼이다.
            Task series = app.store.tasks.stream().filter(x -> x.seriesId != null).findFirst().orElseThrow();
            String[] options = {"이 일정만", "이후 반복 모두", "취소"};
            javax.swing.JOptionPane ask = new javax.swing.JOptionPane("'" + series.title + "'"
                    + Actions.josa(series.title, "은", "는") + " 매주 반복 일정이에요.",
                    javax.swing.JOptionPane.QUESTION_MESSAGE, javax.swing.JOptionPane.DEFAULT_OPTION, null, options,
                    options[2]);
            shoot(ask, null, "일정 삭제", images, key, n++, out, "일정 삭제 확인 창(반복 일정)");
        }
        System.out.println("그렸다: " + images.getPath() + ", 열쇠: " + key.getPath());
        System.exit(0);
    }

    private static void shoot(JComponent c, Dimension size, String title, File images, File key, int n, PrintWriter out,
            String name) throws Exception {
        Dimension d = size != null ? size : c.getPreferredSize();
        c.setSize(d);
        layout(c);
        layout(c);
        morning(c);
        BufferedImage body = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = body.createGraphics();
