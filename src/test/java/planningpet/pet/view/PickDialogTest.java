package planningpet.pet.view;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultButtonModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

    /** 글을 모두 비우고 두 카드를 글 없는 카드로 바꿔 끼운 패널을 기준 그림과 맞춘다. */
    private static void looksLikePrototype(String name, int w, int h) throws IOException {
        PickDialog d = new PickDialog();
        Container row = d.cat.getParent();
        row.removeAll();
        row.add(new PickCard("", PetKind.CAT, true, ""));
        row.add(new PickCard("", PetKind.DOG, false, ""));
        d.panel.remove(1);
        d.panel.add(new PickDialog.Logo("", ""), 1);
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
        assertEquals(7, d.panel.getComponentCount());
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

    @Test
    void eDlg3DefaultSize() throws IOException {
        assertEquals(new Dimension(800, 416), new PickDialog().panel.getPreferredSize());
        looksLikePrototype("800x416", 800, 416);
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
    void eDlg9Subtitle() {
        JPanel line = (JPanel) new PickDialog().panel.getComponent(3);
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
        assertEquals(new Dimension(1000, 421), d.panel.getPreferredSize());
    }
}
