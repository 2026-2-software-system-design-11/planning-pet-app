package planningpet.schedule.controller;

import planningpet.app.model.Examples;
import planningpet.app.view.App;
import planningpet.common.model.Rules;
import planningpet.reward.model.*;
import planningpet.schedule.model.*;
import planningpet.schedule.view.*;
import java.time.LocalDate;
import javax.swing.JOptionPane;

/** 일정 controller. 여러 화면이 같이 쓰는 사용자 동작(체크, 수정, 삭제, 추가, 저장)을 받아 Model을 바꾸고 알린다. */
public final class TaskController {
    private final App app;

    public TaskController(App app) {
        this.app = app;
        this.opener = (t, date) -> TaskDialog.open(app, t, date);
        this.timeOpener = (t, day, anchor) -> TimeEditor.open(app, t, day, anchor);
    }

    /** 시각 고치기 창을 연다(D-32). QA와 검사는 이것을 바꿔 끼운다. */
    public interface TimeOpener {
        void open(Task t, LocalDate day, java.awt.Component anchor);
    }

    public TimeOpener timeOpener;

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

    /** 설정 "목록 누르기"(D-102). 켜면 목록 줄을 한 번 누를 때 시각 고치기 창이 뜬다. */
    public boolean listClickTime() {
        return app.store.listClickTime;
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

    /** D-32. 메인 시각 칸이나 일정관리 목록 줄(D-99)을 누르면 그 아래에 시각 고치기 창을 연다. day는 그 목록의 날이다(D-65). */
    public void editTime(Task t, LocalDate day, java.awt.Component anchor) {
        // D-94: 끝낸 일과 지난 날 목록의 기간 할 일은 목록에서 시각을 정하지 않는다(화면은 알약을 흐린다).
        if (t.done || t.period() && day.isBefore(app.store.today())) {
            return;
        }
        timeOpener.open(t, day, anchor);
    }

    /** D-87: 같은 날 다른 일정과 겹치면 막지 않고 묻는다. 겹치지 않거나 넣기를 고르면 true. */
    private boolean overlapOk(TaskBook.Overlap o) {
        return o == null || chooser.choose("시간이 겹쳐요", o.question(), new String[] {"넣기", "취소"}, 0) == 0;
    }

    /** UC-03, UC-05. 검사를 통과한 입력 값을 저장한다. 반복 사본이면 범위를 묻고, 취소하면 false. */
    public boolean save(Task editing, TaskDraft d) {
        if (!overlapOk(app.store.book.overlap(d, editing))) {
            return false;
        }
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
                } else if (fromHome()) {
                    // D-90: 홈에서 넣으면 펫이 넣은 일의 이름과 오늘 남은 수를 말한다.
                    app.store.say(app.talk.added(made.get(0)));
                    app.store.recount = app.talk.addedRecount(made.get(0)); // D-106
                } else {
                    // D-94: 일정관리에서 넣으면 홈 말풍선에만 남기고, 일정관리 말풍선은 D-39대로 힌트를 다시 정한다.
                    app.store.sayAtHome(app.talk.added(made.get(0)));
                    app.store.recount = app.talk.addedRecount(made.get(0)); // D-106
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
        app.control.change(() -> {
            Task made = app.store.book.add(Task.Kind.TASK, title, date, null, 30, "", null).get(0);
            app.store.say(app.talk.added(made)); // D-90
            app.store.recount = app.talk.addedRecount(made); // D-106
        });
        return true;
    }

    /** 일정 입력 창을 홈에서 열었는가(D-90). 일정관리에서 넣으면 일정관리의 펫 말은 D-39를 따른다(D-94). */
    private boolean fromHome() {
        return "홈".equals(app.current());
    }

    /** 시각 고치기 창의 저장(D-32, D-42). 반복 사본이면 범위를 묻고, 취소하면 false. day는 창을 연 목록의 날이다(D-65). */
    public boolean saveTime(Task t, int start, int minutes, LocalDate day) {
        return saveTime(t, start, minutes, day, false);
    }

    /** 시각 고치기 창의 저장. move면 보통 할 일도 day로 옮긴다("밤 4시", D-98). */
    public boolean saveTime(Task t, int start, int minutes, LocalDate day, boolean move) {
        // 기간 할 일에 시각을 정하면 창을 연 목록의 날로, move면("밤 4시", D-98) day로 옮겨지므로 그 날과 잰다(D-65).
        if (!overlapOk(app.store.book.overlap(t.period() || move ? day : t.date, start, minutes, t))) {
            return false;
        }
        int scope = editScope(t);
        if (scope < 0) {
            return false;
        }
        app.control.change(() -> {
            app.store.book.setTime(t, start, minutes, day, scope == 1);
            if (move) {
                // D-98: 이후 반복 모두면 끝내지 않은 뒤 사본도 각자 다음 날로 옮긴다(18일은 19일, 25일은 26일). 끝낸 사본은 그대로(D-42).
                if (scope == 1 && t.seriesId != null) {
                    app.store.tasks.stream().filter(c -> c != t && !c.done && t.seriesId.equals(c.seriesId) && c.date.isAfter(t.date))
                            .forEach(c -> c.date = c.date.plusDays(1));
                }
                t.date = day;
            }
        });
        return true;
    }

    /** D-99: 끌어 놓은 결과. text는 놓은 자리 옆 글(물음에서 취소했으면 null), undo는 되돌리기(놓지 못했으면 null). */
    public record Drop(String text, Runnable undo) {
    }

    /** D-99: 끄는 동안 그 날에 놓을 수 없으면 짧은 까닭("지난 날", "11/18~11/21만 돼요"), 놓을 수 있으면 null. */
    public String blocked(Task t, LocalDate day) {
        LocalDate[] range = app.store.book.dropRange(t);
        if (day.isBefore(app.store.today())) {
            return "지난 날";
        }
        return day.isBefore(range[0]) || range[1] != null && day.isAfter(range[1]) ? md(range[0]) + "~" + md(range[1]) + "만 돼요" : null;
    }

    /** D-99: 일정관리 목록의 줄을 시간표의 그 날, 앱 하루의 분 minute에 놓는다. 겹침(D-87)과 반복 범위(D-42)를 묻는다. */
    public Drop drop(Task t, LocalDate day, int minute) {
        // #272: 마감이 지난 기간 할 일은 받지 않는 일이라 끝낸 일처럼 아무것도 바꾸지 않는다.
        if (t.pastDue(app.store.today())) {
            return new Drop(null, null);
        }
        String why = blocked(t, day);
        if (why != null) {
            LocalDate[] range = app.store.book.dropRange(t);
            return new Drop(why.equals("지난 날") ? "지난 날에는 놓을 수 없어요"
                    : md(range[0]) + "부터 " + md(range[1]) + "까지 놓을 수 있어요", null);
        }
        int start = TaskBook.dropStart(minute, t.minutes);
        if (t.done || !overlapOk(app.store.book.overlap(day, start, t.minutes, t))) {
            return new Drop(null, null);
        }
        int scope = editScope(t);
        if (scope < 0) {
            return new Drop(null, null);
        }
        Runnable before = app.store.book.keep(t, scope == 1);
        app.control.change(() -> app.store.book.placeAt(t, day, start, scope == 1));
        String text = Math.max(0, minute) / 30 * 30 > start ? "다음 날 04:00까지 끝나야 해서 " + Times.value(start) + "에 놓았어요"
                : md(day) + " " + TaskBook.timeText(t) + "에 놓았어요";
        return new Drop(text, () -> app.control.change(before));
    }

    private static String md(LocalDate d) {
        return d.getMonthValue() + "/" + d.getDayOfMonth();
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

    /** 방금 자동 배치로 놓은 일(D-89). 일정관리가 한 번 꺼내 그 자리로 내리고 진하게 보인다. */
    private java.util.List<Task> justPlaced = java.util.List.of();

    /** 방금 놓은 일을 돌려주고 비운다. */
    public java.util.List<Task> takePlaced() {
        java.util.List<Task> out = justPlaced;
        justPlaced = java.util.List.of();
        return out;
    }

    /** 남은 예시 일정(D-74). 사용자가 고친 예시는 내 일정으로 보아 뺀다(D-94). */
    public java.util.List<Task> samples() {
        return app.store.tasks.stream().filter(t -> app.store.sampleIds.contains(t.id) && !Examples.edited(app.store, t)).toList();
    }

    /** 06의 31번. 확인 창을 거쳐 예시 일정을 모두 지운다. 지운 수를 돌려주고 없거나 취소하면 0. */
    public int clearSamples() {
        java.util.List<Task> list = samples();
        if (list.isEmpty()) {
            return 0;
        }
        long kept = app.store.tasks.stream().filter(t -> app.store.sampleIds.contains(t.id) && Examples.edited(app.store, t)).count();
        int answer = chooser.choose("예시 지우기", "예시 일정 " + list.size() + "개를 지울까요? 새로 넣은 일정은 그대로 남아요."
                + (kept > 0 ? " 고친 예시 " + kept + "개는 남아요." : ""), new String[] {"지우기", "취소"});
        if (answer != 0) {
            return 0;
        }
        app.control.change(() -> {
            list.forEach(t -> app.store.book.delete(t, false));
            app.store.sampleIds.clear();
            app.store.sampleLooks.clear();
            app.store.say("예시를 지웠어. 수업부터 넣어 볼까?");
        });
        return list.size();
    }

    /** 자동 배치 결과 창의 적용(UC-11). */
    public void applyPlacement(Planner.Placement p) {
        justPlaced = p.placed().stream().map(Planner.Placed::task).toList();
        app.control.change(() -> p.placed().forEach(x -> {
            x.task().setStart(x.start().getHour() * 60 + x.start().getMinute());
            if (x.task().dueDate != null) {
                // D-65: 기간 할 일은 놓은 날로 옮긴다. 마감은 그대로다.
                x.task().date = x.day();
            }
        }));
    }
}
