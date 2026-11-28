package planningpet.schedule.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.time.LocalDate;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import planningpet.app.model.AppData;
import planningpet.common.view.CircleButton;
import planningpet.common.view.Icons;
import planningpet.common.view.Theme;
import planningpet.common.view.Ui;
import planningpet.reward.controller.RewardController;
import planningpet.schedule.model.Task;

/** 할 일 줄 목록(07). 홈의 그날 목록과 일정관리 오른쪽 목록이 같이 쓴다. 옮기는 몫은 docs/porting/task-rows.md. */
public final class TaskRows extends JPanel {
    /** 시각 알약을 누르면 할 일, 그 줄이 보이는 목록의 날, 누른 알약을 받는다. 시각 고치기 창(S5)이 알약 아래에 뜬다. */
    public interface TimeOpener {
        void open(Task t, LocalDate day, Component anchor);
    }

    private final AppData data;
    private final RewardController reward;
    private final Consumer<Task> onEdit;
    private final Consumer<Task> onDelete;
    private final TimeOpener onTime;

    /**
     * 둥근 체크는 reward의 complete와 uncomplete를 부르고, 연필, 휴지통, 시각 알약은 누르면 onEdit, onDelete, onTime에 알린다. 그것이
     * 여는 일정 입력 창(S4), 삭제 확인, 시각 고치기 창(S5)은 그 단위가 잇는다(docs/porting/task-rows.md S3v).
     */
    public TaskRows(AppData data, RewardController reward, Consumer<Task> onEdit, Consumer<Task> onDelete, TimeOpener onTime) {
        this.data = data;
        this.reward = reward;
        this.onEdit = onEdit;
        this.onDelete = onDelete;
        this.onTime = onTime;
    }

    /**
     * 제목 칸. 글(getText)은 HTML(화면 읽기 프로그램이 읽는다)이고, 그림은 한 줄로 그려 칸 폭을 넘으면 "…"로 줄인다. HTML 라벨은 좁으면
     * 두 줄로 꺾여 줄 높이를 넘었다. 끝낸 일은 흐린 글에 취소선, 메모가 있으면 끝에 흐린 "메모"다(docs/porting/task-rows.md S3t).
     */
    public static final class TitleLabel extends JLabel {
        private final Task t;

        TitleLabel(Task t) {
            super("<html>" + (t.isDone() ? "<s>" + Ui.esc(t.getTitle()) + "</s>" : Ui.esc(t.getTitle()))
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
            g.setColor(t.isDone() ? Theme.INK_DIM : Theme.INK);
            g.drawString(text, 0, y);
            int tw = fm.stringWidth(text);
            if (t.isDone()) {
                g.fillRect(0, y - fm.getAscent() / 3 - 1, tw, 1);
            }
            if (!t.getMemo().isBlank()) {
                g.setFont(Theme.font(11));
                g.setColor(Theme.INK_DIM);
                g.drawString("메모", tw + 5, y);
            }
            g.dispose();
        }
    }

    /**
     * 제목 아래 둘째 줄 왼쪽의 표시(D-94). 흐린 마감 글("~11/22까지", D-65)과 하려던 일("17일에 하려던 일", D-67), 빨간 "오늘 마감"이나
     * "마감"(D-70, D-93) 순이다. 하나도 없으면 null(docs/porting/task-rows.md S3u).
     */
    static JLabel marks(String due, String note, String tag) {
        StringBuilder html = new StringBuilder();
        BiConsumer<String, String> part = (text, color) -> {
            if (text != null) {
                html.append(html.length() == 0 ? "" : " ").append("<font color='").append(color).append("'>").append(text)
                        .append("</font>");
            }
        };
        part.accept(due, "#8A96A0");
        part.accept(note, "#8A96A0");
        part.accept(tag, "#C0392B");
        if (html.length() == 0) {
            return null;
        }
        JLabel label = new JLabel("<html>" + html + "</html>");
        label.setFont(Theme.font(12));
        label.setBorder(BorderFactory.createEmptyBorder(0, RoundCheck.SIZE + 10, 0, 0));
        return label;
    }

    /** D-67: 시각을 정한 날에 못 끝낸 기간 할 일이면 "{일}일에 하려던 일"(정한 날의 일), 아니면 null. */
    static String missedNote(Task t, LocalDate today) {
        return t.missed(today) ? t.getDate().getDayOfMonth() + "일에 하려던 일" : null;
    }

