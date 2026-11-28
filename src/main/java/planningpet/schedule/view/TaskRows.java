package planningpet.schedule.view;

import java.awt.Dimension;
import java.awt.FontMetrics;
import javax.swing.JLabel;
import javax.swing.JPanel;
import planningpet.common.view.Theme;
import planningpet.common.view.Ui;
import planningpet.schedule.model.Task;

/** 할 일 줄 목록(07). 홈의 그날 목록과 일정관리 오른쪽 목록이 같이 쓴다. 옮기는 몫은 docs/porting/task-rows.md. */
public final class TaskRows extends JPanel {
    /** 제목 칸. 글(getText)은 HTML(화면 읽기 프로그램이 읽는다)이고, 칸 폭을 넘으면 "…"로 줄인다(docs/porting/task-rows.md S3t). */
    public static final class TitleLabel extends JLabel {
        private final Task t;

        TitleLabel(Task t) {
            super("<html>" + Ui.esc(t.getTitle()) + "</html>");
            this.t = t;
            setFont(Theme.font(14));
            setMinimumSize(new Dimension(0, 0));
            setPreferredSize(new Dimension(40, 24));
        }

        /** 지금 폭에서 그리는 제목 글("…"로 줄였으면 그 글). 검사가 읽는다(T-LIST-9). */
        public String shown() {
            FontMetrics fm = getFontMetrics(getFont());
            return Ui.fit(t.getTitle(), fm, getWidth());
        }
    }
}
