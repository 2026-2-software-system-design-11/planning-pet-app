package planningpet.schedule.view;

import planningpet.common.view.*;
import planningpet.schedule.controller.TaskController;
import planningpet.schedule.model.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** 할 일 줄 목록. 메인의 하루 시간표와 일정관리의 시각 없는 할 일 목록이 같이 쓴다. */
public final class TaskRows extends JPanel implements javax.swing.Scrollable {
    private final TaskController control;
    /** D-88: 홈 목록에서 방금 체크해 아직 제자리에 머무는 일. 1.5초 뒤 "끝낸 일" 묶음으로 내려간다. */
    private final java.util.Set<Task> lingering = new java.util.HashSet<>();
    /** D-88: 마지막 체크에서 1.5초 뒤 머물던 일을 모두 내려보낸다. 이어서 체크하면 다시 센다. */
    private final javax.swing.Timer settleTimer = new javax.swing.Timer(1500, e -> settle());
    /** "끝낸 일" 묶음을 접었는가. 앱을 끌 때까지 남고 저장하지 않는다. */
    private boolean doneFolded;
    /** 머물던 줄을 내려보낸 뒤 목록을 다시 만든다. 메인이 넣는다. */
    public Runnable resort = () -> { };
    /** D-94: 지난번에 그린 묶음(due, timed, untimed, period)과 그 날. 머무는 줄을 체크하기 전 자리에 두는 데 쓴다. */
    private java.util.Map<String, List<Task>> shown = java.util.Map.of();
    private java.time.LocalDate shownDay;
    /** D-99: 일정관리 목록 줄을 끌어 놓을 시간표. 일정관리가 넣는다. 없으면 끌 수 없다. */
    TimetablePanel dropOn;
    /** D-99: 끄는 중이라 흐리게 그리는 줄의 일. 끌지 않으면 null. */
    private Task lifted;
    private javax.swing.Timer rolling;
    /** "끝낸 일 {수}개" 묶음 제목. 묶음이 없으면 목록에 없다. */
    public final javax.swing.JButton doneHead = new javax.swing.JButton();

    public TaskRows(TaskController control) {
        this.control = control;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        settleTimer.setRepeats(false);
        doneHead.putClientProperty(RoundButtonUI.NAV, Boolean.TRUE);
        doneHead.setFont(Theme.bold(12));
        doneHead.setForeground(Theme.INK_SUB);
        doneHead.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        doneHead.setBorder(BorderFactory.createEmptyBorder(14, 6, 4, 0));
        doneHead.setAlignmentX(LEFT_ALIGNMENT);
        doneHead.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        doneHead.addActionListener(e -> {
            doneFolded = !doneFolded;
            resort.run();
        });
    }

    /** D-99: 끄는 중이라 흐리게 그리는 줄의 일. 끌지 않으면 null. */
    public Task lifted() {
        return lifted;
    }

