package planningpet.schedule.controller;

import planningpet.app.view.App;
import planningpet.common.model.Rules;
import planningpet.reward.model.*;
import planningpet.schedule.model.*;
import planningpet.schedule.view.*;
import java.time.LocalDate;
import java.time.LocalTime;
import javax.swing.JOptionPane;

/** 일정 controller. 여러 화면이 같이 쓰는 사용자 동작(체크, 수정, 삭제, 추가, 저장)을 받아 Model을 바꾸고 알린다. */
public final class TaskController {
    private final App app;

    public TaskController(App app) {
        this.app = app;
        this.opener = (t, date) -> TaskDialog.open(app, t, date);
    }

    /** 삭제 확인 창. 고른 버튼의 번호를 돌려준다. QA는 이것을 바꿔 끼운다. */
    public interface Chooser {
        int choose(String title, String message, String[] options);

        /** initial은 처음 골라 둘 버튼의 번호다(D-72). 바꿔 끼운 것은 따로 정하지 않으면 무시한다. */
        default int choose(String title, String message, String[] options, int initial) {
            return choose(title, message, options);
        }
    }

    public Chooser chooser = new Chooser() {
        @Override
        public int choose(String title, String message, String[] options) {
            return ask(title, message, options, options.length - 1);
        }

        @Override
        public int choose(String title, String message, String[] options, int initial) {
            return ask(title, message, options, initial);
        }
    };

    /** 일정 입력 창을 연다. editing이 null이면 추가다. QA는 이것을 바꿔 끼운다. */
    public interface Opener {
        void open(Task editing, LocalDate date);
    }

    public Opener opener;

    private int ask(String title, String message, String[] options, int initial) {
        return JOptionPane.showOptionDialog(app.root, message, title, JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[initial]);
    }

    /** UC-07, UC-08. */
    public void check(Task t) {
        app.control.change(() -> {
            if (t.done) {
                int points = t.rewardPoints;
                planningpet.pet.model.PetState pet = t.rewardPet == null ? app.store.pet() : app.store.pet(t.rewardPet);
                int before = Level.level(pet.exp);
                app.game.uncomplete(t);
                app.store.say(app.talk.uncompleted(points, before, Level.level(pet.exp)));
            } else {
                RewardResult r = app.game.complete(t);
                app.store.say(app.talk.completed(r, t));
            }
        });
    }

    /** UC-06. 반복 사본이면 범위를 묻는다. 지웠으면 true. */
    public boolean delete(Task t) {
        int scope;
        if (t.seriesId == null) {
            int answer = chooser.choose("일정 삭제", "'" + t.title + "'" + Rules.josa(t.title, "을", "를") + " 삭제할까요?",
                    new String[] {"삭제", "취소"});
            scope = answer == 0 ? 0 : -1;
        } else {
            int answer = chooser.choose("일정 삭제", "'" + t.title + "'" + Rules.josa(t.title, "은", "는") + " 반복 일정이에요.",
                    new String[] {"이 일정만", "이후 반복 모두", "취소"});
            scope = answer == 0 || answer == 1 ? answer : -1;
        }
        app.control.change(() -> {
            if (scope >= 0) {
                app.store.book.delete(t, scope == 1);
            }
        });
        return scope >= 0;
    }

    public LocalDate today() {
        return app.store.today();
    }

    /** D-42. 반복 사본을 저장할 때 범위를 묻는다. 0 이 일정만, 1 이후 반복 모두, -1 취소. 반복이 아니면 묻지 않고 0. */
    private int editScope(Task t) {
        if (t.seriesId == null) {
            return 0;
        }
        int answer = chooser.choose("반복 일정 저장", "'" + t.title + "'" + Rules.josa(t.title, "은", "는") + " 반복 일정이에요. 어디까지 바꿀까요?",
                new String[] {"이 일정만", "이후 반복 모두", "취소"});
        return answer == 0 || answer == 1 ? answer : -1;
    }

    /** UC-05. */
    public void edit(Task t) {
        opener.open(t, t.date);
    }

    /** UC-03. 날짜는 띄운 화면이 고른다(06의 14번). */
    public void add(LocalDate date) {
        opener.open(null, date);
    }

    /** D-32. 메인 시각 칸을 누르면 그 아래에 시각 고치기 창을 연다. day는 그 목록의 날이다(D-65). */
    public void editTime(Task t, LocalDate day, java.awt.Component anchor) {
        TimeEditor.open(app, t, day, anchor);
    }

    /** UC-03, UC-05. 검사를 통과한 입력 값을 저장한다. 반복 사본이면 범위를 묻고, 취소하면 false. */
    public boolean save(Task editing, TaskDraft d) {
        int scope = editing == null ? 0 : editScope(editing);
        if (scope < 0) {
            return false;
        }
        app.control.change(() -> {
            if (editing == null) {
                java.util.List<Task> made = app.store.book.add(d);
                if (d.repeat() != null && !d.repeat().daily()) {
                    // D-75: 날짜 칸이 고른 요일이 아니어도 막지 않고 처음 들어간 날을 알린다.
                    app.store.say(app.talk.weeklyAdded(made));
                }
            } else {
                app.store.book.update(editing, d, scope == 1);
            }
        });
        return true;
    }

    /** 홈 빠른 추가(D-40). 시각 없는 30분 할 일이다. 비었거나 100자를 넘으면 넣지 않고 false. */
    public boolean quickAdd(String text, LocalDate date) {
        String title = text.strip();
        if (title.isEmpty() || title.length() > 100) {
            return false;
        }
        app.control.change(() -> app.store.book.add(Task.Kind.TASK, title, date, null, 30, "", null));
        return true;
    }

    /** 시각 고치기 창의 저장(D-32, D-42). 반복 사본이면 범위를 묻고, 취소하면 false. day는 창을 연 목록의 날이다(D-65). */
    public boolean saveTime(Task t, LocalTime start, int minutes, LocalDate day) {
        int scope = editScope(t);
        if (scope < 0) {
            return false;
        }
        app.control.change(() -> app.store.book.setTime(t, start, minutes, day, scope == 1));
        return true;
    }

    /** 시각 빼기(D-72). 반복 사본이면 범위를 묻고 처음 골라 둔 버튼은 "이 일정만"이다. 취소하면 false. */
    public boolean clearTime(Task t) {
        int scope = 0;
        if (t.seriesId != null) {
            int answer = chooser.choose("시각 빼기", "'" + t.title + "'" + Rules.josa(t.title, "은", "는")
                    + " 반복 일정이에요. 어디까지 시각을 뺄까요?", new String[] {"이 일정만", "이후 반복 모두", "취소"}, 0);
            scope = answer == 0 || answer == 1 ? answer : -1;
        }
        if (scope < 0) {
            return false;
        }
        boolean all = scope == 1;
        app.control.change(() -> app.store.book.clearTime(t, all));
        return true;
    }

    /** 자동 배치 결과 창의 적용(UC-11). */
    public void applyPlacement(Planner.Placement p) {
        app.control.change(() -> p.placed().forEach(x -> {
            x.task().setStart(x.start().getHour() * 60 + x.start().getMinute());
            if (x.task().dueDate != null) {
                // D-65: 기간 할 일은 놓은 날로 옮긴다. 마감은 그대로다.
                x.task().date = x.day();
            }
        }));
    }
}
