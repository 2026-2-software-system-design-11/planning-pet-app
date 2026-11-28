package planningpet.schedule.view;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JLabel;
import javax.swing.JPanel;
import planningpet.common.view.Theme;
import planningpet.common.view.Ui;
import planningpet.schedule.model.Task;

/** 할 일 줄 목록(07). 홈의 그날 목록과 일정관리 오른쪽 목록이 같이 쓴다. 옮기는 몫은 docs/porting/task-rows.md. */
public final class TaskRows extends JPanel {
    /**
     * 제목 칸. 글(getText)은 HTML(화면 읽기 프로그램이 읽는다)이고, 그림은 한 줄로 그려 칸 폭을 넘으면 "…"로 줄인다. HTML 라벨은 좁으면
     * 두 줄로 꺾여 줄 높이를 넘었다. 메모가 있으면 끝에 흐린 "메모"다(docs/porting/task-rows.md S3t).
     */
    public static final class TitleLabel extends JLabel {
        private final Task t;

        TitleLabel(Task t) {
            super("<html>" + Ui.esc(t.getTitle())
                    + (t.getMemo().isBlank() ? "" : " <font size='2' color='#8A96A0'>메모</font>") + "</html>");
            this.t = t;
            setFont(Theme.font(14));
            setMinimumSize(new Dimension(0, 0));
            setPreferredSize(new Dimension(40, 24));
        }

        private int memoWidth(FontMetrics sm) {
            return t.getMemo().isBlank() ? 0 : sm.stringWidth("메모") + 5;
        }

        /** 지금 폭에서 그리는 제목 글("…"로 줄였으면 그 글). 검사가 읽는다(T-LIST-9). */
        public String shown() {
            FontMetrics fm = getFontMetrics(getFont());
            return Ui.fit(t.getTitle(), fm, getWidth() - memoWidth(getFontMetrics(Theme.font(11))));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            FontMetrics fm = g.getFontMetrics(getFont());
            String text = shown();
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g.setFont(getFont());
            g.setColor(Theme.INK);
            g.drawString(text, 0, y);
            int tw = fm.stringWidth(text);
            if (!t.getMemo().isBlank()) {
                g.setFont(Theme.font(11));
                g.setColor(Theme.INK_DIM);
                g.drawString("메모", tw + 5, y);
            }
            g.dispose();
        }
    }
}
