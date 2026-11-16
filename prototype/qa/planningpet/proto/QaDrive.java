package planningpet.proto;

import java.awt.Component;
import java.awt.Container;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** QA 동작. 사람이 누르는 버튼과 칸을 같은 길로 부른다. 창(JDialog)은 띄우지 않고 그 안의 함수를 부른다. */
class QaDrive extends QaCore {
    MainScreen main;
    ScheduleScreen sched;
    PetScreen pet;
    String lastQuestion;

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
        app.store.say(app.talk.onWindowShown(app.game));
        app.store.changed();
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

    void check(Task t) {
        app.actions.check(t);
    }
    void select(String date) {
        app.show("홈");
        main.selectDate(d(date));
    }
    void quick(String text) {
        main.quick.setText(text);
        main.quick.postActionEvent();
    }
    void attend() {
        app.attend.doClick();
    }

    /** 삭제 확인 창에서 answer번째 버튼을 누른다. 물어본 문장은 lastQuestion에 남는다. */
    boolean delete(Task t, int answer) {
        app.actions.chooser = (message, options) -> {
            lastQuestion = message;
            return answer;
        };
        return app.actions.delete(t);
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
    boolean buy(String item) {
        return ShopDialog.buy(app, pet, item);
    }
}
