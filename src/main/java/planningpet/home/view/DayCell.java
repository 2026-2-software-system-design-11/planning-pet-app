package planningpet.home.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.function.Consumer;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import planningpet.common.view.Theme;
import planningpet.common.view.Ui;
import planningpet.schedule.model.CellPreview;

/**
 * 홈 달력의 날짜 칸 하나(05 홈 "달력", D-86, 06 26번). 프로토타입 CalendarPanel.cell의 칸이다(docs/porting/home.md S3e).
 * 글(getText)은 숫자, 일의 수, 마감 표시의 HTML이고(QA와 화면 읽기 프로그램이 읽는다), 그림은 손수 그린다: 숫자와 그 아래 "{수}개",
 * 마감 날이면 숫자 오른쪽 위 빨간 점(D-86). 마감 날에 마우스를 올리면 "{제목} 마감"을 쉼표로 이어 보인다.
 * 오늘은 DAY_TODAY 둥근 네모, 고른 날은 MINT 테두리, 마우스를 올리면 DAY다.
 */
final class DayCell extends JLabel {
    /** 마감 날 빨간 점. 목록의 "오늘 마감"과 같은 색이다(디자인 시스템 7절). */
    private static final Color DUE = new Color(0xC0392B);
    private final LocalDate date;
    private final CellPreview preview;
    private final boolean today;
    private final boolean picked;
    private boolean over;
    /** 기준 그림을 글 없이 그릴 때만 켠다. 숫자와 수를 빈 글로 그리고 자리는 그대로 잰다. */
    private boolean blank;

    DayCell(LocalDate date, CellPreview preview, boolean today, boolean picked, Consumer<LocalDate> onPick) {
        super("", SwingConstants.CENTER);
        this.date = date;
        this.preview = preview;
        this.today = today;
        this.picked = picked;
        String day = picked ? "<b>" + date.getDayOfMonth() + "</b>" : String.valueOf(date.getDayOfMonth());
        String count = preview.text();
        setText("<html><center>" + day + (count.isEmpty() ? "" : "<br><font size='2' color='"
                + (preview.open() ? "#28977F" : "#8A96A0") + "'>" + count + "</font>")
                + (preview.due().isEmpty() ? "" : " <font size='2' color='#C0392B'>마감</font>") + "</center></html>");
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (!preview.due().isEmpty()) {
            setToolTipText(String.join(", ", preview.due().stream().map(t -> t + " 마감").toList()));
        }
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                over = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                over = false;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                onPick.accept(date);
            }
        });
    }

    /** 기준 그림과 맞출 때 운영체제마다 다른 글자 픽셀을 빼고 그린다(검사용). */
    void blankText() {
        blank = true;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        int r = 12;
        if (today || over) {
            g.setColor(today ? Theme.DAY_TODAY : Theme.DAY);
            g.fillRoundRect(2, 1, w - 4, h - 2, r, r);
        }
        if (picked) {
            g.setColor(Theme.MINT);
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(2, 1, w - 5, h - 3, r, r);
        }
        g.setFont(today || picked ? Theme.bold(14) : Theme.font(14));
        FontMetrics fm = g.getFontMetrics();
        String count = preview.text();
        String n = blank ? "" : String.valueOf(date.getDayOfMonth());
        int numY = count.isEmpty() ? (h + fm.getAscent() - fm.getDescent()) / 2 : h / 2 - 1;
        int dow = date.getDayOfWeek().getValue() % 7;
        g.setColor(dow == 0 ? Theme.SUNDAY : dow == 6 ? Theme.SATURDAY : Theme.INK);
        g.drawString(n, (w - fm.stringWidth(n)) / 2, numY);
        if (!preview.due().isEmpty()) {
            g.setColor(DUE);
            g.fillOval((w + fm.stringWidth(n)) / 2 + 1, numY - fm.getAscent() + 1, 6, 6);
        }
        if (!count.isEmpty()) {
            g.setFont(Theme.font(11));
            FontMetrics pf = g.getFontMetrics();
            String p = blank ? "" : Ui.fit(count, pf, w - 6);
            g.setColor(preview.open() ? Theme.TASK_TEXT : Theme.INK_DIM);
            g.drawString(p, (w - pf.stringWidth(p)) / 2, numY + 3 + pf.getAscent());
        }
        g.dispose();
    }
}
