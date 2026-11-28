package planningpet.schedule.view;

import java.util.function.Consumer;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import planningpet.schedule.model.Task;

/** 할 일 줄의 오른쪽 버튼 메뉴: 수정, 삭제(05 홈 "할 일 고치기와 지우기", docs/porting/task-rows.md S3y). */
final class TaskMenu {
    private TaskMenu() {
    }

    /** 부를 때마다 새 메뉴다. 고르면 그 할 일을 onEdit이나 onDelete에 넘긴다. 여는 창은 S4와 삭제 확인이 잇는다. */
    static JPopupMenu of(Consumer<Task> onEdit, Consumer<Task> onDelete, Task t) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem edit = new JMenuItem("수정");
        edit.addActionListener(e -> onEdit.accept(t));
        JMenuItem delete = new JMenuItem("삭제");
        delete.addActionListener(e -> onDelete.accept(t));
        menu.add(edit);
        menu.add(delete);
        return menu;
    }
}
