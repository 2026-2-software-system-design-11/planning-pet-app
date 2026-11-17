package planningpet.qa;

import planningpet.app.*;
import planningpet.app.model.*;
import planningpet.app.view.App;
import planningpet.common.view.Theme;
import planningpet.pet.view.PickDialog;
import planningpet.common.model.Rules;
import planningpet.schedule.model.*;
import planningpet.schedule.view.*;
import planningpet.shop.view.ShopDialog;
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
            app.control.windowShown();
            int n = 1;
            for (String screen : new String[] {"홈", "일정관리", "pet", "설정"}) {
                app.show(screen);
                shoot(app.root, new Dimension(1000, 680), null, images, key, n++, out, screen);
            }
            LocalDate monday = TaskBook.mondayOf(app.store.today());
            shoot(TaskDialog.content(new TaskForm(null, app.store.today(), app.store.now), new JButton("취소"),
                    new JButton("저장"), null), null, "일정 추가", images, key, n++, out, "일정 입력 창(추가)");
            Task essay = app.store.book.dayPlan(app.store.today()).timed().get(1);
            shoot(TaskDialog.content(new TaskForm(essay, essay.date, app.store.now), new JButton("취소"),
                    new JButton("저장"), new JButton("삭제")), null, "일정 수정", images, key, n++, out, "일정 입력 창(수정)");
            shoot(new TimeEditor(essay, app.store.now).panel, null, null, images, key, n++, out, "시각 고치기 창");
            shoot(AutoPlaceDialog.content(app.store, monday, Planner.place(app.store, monday), new JButton("취소"),
                    new JButton("적용")), null, "자동 배치 결과", images, key, n++, out, "자동 배치 결과 창");
            shoot(ShopDialog.content(app.store, item -> { }, new JButton("닫기")), null, "상점", images, key, n++, out,
                    "상점 창");
            shoot(new PickDialog().panel, null, "플래닝펫 시작", images, key, n++, out, "반려동물 고르기 창");
            // TaskController.delete가 반복 일정에 띄우는 확인 창과 같은 글과 버튼이다.
            Task series = app.store.tasks.stream().filter(x -> x.seriesId != null).findFirst().orElseThrow();
            String[] options = {"이 일정만", "이후 반복 모두", "취소"};
            javax.swing.JOptionPane ask = new javax.swing.JOptionPane("'" + series.title + "'"
                    + Rules.josa(series.title, "은", "는") + " 매주 반복 일정이에요.",
                    javax.swing.JOptionPane.QUESTION_MESSAGE, javax.swing.JOptionPane.DEFAULT_OPTION, null, options,
                    options[2]);
            shoot(ask, null, "일정 삭제", images, key, n++, out, "일정 삭제 확인 창(반복 일정)");
        }
        System.out.println("그렸다: " + images.getPath() + ", 열쇠: " + key.getPath());
        System.exit(0);
    }

    static void shoot(JComponent c, Dimension size, String title, File images, File key, int n, PrintWriter out,
            String name) throws Exception {
        Dimension d = size != null ? size : c.getPreferredSize();
        c.setSize(d);
        layout(c);
        layout(c);
        morning(c);
        BufferedImage body = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = body.createGraphics();
        c.paint(g);
        g.dispose();
        BlindShots b = new BlindShots();
        b.walk(c, c);
        int top = title == null ? 0 : 28;
        BufferedImage img = new BufferedImage(d.width, d.height + top, BufferedImage.TYPE_INT_RGB);
        g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if (title != null) {
            // 운영체제가 그리는 창 제목 줄을 흉내 낸다. 사용자가 보는 단서이기 때문이다.
            g.setColor(new Color(0xE6E6E6));
            g.fillRect(0, 0, d.width, top);
            g.setColor(new Color(0x333333));
            g.setFont(Theme.font(13));
            int w = g.getFontMetrics().stringWidth(title);
            g.drawString(title, (d.width - w) / 2, 19);
        }
        g.drawImage(body, 0, top, null);
        String file = String.format("screen-%02d.png", n);
        ImageIO.write(img, "png", new File(images, file));
        // 번호 그림: 번호가 글자를 가리므로 깨끗한 그림을 같이 준다.
        g.setColor(new Color(0xD7263D));
        g.setStroke(new BasicStroke(1.5f));
        for (Mark m : b.marks) {
            g.drawRect(m.at.x, m.at.y + top, m.at.width, m.at.height);
        }
        for (Mark m : b.marks) {
            if (m.low) {
                // 블록 안 오른쪽 아래. 옆 블록 경계에 걸리지 않게 한다.
                badge(g, m.no, m.at.x + m.at.width - 2, m.at.y + m.at.height + top - 4, d.width, d.height + top);
            } else {
                badge(g, m.no, m.at.x, m.at.y + top, d.width, d.height + top);
            }
        }
        g.dispose();
        ImageIO.write(img, "png", new File(images, String.format("screen-%02d-numbered.png", n)));
        out.println();
        out.println("## " + file + " " + name);
        out.println();
        out.println("| 번호 | 부품 |");
        out.println("|---|---|");
        for (Mark m : b.marks) {
            out.println("| " + m.no + " | " + m.what.replace("|", "/") + " |");
        }
    }

    private void walk(Component c, JComponent root) {
        if (!c.isShowing() && !c.isVisible() || c.getWidth() == 0 || c instanceof JScrollBar) {
            return;
        }
        if (c instanceof TimetablePanel t) {
            timetable(t, root);
            return;
        }
        String what = interactive(c);
        // 줄 묶음에 마우스가 걸려 있어도 안에 누를 것이 있으면 안쪽 것만 찍는다.
        if (what != null && (c.getClass() == javax.swing.JPanel.class) && hasInner(c)) {
            what = null;
        }
        if (what != null) {
            add(c, root, what);
            if (c instanceof JSpinner || c instanceof JComboBox || c instanceof AbstractButton) {
                return;
            }
        }
        if (c instanceof Container k) {
            for (Component child : k.getComponents()) {
                walk(child, root);
            }
        }
    }

    private static boolean hasInner(Component c) {
        if (c instanceof Container k) {
            for (Component child : k.getComponents()) {
                if (child.isVisible() && (interactive(child) != null || hasInner(child))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 목록 줄, 달력 칸, 상점 표처럼 같은 모양이 되풀이되는 곳 안이면, 되풀이 단위 안에서의 자리를 돌려준다.
     * 아니면 null이다. 자리는 단위에서 부품까지 내려가는 순번이다.
     */
    private static String slot(Component c) {
        String path = "";
        for (Component k = c; k.getParent() != null; k = k.getParent()) {
            Container p = k.getParent();
            if (p instanceof TaskRows
                    || p instanceof javax.swing.JPanel q && q.getLayout() instanceof java.awt.GridLayout l
                            && (l.getColumns() == 4 || l.getColumns() == 7)) {
                return path;
            }
            path = "/" + p.getComponentZOrder(k) + path;
        }
        return null;
    }

    /** 사람이 누르거나 쓸 수 있으면 부품 설명을, 아니면 null을 돌려준다. */
    private static String interactive(Component c) {
        String text = c instanceof AbstractButton b ? b.getText() : c instanceof JLabel l ? Qa.plain(l.getText()) : "";
        String kind = c.getClass().getSimpleName().isEmpty() ? c.getClass().getSuperclass().getSimpleName()
                : c.getClass().getSimpleName();
        String state = c.isEnabled() ? "" : " 비활성";
        if (c instanceof AbstractButton || c instanceof JSpinner || c instanceof JComboBox || c instanceof JTable
                || c instanceof JList || c instanceof JTextComponent tc && tc.isEditable()) {
            return kind + " \"" + text + "\"" + state;
        }
        if (c instanceof JComponent j && (c.getMouseListeners().length > 0 && !(c instanceof JTextComponent))) {
            String tip = j.getToolTipText();
            return kind + " \"" + text + "\"" + (tip != null ? " (마우스를 올리면 설명)" : "") + state;
        }
        return null;
    }

    /** 같은 모양이 되풀이되면 처음 하나만 찍는다. 글자의 숫자와 한글은 모양으로 바꿔 비교한다. */
    private void add(Component c, JComponent root, String what) {
        String slot = slot(c);
        if (slot != null) {
            String shape = c instanceof AbstractButton && !(c instanceof javax.swing.JCheckBox) ? what : what.replaceAll("[0-9]+", "#").replaceAll("[가-힣][가-힣# …~:]*", "가");
            boolean tip = c instanceof JComponent j && j.getToolTipText() != null;
            if (!seen.add(c.getClass().getName() + shape + "@" + slot + (tip ? " 설명" : ""))) {
                return;
            }
        }
        Point p = SwingUtilities.convertPoint(c, 0, 0, root);
        Rectangle visible = c instanceof JComponent j ? j.getVisibleRect() : new Rectangle(c.getSize());
        if (visible.isEmpty()) {
            return;
        }
        marks.add(new Mark(marks.size() + 1, new Rectangle(p.x + visible.x, p.y + visible.y, visible.width,
                visible.height), what));
    }

    /** 시간표 블록은 부품이 아니라 그림이라 블록 자리와 체크박스 자리를 따로 찍는다. */
    private void timetable(TimetablePanel t, JComponent root) {
        Rectangle view = t.getVisibleRect();
        for (Map.Entry<Rectangle, Task> e : t.boxAreas().entrySet()) {
            Task task = e.getValue();
            String kind = (task.kind == Task.Kind.FIXED ? "고정 일정" : "할 일") + (task.done ? ", 끝냄" : "");
            if (!view.contains(e.getKey()) || !seen.add("box " + kind)) {
                continue;
            }
            Rectangle r = SwingUtilities.convertRectangle(t, e.getKey(), root);
            marks.add(new Mark(marks.size() + 1, r,
                    "시간표 블록의 체크박스 (" + kind + ", " + task.title + ")"));
        }
        Set<String> blocks = new HashSet<>();
        for (Map.Entry<Rectangle, Task> e : t.blockAreas().entrySet()) {
            Task task = e.getValue();
            String kind = (task.kind == Task.Kind.FIXED ? "고정 일정" : "할 일") + (task.done ? ", 끝냄" : "");
            if (!view.contains(e.getKey().getLocation()) || !blocks.add(kind)) {
                continue;
            }
            Rectangle r = SwingUtilities.convertRectangle(t, e.getKey(), root);
            marks.add(new Mark(marks.size() + 1, r, "시간표 블록 (" + kind + ", " + task.title + ")", true));
        }
        Rectangle empty = SwingUtilities.convertRectangle(t, new Rectangle(TimetablePanel.GUTTER,
                16 * TimetablePanel.HOUR, (t.getWidth() - TimetablePanel.GUTTER) / 7, TimetablePanel.HOUR), root);
        marks.add(new Mark(marks.size() + 1, empty, "시간표의 빈 칸"));
    }

    private static void badge(Graphics2D g, int no, int x, int y, int maxW, int maxH) {
        String s = String.valueOf(no);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        int w = Math.max(16, g.getFontMetrics().stringWidth(s) + 8);
        int bx = Math.max(0, Math.min(x - w + 4, maxW - w));
        int by = Math.max(0, Math.min(y - 12, maxH - 16));
        g.setColor(new Color(0xD7263D));
        g.fillOval(bx, by, w, 16);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(bx, by, w, 16);
        g.drawString(s, bx + (w - g.getFontMetrics().stringWidth(s)) / 2, by + 12);
    }

    private static void layout(Component c) {
        c.doLayout();
        if (c instanceof Container k) {
            for (Component child : k.getComponents()) {
                layout(child);
            }
        }
    }

    private static void morning(Component c) {
        if (c instanceof javax.swing.JScrollPane s && s.getViewport().getView() instanceof TimetablePanel) {
            s.getViewport().setViewPosition(new Point(0, 8 * TimetablePanel.HOUR));
        } else if (c instanceof Container k) {
            for (Component child : k.getComponents()) {
                morning(child);
            }
        }
    }
}
