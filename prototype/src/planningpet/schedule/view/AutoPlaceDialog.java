package planningpet.schedule.view;

import planningpet.app.model.Store;
import planningpet.app.view.App;
import planningpet.common.view.*;
import planningpet.schedule.model.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.time.LocalDate;
import java.time.LocalTime;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/** 자동 배치 결과 창(05, UC-11). 적용을 눌러야 저장한다. */
public final class AutoPlaceDialog {
    private AutoPlaceDialog() {
    }

    public static JPanel content(Store s, LocalDate monday, Planner.Placement p, JButton cancel, JButton apply) {
        LocalDate today = s.today();
        boolean thisWeek = TaskBook.mondayOf(today).equals(monday);
        java.time.LocalDateTime soonest = Planner.earliest(s);
        java.time.LocalDateTime start = thisWeek && soonest.toLocalTime().isAfter(LocalTime.of(9, 0)) ? soonest
                : (thisWeek ? soonest.toLocalDate() : monday).atTime(9, 0);
        LocalDate from = start.toLocalDate();
        LocalTime fromTime = start.toLocalTime();
        LocalDate to = monday.plusDays(6);
        JLabel head = new JLabel(day(from) + " " + fromTime + "부터 " + day(to) + "까지, 할 일마다 그 날짜(기간 할 일은 기간) 안의 빈 시간에 놓았어요");
        // D-43: 결과 표는 보기만 한다. 칸을 고치거나 열을 옮길 수 없다.
        DefaultTableModel model = new DefaultTableModel(new Object[] {"할 일", "날짜", "시각", "걸리는 시간"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (Planner.Placed x : p.placed()) {
            LocalTime end = x.start().plusMinutes(x.task().minutes);
            model.addRow(new Object[] {x.task().title, day(x.day()), x.start() + " ~ " + (end.equals(LocalTime.MIDNIGHT) ? "24:00" : end), x.task().minutes + "분"});
        }
        JTable table = new JTable(model);
        table.getTableHeader().setReorderingAllowed(false);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setColumnHeaderView(table.getTableHeader());
        scroll.setPreferredSize(new java.awt.Dimension(560, 40 + 20 * model.getRowCount()));
        apply.setEnabled(!p.placed().isEmpty());
        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.add(head, BorderLayout.NORTH);
        JPanel middle = new JPanel(new BorderLayout(0, 8));
        middle.add(scroll, BorderLayout.CENTER);
        if (!p.unplaced().isEmpty()) {
            middle.add(unplaced(p, monday), BorderLayout.SOUTH);
        }
        root.add(middle, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new BorderLayout());
        buttons.add(Ui.flow(cancel, apply), BorderLayout.EAST);
        root.add(buttons, BorderLayout.SOUTH);
        return root;
    }

    /** D-76: 표 아래 "놓지 못한 일" 칸. 일마다 제목, 마감 날짜, 빨간 까닭이고 맨 아래에 다음 할 일을 적는다. */
    public static JPanel unplaced(Planner.Placement p, LocalDate monday) {
        JPanel box = new JPanel();
        box.setLayout(new javax.swing.BoxLayout(box, javax.swing.BoxLayout.Y_AXIS));
        box.setName("놓지 못한 일");
        JLabel title = new JLabel("놓지 못한 일");
        title.setFont(Theme.bold(13));
        box.add(title);
        for (Task t : p.unplaced()) {
            JLabel line = new JLabel("<html><div style='width:520px'><b>" + Ui.esc(t.title) + "</b> " + Planner.dueText(t)
                    + "<br><font color='#E55C64'>" + Planner.reason(t, monday) + "</font></div></html>");
            line.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 0, 0, 0));
            box.add(line);
        }
        JLabel hint = Ui.hint(Planner.UNPLACED_HINT);
        hint.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 0, 0, 0));
        box.add(hint);
        return box;
    }

    public static void open(App app, LocalDate monday) {
        Planner.Placement p = Planner.place(app.store, monday);
        JButton cancel = new JButton("취소");
        JButton apply = new JButton("적용");
        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(app.root), "자동 배치 결과", JDialog.ModalityType.APPLICATION_MODAL);
        d.setContentPane(content(app.store, monday, p, cancel, apply));
        cancel.addActionListener(e -> d.dispose());
        apply.addActionListener(e -> {
            apply(app, p);
            d.dispose();
        });
        d.pack();
        d.setLocationRelativeTo(app.root);
        d.setVisible(true);
    }

    /** 적용 버튼과 같다. */
    public static void apply(App app, Planner.Placement p) {
        app.schedule.applyPlacement(p);
    }

    private static String day(LocalDate d) {
        return d.getMonthValue() + "월 " + d.getDayOfMonth() + "일";
    }
}