    /**
     * D-70, D-93: 끝내지 않은 기간 할 일을 마감 날 목록(listDay)에서 보면, 그날이 오늘이면 "오늘 마감", 앞날이면 "마감"이다. 마감이
     * 지났으면 null이다.
     */
    static String dueTag(Task t, LocalDate listDay, LocalDate today) {
        if (!t.period() || t.isDone() || !t.getDueDate().equals(listDay)) {
            return null;
        }
        return listDay.equals(today) ? "오늘 마감" : listDay.isAfter(today) ? "마감" : null;
    }

    /** 고정 일정 줄의 "고정" 꼬리표(D-35): TRACK 바탕, CARD_LINE 테두리의 작은 둥근 꼬리표다. 부를 때마다 새 라벨이다. */
    static JLabel fixedTag() {
        JLabel tag = new JLabel("고정") {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(Theme.TRACK);
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g.setColor(Theme.CARD_LINE);
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        tag.setFont(Theme.bold(11));
        tag.setForeground(Theme.INK_SUB);
        tag.setBorder(BorderFactory.createEmptyBorder(2, 7, 2, 7));
        return tag;
    }

    /** 홈 목록의 할 일 한 줄. time이 ""이면 "시각 정하기", listDay는 이 줄이 보이는 목록의 날이다(D-65). 순서는 D-59, 아래에 가는 선, 끝낸 줄은 민트빛 바탕. */
    JPanel row(Task t, String time, LocalDate listDay) {
        JPanel row = new JPanel(new BorderLayout(6, 0)) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (t.isDone()) {
                    g.setColor(Theme.ROW_DONE);
                    g.fillRoundRect(0, 1, getWidth(), getHeight() - 3, 12, 12);
                }
                g.setColor(Theme.CARD_EDGE);
                g.fillRect(6, getHeight() - 1, getWidth() - 12, 1);
                g.dispose();
            }
        };
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(5, 6, 6, 4));
        row.setAlignmentX(LEFT_ALIGNMENT);
        JCheckBox box = RoundCheck.style(new JCheckBox());
        box.setSelected(t.isDone());
        box.putClientProperty(Task.class, t);
        box.getAccessibleContext().setAccessibleName((t.isDone() ? "끝냄 취소: " : "끝냄: ") + t.getTitle());
        // UC-07, UC-08: 끝내지 않은 일은 끝내고 끝낸 일은 체크를 푼다(프로토타입 TaskController.check의 갈래).
        box.addActionListener(e -> {
            if (t.isDone()) {
                reward.uncomplete(t);
            } else {
                reward.complete(t);
            }
        });
        JLabel minutes = new JLabel(t.getDurationMinutes() + "분", JLabel.RIGHT);
        minutes.setFont(Theme.font(13));
        minutes.setForeground(t.isDone() ? Theme.INK_DIM : Theme.INK_SUB);
        minutes.setPreferredSize(new Dimension(Math.max(34, minutes.getPreferredSize().width), 20));
        CircleButton pencil = new CircleButton(Icons.Kind.PENCIL, 28, "수정", true);
        pencil.addActionListener(e -> onEdit.accept(t));
        CircleButton trash = new CircleButton(Icons.Kind.TRASH, 28, "삭제", true);
        trash.addActionListener(e -> onDelete.accept(t));
        // D-94: 끝낸 줄과 지난 날 목록의 기간 할 일은 시각을 정하지 않는다.
        boolean pastPeriod = !t.isDone() && t.period() && listDay.isBefore(data.today());
        TimePill when = new TimePill(time, t.isDone(), t.isDone() || pastPeriod, pastPeriod ? "지난 날에는 시각을 정할 수 없어요" : null);
        when.addActionListener(e -> onTime.open(t, listDay, when));
        row.add(middle(box), BorderLayout.WEST);
        row.add(new TitleLabel(t), BorderLayout.CENTER);
        row.add(middle(when, minutes, pencil, trash), BorderLayout.EAST);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        return row;
    }

    /** 부품을 가로로 놓고 줄 높이의 가운데에 맞춘다. 둘째부터는 앞과 4 띄운다. 칸은 불투명이고 앱의 Theme.clear가 투명하게 한다. */
    static JPanel middle(JComponent... parts) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints k = new GridBagConstraints();
        for (int i = 0; i < parts.length; i++) {
            k.gridx = i;
            k.insets = new Insets(0, i == 0 ? 0 : 4, 0, 0);
            p.add(parts[i], k);
        }
        return p;
    }
}
