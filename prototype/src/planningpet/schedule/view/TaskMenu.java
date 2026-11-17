package planningpet.schedule.view;

import planningpet.schedule.controller.TaskController;
import planningpet.schedule.model.Task;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/** 할 일 줄과 시간표 블록의 오른쪽 버튼 메뉴: 수정, 삭제. */
public final class TaskMenu {
    private TaskMenu() {
    }

    public static JPopupMenu of(TaskController control, Task t) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem edit = new JMenuItem("수정");
        edit.addActionListener(e -> control.edit(t));
        JMenuItem delete = new JMenuItem("삭제");
        delete.addActionListener(e -> control.delete(t));
        menu.add(edit);
        menu.add(delete);
        return menu;
    }
}
