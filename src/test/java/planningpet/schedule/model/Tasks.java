package planningpet.schedule.model;

import java.time.LocalDate;
import java.time.LocalTime;
import planningpet.pet.model.PetKind;

/** 테스트가 함께 쓰는 날짜와 일정 만들기(#518). 고칠 값만 적고 나머지는 처음 값으로 둔다. */
public final class Tasks {
    private Tasks() {
    }

    /** 2026-11-01에서 day-1일 뒤. 31부터는 12월로 넘어간다. */
    public static LocalDate d(int day) { return LocalDate.of(2026, 11, 1).plusDays(day - 1); }

    public static LocalDate d(int month, int day) { return LocalDate.of(2026, month, day); }

    /** id는 제목, 순번 1, 할 일, 시각 없음, 30분, 메모 없음, 기타, 반복과 기간 없음, 안 함, 보상 없음. */
    public static Spec task(String title, LocalDate date) { return new Spec(title, date); }

    /** 일정 하나의 값. task()는 Task, draft()는 입력 창 값을 만든다. */
    public static final class Spec {
        private final String title;
        private final LocalDate date;
        private String id;
        private long seq = 1;
        private Kind kind = Kind.TASK;
        private LocalTime start;
        private int minutes = 30;
        private String memo = "";
        private boolean nextDay;
        private Category category = Category.OTHER;
        private LocalDate doneDay;
        private String seriesId;
        private Repeat repeat;
        private LocalDate from;
        private LocalDate due;
        private int points;
        private int exp;
        private LocalDate rewardDay;
        private PetKind rewardPet;
        private LocalDate today;

        private Spec(String title, LocalDate date) {
            this.title = title;
            this.date = date;
            this.id = title;
        }

        public Spec id(String v) { id = v; return this; }
        public Spec seq(long v) { seq = v; return this; }
        public Spec kind(Kind v) { kind = v; return this; }
        /** 시작 시각 "HH:mm". */
        public Spec at(String hhmm) { return at(LocalTime.parse(hhmm)); }
        public Spec at(LocalTime v) { start = v; return this; }
        public Spec minutes(int v) { minutes = v; return this; }
        public Spec memo(String v) { memo = v; return this; }
        public Spec nextDay() { nextDay = true; return this; }
        public Spec category(Category v) { category = v; return this; }
        public Spec done(LocalDate day) { doneDay = day; return this; }

        public Spec series(String sid, Repeat r) {
            seriesId = sid;
            repeat = r;
            return this;
        }

        /** 기간 할 일. last가 null이면 기간이 없다. draft()는 date를 첫날로 써서 first를 쓰지 않는다. */
        public Spec period(LocalDate first, LocalDate last) {
            from = first;
            due = last;
            return this;
        }

        public Spec reward(int p, int e, LocalDate day, PetKind pet) {
            points = p;
            exp = e;
            rewardDay = day;
            rewardPet = pet;
            return this;
        }

        /** draft()의 오늘. */
        public Spec today(LocalDate v) { today = v; return this; }

        public Task task() {
            return new Task(id, seq, kind, title, date, start, minutes, memo, nextDay, category, seriesId, repeat, due,
                    due == null ? null : from, doneDay != null, doneDay, points, exp, rewardDay, rewardPet);
        }

        public TaskDraft draft() {
            return new TaskDraft(kind, title, category, date, due, start, minutes, memo, repeat, null, nextDay, null,
                    today, false);
        }
    }
}
