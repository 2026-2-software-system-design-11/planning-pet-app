package planningpet.pet.view;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultButtonModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import planningpet.common.view.RoundButtonUI;
import planningpet.common.view.RoundField;
import planningpet.common.view.Snap;
import planningpet.common.view.Theme;
import planningpet.pet.model.PetKind;

/**
 * 반려동물 고르기 창의 내용. 번호는 docs/porting/pick.md 2절. 기준 그림(proto/pickdlg-*.png)은 글을 모두 비우고 두 카드를
 * 글 없는 카드로 바꿔 끼워 그렸다. 글이 든 부품의 자리와 크기는 숫자로 맞춘다.
 */
class PickDialogTest {
    private static final String CAT_ABOUT = "조용하고 편안한 성격으로\n언제나 곁에서 응원해요!";
    private static final String DOG_ABOUT = "활발하고 다정한 성격으로\n항상 힘이 되어줘요!";

    @BeforeEach
    void install() {
        Theme.install();
    }

    @AfterEach
    void undoInstall() {
        for (Object key : List.copyOf(UIManager.getLookAndFeelDefaults().keySet())) {
            UIManager.put(key, null);
        }
        UIManager.put("swing.boldMetal", null);
    }

    /** 카드를 그린 픽셀. 같은 맥에서 두 카드를 견주므로 글이 들어도 된다. */
    private static int[] pixels(PickCard c) {
        c.setSize(PickCard.W, PickCard.H);
        BufferedImage img = new BufferedImage(PickCard.W, PickCard.H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        c.paint(g);
        g.dispose();
        return img.getRGB(0, 0, PickCard.W, PickCard.H, null, 0, PickCard.W);
    }

    /** 이름 칸을 예시 글과 글자 수가 없는 같은 글꼴과 열의 칸으로 바꿔 끼운다. */
    private static void plainName(RoundField name) {
        Container bar = name.getParent();
        GridBagConstraints at = ((GridBagLayout) bar.getLayout()).getConstraints(name);
        RoundField plain = new RoundField(null, null);
        plain.setFont(name.getFont());
        plain.setColumns(12);
        bar.remove(name);
        bar.add(plain, at, 1);
    }

    /** 글을 모두 비우고 두 카드를 글 없는 카드로, 이름 칸을 plainName으로 바꿔 끼운 패널을 기준 그림과 맞춘다. */
    private static void looksLikePrototype(String name, int w, int h) throws IOException {
        PickDialog d = new PickDialog();
        Container row = d.cat.getParent();
        row.removeAll();
        row.add(new PickCard("", PetKind.CAT, true, ""));
        row.add(new PickCard("", PetKind.DOG, false, ""));
        d.panel.remove(1);
        d.panel.add(new PickDialog.Logo("", ""), 1);
        plainName(d.name);
        Snap.blank(d.panel);
        Snap.assertLaidOutLooksLikePrototype("pickdlg-" + name, d.panel, w, h);
    }

    @Test
    void eDlg1Parts() {
        PickDialog d = new PickDialog();
        assertArrayEquals(pixels(new PickCard("고양이", PetKind.CAT, true, CAT_ABOUT)), pixels(d.cat));
        assertArrayEquals(pixels(new PickCard("강아지", PetKind.DOG, false, DOG_ABOUT)), pixels(d.dog));
        assertEquals(List.of(true, false), List.of(d.cat.isSelected(), d.dog.isSelected()));
        DefaultButtonModel cat = (DefaultButtonModel) d.cat.getModel();
        assertEquals(cat.getGroup(), ((DefaultButtonModel) d.dog.getModel()).getGroup());
        assertEquals(2, cat.getGroup().getButtonCount());
        assertTrue(d.panel.isOpaque());
        assertEquals(BoxLayout.Y_AXIS, ((BoxLayout) d.panel.getLayout()).getAxis());
        assertEquals(new Insets(16, 0, 22, 0), d.panel.getInsets());
        assertEquals(13, d.panel.getComponentCount());
        JPanel row = (JPanel) d.cat.getParent();
        FlowLayout flow = (FlowLayout) row.getLayout();
        assertEquals(List.of(FlowLayout.CENTER, 20, 0), List.of(flow.getAlignment(), flow.getHgap(), flow.getVgap()));
        assertFalse(row.isOpaque());
        assertEquals(0.5f, row.getAlignmentX());
        assertEquals(262, row.getMaximumSize().height);
    }

    @Test
    void eDlg2Click() {
        PickDialog d = new PickDialog();
        d.dog.doClick();
        assertEquals(List.of(false, true), List.of(d.cat.isSelected(), d.dog.isSelected()));
        d.dog.doClick();
        assertEquals(List.of(false, true), List.of(d.cat.isSelected(), d.dog.isSelected()));
        d.cat.doClick();
        assertEquals(List.of(true, false), List.of(d.cat.isSelected(), d.dog.isSelected()));
        d.cat.doClick();
        assertEquals(List.of(true, false), List.of(d.cat.isSelected(), d.dog.isSelected()));
    }

    /** w x h로 배치한 패널의 줄(빈 곳 빼고)과 그 안 부품의 자리를 위에서 아래로 센다. */
    private static List<Rectangle> places(int w, int h) {
        PickDialog d = new PickDialog();
        Snap.layOut(d.panel, w, h);
        List<Rectangle> all = new ArrayList<>();
        for (Component c : d.panel.getComponents()) {
            if (!(c instanceof Box.Filler)) {
                all.add(c.getBounds());
                for (Component p : c instanceof JPanel k ? k.getComponents() : new Component[0]) {
                    all.add(p.getBounds());
                }
            }
        }
        return all;
    }

    /** x, y, 폭, 높이를 넷씩 묶은 자리들. */
    private static List<Rectangle> rects(int... v) {
        List<Rectangle> all = new ArrayList<>();
        for (int i = 0; i < v.length; i += 4) {
            all.add(new Rectangle(v[i], v[i + 1], v[i + 2], v[i + 3]));
        }
        return all;
    }

    @Test
    void eDlg3DefaultSize() throws IOException {
        PickDialog d = new PickDialog();
        assertEquals(List.of(new Dimension(800, 651), new Dimension(556, 567)),
                List.of(d.panel.getPreferredSize(), d.panel.getMinimumSize()));
        assertEquals(rects(0, 16, 800, 60, 0, 80, 800, 62, 0, 144, 800, 40, 285, 0, 230, 40, 0, 196, 800, 262, 142, 0, 248, 262,
                410, 0, 248, 262, 0, 470, 800, 83, 142, 0, 516, 83, 0, 563, 800, 66, 142, 0, 516, 66), places(800, 651));
        looksLikePrototype("800x651", 800, 651);
    }

    @Test
    void eDlg4BigWindow() throws IOException {
        assertEquals(rects(0, 140, 1200, 60, 0, 204, 1200, 62, 0, 268, 1200, 40, 485, 0, 230, 40, 0, 320, 1200, 262, 342, 0, 248, 262,
                610, 0, 248, 262, 0, 594, 1200, 83, 342, 0, 516, 83, 0, 687, 1200, 66, 342, 0, 516, 66), places(1200, 900));
        looksLikePrototype("1200x900", 1200, 900);
    }

    @Test
    void eDlg5SmallWindows() throws IOException {
        assertEquals(rects(0, 16, 800, 18, 0, 38, 800, 26, 0, 66, 800, 40, 285, 0, 230, 40, 0, 118, 800, 262, 142, 0, 248, 262,
                410, 0, 248, 262, 0, 392, 800, 77, 142, 0, 516, 83, 0, 479, 800, 66, 142, 0, 516, 66), places(800, 300));
        assertEquals(rects(42, 0, 248, 262, 310, 0, 248, 262, 0, 470, 600, 83, 42, 0, 516, 83, 0, 563, 600, 66, 42, 0, 516, 66),
                places(600, 651).subList(5, 11));
        assertEquals(rects(-124, 0, 248, 262, -124, 262, 248, 262, 0, 470, 0, 83, -258, 0, 516, 83, 0, 563, 0, 66, -258, 0, 516, 66),
                places(0, 651).subList(5, 11));
        looksLikePrototype("800x300", 800, 300);
        looksLikePrototype("600x651", 600, 651);
        looksLikePrototype("0x651", 0, 651);
        looksLikePrototype("1x1", 1, 1);
    }

    @Test
    void eDlg6BlankPreferredSize() {
        PickDialog d = new PickDialog();
        Snap.blank(d.panel);
        assertEquals(new Dimension(800, 611), d.panel.getPreferredSize());
    }

    @Test
    void eDlg7Logo() throws IOException {
        JLabel logo = (JLabel) new PickDialog().panel.getComponent(1);
        assertEquals(List.of("플래닝펫 Planning Pet", new Dimension(800, 60), 60, 0.5f, false), List.of(logo.getText(),
                logo.getPreferredSize(), logo.getMaximumSize().height, logo.getAlignmentX(), logo.isOpaque()));
        Snap.assertLaidOutLooksLikePrototype("pickdlg-logo-800x60", new PickDialog.Logo("", ""), 800, 60);
        Snap.assertLaidOutLooksLikePrototype("pickdlg-logo-300x60", new PickDialog.Logo("", ""), 300, 60);
        Snap.assertLaidOutLooksLikePrototype("pickdlg-logo-0x0", new PickDialog.Logo("", ""), 0, 0);
    }

    @Test
    void eDlg8Ribbon() throws IOException {
        JLabel ribbon = (JLabel) new PickDialog().panel.getComponent(3);
        assertEquals(List.of("함께할 반려동물을 골라 주세요", Theme.heavy(21), Color.WHITE, SwingConstants.CENTER, new Dimension(800, 62),
                62, 0.5f, false), List.of(ribbon.getText(), ribbon.getFont(), ribbon.getForeground(), ribbon.getHorizontalAlignment(),
                ribbon.getPreferredSize(), ribbon.getMaximumSize().height, ribbon.getAlignmentX(), ribbon.isOpaque()));
        ribbon.setText("");
        Snap.assertLaidOutLooksLikePrototype("pickdlg-ribbon-800x62", ribbon, 800, 62);
        Snap.assertLaidOutLooksLikePrototype("pickdlg-ribbon-300x62", ribbon, 300, 62);
        Snap.assertLaidOutLooksLikePrototype("pickdlg-ribbon-0x0", ribbon, 0, 0);
        // 공백은 칠하는 픽셀 없이 폭만 있다. 52칸(폭 260)까지 띠는 380이고 53칸(265)부터 넓어진다.
        ribbon.setText(" ".repeat(52));
        assertEquals(260, ribbon.getFontMetrics(ribbon.getFont()).stringWidth(ribbon.getText()));
        Snap.assertLaidOutLooksLikePrototype("pickdlg-ribbon-800x62", ribbon, 800, 62);
        ribbon.setText(" ".repeat(53));
        Snap.assertLaidOutLooksLikePrototype("pickdlg-ribbon-spaces53", ribbon, 800, 62);
    }

    @Test
    void eDlg9Subtitle() {
        JPanel line = (JPanel) new PickDialog().panel.getComponent(5);
        JLabel sub = (JLabel) line.getComponent(0);
        assertEquals(List.of("<html><div style='text-align:center'>선택한 친구와 함께 일정을 관리하고,<br>계획을 실천하며 성장할 수 있어요!"
                + "</div></html>", Theme.font(16), Theme.INK_SUB, SwingConstants.CENTER, new Dimension(230, 40)),
                List.of(sub.getText(), sub.getFont(), sub.getForeground(), sub.getHorizontalAlignment(), sub.getPreferredSize()));
        FlowLayout flow = (FlowLayout) line.getLayout();
        assertEquals(List.of(FlowLayout.CENTER, 0, 0, false, 0.5f, 40), List.of(flow.getAlignment(), flow.getHgap(),
                flow.getVgap(), line.isOpaque(), line.getAlignmentX(), line.getMaximumSize().height));
    }

    @Test
    void eDlg10BackdropWidth() {
        PickDialog d = new PickDialog();
        Dimension wide = new Dimension(1000, 5);
        d.panel.add(new Box.Filler(wide, wide, wide));
        assertEquals(new Dimension(1000, 656), d.panel.getPreferredSize());
        PickDialog narrow = new PickDialog();
        narrow.panel.remove(1);
        assertEquals(800, narrow.panel.getPreferredSize().width);
    }

    @Test
    void eDlg12NameBar() throws IOException {
        PickDialog d = new PickDialog();
        JPanel bar = d.nameBar();
        Snap.layOut(bar, 516, 83);
        assertEquals(List.of(false, new Insets(10, 20, 14, 22), new Dimension(516, 83), new Dimension(516, 83)),
                List.of(bar.isOpaque(), bar.getInsets(), bar.getPreferredSize(), bar.getMaximumSize()));
        JLabel label = (JLabel) bar.getComponent(0);
        JLabel hint = (JLabel) bar.getComponent(2);
        assertEquals(List.of("반려동물의 이름을 지어주세요!", 22, Theme.bold(15), 8, new Insets(0, 0, 0, 6), d.name),
                List.of(label.getText(), label.getIcon().getIconWidth(), label.getFont(), label.getIconTextGap(), label.getInsets(),
                        label.getLabelFor()));
        assertEquals(List.of(Theme.font(14), 12, new Insets(8, 18, 8, 68), "반려동물 이름", "", "0 / 10"), List.of(d.name.getFont(),
                d.name.getColumns(), d.name.getInsets(), d.name.getAccessibleContext().getAccessibleName(), d.name.getText(),
                d.name.countText()));
        assertEquals(List.of("비워 두면 나비, 바둑이로 불러요", new Insets(0, 18, 0, 6)), List.of(hint.getText(), hint.getInsets()));
        assertEquals(rects(20, 19, 216, 22, 252, 10, 242, 40, 252, 54, 169, 15),
                List.of(label.getBounds(), d.name.getBounds(), hint.getBounds()));
        plainName(d.name);
        Snap.blank(bar);
        Snap.assertLaidOutLooksLikePrototype("pickdlg-name-516x83", bar, 516, 83);
    }

    @Test
    void eDlg13NameLimit() {
        RoundField name = new PickDialog().name;
        for (int i = 0; i < 11; i++) {
            name.replaceSelection("가");
        }
        assertEquals(List.of("가".repeat(10), "10 / 10"), List.of(name.getText(), name.countText()));
        name.setText("");
        name.replaceSelection("가나다라마바사아자차카타파");
        assertEquals("가나다라마바사아자차", name.getText());
        name.setText("123456789012");
        assertEquals("1234567890", name.getText());
        // 06 읽는 법대로 코드 포인트로 센다(프로토타입은 UTF-16 단위라 이모지 5개, E-LIMIT-6).
        name.setText("");
        name.replaceSelection("\uD83D\uDE00".repeat(11));
        assertEquals(List.of("\uD83D\uDE00".repeat(10), "10 / 10"), List.of(name.getText(), name.countText()));
    }

    @Test
    void eDlg14HintAndButtons() {
        PickDialog d = new PickDialog();
        Snap.layOut(d.panel, 800, 651);
        JPanel buttons = (JPanel) d.start.getParent();
        JPanel choose = (JPanel) buttons.getParent();
        BorderLayout border = (BorderLayout) choose.getLayout();
        JLabel later = (JLabel) border.getLayoutComponent(BorderLayout.NORTH);
        assertEquals(List.of(0, 6, false, new Dimension(516, 66), 516, d.panel.getComponent(11), buttons), List.of(border.getHgap(),
                border.getVgap(), choose.isOpaque(), choose.getPreferredSize(), choose.getMaximumSize().width, choose.getParent(),
                border.getLayoutComponent(BorderLayout.CENTER)));
        assertEquals(List.of("예시는 나중에 '예시 지우기'로 한 번에 지울 수 있어요", SwingConstants.CENTER, new Insets(0, 6, 0, 6)),
                List.of(later.getText(), later.getHorizontalAlignment(), later.getInsets()));
        FlowLayout flow = (FlowLayout) buttons.getLayout();
        assertEquals(List.of(FlowLayout.CENTER, 10, 0, false, List.of(d.examples, d.start)), List.of(flow.getAlignment(),
                flow.getHgap(), flow.getVgap(), buttons.isOpaque(), List.of(buttons.getComponents())));
        for (JButton b : List.of(d.examples, d.start)) {
            assertEquals(List.of(Theme.bold(16), new Insets(12, 26, 13, 26)), List.of(b.getFont(), b.getInsets()));
        }
        assertEquals(List.of("예시로 둘러보기", "바로 시작", true, 18, 8), List.of(d.examples.getText(), d.start.getText(),
                d.start.getClientProperty(RoundButtonUI.PRIMARY), d.start.getIcon().getIconWidth(), d.start.getIconTextGap()));
        assertEquals(null, d.examples.getClientProperty(RoundButtonUI.PRIMARY));
        assertEquals(rects(0, 0, 516, 15, 0, 21, 516, 45, 107, 0, 154, 45, 271, 0, 138, 45),
                List.of(later.getBounds(), buttons.getBounds(), d.examples.getBounds(), d.start.getBounds()));
    }

    private static List<Object> state(Graphics2D g) {
        return List.of(g.getColor(), g.getStroke(), g.getTransform(), g.getClipBounds(), g.getFont(),
                g.getRenderingHint(RenderingHints.KEY_ANTIALIASING));
    }

    @Test
    void eDlg11CallerGraphicsState() throws ReflectiveOperationException {
        PickDialog d = new PickDialog();
        for (Component c : List.of(d.panel, d.panel.getComponent(1), d.panel.getComponent(3), d.nameBar())) {
            c.setSize(800, 416);
            Graphics2D g = new BufferedImage(810, 430, BufferedImage.TYPE_INT_ARGB).createGraphics();
            g.setColor(Color.RED);
            g.setStroke(new BasicStroke(5));
            g.translate(3, 4);
            g.clipRect(10, 10, 100, 100);
            g.setFont(Theme.font(9));
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            List<Object> before = state(g);
            Method paint = c.getClass().getDeclaredMethod("paintComponent", Graphics.class);
            paint.setAccessible(true);
            paint.invoke(c, g);
            assertEquals(before, state(g));
        }
    }
}
