package planningpet.schedule.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import planningpet.common.view.Theme;
import planningpet.common.view.Ui;
import planningpet.schedule.model.Category;

/**
 * 일정 입력 창의 칸(05 일정 입력 창 1~11번). 창 없이 만들 수 있다. 지금은 추가 창의 칸과 처음 값, 칸에 쓴 값 draft()만 있고, 칸끼리 켜고
 * 끄기, 오류 글, 수정 창의 잠금 글은 뒤 몫이 더한다(docs/porting/task-dialog.md S4f). 쳐 넣은 글 읽기(C1)의 칸 아래 글 줄은 두지 않는다.
 */
final class TaskForm {
    final JPanel panel = new JPanel(new GridBagLayout());
    final JRadioButton task = new JRadioButton("할 일", true);
    final JRadioButton fixed = new JRadioButton("고정 일정");
    final JTextField title = new JTextField(22);
    /** D-54 유형. 칸은 label()을 보이고 고른 차례가 Category의 차례다. */
    final JComboBox<String> category = new JComboBox<>(Arrays.stream(Category.values()).map(Category::label).toArray(String[]::new));
    final JSpinner date = new JSpinner(new SpinnerDateModel());
    /** 날짜 칸 이름. 기간 안에 하기를 켜면 "시작일"이다(D-73). */
    final JLabel dateLabel;
    /** 11번 기간 안에 하기와 마감(D-65). */
    final JCheckBox period = new JCheckBox("기간 안에 하기");
    final JLabel periodHint = Ui.hint("반복 일정은 기간을 정할 수 없어요");
    final JLabel dueLabel = new JLabel("마감일");
    final JSpinner due = new JSpinner(new SpinnerDateModel());
    /** 칸 아래 빨간 오류 글(05). 칸 차례다. */
    final Map<String, JLabel> errors = new LinkedHashMap<>();
    private int row;

    TaskForm() {
        ButtonGroup kinds = new ButtonGroup();
        kinds.add(task);
        kinds.add(fixed);
        Ui.dayEditor(this.date, "yyyy-MM-dd (E)");
        Ui.dayEditor(due, "yyyy-MM-dd");
        for (String k : new String[] {"제목", "마감", "시작 시각", "걸리는 시간", "메모", "반복"}) {
            JLabel l = new JLabel();
            l.setForeground(Theme.SUNDAY);
            l.setVisible(false);
            errors.put(k, l);
        }
        line("종류", Ui.flow(task, fixed));
        line("제목", title);
        line("", errors.get("제목"));
        line("유형", Ui.flow(category));
        dateLabel = line("날짜", this.date);
        line("기간", Ui.flow(period, periodHint, dueLabel, due));
        line("", errors.get("마감"));
    }

    /** 이름 칸과 칸을 한 줄에 놓는다. 이름 칸을 돌려준다. */
    private JLabel line(String label, JComponent field) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 8, 3, 8);
        c.gridy = row++;
        c.anchor = GridBagConstraints.WEST;
        JLabel name = new JLabel(label);
        panel.add(name, c);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, c);
        return name;
    }
}
