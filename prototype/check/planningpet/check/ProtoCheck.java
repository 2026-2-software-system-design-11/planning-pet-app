package planningpet.check;

import planningpet.app.*;
import planningpet.app.controller.AppController;
import planningpet.app.model.*;
import planningpet.app.view.App;
import planningpet.common.model.Rules;
import planningpet.common.view.Ui;
import planningpet.pet.model.*;
import planningpet.stats.model.Stats;
import planningpet.pet.view.PetView;
import planningpet.reward.model.*;
import planningpet.reward.view.WalkBar;
import planningpet.schedule.model.*;
import planningpet.schedule.view.*;
import planningpet.shop.model.Shop;

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
        eq("T-LVL-3", "경험치 0 / 80", Level.expText(50));
        eq("T-LVL-7", "최고 레벨", Level.expText(1600));
        eq("T-TALK-3", "콩이야 나비야", "콩" + Rules.iya("콩") + " 나비" + Rules.iya("나비"));
        eq("메인 브리핑", "오늘 할 일 3개 남았어. '은행 서류' 마감이 내일이야.", new AppController(s, t).windowShown());
        eq("T-VIS-2", 5, s.streak);
        eq("T-ATT-1", "true 120", g.attend() + " " + s.points);
        Shop shop = new Shop(s);
        eq("T-SHOP-1", "0 0", shop.buy("액자") + " " + s.points);
        eq("T-SHOP-2, T-SHOP-4", "150 -1", shop.buy("조명") + " " + shop.buy("쿠션"));
        eq("삭제 확인 조사", "을 는", Rules.josa("자소서 초안", "을", "를") + " " + Rules.josa("자료구조", "은", "는"));
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
        p.book.add(Task.Kind.FIXED, "수업", nov16, java.time.LocalTime.of(16, 0), 90, "", null);
        p.book.add(Task.Kind.TASK, "끝낸 일", nov16, java.time.LocalTime.of(18, 0), 60, "", null).get(0).done = true;
        Task a = p.book.add(Task.Kind.TASK, "A", nov16, null, 60, "", null).get(0);
        Task b = p.book.add(Task.Kind.TASK, "B", nov16, null, 120, "", null).get(0);
        var placed = Planner.place(p, nov16).placed();
        eq("T-AUTO-3 개수", 2, placed.size());
        eq("T-AUTO-3", "B 19:00 A 21:00", placed.get(0).task().title + " " + placed.get(0).start() + " "
                + placed.get(1).task().title + " " + placed.get(1).start());
        a.start = java.time.LocalTime.of(9, 0);
        b.start = java.time.LocalTime.of(9, 0);
        var cols = Planner.columns(java.util.List.of(a, b));
        eq("T-TT-5", "1/2 0/2", cols.get(a)[0] + "/" + cols.get(a)[1] + " " + cols.get(b)[0] + "/" + cols.get(b)[1]);
        Store first = MockData.empty();
        first.pets.clear();
        first.pets.add(new planningpet.pet.model.PetState("DOG", "바둑이"));
        first.activeKind = "DOG";
        eq("T-BRF-6, FIRST_RUN", "안녕, 나는 바둑이야. 일정관리에서 이번 학기 수업부터 넣어 볼까? 1",
                new AppController(first, new Talk(first)).windowShown() + " " + first.streak);
        Store d = MockData.empty();
        java.util.function.BiFunction<String, java.time.LocalTime, Task> at = (title, time) ->
                d.book.add(Task.Kind.TASK, title, nov16, time, 30, "", null).get(0);
        at.apply("B", null);
        at.apply("D", java.time.LocalTime.of(15, 0));
        at.apply("A", java.time.LocalTime.of(15, 0));
        at.apply("C", java.time.LocalTime.of(9, 0));
        at.apply("F", null).done = true;
        d.book.add(Task.Kind.FIXED, "E", nov16, java.time.LocalTime.of(10, 30), 75, "", null);
        TaskBook.DayPlan plan = d.book.dayPlan(nov16);
        eq("T-LIST-1", "CEDA BF", plan.timed().stream().map(x -> x.title).reduce("", String::concat) + " "
                + plan.untimed().stream().map(x -> x.title).reduce("", String::concat));
        Task late = at.apply("늦게", java.time.LocalTime.of(23, 30));
        eq("T-LIST-2", "23:30~24:00", TaskBook.timeText(late));
        java.time.LocalDateTime t1523 = nov16.atTime(15, 23);
        eq("T-DEF-6~9", "15:25 09:00 09:00 23:55", DefaultDates.start(nov16, t1523) + " "
                + DefaultDates.start(nov16, nov16.atTime(7, 0)) + " " + DefaultDates.start(nov16.plusDays(1), t1523)
                + " " + DefaultDates.start(nov16, nov16.atTime(23, 57)));
        TimeEditor longOne = new TimeEditor(late, t1523);
        longOne.minutes.setValue(40);
        eq("시각 고치기 하루 넘김", "하루 안에 끝나야 해요", longOne.check());
        eq("시간 빼기는 할 일만", "true false", longOne.clear.isVisible() + " "
                + new TimeEditor(d.book.dayPlan(nov16).timed().get(1), t1523).clear.isVisible());
        Store r = MockData.empty();
        r.now = nov16.atTime(15, 0);
        Game rg = new Game(r);
        Task x = r.book.add(Task.Kind.TASK, "과제", nov16, null, 30, "", null).get(0);
        RewardResult done1 = rg.complete(x);
        eq("T-RWD-1", "REWARDED 10 10 1", done1.outcome() + " " + r.points + " " + r.pet().exp + " " + r.rewardCount);
        rg.uncomplete(x);
        eq("T-UNDO-1", "0 0 0 false", r.points + " " + r.pet().exp + " " + r.rewardCount + " " + x.done);
        r.rewardCount = 20;
        eq("T-RWD-5", "CAPPED 0", rg.complete(x).outcome() + " " + r.points);
        rg.uncomplete(x);
        r.rewardCount = 0;
        rg.complete(x);
        r.book.delete(x, false);
        eq("T-DEL-1", "10 1", r.points + " " + r.rewardCount);
        eq("숫자 조사", "을 를", Rules.josa("과제 1", "을", "를") + " " + Rules.josa("과제 2", "을", "를"));
        eq("D-38 5분 단위", "30 null 75", fiveMinute());
        Store c = MockData.sample();
        java.time.LocalDate nov = java.time.LocalDate.of(2026, 11, 1);
        TaskBook.CellPreview c14 = c.book.cellPreview(nov.withDayOfMonth(14));
        TaskBook.CellPreview c13 = c.book.cellPreview(nov.withDayOfMonth(13));
        TaskBook.CellPreview c15 = c.book.cellPreview(nov.withDayOfMonth(15));
        eq("T-CAL-1", "알고리즘… true", c14.title() + " " + c14.open());
        eq("T-CAL-2", null, c.book.cellPreview(nov.withDayOfMonth(16)));
        eq("T-CAL-3", "토익 스터… false", c13.title() + " " + c13.open());
        eq("T-CAL-4", "은행 서류 true", c15.title() + " " + c15.open());
        Store w = MockData.sample();
        int before = w.listenerCount();
        new WalkBar(w);
        new PetView(w, 1);
        eq("T-SAVE-5 다시 그리기 연결", 2, w.listenerCount() - before);
        App main = Main.create(MockData.sample());
        Task essay = main.store.book.dayPlan(main.store.today()).timed().get(1);
        int seq = main.store.talkSeq;
        main.schedule.check(essay);
        long doneToday = main.store.book.checkables(main.store.today()).stream().filter(z -> z.done).count();
        eq("T-SAVE-5", "110 2/4 잘했어! +10P 1", main.store.points + " " + doneToday + "/"
                + main.store.book.checkables(main.store.today()).size() + " " + main.store.talk + " " + (main.store.talkSeq - seq));
        main.schedule.check(essay);
        eq("T-TALK-7 화면에서", "100 체크를 풀었어. -10P", main.store.points + " " + main.store.talk);
        Store od = MockData.sample();
        eq("T-TALK-9", "11월 16일 할 일은 2개야.", new Talk(od).otherDate(java.time.LocalDate.of(2026, 11, 16)));
        Store f2 = MockData.empty();
        f2.now = nov16.atTime(9, 0);
        Task cls = f2.book.add(Task.Kind.FIXED, "수업", nov16, java.time.LocalTime.of(10, 0), 75, "", null).get(0);
        f2.book.add(Task.Kind.FIXED, "수업2", nov16, java.time.LocalTime.of(13, 0), 75, "", null);
        Task hw = f2.book.add(Task.Kind.TASK, "과제", nov16, null, 30, "", null).get(0);
        f2.book.add(Task.Kind.TASK, "운동", nov16, null, 30, "", null);
        Game fg = new Game(f2);
        eq("T-RWD-7", "REWARDED 10 10", fg.complete(cls).outcome() + " " + f2.points + " " + f2.pet().exp);
        fg.complete(hw);
        long doneCount = f2.book.checkables(nov16).stream().filter(z -> z.done).count();
        eq("T-PRG-3", "2/4", doneCount + "/" + f2.book.checkables(nov16).size());
        Talk ft = new Talk(f2);
        eq("T-TALK-7", "체크를 풀었어. -10P, 이제 Lv.1", ft.uncompleted(10, 2, 1));
        eq("T-TALK-8", "체크를 풀었어.", ft.uncompleted(0, 1, 1));
        eq("T-EDIT-7", "10:30 10:30 13:00 13:00 13:00 13:00 13:00", series(false));
        eq("T-EDIT-8", "대학수학 대학수학 대학수학 보강@12-01 대학수학 보강 대학수학 보강 대학수학 보강 대학수학 보강", series(true));
        eq("D-43 날짜 화살표는 하루씩", "2026-11-15", dayArrow());
        eq("D-43 오류 글 다시 검사, 30분 안내", "하루 안에 끝나야 해요 / true false", recheck());
        eq("D-42 반복 끝 밖으로 옮겨 저장, 반복 표시", "true true 2026-12-29", moveLastCopy());
        eq("D-42 시각 창의 이후 반복 모두는 시각만", "11:00 보강 11:00", timeOnly());
        newRules();
        System.out.println(failed == 0 ? "모두 통과" : failed + "개 실패");
        System.exit(failed == 0 ? 0 : 1);
    }

    /** T-ADD-3의 사본 7개에서 11-30 사본을 THIS_AND_AFTER로 고친다(06의 4번, D-42). */
    private static String series(boolean moveAndRename) {
        Store s = MockData.empty();
        java.util.List<Task> copies = s.book.add(Task.Kind.FIXED, "대학수학", java.time.LocalDate.of(2026, 11, 16),
                java.time.LocalTime.of(10, 30), 75, "", java.time.LocalDate.of(2026, 12, 28));
        Task t = copies.get(2);
        java.time.LocalDate original = t.date;
        if (moveAndRename) {
            t.date = java.time.LocalDate.of(2026, 12, 1);
            t.title = "대학수학 보강";
        } else {
            t.start = java.time.LocalTime.of(13, 0);
        }
        s.book.applyToSeries(t, original);
        StringBuilder out = new StringBuilder();
        for (Task c : copies) {
            out.append(moveAndRename ? c.title + (c == t ? "@" + c.date.toString().substring(5) : "") : c.start.toString()).append(" ");
        }
        return out.toString().strip();
    }

    /** 반복의 마지막 사본을 수정 창에서 하루 뒤로 옮겨 이 일정만으로 저장한다. 수정할 때 반복 칸은 검사하지 않는다. */
    private static String moveLastCopy() {
        App app = Main.create(MockData.empty());
        java.util.List<Task> copies = app.store.book.add(Task.Kind.FIXED, "대학수학", java.time.LocalDate.of(2026, 11, 16),
                java.time.LocalTime.of(10, 30), 75, "", java.time.LocalDate.of(2026, 12, 28));
        Task last = copies.get(copies.size() - 1);
        TaskForm f = new TaskForm(last, last.date, app.store.now);
        f.showSeries(app.store.book.seriesEnd(last));
        boolean shown = f.repeat.isSelected() && !f.repeat.isEnabled();
        Ui.setDate(f.date, java.time.LocalDate.of(2026, 12, 29));
        app.schedule.chooser = (title, message, options) -> 0;
        boolean saved = TaskDialog.trySave(app, f, last);
        return shown + " " + saved + " " + last.date;
    }

    /** 뒤 사본의 제목을 따로 고쳐 둔 뒤, 앞 사본을 시각 고치기 창에서 이후 반복 모두로 고친다. */
    private static String timeOnly() {
        App app = Main.create(MockData.empty());
        java.util.List<Task> copies = app.store.book.add(Task.Kind.FIXED, "대학수학", java.time.LocalDate.of(2026, 11, 16),
                java.time.LocalTime.of(10, 30), 75, "", java.time.LocalDate.of(2026, 11, 30));
        copies.get(1).title = "보강";
        TimeEditor e = new TimeEditor(copies.get(0), app.store.now);
        e.start.setValue("11:00");
        app.schedule.chooser = (title, message, options) -> 1;
        TimeEditor.save(app, copies.get(0), e);
        return copies.get(0).start + " " + copies.get(1).title + " " + copies.get(1).start;
    }

    /** 일정 입력 창 날짜 칸의 위 화살표가 바꿀 값. DateEditor가 아니라서 커서 자리와 관계없이 하루다. */
    private static String dayArrow() {
        TaskForm f = new TaskForm(null, java.time.LocalDate.of(2026, 11, 14), java.time.LocalDateTime.of(2026, 11, 14, 15, 23));
        boolean dateEditor = f.date.getEditor() instanceof javax.swing.JSpinner.DateEditor;
        java.util.Date next = (java.util.Date) f.date.getNextValue();
        return dateEditor ? "DateEditor" : next.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate().toString();
    }

    /** 저장 뒤 오류 글이 칸을 고칠 때 다시 정해지는지, 30분 안내가 값이 30일 때만 보이는지. */
    private static String recheck() {
        TaskForm f = new TaskForm(null, java.time.LocalDate.of(2026, 11, 16), java.time.LocalDateTime.of(2026, 11, 16, 9, 0));
        f.timed.setSelected(true);
        f.sync();
        f.start.setValue("23:30");
        f.minutes.setValue(40);
        f.showErrors(TaskCheck.check(f.draft(), f.editing()));
        f.title.setText("밤 공부");
        String afterTitle = f.errorText();
        boolean hintAt30 = f.minutesHint.isVisible();
        f.minutes.setValue(30);
        return afterTitle + " / " + f.minutesHint.isVisible() + " " + hintAt30;
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
        var p = TaskCheck.check(f.draft(), f.editing());
        return p.isEmpty() ? null : String.join(" / ", p.stream().map(x -> x[1]).toList());
    }

    /** D-48~D-55의 규칙. */
    static void newRules() {
        App app = Main.create(MockData.sample());
        Store s = app.store;
        int catExp = s.pet().exp;
        planningpet.pet.view.PetScreen room = find(app.root, planningpet.pet.view.PetScreen.class);
        eq("D-56 방 말풍선 평소", "", room.room.bubble);
        eq("D-48 데려오기", "true false 2 DOG 바둑이", app.pet.adopt(true, " ") + " " + app.pet.adopt(true, "x") + " "
                + s.pets.size() + " " + s.activeKind + " " + s.pet().name);
        eq("D-56 데려온 직후 인사", "안녕, 나는 바둑이야. 잘 부탁해!", room.room.bubble);
        Task t = s.book.add(Task.Kind.TASK, "보고서", s.today(), null, 30, "", null).get(0);
        app.schedule.check(t);
        eq("D-48 경험치는 고른 펫", "10 " + catExp + " DOG", s.pet("DOG").exp + " " + s.pet("CAT").exp + " " + t.rewardPet);
        app.pet.select("CAT");
        app.schedule.check(t);
        eq("D-48 취소는 받은 펫에서", "0 " + catExp, s.pet("DOG").exp + " " + s.pet("CAT").exp);
        app.pet.step(1);
        eq("D-56 화살표", "DOG", s.activeKind);
        java.util.List<Task> days = new java.util.ArrayList<>();
        for (int i = 0; i < 40; i++) {
            Task d = new Task("b" + i, 900 + i, Task.Kind.TASK, "b", s.today().minusDays(i), null, 30);
            d.rewardExp = 10;
            d.rewardPet = "CAT";
            d.rewardDay = d.date;
            days.add(d);
        }
        eq("D-49 친밀도", "50 100 0", Bond.percent(days.subList(0, 15), "CAT") + " " + Bond.percent(days, "CAT") + " "
                + Bond.percent(days, "DOG"));
        java.util.Map<String, String> want = new java.util.LinkedHashMap<>();
        want.put(Wardrobe.NECK, "별 목걸이");
        want.put(Wardrobe.HAT, "밀짚모자");
        want.put(Wardrobe.CLOTHES, "스카프");
        app.pet.select("CAT");
        app.pet.applyWear("CAT", want);
        eq("D-51 열린 것만 입는다(Lv.3)", "{모자=밀짚모자, 의상=스카프}", new java.util.TreeMap<>(s.pet().wear).toString());
        eq("D-52 아이템 13개, 합 2210P", "13 2210", Shop.ITEMS.size() + " " + Shop.ITEMS.values().stream().mapToInt(i -> i).sum());
        eq("D-52 만족도", "0 14 100", Shop.satisfaction(java.util.List.of()) + " " + Shop.satisfaction(java.util.List.of("소파"))
                + " " + Shop.satisfaction(Shop.ITEMS.keySet()));
        eq("D-53 찾기", "털실 공 뼈다귀 / 걸이 화분", names(Shop.find("장난감", "", false, s.owned)) + " / "
                + names(Shop.find("전체", "걸이", false, s.owned)));
        Store e = MockData.empty();
        Task past = e.book.add(Task.Kind.TASK, "a", e.today().minusDays(1), null, 30, "", null).get(0);
        past.done = true;
        past.category = "운동";
        e.book.add(Task.Kind.TASK, "b", e.today(), null, 30, "", null);
        e.book.add(Task.Kind.TASK, "c", e.today().plusDays(1), null, 30, "", null);
        Stats.Period w = Stats.Period.week(e.today());
        eq("D-55 앞날은 세지 않는다", "1/2 50%", Stats.rate(e, w).done() + "/" + Stats.rate(e, w).total() + " " + Stats.rate(e, w).percent() + "%");
        eq("D-55 유형", "{운동=Rate[done=1, total=1], 기타=Rate[done=0, total=1]}", Stats.byCategory(e, w).toString());
        eq("D-55 주간 칸", "7", String.valueOf(Stats.trend(e, w).size()));
        // 06 27~30번의 나머지 검사 줄.
        eq("T-SHOP-10 보유만 찾기", "쿠션", names(Shop.find("전체", "", true, java.util.Set.of("쿠션"))));
        java.util.List<Task> two = new java.util.ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Task d = new Task("c" + i, 950 + i, Task.Kind.TASK, "c", java.time.LocalDate.of(2026, 11, 16), null, 30);
            d.rewardExp = 10;
            d.rewardPet = "CAT";
            d.rewardDay = d.date;
            two.add(d);
        }
        eq("T-BOND-2 같은 날 두 번", "1 3", Bond.days(two, "CAT") + " " + Bond.percent(two, "CAT"));
        eq("T-WEAR-2 처음 장신구", "{목걸이=기본 목걸이}", new PetState("CAT", "나비").wear.toString());
        Store top = MockData.sample();
        top.pet().exp = 1600;
        App topApp = Main.create(top);
        java.util.Map<String, String> all = new java.util.LinkedHashMap<>();
        all.put(Wardrobe.NECK, "왕관 목걸이");
        all.put(Wardrobe.ACCESSORY, "안경");
        all.put(Wardrobe.HAT, "왕관");
        all.put(Wardrobe.CLOTHES, "망토");
        topApp.pet.applyWear("CAT", all);
        eq("T-WEAR-3 레벨 10은 넷 다", all.toString(), top.pet().wear.toString());
        Store none = MockData.empty();
        Stats.Period nov = Stats.Period.month(java.time.LocalDate.of(2026, 11, 14));
        StringBuilder cells = new StringBuilder();
        Stats.trend(none, nov).forEach(b -> cells.append(b.from().getDayOfMonth()).append("-").append(b.to().getDayOfMonth()).append(" "));
        eq("T-STAT-4 월간 칸", "1-1 2-8 9-15 16-22 23-29 30-30 ", cells.toString());
        Store pts = MockData.empty();
        pts.now = java.time.LocalDateTime.of(2026, 11, 18, 12, 0);
        pts.attendDays.add(java.time.LocalDate.of(2026, 11, 16));
        pts.attendDays.add(java.time.LocalDate.of(2026, 11, 17));
        for (int i = 0; i < 3; i++) {
            Task d = pts.book.add(Task.Kind.TASK, "p" + i, java.time.LocalDate.of(2026, 11, 16 + i), null, 30, "", null).get(0);
            d.done = true;
            d.rewardPoints = 10;
            d.rewardDay = d.date;
        }
        Stats.Period week = Stats.Period.week(pts.today());
        eq("T-STAT-5 주간 포인트", "70", String.valueOf(Stats.points(pts, week)));
        pts.book.delete(pts.tasks.get(0), false);
        eq("T-STAT-7 지워도 포인트는 그대로(D-19)", "70", String.valueOf(Stats.points(pts, week)));
        Stats.Rate zero = Stats.rate(none, Stats.Period.week(none.today()));
        eq("T-STAT-6 일정 없음", "0/0 0%", zero.done() + "/" + zero.total() + " " + zero.percent() + "%");
        java.util.List<Task> kept = new java.util.ArrayList<>(two);
        Store bs = MockData.empty();
        Task bt = bs.book.add(Task.Kind.TASK, "b", bs.today(), null, 30, "", null).get(0);
        bt.done = true;
        bt.rewardExp = 10;
        bt.rewardPet = "CAT";
        bt.rewardDay = bs.today();
        bs.book.delete(bt, false);
        eq("T-BOND-3 지워도 친밀도는 그대로(D-19, D-49)", "1", String.valueOf(Bond.days(bs.book.rewardHistory(), "CAT")));
        App pa = Main.create(MockData.sample());
        pa.pet.rename("CAT", "  ");
        String blank = pa.store.pet().name;
        pa.pet.rename("CAT", "가나다라마바사아자차카");
        eq("T-PET-3 이름", "나비 가나다라마바사아자차", blank + " " + pa.store.pet().name);
        pa.pet.pose("CAT", "자기");
        pa.pet.pose("CAT", "춤");
        eq("T-PET-4 자세", "자기", pa.store.pet().pose);
        pa.pet.select("DOG");
        eq("T-PET-5 없는 펫은 못 고른다", "CAT", pa.store.activeKind);
    }

    static <T> T find(java.awt.Component c, Class<T> type) {
        if (type.isInstance(c)) {
            return type.cast(c);
        }
        if (c instanceof java.awt.Container k) {
            for (java.awt.Component child : k.getComponents()) {
                T t = find(child, type);
                if (t != null) {
                    return t;
                }
            }
        }
        return null;
    }

    private static String names(java.util.List<Shop.Item> items) {
        return String.join(" ", items.stream().map(Shop.Item::name).toList());
    }
}