    /** 끄는 동안 시간표를 굴리는 타이머(30ms마다). 끌지 않으면 null. */
    public javax.swing.Timer rolling() {
        return rolling;
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
     * 메인(D-31): 맨 위 "오늘 마감"인 기간 할 일, 시각 있는 일정을 시각과 함께, 그 아래 "시각 없는 할 일"(D-41), 맨 아래
     * "기간 안에 할 일"(D-70).
     */
    public void showPlan(TaskBook.DayPlan plan) {
        removeAll();
        if (plan.isEmpty()) {
            JLabel none = Ui.hint("아직 일정이 없어요. 작은 계획을 추가해 보세요.");
            none.setFont(Theme.font(13));
            none.setForeground(Theme.INK_SUB);
            none.setBorder(BorderFactory.createEmptyBorder(16, 6, 0, 0));
            add(none);
        }
        // D-88: 끝낸 일은 체크한 자리에 잠깐 머문 뒤 맨 아래 "끝낸 일" 묶음으로 내려간다.
        java.util.function.Predicate<Task> down = t -> t.done && !lingering.contains(t);
        List<Task> done = new java.util.ArrayList<>();
        java.util.Map<String, List<Task>> groups = new java.util.LinkedHashMap<>();
        groups.put("due", new java.util.ArrayList<>(plan.dueToday()));
        groups.put("timed", new java.util.ArrayList<>(plan.timed().stream().filter(down.negate()).toList()));
        groups.put("untimed", new java.util.ArrayList<>(plan.untimed().stream().filter(down.negate()).toList()));
        groups.put("period", new java.util.ArrayList<>(plan.period()));
        plan.timed().stream().filter(down).forEach(done::add);
        plan.untimed().stream().filter(down).forEach(done::add);
        stay(groups, plan.date());
        groups.get("due").forEach(t -> add(row(t, "", null, plan.date())));
        groups.get("timed").forEach(t -> add(row(t, TaskBook.timeText(t), null, plan.date())));
        group("시각 없는 할 일", groups.get("untimed"), plan.date());
        group("기간 안에 할 일", groups.get("period"), plan.date());
        if (!done.isEmpty()) {
            doneHead.setText("끝낸 일 " + done.size() + "개");
            doneHead.setIcon(fold(doneFolded));
            doneHead.setToolTipText(doneFolded ? "펼치기" : "접기");
            doneHead.getAccessibleContext().setAccessibleName("끝낸 일 " + done.size() + "개 " + (doneFolded ? "펼치기" : "접기"));
            add(doneHead);
            if (!doneFolded) {
                done.forEach(t -> add(row(t, t.onTimetable(control.today()) ? TaskBook.timeText(t) : "", null, plan.date())));
            }
        }
        revalidate();
        repaint();
    }

    /**
     * D-94: 머무는 줄은 체크하기 전에 그린 묶음과 순서 그대로 둔다. 끝낸 기간 할 일은 dayPlan에서 "시각 없는 할 일"로 옮겨 가므로
     * 지난번 묶음으로 돌려놓는다. 그 날 목록에서 빠진 일(다른 날을 보며 끝낸 기간 할 일, D-66)은 그대로 빠진다.
     */
    private void stay(java.util.Map<String, List<Task>> groups, java.time.LocalDate day) {
        if (day.equals(shownDay)) {
            java.util.Set<Task> here = new java.util.HashSet<>();
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
        shown = new java.util.HashMap<>(groups);
        shownDay = day;
    }

    /** 묶음 제목 앞의 작은 삼각형. 펼쳤으면 아래, 접었으면 오른쪽을 가리킨다. */
    private static javax.swing.Icon fold(boolean folded) {
        return new javax.swing.Icon() {
            @Override
            public void paintIcon(java.awt.Component c, java.awt.Graphics graphics, int x, int y) {
                java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
                g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
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
    private void group(String name, List<Task> tasks, java.time.LocalDate day) {
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

    /** 일정관리 오른쪽 목록: 줄의 날짜(그 주에서 보이는 첫날, 기간 할 일의 마감 날 줄은 마감 날, D-65, D-93)를 붙인다. */
    public void show(List<TaskBook.WeekRow> rows) {
        removeAll();
        if (rows.isEmpty()) {
            add(Ui.hint("이번 주에 시각 없는 할 일이 없어요."));
        }
        rows.forEach(r -> add(row(r.task(), null, r.day(), r.day())));
        revalidate();
        repaint();
    }

    /**
     * time이 null이면 시각 알약이 없고(일정관리 목록), ""이면 "시각 정하기" 알약이다. 끝낸 할 일은 제자리에서 회색 취소선이 된다.
     * rowDay가 있으면 줄 앞에 그 날짜를 붙인다(일정관리 목록). listDay는 이 줄이 보이는 목록의 날이다(D-65).
     * 줄 순서는 둥근 체크, (고정 표시), 제목, 시각 알약, 걸리는 시간, 연필, 휴지통이다(D-59).
     * 모양은 2026-11-24 홈 예시의 할 일 줄: 줄 사이 가는 선, 끝낸 줄은 민트빛 바탕, 마우스를 올린 줄은 DAY 바탕.
     */
    private JPanel row(Task t, String time, java.time.LocalDate rowDay, java.time.LocalDate listDay) {
        boolean withDate = rowDay != null;
        boolean[] over = {false};
        JPanel row = new JPanel(new BorderLayout(6, 0)) {
            @Override
            public void paint(java.awt.Graphics graphics) {
                // D-99: 끄는 동안 그 줄은 흐리게 남는다.
                java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
                g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, lifted == t ? 0.4f : 1f));
                super.paint(g);
                g.dispose();
            }

            @Override
            protected void paintComponent(java.awt.Graphics graphics) {
                java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
                g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                if (t.done || over[0]) {
                    g.setColor(over[0] ? Theme.DAY : Theme.ROW_DONE);
                    g.fillRoundRect(0, 1, getWidth(), getHeight() - 3, 12, 12);
                }
                g.setColor(Theme.CARD_EDGE);
                g.fillRect(6, getHeight() - 1, getWidth() - 12, 1);
                g.dispose();
            }

            @Override
            public Dimension getMaximumSize() {
                // 일정관리 목록 줄은 둘째 줄이 한 줄인지 두 줄인지(D-94)에 따라 높이가 바뀐다.
                return withDate ? new Dimension(Integer.MAX_VALUE, getPreferredSize().height) : super.getMaximumSize();
            }
        };
        row.setOpaque(false);
        row.setBorder(withDate ? BorderFactory.createEmptyBorder(3, 6, 3, 4) : BorderFactory.createEmptyBorder(5, 6, 6, 4));
        row.setAlignmentX(LEFT_ALIGNMENT);
        // D-35: 고정 일정도 체크한다. 체크박스 옆에 "고정" 표시를 둔다.
        JCheckBox box = RoundCheck.style(new JCheckBox());
        box.setSelected(t.done);
        box.putClientProperty(Task.class, t);
        box.getAccessibleContext().setAccessibleName((t.done ? "끝냄 취소: " : "끝냄: ") + t.title);
        box.addActionListener(e -> {
            if (!withDate) {
                // D-88: 홈 목록에서 체크하면 제자리에 머물고, 마지막 체크에서 1.5초 뒤 묶음으로 내려간다. 풀면 바로 제자리다.
                if (t.done) {
                    lingering.remove(t);
                } else {
                    lingering.add(t);
                    if (isShowing()) {
                        settleTimer.restart();
                    }
                }
            }
            control.check(t);
        });
        JPanel left = t.kind == Task.Kind.FIXED ? middle(box, fixedTag()) : middle(box);
        String date = withDate ? rowDay.getMonthValue() + "/" + rowDay.getDayOfMonth() + " " : "";
        String memoMark = t.memo.isBlank() ? "" : " <font size='2' color='#8A96A0'>메모</font>";
        boolean dim = withDate && t.pastCopy(control.today());
        JLabel label = new TitleLabel(t, date, dim, "<html>" + date + (t.done ? "<s>" + Ui.esc(t.title) + "</s>" : Ui.esc(t.title))
                + memoMark + "</html>");
        // D-94: 마감 글, 하려던 일, "오늘 마감"과 "마감", "지난 날"은 제목 아래 둘째 줄 왼쪽이다. 제목이 줄의 폭을 먼저 받는다.
        // D-69: 지난 반복 사본은 "지난 날" 없이 흐리게 보인다. D-78: 끝낸 일에는 붙이지 않는다.
        boolean past = withDate && !t.done && t.countDay().isBefore(control.today()) && !t.pastCopy(control.today());
        JLabel marks = marks(TaskBook.dueText(t), missedNote(t), dueTag(t, listDay), past);
        label.setToolTipText(memoTip(t));
        row.setToolTipText(memoTip(t));
        JLabel minutes = new JLabel(t.minutes + "분", JLabel.RIGHT);
        minutes.setFont(Theme.font(13));
        minutes.setForeground(t.done || dim ? Theme.INK_DIM : Theme.INK_SUB);
        minutes.setPreferredSize(new Dimension(Math.max(34, minutes.getPreferredSize().width), 20));
        row.add(left, BorderLayout.WEST);
        row.add(label, BorderLayout.CENTER);
        int iconButton = withDate ? 26 : 28;
        CircleButton edit = new CircleButton(Icons.Kind.PENCIL, iconButton, "수정", true);
        edit.addActionListener(e -> control.edit(t));
        CircleButton delete = new CircleButton(Icons.Kind.TRASH, iconButton, "삭제", true);
        delete.addActionListener(e -> control.delete(t));
        java.util.List<JComponent> watched = new java.util.ArrayList<>(java.util.List.of(row, label, box, edit, delete));
        if (withDate) {
            // 일정관리 오른쪽 목록은 좁아서 버튼을 둘째 줄 오른쪽에 둔다.
            row.add(middle(minutes), BorderLayout.EAST);
            JPanel buttons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 2, 0));
            buttons.add(edit);
            buttons.add(delete);
            // D-44: 오늘보다 앞선 날짜는 둘째 줄 왼쪽에 "지난 날". 제목 줄에 붙이면 좁은 목록에서 제목이 잘린다.
            // D-94: 표시와 버튼이 한 줄에 안 들면 표시를 버튼 위 한 줄에 둔다.
            row.add(SecondLine.of(marks, buttons), BorderLayout.SOUTH);
        } else {
            JComponent[] right;
            if (time != null) {
                // D-94: 끝낸 줄은 누를 수 없다. 지난 날 목록에서 기간 할 일의 시각은 정하지 않는다.
                boolean pastPeriod = !t.done && t.period() && listDay.isBefore(control.today());
                TimePill when = new TimePill(time, t.done, t.done || pastPeriod, pastPeriod ? "지난 날에는 시각을 정할 수 없어요" : null);
                when.addActionListener(e -> control.editTime(t, listDay, when));
                watched.add(when);
                right = new JComponent[] {when, minutes, edit, delete};
            } else {
                right = new JComponent[] {minutes, edit, delete};
            }
            row.add(middle(right), BorderLayout.EAST);
            if (marks != null) {
                row.add(marks, BorderLayout.SOUTH);
            }
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, marks == null ? 44 : 62));
        }
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    TaskMenu.of(control, t).show(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mousePressed(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (withDate) {
                    // D-99: 일정관리 목록은 한 번 누르면 시각 고치기 창이다. 두 번 눌러도 창은 하나이고 수정 창은 연필로 연다.
                    if (e.getClickCount() == 1 && javax.swing.SwingUtilities.isLeftMouseButton(e)) {
                        // D-99: 지난 날 줄의 기간 할 일도 마감이 남았으면 창을 열고, 정하면 오늘의 일이 된다.
                        java.time.LocalDate today = control.today();
                        boolean late = t.period() && listDay.isBefore(today) && !t.dueDate.isBefore(today);
                        control.editTime(t, late ? today : listDay, label);
                    }
                } else if (e.getClickCount() == 2) {
                    control.edit(t);
                }
            }
        };
        label.addMouseListener(mouse);
        row.addMouseListener(mouse);
        // D-99: 끝낸 줄과 마감이 지난 기간 할 일(#272)은 끌리지 않는다.
        if (withDate && !t.done && !t.pastDue(control.today()) && dropOn != null) {
            draggable(t, row, label);
        }
        // 줄 위에 마우스가 있는 동안(줄 안 버튼 위 포함) 줄 바탕을 칠한다.
        MouseAdapter hover = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                over[0] = true;
                row.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                over[0] = row.isShowing() && row.getMousePosition(true) != null;
                row.repaint();
            }
        };
        watched.forEach(c -> c.addMouseListener(hover));
        return row;
    }

    /** D-99: 체크박스, 연필, 휴지통을 뺀 줄을 잡아 시간표로 끈다. 줄 위는 펼친 손, 끄는 동안 쥔 손, 놓을 수 없는 날 위는 금지 모양. */
    private void draggable(Task t, JComponent... parts) {
        MouseAdapter drag = new MouseAdapter() {
            private java.awt.Point from;
            private boolean on;
            /** D-99: 끄는 동안 마우스가 시간표 끝에 머물면 굴린다(S66). inView는 보이는 칸 좌표의 마우스 자리다. */
            private java.awt.Point inView;
            private java.awt.Component held;
            /** 마지막 마우스 자리(held 좌표). */
            private java.awt.Point last;
            private final javax.swing.Timer roll = new javax.swing.Timer(30,
                    ev -> held.setCursor(dropOn.dragTo(dropOn.roll(inView, 30)) ? DragCursors.NO : DragCursors.GRAB));
            /** D-99: 끄는 동안 Esc를 누르면 그만둔다. */
            private final java.awt.event.AWTEventListener esc = ev -> {
                if (ev.getID() == java.awt.event.KeyEvent.KEY_PRESSED
                        && ((java.awt.event.KeyEvent) ev).getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                    stop();
                    // S94: Esc로 그만둘 때도 상자가 줄 자리로 돌아간다.
                    DragReturn.start(parts[0], javax.swing.SwingUtilities.convertPoint(held, last, parts[0]), dropOn.boxSize(t));
                }
            };

            private void stop() {
                on = false;
                from = null;
                lifted = null;
                rolling = null;
                roll.stop();
                java.awt.Toolkit.getDefaultToolkit().removeAWTEventListener(esc);
                for (JComponent c : parts) {
                    c.setCursor(DragCursors.OPEN);
                }
                dropOn.drag(null);
                parts[0].repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                from = javax.swing.SwingUtilities.isLeftMouseButton(e) ? e.getPoint() : null;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (from == null || !on && from.distance(e.getPoint()) <= 5) {
                    return;
                }
                if (!on) {
                    on = true;
                    lifted = t;
                    parts[0].repaint();
                    java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(esc, java.awt.AWTEvent.KEY_EVENT_MASK);
                    dropOn.drag(t);
                    held = e.getComponent();
                    rolling = roll;
                    roll.start();
                }
                inView = javax.swing.SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), dropOn.getParent());
                last = e.getPoint();
                e.getComponent().setCursor(dropOn.dragTo(onTable(e)) ? DragCursors.NO : DragCursors.GRAB);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!on) {
                    from = null;
                    return;
                }
                java.awt.Point p = onTable(e);
                TimetablePanel.Spot spot = p == null ? null : dropOn.spotAt(p);
                stop();
                if (spot != null) {
                    TaskController.Drop r = control.drop(t, spot.day(), spot.minute());
                    dropOn.dropped(r, t, p);
                    if (r.text() != null && r.undo() == null) {
                        // S150: 놓을 수 없는 날에 놓아도 상자가 목록의 그 줄 자리로 돌아간다(까닭 글은 dropped가 띄운다).
                        DragReturn.start(parts[0], javax.swing.SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), parts[0]),
                                dropOn.boxSize(t));
                    }
                } else {
                    // S65: 시간표 밖에 놓으면 바뀌는 것 없이 상자가 목록의 그 줄 자리로 돌아간다.
                    DragReturn.start(parts[0], javax.swing.SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), parts[0]),
                            dropOn.boxSize(t));
                }
            }
        };
        for (JComponent c : parts) {
            c.setCursor(DragCursors.OPEN);
            c.addMouseListener(drag);
            c.addMouseMotionListener(drag);
        }
    }

    /** 마우스 자리를 시간표 좌표로. 시간표의 보이는 칸 밖이면 null. */
    private java.awt.Point onTable(MouseEvent e) {
        java.awt.Point p = javax.swing.SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), dropOn);
        return dropOn.getVisibleRect().contains(p) ? p : null;
    }

    /**
     * 제목 칸. 글(getText)은 HTML(QA와 화면 읽기 프로그램이 읽는다)이고, 그림은 한 줄로 그려 칸 폭을 넘으면 "…"로 줄인다.
     * HTML 라벨은 좁으면 두 줄로 꺾여 줄 높이를 넘었다. 끝낸 일은 흐린 글에 취소선, 메모가 있으면 끝에 흐린 "메모".
     * 일정관리 목록의 날짜는 줄이지 않고 제목이 그 뒤 남는 폭을 받는다(D-94).
     */
    public static final class TitleLabel extends JLabel {
        private final Task t;
        private final String date;
        private final boolean dim;

        TitleLabel(Task t, String date, boolean dim, String html) {
            super(html);
            this.t = t;
            this.date = date;
            this.dim = dim;
            setFont(Theme.font(14));
            setMinimumSize(new Dimension(0, 0));
            setPreferredSize(new Dimension(40, 24));
        }

        private int memoWidth(java.awt.FontMetrics sm) {
            return t.memo.isBlank() ? 0 : sm.stringWidth("메모") + 5;
        }

        /** 지금 폭에서 그리는 제목 글(날짜 뒤, "…"로 줄였으면 그 글). 검사가 읽는다(T-LIST-9). */
        public String shown() {
            java.awt.FontMetrics fm = getFontMetrics(getFont());
            int max = getWidth() - fm.stringWidth(date) - memoWidth(getFontMetrics(Theme.font(11)));
            return fit(t.title, fm, max);
        }

        @Override
        protected void paintComponent(java.awt.Graphics graphics) {
            java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
            g.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            java.awt.FontMetrics fm = g.getFontMetrics(getFont());
            java.awt.Font small = Theme.font(11);
            String text = shown();
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g.setFont(getFont());
            g.setColor(t.done || dim ? Theme.INK_DIM : Theme.INK);
            int dx = fm.stringWidth(date);
            g.drawString(date + text, 0, y);
            int tw = dx + fm.stringWidth(text);
            if (t.done) {
                g.fillRect(dx, y - fm.getAscent() / 3 - 1, tw - dx, 1);
            }
            if (!t.memo.isBlank()) {
                g.setFont(small);
                g.setColor(Theme.INK_DIM);
                g.drawString("메모", tw + 5, y);
            }
            g.dispose();
        }
    }

    /**
     * D-94: 제목 아래 둘째 줄 왼쪽의 표시. 흐린 마감 글("~11/22까지", D-65)과 하려던 일("17일에 하려던 일", D-67), 빨간 "오늘 마감"이나
     * "마감"(D-70, D-93), 빨간 "지난 날"(D-44, 일정관리 목록) 순이다. 하나도 없으면 null.
     */
    private static JLabel marks(String due, String note, String tag, boolean past) {
        StringBuilder html = new StringBuilder();
        java.util.function.BiConsumer<String, String> part = (text, color) -> {
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

    /** D-67: 시각을 정한 날에 못 끝낸 기간 할 일이면 "{일}일에 하려던 일", 아니면 null. */
    private String missedNote(Task t) {
        return t.missed(control.today()) ? t.date.getDayOfMonth() + "일에 하려던 일" : null;
    }

    /**
     * D-70, D-93: 끝내지 않은 기간 할 일을 마감 날 목록에서 보면, 그날이 오늘이면 "오늘 마감", 앞날이면 "마감"이다. 마감이 지났으면
     * "지난 날"만 붙으므로 null.
     */
    private String dueTag(Task t, java.time.LocalDate listDay) {
        if (!t.period() || t.done || !t.dueDate.equals(listDay)) {
            return null;
        }
        java.time.LocalDate today = control.today();
        return listDay.equals(today) ? "오늘 마감" : listDay.isAfter(today) ? "마감" : null;
    }

    /** max 폭에 들어가게 끝을 "…"로 줄인다. */
    private static String fit(String s, java.awt.FontMetrics fm, int max) {
        if (fm.stringWidth(s) <= max) {
            return s;
        }
        String cut = s;
        while (cut.length() > 1 && fm.stringWidth(cut.strip() + "…") > max) {
            cut = cut.substring(0, cut.length() - 1);
        }
        return cut.strip() + "…";
    }

    /** "고정" 표시(D-35): TRACK 바탕, CARD_LINE 테두리의 작은 둥근 꼬리표. */
    private static JLabel fixedTag() {
        JLabel tag = new JLabel("고정") {
            @Override
            protected void paintComponent(java.awt.Graphics graphics) {
                java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
                g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
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

    /** 부품을 가로로 놓고 줄 높이의 가운데에 맞춘다. */
    private static JPanel middle(JComponent... parts) {
        JPanel p = new JPanel(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints k = new java.awt.GridBagConstraints();
        for (int i = 0; i < parts.length; i++) {
            k.gridx = i;
            k.insets = new java.awt.Insets(0, i == 0 ? 0 : 4, 0, 0);
            p.add(parts[i], k);
        }
        return p;
    }

    /** 메모가 있으면 마우스를 올렸을 때 200자까지 보인다(D-33). */
    static String memoTip(Task t) {
        if (t.memo.isBlank()) {
            return null;
        }
        String m = t.memo.length() > 200 ? t.memo.substring(0, 200) + "…" : t.memo;
        return "<html><div style='width:280px'>" + Ui.esc(m).replace("\n", "<br>") + "</div></html>";
    }

    /** 목록 너비를 보이는 칸에 맞춰서 긴 제목이 버튼을 밀어내지 않게 한다. 제목은 남는 폭에서 잘린다. */
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
    public int getScrollableUnitIncrement(java.awt.Rectangle r, int orientation, int direction) {
        return 16;
    }

    @Override
    public int getScrollableBlockIncrement(java.awt.Rectangle r, int orientation, int direction) {
        return 64;
    }
}
