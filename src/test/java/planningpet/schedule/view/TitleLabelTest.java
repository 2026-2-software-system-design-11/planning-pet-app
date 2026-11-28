package planningpet.schedule.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import planningpet.common.view.Theme;
import planningpet.schedule.model.Category;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Task;

/**
 * 목록 줄의 제목 칸. 번호는 docs/porting/task-rows.md S3t 2절. 그림은 모두 글이라 기준 그림 대신 같은 글꼴로 정한 자리에 그린 그림과
 * 견준다.
 */
class TitleLabelTest {
    static final String LONG = "졸업 작품 기획서 마무리하고 교수님께 메일 보내기";
    private static final LocalDate DAY = LocalDate.of(2026, 11, 16);

    static TaskRows.TitleLabel label(String title, String memo, boolean done, int w, int h) {
        Task t = new Task("a", 1, Kind.TASK, title, DAY, null, 30, memo, false, Category.OTHER, null, null, null, null, done,
                done ? DAY : null, 0, 0, null, null);
        TaskRows.TitleLabel l = new TaskRows.TitleLabel(t);
        l.setSize(w, h);
        return l;
    }

    /** 칸이 text를 그리는지, 그림이 text를 (0, y)에, strike가 0 이상이면 그 줄에 취소선을, memo면 그 뒤 5에 "메모"를 그린 것과 같은지 본다. */
    static void draws(TaskRows.TitleLabel l, String text, int y, int strike, boolean memo) {
        assertEquals(text, l.shown());
        int w = l.getWidth();
        int h = l.getHeight();
        BufferedImage got = new BufferedImage(w + 4, h + 4, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = got.createGraphics();
        l.paint(g);
        g.dispose();
        BufferedImage want = new BufferedImage(w + 4, h + 4, BufferedImage.TYPE_INT_ARGB);
        Graphics2D e = want.createGraphics();
        e.clipRect(0, 0, w, h);
        e.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        e.setFont(Theme.font(14));
        e.setColor(strike >= 0 ? Theme.INK_DIM : Theme.INK);
        e.drawString(text, 0, y);
        int tw = e.getFontMetrics().stringWidth(text);
        if (strike >= 0) {
            e.fillRect(0, strike, tw, 1);
        }
        if (memo) {
            e.setFont(Theme.font(11));
            e.setColor(Theme.INK_DIM);
            e.drawString("메모", tw + 5, y);
        }
        e.dispose();
        int differ = 0;
        for (int yy = 0; yy < got.getHeight(); yy++) {
            for (int x = 0; x < got.getWidth(); x++) {
                differ += got.getRGB(x, yy) == want.getRGB(x, yy) ? 0 : 1;
            }
        }
        assertEquals(0, differ, text + "에서 다른 픽셀 수");
    }

    @Test
    void eTitle1Fits() {
        TaskRows.TitleLabel l = label("토익 공부", "", false, 200, 24);
        draws(l, "토익 공부", 17, -1, false);
        assertEquals("<html>토익 공부</html>", l.getText());
        assertEquals(new Dimension(40, 24), l.getPreferredSize());
        assertEquals(new Dimension(0, 0), l.getMinimumSize());
        assertEquals(Theme.font(14), l.getFont());
        assertFalse(l.isOpaque());
    }

    @Test
    void eTitle2Long() {
        draws(label(LONG, "", false, 120, 24), "졸업 작품 기획서 마…", 17, -1, false);
        TaskRows.TitleLabel wide = label(LONG, "", false, 600, 24);
        draws(wide, LONG, 17, -1, false);
        assertEquals("<html>" + LONG + "</html>", wide.getText());
    }

    @Test
    void eTitle3Memo() {
        TaskRows.TitleLabel l = label("토익 공부", "단어 50개", false, 200, 24);
        draws(l, "토익 공부", 17, -1, true);
        assertEquals("<html>토익 공부 <font size='2' color='#8A96A0'>메모</font></html>", l.getText());
        draws(label(LONG, "단어 50개", false, 120, 24), "졸업 작품 기획…", 17, -1, true);
    }

    @Test
    void eTitle4BlankMemo() {
        TaskRows.TitleLabel l = label("토익 공부", "   ", false, 200, 24);
        draws(l, "토익 공부", 17, -1, false);
        assertEquals("<html>토익 공부</html>", l.getText());
    }

    @Test
    void eTitle6Html() {
        TaskRows.TitleLabel l = label("<b>A&B</b>", "", false, 200, 24);
        draws(l, "<b>A&B</b>", 17, -1, false);
        assertEquals("<html>&lt;b&gt;A&amp;B&lt;/b&gt;</html>", l.getText());
    }

    @Test
    void eTitle7Narrow() {
        draws(label(LONG, "", false, 20, 24), "졸…", 17, -1, false);
        draws(label(LONG, "", false, 0, 24), "졸…", 17, -1, false);
        draws(label(LONG, "m", false, 10, 24), "졸…", 17, -1, true);
        draws(label("가", "", false, 5, 24), "가…", 17, -1, false);
        Task t = new Task("a", 1, Kind.TASK, "토익 공부", DAY, null, 30, "", false, Category.OTHER, null, null, null, null, false, null,
                0, 0, null, null);
        assertEquals("토…", new TaskRows.TitleLabel(t).shown());
    }
}
