package planningpet.proto;

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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/** 자동 배치 결과 창(05, UC-11). 적용을 눌러야 저장한다. */
public final class AutoPlaceDialog {
    private AutoPlaceDialog() {
    }

    public static JPanel content(Store s, LocalDate monday, Planner.Placement p, JButton cancel, JButton apply) {
        LocalDate today = s.today();
        boolean thisWeek = Store.mondayOf(today).equals(monday);
        LocalDate from = thisWeek ? today : monday;
        LocalTime nine = LocalTime.of(9, 0);
        LocalTime soonest = Planner.earliest(s).toLocalTime();
        LocalTime fromTime = thisWeek && soonest.isAfter(nine) ? soonest : nine;
        LocalDate to = monday.plusDays(6);
        JLabel head = new JLabel(day(from) + " " + fromTime + "부터 " + day(to) + "까지, 할 일마다 그 날짜 안의 빈 시간에 놓았어요");
        DefaultTableModel model = new DefaultTableModel(new Object[] {"할 일", "날짜", "시각", "걸리는 시간"}, 0);
        for (Planner.Placed x : p.placed()) {
            LocalTime end = x.start().plusMinutes(x.task().minutes);
            model.addRow(new Object[] {x.task().title, day(x.task().date), x.start() + " ~ " + (end.equals(LocalTime.MIDNIGHT) ? "24:00" : end), x.task().minutes + "분"});
        }
        for (Task t : p.unplaced()) {
            model.addRow(new Object[] {t.title, "놓지 못함", "그날 빈 시간이 모자라요", t.minutes + "분"});
        }
        JTable table = new JTable(model);
        int firstUnplaced = p.placed().size();
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int row, int col) {
                java.awt.Component c = super.getTableCellRendererComponent(t, v, sel, f, row, col);
                c.setForeground(row >= firstUnplaced && (col == 1 || col == 2) ? Theme.SUNDAY : Theme.INK);
                return c;
            }
        });
        JScrollPane scroll = new JScrollPane(table);
        scroll.setColumnHeaderView(table.getTableHeader());
        scroll.setPreferredSize(new java.awt.Dimension(560, 40 + 20 * model.getRowCount()));
        apply.setEnabled(!p.placed().isEmpty());
        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.add(head, BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        root.add(Ui.flow(cancel, apply), BorderLayout.SOUTH);
        return root;
    }

    public static void open(App app, LocalDate monday) {
        Planner.Placement p = Planner.place(app.store, monday);
        JButton cancel = new JButton("취소");
        JButton apply = new JButton("적용");
        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(app.root), "자동 배치 결과", JDialog.ModalityType.APPLICATION_MODAL);
        d.setContentPane(content(app.store, monday, p, cancel, apply));
        cancel.addActionListener(e -> d.dispose());
        apply.addActionListener(e -> {
            p.placed().forEach(x -> x.task().start = x.start());
            d.dispose();
            app.store.changed();
        });
        d.pack();
        d.setLocationRelativeTo(app.root);
        d.setVisible(true);
    }

    private static String day(LocalDate d) {
        return d.getMonthValue() + "월 " + d.getDayOfMonth() + "일";
    }
}
