package planningpet.qa;

import planningpet.app.Main;
import planningpet.app.model.Store;
import planningpet.home.view.MainScreen;
import planningpet.pet.view.PetScreen;
import planningpet.schedule.controller.TaskController;
import planningpet.schedule.model.*;
import planningpet.schedule.view.*;
import java.awt.Component;
import java.awt.Container;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * QA 동작. 체크, 빠른 추가, 출석, 화면 바꾸기는 사람이 누르는 버튼과 칸을 누른다. 창(JDialog) 안의 동작은 창을 띄우지 않고
 * 그 창의 칸을 채운 뒤 창의 버튼이 부르는 함수를 부른다.
 */
class QaDrive extends QaCore {
    MainScreen main;
    ScheduleScreen sched;
    PetScreen pet;
    String lastQuestion;
    String lastOptions;
    String lastTitle;

    QaDrive(String flow) {
        super(flow);
    }

    void start(Store s) {
        app = Main.create(s);
        main = find(app.root, MainScreen.class);
        sched = find(app.root, ScheduleScreen.class);
        pet = find(app.root, PetScreen.class);
    }

    static <T> T find(Component c, Class<T> type) {
        if (type.isInstance(c)) {
            return type.cast(c);
        }
        if (c instanceof Container k) {
            for (Component child : k.getComponents()) {
                T t = find(child, type);
                if (t != null) {
                    return t;
                }
            }
        }
        return null;
    }

    void open() {
        app.control.windowShown();
    }

    void at(String dateTime) {
        app.store.now = LocalDateTime.parse(dateTime);
    }
    static LocalDate d(String date) {
        return LocalDate.parse(date);
    }

    Task task(String title) {
        return app.store.tasks.stream().filter(t -> t.title.equals(title)).findFirst().orElseThrow();
    }
    Task task(String title, String date) {
        return app.store.tasks.stream().filter(t -> t.title.equals(title) && t.date.equals(d(date)))
                .findFirst().orElseThrow();
    }

    /** 보이는 화면의 목록 줄에서 그 일의 체크박스를 누른다. 홈이면 메인 목록, 일정관리면 오른쪽 목록. */
    void check(Task t) {
        Container list = "일정관리".equals(app.current()) ? sched.untimed : main.rows;
        javax.swing.JCheckBox box = boxOf(list, t);
        if (box == null) {
            yes("'" + t.title + "' 체크박스가 화면 목록에 있음", false);
            return;
        }
        box.doClick();
    }

    static javax.swing.JCheckBox boxOf(Component c, Task t) {
        if (c instanceof javax.swing.JCheckBox b && b.getClientProperty(Task.class) == t) {
            return b;
        }
        if (c instanceof Container k) {
            for (Component child : k.getComponents()) {
                javax.swing.JCheckBox b = boxOf(child, t);
                if (b != null) {
                    return b;
                }
            }
        }
        return null;
    }

    /** 시계를 옮기고 30초 타이머가 한 번 돈 것처럼 날 바뀜을 확인한다(06의 23번). */
    void tick(String dateTime) {
        at(dateTime);
        app.watcher.tick();
    }
    void select(String date) {
        app.show("홈");
        main.selectDate(d(date));
    }
    void quick(String text) {
        main.quick.setText(text);
        main.quick.postActionEvent();
    }
    /** D-40: 칸에 쓰고 칸 옆 "추가"를 누른다. */
    void quickButton(String text) {
        main.quick.setText(text);
        main.quickButton.doClick();
    }

    /** 목록 제목 줄의 "일정 추가"를 누른다. 창 대신 무엇을 열었는지와 빠른 추가 칸에 남은 글을 돌려준다. */
    String addFromList() {
        String[] opened = {"안 열림"};
        TaskController.Opener real = app.schedule.opener;
        app.schedule.opener = (t, date) -> opened[0] = (t == null ? "새 일정 " : "수정 ") + date;
        main.addButton.doClick();
        app.schedule.opener = real;
        return opened[0] + ", 칸에 남은 글 " + main.quick.getText();
    }

    /** 반복 사본 저장의 범위 창에서 answer번째 버튼을 누르게 한다(D-42). 물어본 문장은 lastQuestion에 남는다. */
    void answer(int answer) {
        app.schedule.chooser = (title, message, options) -> {
            lastTitle = title;
            lastQuestion = message;
            lastOptions = String.join(", ", options);
            return answer;
        };
    }

    void attend() {
        app.attend.doClick();
    }

    /** 삭제 확인 창에서 answer번째 버튼을 누른다. 물어본 문장은 lastQuestion에 남는다. */
    boolean delete(Task t, int answer) {
        app.schedule.chooser = (title, message, options) -> {
            lastTitle = title;
            lastQuestion = message;
            lastOptions = String.join(", ", options);
            return answer;
        };
        return app.schedule.delete(t);
    }
    TaskForm form(String date) {
        return new TaskForm(null, d(date), app.store.now);
    }
    boolean save(TaskForm f, Task editing) {
        return TaskDialog.trySave(app, f, editing);
    }
    Planner.Placement place() {
        return Planner.place(app.store, sched.monday());
    }

    void apply(Planner.Placement p) {
        AutoPlaceDialog.apply(app, p);
    }
}
