package planningpet.app.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import planningpet.app.model.AppData;
import planningpet.app.model.DataStore;
import planningpet.app.model.Visits;
import planningpet.pet.model.Bubble;
import planningpet.pet.model.PetTalk;
import planningpet.pet.model.Situation;

/** 앱 하나의 controller. 도메인 controller의 변경을 저장과 함께 감싼다(06 22번, 07). */
public final class AppController {
    private final AppData data;
    private final DataStore store;
    /** 저장 실패 창을 띄우는 view의 함수. 창 내용을 받는다(07 "controller는 창을 모른다"). */
    private final Consumer<String> errors;
    private final Bubble bubble = new Bubble();
    private final PetTalk talk;
    /** 메인 달력에서 고른 날(D-106). S3의 달력이 잇는다. */
    private Supplier<LocalDate> homeDay;
    private boolean changing;
    /** 창이 보이는 중인지. onWindowShown이 true로 둔다. 숨기는 길(onClose)은 S11이다(docs/porting/app-window.md). */
    private boolean windowVisible;

    public AppController(AppData data, DataStore store, Consumer<String> errors) {
        this.data = data;
        this.store = store;
        this.errors = errors;
        this.talk = new PetTalk(data);
        this.homeDay = data::today;
    }

    public Bubble getBubble() { return bubble; }
    public boolean isWindowVisible() { return windowVisible; }
    public void setHomeDay(Supplier<LocalDate> homeDay) { this.homeDay = homeDay; }

    /**
     * 06 22번. 복사본을 떠 두고 edit를 적용한 뒤 save를 한 번 부른다. 저장에 실패하면 복사본으로 되돌리고 errors에 창 내용을 넘긴다.
     * edit가 예외를 던지면 되돌리고 저장하지 않고 그 예외를 다시 던진다(docs/porting/store.md). 되돌릴 때는 말풍선도 되돌린다. 어느 길이든
     * 마지막에 데이터 변경 알림을 한 번 보낸다. 나의 방의 한 번 말은 처음에 비운다(D-56).
     */
    public void change(String what, Runnable edit) {
        AppData before = data.copy();
        Bubble said = bubble.copy();
        LocalDate day = said.getRecountDay() != null ? said.getRecountDay() : homeDay.get();
        String list = talk.listKey(day);
        bubble.setRoomLine(null);
        changing = true;
        try {
            edit.run();
        } catch (RuntimeException e) {
            data.restore(before);
            bubble.restore(said);
            data.changed();
            throw e;
        } finally {
            changing = false;
        }
        try {
            store.save(data);
            recount(said.getNewLines(), day, list);
        } catch (IOException e) {
            data.restore(before);
            bubble.restore(said);
            errors.accept(what + " 저장에 실패해서 바꾸기 전으로 돌렸어요.");
        }
        data.changed();
    }

    /** D-106: 이 변경이 새 말을 하지 않았고 말풍선이 보는 날의 목록이 바뀌었으면 그날 수를 다시 센다. 다시 쓴 말은 새 말이 아니다. */
    private void recount(int newLines, LocalDate day, String list) {
        if (bubble.getNewLines() != newLines || list.equals(talk.listKey(day))) {
            return;
        }
        Supplier<String> how = bubble.getRecount() != null ? bubble.getRecount() : talk.dayLine(day);
        bubble.retell(how.get());
        bubble.follow(day, how);
    }

    /**
     * 펫이 새 말을 한다. day는 그 말이 세는 날이고 수가 없는 말이면 null이다(docs/porting/pet-talk.md). change 밖이면 저장할 데이터가
     * 바뀌지 않으므로 알림만 보낸다(06 읽는 법).
     */
    public void say(String line, LocalDate day) {
        bubble.say(line);
        if (day != null) {
            bubble.follow(day, talk.dayLine(day));
        }
        if (!changing) {
            data.changed();
        }
    }

    /** 나의 방 말풍선에 한 번 보일 말(BOUGHT, ADOPTED). change 안에서 부르고 다음 change가 비운다(D-56, 07). */
    public void sayInRoom(String line) {
        bubble.setRoomLine(line);
    }

    /** 06 15번. 그날 처음이면 연속 접속을 세고(12번) 말한다. BRIEFING과 WELCOME_BACK은 오늘 목록을 따라 다시 센다(D-106). */
    public void onWindowShown() {
        windowVisible = true;
        LocalDate today = data.today();
        LocalDate before = data.getLastOpenDay();
        if (today.equals(before)) {
            say(talk.say(Situation.USUAL), today);
            return;
        }
        Situation situation = before == null ? Situation.FIRST_RUN
                : ChronoUnit.DAYS.between(before, today) >= 8 ? Situation.WELCOME_BACK : Situation.BRIEFING;
        Supplier<String> how = situation == Situation.FIRST_RUN ? null : talk.recount(situation);
        String line = how != null ? how.get() : talk.say(situation);
        Runnable speak = () -> {
            bubble.say(line);
            if (how != null) {
                bubble.follow(today, how);
            }
        };
        change("연속 접속", () -> {
            new Visits(data).recordOpen(today);
            speak.run();
        });
        if (!today.equals(data.getLastOpenDay())) { // 저장에 실패해 되돌렸어도 브리핑은 보인다(02 UC-01 4a).
            speak.run();
            data.changed();
        }
    }
}
