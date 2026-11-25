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
import planningpet.home.view.PetCard;
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
        eq("메인 브리핑", "오늘 할 일 3개 남았어. 내일 '은행 서류'가 있어.", new AppController(s, t).windowShown());
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
        eq("T-VAL-20 23:30 40분", null, msgs(f));
        f.minutes.setValue(90);
        eq("T-VAL-20 23:30 90분", null, msgs(f));
        f.start.setValue("03:30(다음 날)");
        f.minutes.setValue(30);
        eq("T-VAL-8 30분", null, msgs(f));
        f.minutes.setValue(40);
        eq("T-VAL-8", "다음 날 04:00까지 끝나야 해요", msgs(f));
        f.title.setText("");
        eq("T-VAL-14", "제목을 써 주세요 / 다음 날 04:00까지 끝나야 해요", msgs(f));
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
        eq("T-BRF-6, FIRST_RUN", "처음 만났네! 나는 바둑이야. 수업부터 넣어 볼까? 1",
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
        eq("T-DEF-6~9", "15:25 09:00 09:00 00:00(다음 날)", Times.value(DefaultDates.start(nov16, t1523)) + " "
                + Times.value(DefaultDates.start(nov16, nov16.atTime(7, 0))) + " "
                + Times.value(DefaultDates.start(nov16.plusDays(1), t1523))
                + " " + Times.value(DefaultDates.start(nov16, nov16.atTime(23, 57))));
        TimeEditor longOne = new TimeEditor(late, t1523);
        longOne.minutes.setValue(40);
        eq("D-77 시각 고치기 자정 넘김", null, longOne.check());
        longOne.start.setValue("03:30(다음 날)");
        eq("D-77 시각 고치기 04:00 넘김", "다음 날 04:00까지 끝나야 해요 (화 03:30)",
                longOne.check() + " " + longOne.realTime.getText());
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
        eq("T-TYPE-1", "25 5분 단위로 맞췄어요(25분) / 75 null", typed("27") + " / " + typed("75"));
        eq("T-TYPE-2", "720 최대 720분(12시간)까지 넣을 수 있어요 / 10 최소 10분부터 넣을 수 있어요", typed("730") + " / " + typed("5"));
        eq("T-TYPE-3", "null 숫자(분)로 넣어 주세요(예: 30) / null 숫자(분)로 넣어 주세요(예: 30) / 30 null",
                typed("반시간") + " / " + typed("30abc") + " / " + typed("30분"));
        eq("T-TYPE-4", "60 90 90 90 150", String.join(" ", java.util.stream.Stream.of("1시간", "1시간 30분", "1시간 반", "1.5시간", "2시간 반")
                .map(w -> String.valueOf(Typed.minutes(w).value())).toList()));
        Store c = MockData.sample();
        java.time.LocalDate nov = java.time.LocalDate.of(2026, 11, 1);
        TaskBook.CellPreview c14 = c.book.cellPreview(nov.withDayOfMonth(14));
        TaskBook.CellPreview c13 = c.book.cellPreview(nov.withDayOfMonth(13));
        TaskBook.CellPreview c15 = c.book.cellPreview(nov.withDayOfMonth(15));
        eq("T-CAL-1", "4개 true []", c14.text() + " " + c14.open() + " " + c14.due());
        TaskBook.CellPreview c16 = c.book.cellPreview(nov.withDayOfMonth(16), true);
        eq("T-CAL-2", "null 2개 true", c.book.cellPreview(nov.withDayOfMonth(16)) + " " + c16.text() + " " + c16.open());
        eq("T-CAL-3", "1개 false", c13.text() + " " + c13.open());
        eq("T-CAL-4", "1개 true", c15.text() + " " + c15.open());
        Store w = MockData.sample();
        int before = w.listenerCount();
        new PetCard(w, () -> { });
        new PetView(w, 1);
        eq("T-SAVE-5 다시 그리기 연결", 2, w.listenerCount() - before);
        App main = Main.create(MockData.sample());
        Task essay = main.store.book.dayPlan(main.store.today()).timed().get(1);
        int seq = main.store.talkSeq;
        PetCard card = find(main.root, PetCard.class);
        eq("D-59 펫 카드 처음", "나비 Lv. 3 경험치 80 / 110, 오늘 끝낸 일 1 / 4, 25% 4칸",
                card.nameText() + " " + card.levelText() + " " + card.expText() + ", " + card.doneText() + ", " + card.doneBarText());
        main.schedule.check(essay);
        long doneToday = main.store.book.checkables(main.store.today()).stream().filter(z -> z.done).count();
        eq("T-SAVE-5", "110 2/4 잘했어! +10P 1", main.store.points + " " + doneToday + "/"
                + main.store.book.checkables(main.store.today()).size() + " " + main.store.talk + " " + (main.store.talkSeq - seq));
        // 06의 22번 T-SAVE-5: 같은 알림에서 펫 카드의 "오늘 끝낸 일" 막대(2 / 4)와 레벨 막대가 함께 바뀐다.
        eq("T-SAVE-5 펫 카드", "경험치 90 / 110, 오늘 끝낸 일 2 / 4, 50% 4칸, 잘했어! +10P",
                card.expText() + ", " + card.doneText() + ", " + card.doneBarText() + ", " + card.bubbleText());
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
        eq("D-43 오류 글 다시 검사, 30분 안내", "다음 날 04:00까지 끝나야 해요 / true false", recheck());
        eq("D-42 반복 끝 밖으로 옮겨 저장, 반복 표시", "true true 2026-12-29", moveLastCopy());
        eq("D-42 시각 창의 이후 반복 모두는 시각만", "11:00 보강 11:00", timeOnly());
        newRules();
        PeriodCheck.run();
        DayCheck.run();
        HomeCheck.run();
        looks();
        ListCheck.run();
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
        f.showSeries(last.repeat, app.store.book.seriesEnd(last));
        boolean shown = "요일".equals(f.repeat.getSelectedItem()) && !f.repeat.isEnabled() && f.days[0].isSelected();
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
        f.start.setValue("03:30(다음 날)");
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

    /** D-97: 고르는 칸은 옆 제목 칸과 같은 테두리, 흰 바탕, 바탕 없는 꺾쇠다. 디자인 시스템을 걸어야 해서 맨 끝에 돈다. */
    private static void looks() {
        planningpet.common.view.Theme.install();
        TaskForm f = new TaskForm(null, java.time.LocalDate.of(2026, 11, 16), java.time.LocalDateTime.of(2026, 11, 16, 9, 0));
        for (javax.swing.JComboBox<String> c : java.util.List.of(f.category, f.repeat)) {
            javax.swing.AbstractButton arrow = (javax.swing.AbstractButton) java.util.Arrays.stream(c.getComponents())
                    .filter(k -> k instanceof javax.swing.AbstractButton).findFirst().orElseThrow();
            eq("D-97 고르는 칸", "ComboUI true ffffffff false",
                    c.getUI().getClass().getSimpleName() + " " + (c.getBorder() == f.title.getBorder()) + " "
                    + Integer.toHexString(c.getBackground().getRGB()) + " " + arrow.isContentAreaFilled());
        }
        // D-97: 통계 기타는 회색이 아닌 자기 색이고, 다른 유형과 색상각이 15도 넘게 다르다.
        float[] other = hsb(planningpet.stats.view.StatsArt.look("기타").bottom());
        StringBuilder near = new StringBuilder();
        for (String k : java.util.List.of("학업", "운동", "프로젝트", "개인/취미")) {
            double gap = Math.abs(hsb(planningpet.stats.view.StatsArt.look(k).bottom())[0] - other[0]) * 360;
            if (Math.min(gap, 360 - gap) <= 15) {
                near.append(k);
            }
        }
        eq("D-97 기타 색", "회색 아님, 가까운 유형 없음", (other[1] > 0.3 ? "회색 아님" : "회색") + ", 가까운 유형 " + (near.length() == 0 ? "없음" : near));
    }

    private static float[] hsb(java.awt.Color c) {
        return java.awt.Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
    }

    /** 걸리는 시간 칸에 쳐 넣은 글을 읽은 값과 칸 아래 글(06의 32번). */
    private static String typed(String text) {
        Typed.Read r = Typed.minutes(text);
        return r.value() + " " + r.notice();
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
        eq("D-56 데려온 직후 인사", "안녕, 나는 바둑이야. 이제 경험치는 내가 받을게!", room.room.bubble);
        Task t = s.book.add(Task.Kind.TASK, "보고서", s.today(), null, 30, "", null).get(0);
        app.schedule.check(t);
        eq("D-48 경험치는 고른 펫", "10 " + catExp + " DOG", s.pet("DOG").exp + " " + s.pet("CAT").exp + " " + t.rewardPet);
        app.pet.select("CAT");
        app.schedule.check(t);
        eq("D-48 취소는 받은 펫에서", "0 " + catExp, s.pet("DOG").exp + " " + s.pet("CAT").exp);
        app.pet.step(1);
        eq("D-80 캐릭터 창 화살표", "DOG", s.activeKind);
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
        e.book.add(Task.Kind.TASK, "b", e.today().minusDays(1), null, 30, "", null);
        e.book.add(Task.Kind.TASK, "b2", e.today(), null, 30, "", null);
        e.book.add(Task.Kind.TASK, "c", e.today().plusDays(1), null, 30, "", null);
        Stats.Period w = Stats.Period.week(e.today());
        eq("D-55 앞날은 세지 않는다", "1/2 50%", Stats.rate(e, w).done() + "/" + Stats.rate(e, w).total() + " " + Stats.rate(e, w).percent() + "%");
        eq("D-55 유형", "{운동=Rate[done=1, total=1], 기타=Rate[done=0, total=1]}", Stats.byCategory(e, w).toString());
        eq("D-55 주간 칸", "7", String.valueOf(Stats.trend(e, w).size()));
        Store td = MockData.empty();
        Task tdDone = td.book.add(Task.Kind.TASK, "끝냄", td.today(), null, 30, "", null).get(0);
        tdDone.done = true;
        Task tdLeft = td.book.add(Task.Kind.TASK, "남음", td.today(), null, 30, "", null).get(0);
        Stats.Period tw = Stats.Period.week(td.today());
        String before = Stats.rate(td, tw).done() + "/" + Stats.rate(td, tw).total() + " " + Stats.rate(td, tw).percent() + "%";
        tdLeft.done = true;
        eq("D-63 오늘은 끝낸 것만 센다", "1/1 100% 2/2", before + " " + Stats.rate(td, tw).done() + "/" + Stats.rate(td, tw).total());
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
        eq("05 캐릭터 창 7번 잠긴 장신구 툴팁 조사", "레벨 5가 되면 열려요 / 레벨 10이 되면 열려요 / 레벨 3이 되면 열려요",
                planningpet.pet.view.CharacterScreen.lockedTip(5) + " / " + planningpet.pet.view.CharacterScreen.lockedTip(10) + " / "
                + planningpet.pet.view.CharacterScreen.lockedTip(3));
        App sw = Main.create(MockData.sample());
        sw.pet.adopt(true, "");
        sw.pet.select("CAT");
        eq("T-PET-6 고르면 새 펫이 말한다", "이제 경험치는 내가 받을게! 오늘 할 일 3개 남았어.", sw.store.talk);
        sw.pet.rename("CAT", "보리");
        eq("T-PET-7 이름 바꾸면 새 이름으로 말한다", "이제 내 이름은 보리야.", sw.store.talk);
        App ca = Main.create(MockData.sample());
        ca.pet.adopt(true, "");
        planningpet.pet.view.PetScreen cr = find(ca.root, planningpet.pet.view.PetScreen.class);
        java.util.List<String> opened = new java.util.ArrayList<>();
        cr.characterOpener = dress -> opened.add(ca.store.activeKind + (dress ? " 꾸미기 탭" : " 상태 탭"));
        StringBuilder pills = new StringBuilder();
        for (planningpet.common.view.Pill pill : all(cr, planningpet.common.view.Pill.class)) {
            if (pill.small() != null) {
                pills.append(pill.getText()).append("(").append(pill.small()).append(") ");
            }
        }
        planningpet.home.view.PetCard homeCard = find(ca.root, planningpet.home.view.MainScreen.class).pet;
        eq("D-81 펫은 어디서나 지은 이름으로 부른다", "나비(고양이) 바둑이(강아지) 나비(고양이) 바둑이(강아지) / 바둑이 강아지 Lv. 1",
                pills + "/ " + homeCard.nameText() + " " + homeCard.kindText() + " " + homeCard.levelText());
        button(cr.room, "나비").doClick();
        eq("D-80 펫 칩은 고르기만 한다", "CAT ", ca.store.activeKind + " " + String.join(", ", opened));
        button(cr, "나비 꾸미기").doClick();
        eq("D-83 {이름} 꾸미기는 꾸미기 탭", "CAT 꾸미기 탭", String.join(", ", opened));
        opened.clear();
        find(ca.root, planningpet.home.view.MainScreen.class).pet.click();
        eq("D-80 홈 펫 카드를 누르면 캐릭터 창(상태 탭)", "CAT 상태 탭", String.join(", ", opened));
        eq("D-80 방 화살표는 없다", "true true", (tipButton(cr.room, "앞 펫") == null) + " " + (tipButton(cr.room, "다음 펫") == null));
        String talkBefore = ca.store.talk;
        cr.room.pat("DOG");
        String patted = cr.room.bubble + " " + cr.room.bubbleKind + " " + ca.store.activeKind + " " + talkBefore.equals(ca.store.talk);
        Task patTask = ca.store.book.add(Task.Kind.TASK, "반응 뒤", ca.store.today(), null, 30, "", null).get(0);
        ca.schedule.check(patTask);
        eq("D-80 방의 펫을 누르면 반응한다", "멍멍, 좋아! DOG CAT true / ", patted + " / " + cr.room.bubble);
        cr.character.opened(true);
        cr.character.opened(false);
        eq("D-93 캐릭터 창은 늘 상태 탭으로 열린다", "상태", cr.character.tab());
        cr.character.nextPet.doClick();
        eq("D-80 캐릭터 창 화살표는 펫을 바꾼다", "DOG " + ca.store.pet("CAT").pose, ca.store.activeKind + " " + ca.store.pet("CAT").pose);
        ca.pet.pose("DOG", "기지개");
        StringBuilder poseCells = new StringBuilder();
        for (javax.swing.JToggleButton b : all(cr.character, javax.swing.JToggleButton.class)) {
            if (Poses.ALL.contains(b.getText())) {
                poseCells.append(b.getText()).append(b.isSelected() ? "(고름) " : " ");
            }
        }
        eq("D-84 자세 칸은 이름을 늘 적고 고른 칸을 보인다", "앉기 자기 기지개(고름) 밥 먹기 노래 ", poseCells.toString());
        eq("D-93 캐릭터 창은 메인 창의 3분의 2쯤, 최소 800x580", "800x580 960x600", sizeText(planningpet.pet.view.CharacterDialog.size(1000, 680))
                + " " + sizeText(planningpet.pet.view.CharacterDialog.size(1440, 900)));
        StringBuilder fills = new StringBuilder();
        for (String kind : new String[] {"CAT", "DOG"}) {
            Store small = MockData.sample();
            small.pets.clear();
            small.pets.add(new PetState(kind, "작은 펫"));
            small.activeKind = kind;
            App sa = Main.create(small);
            planningpet.home.view.PetCard card = find(sa.root, planningpet.home.view.MainScreen.class).pet;
            for (java.awt.Dimension d : new java.awt.Dimension[] {card.getMinimumSize(), card.getPreferredSize(), new java.awt.Dimension(700, 400)}) {
                card.setSize(d);
                fills.append(card.petFill() > 2 / 3.0 ? "넘음 " : String.format("%.2f ", card.petFill()));
            }
        }
        eq("D-93 홈 펫 카드의 1단계 펫은 카드 칸의 3분의 2 넘게", "넘음 넘음 넘음 넘음 넘음 넘음 ", fills.toString());
        planningpet.shop.view.ShopPanel shopPanel = cr.shop;
        String menu0 = shopPanel.shopMode.isSelected() + " " + shopPanel.ownedMode.isSelected() + " " + shopPanel.previewToggle.isSelected();
        shopPanel.ownedMode.doClick();
        String menu1 = shopPanel.shopMode.isSelected() + " " + shopPanel.ownedMode.isSelected() + " " + shopPanel.previewToggle.isSelected();
        shopPanel.previewToggle.doClick();
        String menu2 = shopPanel.shopMode.isSelected() + " " + shopPanel.ownedMode.isSelected() + " " + shopPanel.previewToggle.isSelected();
        eq("D-82 상점 탭은 하나만 눌리고 미리보기는 따로 켜고 끄는 스위치", "true false true / false true true / false true false / Switch",
                menu0 + " / " + menu1 + " / " + menu2 + " / " + shopPanel.previewToggle.getClass().getSimpleName());
        shopPanel.shopMode.doClick();
        shopPanel.previewToggle.doClick();
        Store sv = MockData.sample();
        sv.points = 100;
        Shop svShop = new Shop(sv);
        eq("D-85 되팔기", "50 150 false false", svShop.sell("쿠션") + " " + sv.points + " " + sv.owned.contains("쿠션") + " " + sv.placed.contains("쿠션"));
        eq("T-SHOP-12 없는 것은 못 판다", "-1 150", svShop.sell("소파") + " " + sv.points);
        App bq = Main.create(MockData.sample());
        bq.store.points = 100;
        planningpet.shop.view.ShopPanel buyPanel = find(bq.root, planningpet.pet.view.PetScreen.class).shop;
        java.util.List<String> asks = new java.util.ArrayList<>();
        boolean[] answer = {false};
        buyPanel.ask = (title, message, yes) -> {
            asks.add(title + " / " + message + " / " + yes);
            return answer[0];
        };
        buyPanel.select("털실 공");
        buyPanel.action.doClick();
        String afterCancel = bq.store.owned.contains("털실 공") + " " + bq.store.points;
        answer[0] = true;
        buyPanel.action.doClick();
        eq("D-85 구매하기는 확인 창을 거친다", "아이템 사기 / 털실 공을 40P에 살까요? 남는 포인트 60P / 구매하기, false 100, true 60",
                asks.get(0) + ", " + afterCancel + ", " + bq.store.owned.contains("털실 공") + " " + bq.store.points);
        String sellInShop = buyPanel.sell.isVisible() + "";
        buyPanel.ownedMode.doClick();
        buyPanel.select(null);
        buyPanel.select("털실 공");
        String sellText = buyPanel.sell.isVisible() + " " + buyPanel.sell.getText();
        buyPanel.sell.doClick();
        eq("D-85 되팔기는 보유한 아이템에서", "false / true 되팔기 +40P / false false 100", sellInShop + " / " + sellText + " / "
                + bq.store.owned.contains("털실 공") + " " + bq.store.placed.contains("털실 공") + " " + bq.store.points);
        buyPanel.shopMode.doClick();
        buyPanel.select("우드 침대");
        eq("D-85 모자라면 흐리고 모자란 포인트", "false 300P 모자라요", buyPanel.action.isEnabled() + " " + buyPanel.lack.getText());
        App one = Main.create(MockData.sample());
        planningpet.pet.view.PetScreen oneRoom = find(one.root, planningpet.pet.view.PetScreen.class);
        oneRoom.character.opened(false);
        eq("D-80 펫이 하나면 캐릭터 창 화살표가 흐리다", "false false", oneRoom.character.prevPet.isEnabled() + " "
                + oneRoom.character.nextPet.isEnabled());
        eq("D-58 메뉴에 캐릭터가 없다", "true", String.valueOf(button(ca.root, "캐릭터") == null));
        cr.character.draft.clear();
        cr.character.opened(false);
        eq("D-58 다시 열면 입혀 보던 것을 버린다", ca.store.pet().wear.toString(), cr.character.draft.toString());
    }

    /** 글자가 text인 버튼(칩, 탭, 메뉴 포함). 없으면 null. */
    static javax.swing.AbstractButton button(java.awt.Component c, String text) {
        if (c instanceof javax.swing.AbstractButton b && text.equals(b.getText())) {
            return b;
        }
        if (c instanceof java.awt.Container k) {
            for (java.awt.Component child : k.getComponents()) {
                javax.swing.AbstractButton b = button(child, text);
                if (b != null) {
                    return b;
                }
            }
        }
        return null;
    }

    static String sizeText(java.awt.Dimension d) {
        return d.width + "x" + d.height;
    }

    /** c 아래의 type 부품 모두(그린 순서). */
    static <T> java.util.List<T> all(java.awt.Component c, Class<T> type) {
        java.util.List<T> out = new java.util.ArrayList<>();
        if (type.isInstance(c)) {
            out.add(type.cast(c));
        }
        if (c instanceof java.awt.Container k) {
            for (java.awt.Component child : k.getComponents()) {
                out.addAll(all(child, type));
            }
        }
        return out;
    }

    /** 툴팁이 tip인 버튼. 없으면 null. */
    static javax.swing.AbstractButton tipButton(java.awt.Component c, String tip) {
        if (c instanceof javax.swing.AbstractButton b && tip.equals(b.getToolTipText())) {
            return b;
        }
        if (c instanceof java.awt.Container k) {
            for (java.awt.Component child : k.getComponents()) {
                javax.swing.AbstractButton b = tipButton(child, tip);
                if (b != null) {
                    return b;
                }
            }
        }
        return null;
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
