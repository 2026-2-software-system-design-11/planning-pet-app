package planningpet.schedule.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import planningpet.app.model.AppData;
import planningpet.common.view.CircleButton;
import planningpet.common.view.Icons;
import planningpet.common.view.RoundButtonUI;
import planningpet.common.view.Theme;
import planningpet.common.view.Ui;
import planningpet.reward.controller.RewardController;
import planningpet.schedule.model.DayPlan;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Task;

/** 할 일 줄 목록(07). 홈의 그날 목록과 일정관리 오른쪽 목록이 같이 쓴다. 옮기는 몫은 docs/porting/task-rows.md. */
public final class TaskRows extends JPanel implements Scrollable {
    /** 시각 알약을 누르면 할 일, 그 줄이 보이는 목록의 날, 누른 알약을 받는다. 시각 고치기 창(S5)이 알약 아래에 뜬다. */
    public interface TimeOpener {
        void open(Task t, LocalDate day, Component anchor);
    }

    private final AppData data;
    private final RewardController reward;
    private final Consumer<Task> onEdit;
    private final Consumer<Task> onDelete;
    private final TimeOpener onTime;
    /** "끝낸 일 {수}개" 묶음 제목. 묶음이 없으면 목록에 없다. */
    public final JButton doneHead = new JButton();
    /** D-88: 방금 체크해 아직 제자리에 머무는 일. 1.5초 뒤 "끝낸 일" 묶음으로 내려간다. */
    private final Set<Task> lingering = new HashSet<>();
    /** D-88: 마지막 체크에서 1.5초 뒤 머물던 일을 모두 내려보낸다. 이어서 체크하면 다시 센다. 검사가 값과 리스너를 본다. */
    final Timer settleTimer = new Timer(1500, e -> settle());
    /** D-94: 지난번에 그린 묶음(due, timed, untimed, period)과 그 날. 머무는 줄을 체크하기 전 자리에 두는 데 쓴다. */
    private Map<String, List<Task>> shown = Map.of();
    private LocalDate shownDay;
    /** "끝낸 일" 묶음을 접었는가. 앱을 끌 때까지 남고 저장하지 않는다. */
    private boolean doneFolded;
    /** 접기를 누르거나 머물던 줄을 내려보낸 뒤 목록을 다시 만든다. 홈 화면(MainScreen)이 넣는다. */
    public Runnable resort = () -> { };

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
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        settleTimer.setRepeats(false);
        doneHead.putClientProperty(RoundButtonUI.NAV, Boolean.TRUE);
        doneHead.setFont(Theme.bold(12));
        doneHead.setForeground(Theme.INK_SUB);
        doneHead.setHorizontalAlignment(SwingConstants.LEFT);
        doneHead.setBorder(BorderFactory.createEmptyBorder(14, 6, 4, 0));
        doneHead.setAlignmentX(LEFT_ALIGNMENT);
        doneHead.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        doneHead.addActionListener(e -> {
            doneFolded = !doneFolded;
            resort.run();
        });
    }

    /** D-88: 머물던 끝낸 줄을 "끝낸 일" 묶음으로 내려보낸다(1.5초가 지난 것과 같다). */
    public void settle() {
        settleTimer.stop();
        lingering.clear();
        resort.run();
    }

    /** 체크한 뒤 아직 제자리에 머무는 일이 있는가(D-88). */
    public boolean settling() {
        return !lingering.isEmpty();
    }

    /**
     * 홈 그날 목록(06 6번, D-31): 맨 위 "오늘 마감"인 기간 할 일, 시각 있는 일정을 시각과 함께, 그 아래 "시각 없는 할 일"(D-41), 맨
     * 아래 "기간 안에 할 일"(D-70), 맨 아래 "끝낸 일"(D-88). 일정이 하나도 없는 날은 흐린 안내다(D-33, docs/porting/task-rows.md S3w, S3x).
     */
    public void showPlan(DayPlan plan) {
        removeAll();
        if (plan.isEmpty()) {
            JLabel none = Ui.hint("아직 일정이 없어요. 작은 계획을 추가해 보세요.");
            none.setFont(Theme.font(13));
            none.setForeground(Theme.INK_SUB);
            none.setBorder(BorderFactory.createEmptyBorder(16, 6, 0, 0));
            add(none);
        }
        // D-88: 끝낸 일은 체크한 자리에 잠깐 머문 뒤 맨 아래 "끝낸 일" 묶음으로 내려간다.
        Predicate<Task> down = t -> t.isDone() && !lingering.contains(t);
        Map<String, List<Task>> groups = new LinkedHashMap<>();
        groups.put("due", new ArrayList<>(plan.dueToday()));
        groups.put("timed", new ArrayList<>(plan.timed().stream().filter(down.negate()).toList()));
        groups.put("untimed", new ArrayList<>(plan.untimed().stream().filter(down.negate()).toList()));
        groups.put("period", new ArrayList<>(plan.period()));
        stay(groups, plan.date());
        groups.get("due").forEach(t -> add(row(t, "", null, plan.date())));
        groups.get("timed").forEach(t -> add(row(t, data.getBook().timeText(t), null, plan.date())));
        group("시각 없는 할 일", groups.get("untimed"), plan.date());
        group("기간 안에 할 일", groups.get("period"), plan.date());
        List<Task> done = new ArrayList<>(plan.timed().stream().filter(down).toList());
        done.addAll(plan.untimed().stream().filter(down).toList());
        if (!done.isEmpty()) {
            doneHead.setText("끝낸 일 " + done.size() + "개");
            doneHead.setIcon(fold(doneFolded));
            doneHead.setToolTipText(doneFolded ? "펼치기" : "접기");
            doneHead.getAccessibleContext().setAccessibleName("끝낸 일 " + done.size() + "개 " + (doneFolded ? "펼치기" : "접기"));
            add(doneHead);
            if (!doneFolded) {
                done.forEach(t -> add(row(t, t.onTimetable(data.today()) ? data.getBook().timeText(t) : "", null, plan.date())));
            }
        }
        revalidate();
        repaint();
    }

    /**
     * D-94: 머무는 줄은 체크하기 전에 그린 묶음과 순서 그대로 둔다. 끝낸 기간 할 일은 dayPlan에서 "시각 없는 할 일"로 옮겨 가므로
     * 지난번 묶음으로 돌려놓는다. 그 날 목록에서 빠진 일(다른 날을 보며 끝낸 기간 할 일, D-66)은 그대로 빠진다.
     */
    private void stay(Map<String, List<Task>> groups, LocalDate day) {
        if (day.equals(shownDay)) {
            Set<Task> here = new HashSet<>();
            groups.values().forEach(here::addAll);
            shown.forEach((name, before) -> {
                for (int i = 0; i < before.size(); i++) {
                    Task t = before.get(i);
                    if (!lingering.contains(t) || !here.contains(t)) {
                        continue;
                    }
                    groups.values().forEach(l -> l.remove(t));
                    List<Task> into = groups.get(name);
                    int at = 0;
                    for (int k = i - 1; k >= 0 && at == 0; k--) {
                        at = into.indexOf(before.get(k)) + 1;
                    }
                    into.add(at, t);
                }
            });
        }
        shown = new HashMap<>(groups);
        shownDay = day;
    }

    /** 묶음 제목 앞의 작은 삼각형. 펼쳤으면 아래, 접었으면 오른쪽을 가리킨다. */
    private static Icon fold(boolean folded) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics graphics, int x, int y) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(Theme.INK_SUB);
                if (folded) {
                    g.fillPolygon(new int[] {x + 2, x + 8, x + 2}, new int[] {y + 1, y + 5, y + 9}, 3);
                } else {
                    g.fillPolygon(new int[] {x, x + 10, x + 5}, new int[] {y + 2, y + 2, y + 8}, 3);
                }
                g.dispose();
            }

            @Override
            public int getIconWidth() {
                return 10;
            }

            @Override
            public int getIconHeight() {
                return 10;
            }
        };
    }

    /** 제목 아래에 묶음의 줄. 비면 제목도 없다. */
    private void group(String name, List<Task> tasks, LocalDate day) {
        if (tasks.isEmpty()) {
            return;
        }
        JLabel head = Ui.hint(name);
        head.setFont(Theme.bold(12));
        head.setForeground(Theme.INK_SUB);
        head.setBorder(BorderFactory.createEmptyBorder(14, 6, 4, 0));
        add(head);
        tasks.forEach(t -> add(row(t, "", null, day)));
    }

    /**
     * 제목 칸. 글(getText)은 HTML(화면 읽기 프로그램이 읽는다)이고, 그림은 한 줄로 그려 칸 폭을 넘으면 "…"로 줄인다. HTML 라벨은 좁으면
     * 두 줄로 꺾여 줄 높이를 넘었다. 끝낸 일은 흐린 글에 취소선, 메모가 있으면 끝에 흐린 "메모"다(docs/porting/task-rows.md S3t).
     * 일정관리 목록은 줄 앞 날짜 date("11/14 ")를 줄이지 않고 제목이 그 뒤 남는 폭을 받는다. dim이면 흐린 글이다(D-69, D-94).
     */
    public static final class TitleLabel extends JLabel {
        private final Task t;
        private final String date;
        private final boolean dim;

        TitleLabel(Task t, String date, boolean dim) {
            super("<html>" + date + (t.isDone() ? "<s>" + Ui.esc(t.getTitle()) + "</s>" : Ui.esc(t.getTitle()))
                    + (t.getMemo().isBlank() ? "" : " <font size='2' color='#8A96A0'>메모</font>") + "</html>");
            this.t = t;
            this.date = date;
            this.dim = dim;
            setFont(Theme.font(14));
            setMinimumSize(new Dimension(0, 0));
            setPreferredSize(new Dimension(40, 24));
        }

        private int memoWidth(FontMetrics sm) {
            return t.getMemo().isBlank() ? 0 : sm.stringWidth("메모") + 5;
        }

        /** 지금 폭에서 그리는 제목 글(날짜 뒤, "…"로 줄였으면 그 글). 검사가 읽는다(T-LIST-9). */
        public String shown() {
            FontRenderContext frc = getFontMetrics(getFont()).getFontRenderContext();
            int max = getWidth() - Ui.width(date, getFont(), frc) - memoWidth(getFontMetrics(Theme.font(11)));
            return Ui.fit(t.getTitle(), getFont(), frc, max);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            FontMetrics fm = g.getFontMetrics(getFont());
            String text = shown();
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g.setFont(getFont());
            g.setColor(t.isDone() || dim ? Theme.INK_DIM : Theme.INK);
            g.drawString(date + text, 0, y);
            int dx = Ui.width(date, getFont(), g.getFontRenderContext());
            int tw = dx + Ui.width(text, getFont(), g.getFontRenderContext());
            if (t.isDone()) {
                g.fillRect(dx, y - fm.getAscent() / 3 - 1, tw - dx, 1);
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
     * "마감"(D-70, D-93), 빨간 "지난 날"(D-44, 일정관리 목록) 순이다. 하나도 없으면 null(docs/porting/task-rows.md S3u).
     */
    static JLabel marks(String due, String note, String tag, boolean past) {
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
        part.accept(past ? "지난 날" : null, "#C0392B");
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

    /**
     * 할 일 한 줄. time이 ""이면 "시각 정하기", listDay는 이 줄이 보이는 목록의 날이다(D-65). 순서는 D-59, 아래에 가는 선, 끝낸 줄은
     * 민트빛 바탕. rowDay가 있으면 일정관리 목록 줄이라 줄 앞에 그 날짜를 붙이고 시각 단추는
     * 작은 시계 단추다(time은 쓰지 않는다, D-102, docs/porting/schedule.md S5a2). 홈 목록은 null이다.
     */
    JPanel row(Task t, String time, LocalDate rowDay, LocalDate listDay) {
        boolean withDate = rowDay != null;
        // 마우스를 올린 줄은 DAY 바탕이다(디자인 시스템 10절 홈, docs/porting/task-rows.md S3z).
        boolean[] over = {false};
        JPanel row = new JPanel(new BorderLayout(6, 0)) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (t.isDone() || over[0]) {
                    g.setColor(over[0] ? Theme.DAY : Theme.ROW_DONE);
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
        // D-88: 체크하면 제자리에 머물고 마지막 체크에서 1.5초 뒤 묶음으로 내려간다. 풀면 바로 제자리다.
        box.addActionListener(e -> {
            if (t.isDone()) {
                lingering.remove(t);
                reward.uncomplete(t);
            } else {
                lingering.add(t);
                if (isShowing()) {
                    settleTimer.restart();
                }
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
        LocalDate today = data.today();
        boolean pastPeriod = !t.isDone() && t.period() && listDay.isBefore(today);
        String why = pastPeriod ? "지난 날에는 시각을 정할 수 없어요" : null;
        TimePill when = withDate ? TimePill.icon(t.isDone(), t.isDone() || pastPeriod, why)
                : new TimePill(time, t.isDone(), t.isDone() || pastPeriod, why);
        when.addActionListener(e -> onTime.open(t, listDay, when));
        // D-35: 고정 일정도 체크한다. 체크 옆에 "고정" 꼬리표를 둔다.
        row.add(t.getKind() == Kind.FIXED ? middle(box, fixedTag()) : middle(box), BorderLayout.WEST);
        String date = withDate ? rowDay.getMonthValue() + "/" + rowDay.getDayOfMonth() + " " : "";
        TitleLabel label = new TitleLabel(t, date, false);
        // D-33: 메모가 있으면 제목이나 줄 빈 곳에 마우스를 올렸을 때 보인다.
        String tip = memoTip(t);
        label.setToolTipText(tip);
        row.setToolTipText(tip);
        row.add(label, BorderLayout.CENTER);
        row.add(middle(when, minutes, pencil, trash), BorderLayout.EAST);
        // D-94: 마감 글, 하려던 일, "오늘 마감"과 "마감"은 제목 아래 둘째 줄 왼쪽이다. 제목이 줄의 폭을 먼저 받는다.
        JLabel marks = marks(data.getBook().dueText(t), missedNote(t, today), dueTag(t, listDay, today), false);
        if (marks != null) {
            row.add(marks, BorderLayout.SOUTH);
        }
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, marks == null ? 44 : 62));
        // 줄과 제목 칸을 오른쪽 버튼으로 누르면 수정, 삭제 메뉴다(05 홈 "할 일 고치기와 지우기", docs/porting/task-rows.md S3y).
        // 운영체제마다 누를 때나 뗄 때 팝업 깃발이 서서 둘 다 본다. 빨리 두 번 누르면 수정이다.
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    TaskMenu.of(onEdit, onDelete, t).show(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mousePressed(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // D-102: 한 번 누르면 아무 일도 없고 두 번 누르면 수정이다. 설정 "목록 누르기"(C3) 전에는 꺼진 것으로 둔다.
                if (e.getClickCount() == 2) {
                    onEdit.accept(t);
                }
            }
        };
        label.addMouseListener(mouse);
        row.addMouseListener(mouse);
        MouseAdapter hover = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                over[0] = true;
                row.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // 마우스가 아직 줄 안(자식 위)이면 바탕을 둔다. 창 없는 줄은 보이지 않아 늘 끈다.
                over[0] = row.isShowing() && row.getMousePosition(true) != null;
                row.repaint();
            }
        };
        // 줄과 줄 안 버튼 위에 있는 동안이다. 듣는이가 없는 걸리는 시간, 둘째 줄, "고정" 위의 마우스는 줄이 받는다.
        List.of(row, label, box, pencil, trash, when).forEach(c -> c.addMouseListener(hover));
        return row;
    }

    /** 일정관리 목록의 마감이 지난 줄의 툴팁(D-99). 메모가 있으면 그 아래에 memoTip과 같은 꼴로 잇는다(docs/porting/schedule.md S5a). */
    static String lockedTip(Task t) {
        String why = "마감이 지난 일은 옮길 수 없어요";
        String memo = memoTip(t);
        return memo == null ? why : memo.replace("<div style='width:280px'>", "<div style='width:280px'>" + why + "<br>");
    }

    /** 메모가 있으면 마우스를 올렸을 때 200자까지 보인다(D-33). 글자 수는 코드 포인트로 센다(06 읽는 법). 공백뿐이면 null(docs/porting/task-rows.md S3aa). */
    static String memoTip(Task t) {
        if (t.getMemo().isBlank()) {
            return null;
        }
        String memo = t.getMemo();
        // 프로토타입과 일부러 다름: 프로토타입은 UTF-16 단위로 잘라 200번째 자리의 이모지를 반쪽으로 남겼다(porting.md 4절).
        String m = memo.codePointCount(0, memo.length()) > 200 ? memo.substring(0, memo.offsetByCodePoints(0, 200)) + "…" : memo;
        return "<html><div style='width:280px'>" + Ui.esc(m).replace("\n", "<br>") + "</div></html>";
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

    /** 목록 폭을 보이는 칸에 맞춰서 긴 제목이 버튼을 밀어내지 않게 한다. 제목은 남는 폭에서 줄어든다(D-94). */
    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visible, int orientation, int direction) {
        return 16;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visible, int orientation, int direction) {
        return 64;
    }
}
