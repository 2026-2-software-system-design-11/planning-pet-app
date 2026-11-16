package planningpet.proto;

/** 06 검사 표 가운데 프로토타입이 따르는 줄을 확인한다. JUnit 없이 java로 돌린다. */
public final class ProtoCheck {
    private static int failed;

    static void eq(String id, Object want, Object got) {
        if (!java.util.Objects.equals(want == null ? null : String.valueOf(want), got == null ? null : String.valueOf(got))) {
            failed++;
            System.out.println("실패 " + id + ": 기대 " + want + ", 실제 " + got);
        }
    }

    public static void main(String[] args) {
        Store s = MockData.sample();
        Game g = new Game(s);
        Talk t = new Talk(s);
        eq("T-DAY-1", "2026-11-15", Rules.appDay(java.time.LocalDateTime.of(2026, 11, 16, 3, 59)));
        eq("T-LVL-3", "경험치 0 / 80", Rules.expText(50));
        eq("T-LVL-7", "최고 레벨", Rules.expText(1600));
        eq("T-TALK-3", "콩이야 나비야", "콩" + Rules.iya("콩") + " 나비" + Rules.iya("나비"));
        eq("메인 브리핑", "오늘 할 일 3개 남았어. '은행 서류' 마감이 내일이야.", t.onWindowShown(g));
        eq("T-VIS-2", 5, s.streak);
        eq("T-ATT-1", "true 120", g.attend() + " " + s.points);
        eq("T-SHOP-1", "0 0", g.buy("액자") + " " + s.points);
        eq("T-SHOP-2, T-SHOP-4", "150 -1", g.buy("조명") + " " + g.buy("쿠션"));
        eq("삭제 확인 조사", "을 는", Actions.josa("자소서 초안", "을", "를") + " " + Actions.josa("자료구조", "은", "는"));
        TaskForm f = new TaskForm(null, s.today(), s.now);
        eq("T-VAL-2", "제목을 써 주세요", msgs(f));
        f.title.setText("밤 과제");
        f.timed.setSelected(true);
        f.start.setValue("23:30");
        eq("T-VAL-7", null, msgs(f));
        f.minutes.setValue(40);
        eq("T-VAL-8", "하루 안에 끝나야 해요", msgs(f));
        f.title.setText("");
        eq("T-VAL-14", "제목을 써 주세요 / 하루 안에 끝나야 해요", msgs(f));
        java.time.LocalDate nov16 = java.time.LocalDate.of(2026, 11, 16);
        Store p = MockData.empty();
        p.now = nov16.atTime(15, 23);
        p.add(Task.Kind.FIXED, "수업", nov16, java.time.LocalTime.of(16, 0), 90, "", null);
        p.add(Task.Kind.TASK, "끝낸 일", nov16, java.time.LocalTime.of(18, 0), 60, "", null).get(0).done = true;
        Task a = p.add(Task.Kind.TASK, "A", nov16, null, 60, "", null).get(0);
        Task b = p.add(Task.Kind.TASK, "B", nov16, null, 120, "", null).get(0);
        var placed = Planner.place(p, nov16).placed();
        eq("T-AUTO-3 개수", 2, placed.size());
        eq("T-AUTO-3", "B 19:00 A 21:00", placed.get(0).task().title + " " + placed.get(0).start() + " "
                + placed.get(1).task().title + " " + placed.get(1).start());
        a.start = java.time.LocalTime.of(9, 0);
        b.start = java.time.LocalTime.of(9, 0);
        var cols = Planner.columns(java.util.List.of(a, b));
        eq("T-TT-5", "1/2 0/2", cols.get(a)[0] + "/" + cols.get(a)[1] + " " + cols.get(b)[0] + "/" + cols.get(b)[1]);
        Store first = MockData.empty();
        first.petKind = "DOG";
        first.petName = "바둑이";
        eq("T-BRF-6, FIRST_RUN", "안녕, 나는 바둑이야. 일정관리에서 이번 학기 수업부터 넣어 볼까? 1",
                new Talk(first).onWindowShown(new Game(first)) + " " + first.streak);
        Store d = MockData.empty();
        java.util.function.BiFunction<String, java.time.LocalTime, Task> at = (title, time) ->
                d.add(Task.Kind.TASK, title, nov16, time, 30, "", null).get(0);
        at.apply("B", null);
        at.apply("D", java.time.LocalTime.of(15, 0));
        at.apply("A", java.time.LocalTime.of(15, 0));
        at.apply("C", java.time.LocalTime.of(9, 0));
        at.apply("F", null).done = true;
        d.add(Task.Kind.FIXED, "E", nov16, java.time.LocalTime.of(10, 30), 75, "", null);
        Store.DayPlan plan = d.dayPlan(nov16);
        eq("T-LIST-1", "CEDA BF", plan.timed().stream().map(x -> x.title).reduce("", String::concat) + " "
                + plan.untimed().stream().map(x -> x.title).reduce("", String::concat));
        Task late = at.apply("늦게", java.time.LocalTime.of(23, 30));
        eq("T-LIST-2", "23:30~24:00", Store.timeText(late));
        java.time.LocalDateTime t1523 = nov16.atTime(15, 23);
        eq("T-DEF-6~9", "15:25 09:00 09:00 23:55", TimeEditor.defaultStart(nov16, t1523) + " "
                + TimeEditor.defaultStart(nov16, nov16.atTime(7, 0)) + " " + TimeEditor.defaultStart(nov16.plusDays(1), t1523)
                + " " + TimeEditor.defaultStart(nov16, nov16.atTime(23, 57)));
        TimeEditor longOne = new TimeEditor(late, t1523);
        longOne.minutes.setValue(40);
        eq("시각 고치기 하루 넘김", "하루 안에 끝나야 해요", longOne.check());
        eq("시간 빼기는 할 일만", "true false", longOne.clear.isVisible() + " "
                + new TimeEditor(d.dayPlan(nov16).timed().get(1), t1523).clear.isVisible());
        Store r = MockData.empty();
        r.now = nov16.atTime(15, 0);
        Game rg = new Game(r);
        Task x = r.add(Task.Kind.TASK, "과제", nov16, null, 30, "", null).get(0);
        RewardResult done1 = rg.complete(x);
        eq("T-RWD-1", "REWARDED 10 10 1", done1.outcome() + " " + r.points + " " + r.exp + " " + r.rewardCount);
        rg.uncomplete(x);
        eq("T-UNDO-1", "0 0 0 false", r.points + " " + r.exp + " " + r.rewardCount + " " + x.done);
        r.rewardCount = 20;
        eq("T-RWD-5", "CAPPED 0", rg.complete(x).outcome() + " " + r.points);
        rg.uncomplete(x);
        r.rewardCount = 0;
        rg.complete(x);
        r.delete(x, false);
        eq("T-DEL-1", "10 1", r.points + " " + r.rewardCount);
        eq("숫자 조사", "을 를", Actions.josa("과제 1", "을", "를") + " " + Actions.josa("과제 2", "을", "를"));
        eq("D-38 5분 단위", "30 null 75", fiveMinute());
        Store c = MockData.sample();
        java.time.LocalDate nov = java.time.LocalDate.of(2026, 11, 1);
        Store.CellPreview c14 = c.cellPreview(nov.withDayOfMonth(14));
        Store.CellPreview c13 = c.cellPreview(nov.withDayOfMonth(13));
        Store.CellPreview c15 = c.cellPreview(nov.withDayOfMonth(15));
        eq("T-CAL-1", "알고리즘… true", c14.title() + " " + c14.open());
        eq("T-CAL-2", null, c.cellPreview(nov.withDayOfMonth(16)));
        eq("T-CAL-3", "토익 스터… false", c13.title() + " " + c13.open());
        eq("T-CAL-4", "은행 서류 true", c15.title() + " " + c15.open());
        Store w = MockData.sample();
        int before = w.listenerCount();
        new WalkBar(w);
        new PetView(w, 1);
        eq("T-SAVE-5 다시 그리기 연결", 2, w.listenerCount() - before);
        App main = Main.create(MockData.sample());
        Task essay = main.store.dayPlan(main.store.today()).timed().get(1);
        int seq = main.store.talkSeq;
        main.actions.check(essay);
        long doneToday = main.store.checkables(main.store.today()).stream().filter(z -> z.done).count();
        eq("T-SAVE-5", "110 2/4 잘했어! +10P 1", main.store.points + " " + doneToday + "/"
                + main.store.checkables(main.store.today()).size() + " " + main.store.talk + " " + (main.store.talkSeq - seq));
        main.actions.check(essay);
        eq("T-TALK-7 화면에서", "100 체크를 풀었어. -10P", main.store.points + " " + main.store.talk);
        Store od = MockData.sample();
        eq("T-TALK-9", "11월 16일 할 일은 2개야.", new Talk(od).otherDate(java.time.LocalDate.of(2026, 11, 16)));
        Store f2 = MockData.empty();
        f2.now = nov16.atTime(9, 0);
        Task cls = f2.add(Task.Kind.FIXED, "수업", nov16, java.time.LocalTime.of(10, 0), 75, "", null).get(0);
        f2.add(Task.Kind.FIXED, "수업2", nov16, java.time.LocalTime.of(13, 0), 75, "", null);
        Task hw = f2.add(Task.Kind.TASK, "과제", nov16, null, 30, "", null).get(0);
        f2.add(Task.Kind.TASK, "운동", nov16, null, 30, "", null);
        Game fg = new Game(f2);
        eq("T-RWD-7", "REWARDED 10 10", fg.complete(cls).outcome() + " " + f2.points + " " + f2.exp);
        fg.complete(hw);
        long doneCount = f2.checkables(nov16).stream().filter(z -> z.done).count();
        eq("T-PRG-3", "2/4", doneCount + "/" + f2.checkables(nov16).size());
        Talk ft = new Talk(f2);
        eq("T-TALK-7", "체크를 풀었어. -10P, 이제 Lv.1", ft.uncompleted(10, 2, 1));
        eq("T-TALK-8", "체크를 풀었어.", ft.uncompleted(0, 1, 1));
        System.out.println(failed == 0 ? "모두 통과" : failed + "개 실패");
        System.exit(failed == 0 ? 0 : 1);
    }

    /** 27은 칸이 받지 않아 30 그대로, 75는 받아서 검사를 통과한다. */
    private static String fiveMinute() {
        TaskForm f = new TaskForm(null, java.time.LocalDate.of(2026, 11, 16), java.time.LocalDateTime.of(2026, 11, 16, 9, 0));
        f.title.setText("과제");
        try {
            f.minutes.setValue(27);
        } catch (IllegalArgumentException ignored) {
            // 받지 않는다.
        }
        Object kept = f.minutes.getValue();
        f.minutes.setValue(75);
        return kept + " " + msgs(f) + " " + f.minutes.getValue();
    }

    /** 검사 문장을 칸 순서대로 " / "로 잇는다. 통과면 null. */
    private static String msgs(TaskForm f) {
        var p = TaskCheck.check(f);
        return p.isEmpty() ? null : String.join(" / ", p.stream().map(x -> x[1]).toList());
    }
}
