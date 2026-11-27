package planningpet.pet.model;

import java.time.LocalDate;
import java.util.function.Supplier;

/**
 * 펫 말풍선(07). 저장할 데이터가 아니라 AppData 밖에 둔다(06 읽는 법). 홈과 일정관리 view가 line을, 나의 방 view가 roomLine을
 * 읽는다. 다시 세기(D-106)는 AppController.change가 판단한다.
 */
public final class Bubble {
    private String line = "";
    /** 새 말을 할 때마다 1씩 는다. 다시 쓴 말은 세지 않는다(D-39, D-106). */
    private int newLines;
    /** 말이 수를 세는 날. 없으면 null이고 다시 세기는 메인에서 고른 날을 본다. */
    private LocalDate recountDay;
    /** 그날 목록이 바뀌면 다시 만들 말. 07 필드에 더한 것이다(docs/porting/pet-talk.md). */
    private Supplier<String> recount;
    /** 나의 방 말풍선에 한 번 보일 말. 없으면 null이고 방은 하트를 그린다(D-56). */
    private String roomLine;

    /** 새 말. 수를 세는 날을 지운다. */
    public void say(String line) {
        this.line = line;
        newLines++;
        recountDay = null;
        recount = null;
    }

    /** 다시 쓴 말. 새 말이 아니다(D-106). */
    public void retell(String line) {
        this.line = line;
    }

    /** 지금 말이 day의 수를 세고, 그날 목록이 바뀌면 recount로 다시 만든다. */
    public void follow(LocalDate day, Supplier<String> recount) {
        recountDay = day;
        this.recount = recount;
    }

    public void setRoomLine(String line) { roomLine = line; }

    public String getLine() { return line; }
    public int getNewLines() { return newLines; }
    public LocalDate getRecountDay() { return recountDay; }
    public Supplier<String> getRecount() { return recount; }
    public String getRoomLine() { return roomLine; }

    /** 값이 같은 사본(06 22번의 되돌리기). */
    public Bubble copy() {
        Bubble c = new Bubble();
        c.restore(this);
        return c;
    }

    public void restore(Bubble from) {
        line = from.line;
        newLines = from.newLines;
        recountDay = from.recountDay;
        recount = from.recount;
        roomLine = from.roomLine;
    }
}
