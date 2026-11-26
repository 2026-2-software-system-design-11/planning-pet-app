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
        // 검사가 도중에 예외를 내도 실패로 끝낸다. 끄는 중의 굴리기 타이머가 남으면 JVM이 끝나지 않는다(#246 QA B246-2).
        try {
            checks();
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void checks() {
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
        eq("T-TYPE-3", "null 숫자(분)로 넣어 주세요(예: 30) / null 숫자(분)로 넣어 주세요(예: 30) / 30 null / 30 30분으로 읽었어요",
                typed("30abc") + " / " + typed("점심") + " / " + typed("30분") + " / " + typed("반시간"));
        eq("T-TYPE-4", "60 90 90 90 150", String.join(" ", java.util.stream.Stream.of("1시간", "1시간 30분", "1시간 반", "1.5시간", "2시간 반")
                .map(w -> String.valueOf(Typed.minutes(w).value())).toList()));
        eq("D-98 칸 아래 글", "25 흐린 5분 단위로 맞췄어요(25분) / 25 빨간 " + Typed.MINUTES_BAD, typedField());
        eq("D-98 입력 창 걸리는 시간 칸 아래 글", "흐린 5분 단위로 맞췄어요(25분) / 없음 / 없음", typedForm());
        eq("D-98 시각 고치기 창 칸 아래 글", "720 흐린 최대 720분(12시간)까지 넣을 수 있어요 true true 최대 720분(12시간)까지 넣을<br>수 있어요 true",
                timeEditorNotice());
        eq("T-TYPE-5", "09:05 5분 단위로 맞췄어요(09:05) / 23:55 5분 단위로 맞췄어요(23:55) / 09:00 null", starts("09:03", "23:58", "09:00"));
        eq("T-TYPE-6", "03:55(다음 날) 하루는 새벽 4시에 끝나서 03:55(다음 날)로 맞췄어요", starts("04:10(다음 날)"));
        eq("T-TYPE-11", "19:30 19:30으로 읽었어요 / 19:30 19:30으로 읽었어요 / 07:30 null / 14:30 null / 14:30 null / 14:00 오전을 빼고 14:00으로 읽었어요"
                + " / 12:00 null / 00:05(다음 날) 오전 12시는 밤 자정이라 00:05(다음 날)로 읽었어요 / 03:55(다음 날) 하루는 새벽 4시에 끝나서 03:55(다음 날)로 맞췄어요",
                starts("730", "7:30", "0730", "오후 2:30", "2시 30", "오전 14시", "아침 12시", "오전 12시 3분", "03:58(다음 날)"));
        eq("T-TYPE-7", "14:00 null / 09:00 null / 14:30 null / 09:30 null / 13:00 null / 19:00 null", starts("14", "9", "1430", "930", "1", "7"));
        eq("T-TYPE-10", "04:00 19일(목) 04:00으로 읽었어요 / 05:00 19일(목) 05:00으로 읽었어요 / 00:00(다음 날) 오전 12시는 밤 자정, "
                + "00:00(다음 날)로 읽었어요 / 14:00 null 1 1 0", starts("밤 4시", "밤 5시", "오전 12시", "오후 14시") + " "
                + Typed.start("밤 4시", java.time.LocalDate.of(2026, 11, 18)).days() + " " + Typed.start("밤 5시", java.time.LocalDate.of(2026, 11, 18)).days()
                + " " + Typed.start("오전 12시", java.time.LocalDate.of(2026, 11, 18)).days());
        eq("D-98 한 글자씩 친 시작 시각", "21:30 20:00 22:30 14:30 19:30 17:00 13:00 13:00 / 21:30 20:00 17:00", keyed());
        eq("D-98 한 글자를 채워 보인다", "13:00 3:00", oneDigitFill());
        eq("D-98 비운 칸은 치기 앞의 상태로", "true 없음 true 없음 / 25 흐린 5분 단위로 맞췄어요(25분) / 2026-11-19 04:00 흐린 19일(목) 04:00으로 읽었어요"
                + " / 흐린 19일(목) 04:00으로 읽었어요 없음 2026-11-19 240", blanked());
        eq("D-98 밤 4시", "2026-11-19 04:00 흐린 19일(목) 04:00으로 읽었어요 / 2026-11-19 240", nightForm());
        eq("D-98, D-101 밤 4시 뒤", "2026-11-19 / 2026-11-18 없음 / 2026-11-18 흐린 5분 단위로 맞췄어요(22:05) / 2026-11-18 245 없음 / 2026-11-19 / 2026-11-18 없음", nightAfter());
        Typed.Read r30 = Typed.start("밤 4시", java.time.LocalDate.of(2026, 11, 30));
        Typed.Read due = Typed.start("밤 4시", java.time.LocalDate.of(2026, 11, 18), java.time.LocalDate.of(2026, 11, 18));
        eq("D-98 칸이 친 채 저장을 가름", "true false true false false false", typedBlocks());
        eq("T-TYPE-13 친 채 저장", "false 빨간 숫자(분)로 넣어 주세요(예: 30) / true 1 / false 빨간 14:00처럼 적어 주세요", saveTyped());
        eq("T-TYPE-13 두 창 두 칸에서 저장 단추로", "false 빨간 14:00처럼 적어 주세요 / false 빨간 숫자(분)로 넣어 주세요(예: 30) / "
                + "false 빨간 14:00처럼 적어 주세요 / false 빨간 숫자(분)로 넣어 주세요(예: 30)", pressToSave());
        eq("D-98 이후 반복 모두의 밤 4시", "2026-11-19 240 2026-11-26 240 2026-12-03 240", nightSeries());
        eq("T-TYPE-12", "04:00 1 12월 1일(화) 04:00으로 읽었어요 / 03:55(다음 날) 0 마감(18일) 안에 두려고 03:55로 맞췄어요",
                r30.value() + " " + r30.days() + " " + r30.notice() + " / " + due.value() + " " + due.days() + " " + due.notice());
        eq("T-TYPE-8", "14:00 14:00 20:00 09:00 12:00 14:30 14:30 07:00 19:00 21:00 12:00 11:00 01:00(다음 날) 02:00(다음 날) 00:00(다음 날) 05:00",
                starts("오후 2시", "2시", "8시", "9시", "12시", "오후 2시 반", "2시 30분", "아침 7시", "저녁 7시", "밤 9시", "낮 12시", "낮 11시",
                        "새벽 1시", "밤 2시", "밤 12시", "새벽 5시").replace(" null", "").replace(" /", ""));
        eq("T-TYPE-9", "null 14:00처럼 적어 주세요 / null 14:00처럼 적어 주세요 / null 14:00처럼 적어 주세요", starts("25:00", "09:75", "점심"));
        eq("D-98 다음 날 값 뒤 23:58의 글은 좁아진 칸 묶음 안", "23:55 true", nextThen2358());
        eq("D-98 시작 시각 칸 아래 글", "09:05 흐린 5분 단위로 맞췄어요(09:05) / 14:00 / 흐린 하루는 새벽 4시에 끝나서 03:55(다음 날)로 맞췄어요"
                + " / 흐린 최대 720분(12시간)까지 넣을 수 있어요 true", startTyped());
        eq("D-98 입력 창 칸 아래 글 자리", "true true true", formNoticeRows());
        eq("D-98 칸 아래 글이 뜨면 창 크기를 다시 맞춤", "true true true true", relayouts());
        eq("D-98 쳐 넣은 뒤 30분 안내", "true false 흐린 5분 단위로 맞췄어요(30분)", thirtyHint());
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

    /** 27은 25와 흐린 글, "30abc"는 이전 값과 빨간 글(D-98). */
    private static String typedField() {
        javax.swing.JSpinner s = Ui.minutes(30);
        javax.swing.JLabel l = new TypedField(s, Typed::minutes, () -> { }).notice;
        return type(s, "27") + " " + notice(l) + " / " + type(s, "30abc") + " " + notice(l);
    }

    /** 입력 창의 칸 아래 글은 다른 칸을 고쳐도 남고, 그 칸에 쳐 넣기 시작하거나 화살표를 누르면 사라진다. */
    private static String typedForm() {
        TaskForm f = new TaskForm(null, java.time.LocalDate.of(2026, 11, 16), java.time.LocalDateTime.of(2026, 11, 16, 9, 0));
        type(f.minutes, "27");
        f.title.setText("과제");
        String kept = notice(f.minutesNotice);
        ((javax.swing.JSpinner.DefaultEditor) f.minutes.getEditor()).getTextField().setText("4");
        String typing = notice(f.minutesNotice);
        type(f.minutes, "27");
        f.minutes.setValue(f.minutes.getNextValue());
        return kept + " / " + typing + " / " + notice(f.minutesNotice);
    }

    /** 시각 고치기 창의 글은 걸리는 시간 칸 묶음 너비 안에서 두 줄로 접힌다(D-98). */
    private static String timeEditorNotice() {
        Task t = MockData.sample().tasks.get(0);
        TimeEditor e = new TimeEditor(t, java.time.LocalDateTime.of(2026, 11, 14, 15, 23));
        javax.swing.JLabel l = e.minutesNotice;
        String got = type(e.minutes, "730") + " " + notice(l);
        int group = l.getParent().getComponent(0).getPreferredSize().width;
        return got + " " + (l.getPreferredSize().width <= group) + " "
                + (l.getPreferredSize().height > l.getFontMetrics(l.getFont()).getHeight() * 1.5) + " " + l.getText().replaceAll("</?(html|font)[^>]*>", "") + " " + dim(l);
    }

    /** 라벨을 흰 바탕에 그려 가장 진한 점이 본문 글색(INK)보다 한참 밝으면 참이다. 두 줄로 접은 흐린 글도 흐리게 그려지는가. */
    static boolean dim(javax.swing.JLabel l) {
        l.setSize(l.getPreferredSize());
        var img = new java.awt.image.BufferedImage(l.getWidth(), l.getHeight(), java.awt.image.BufferedImage.TYPE_INT_RGB);
        var g = img.createGraphics();
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, l.getWidth(), l.getHeight());
        l.paint(g);
        g.dispose();
        int darkest = 765;
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int p = img.getRGB(x, y);
                darkest = Math.min(darkest, (p >> 16 & 255) + (p >> 8 & 255) + (p & 255));
            }
        }
        java.awt.Color ink = planningpet.common.view.Theme.INK;
        return darkest > ink.getRed() + ink.getGreen() + ink.getBlue() + 200;
    }

    /** 시작 시각 칸에 쳐 넣은 글마다 읽은 값과 칸 아래 글(06의 32번). */
    private static String starts(String... texts) {
        return String.join(" / ", java.util.Arrays.stream(texts).map(x -> Typed.start(x, java.time.LocalDate.of(2026, 11, 18))).map(r -> r.value() + " " + r.notice()).toList());
    }

    /** 입력 창 시작 시각은 09:03을 맞추고 "14"를 14:00으로 채운다. 시각 고치기 창의 두 글은 각자 칸 묶음 아래에 함께 뜬다. */
    private static String startTyped() {
        TaskForm f = new TaskForm(null, java.time.LocalDate.of(2026, 11, 16), java.time.LocalDateTime.of(2026, 11, 16, 9, 0));
        f.timed.setSelected(true);
        f.sync();
        String form = type(f.start, "09:03") + " " + notice(f.startNotice);
        var field = ((javax.swing.JSpinner.DefaultEditor) f.start.getEditor()).getTextField();
        field.setText("14");
        TimeEditor e = new TimeEditor(MockData.sample().tasks.get(0), java.time.LocalDateTime.of(2026, 11, 14, 15, 23));
        type(e.start, "04:10(다음 날)");
        type(e.minutes, "730");
        return form + " / " + field.getText() + " / " + notice(e.startNotice) + " / " + notice(e.minutesNotice) + " "
                + (e.startNotice.getParent() != e.minutesNotice.getParent());
    }

    /** 칸 아래 글은 입력 창 panel에 붙고, 글이 없을 때는 이름 칸과 여백까지 자리를 차지하지 않는다. 글 줄이 없으면 실패 문장을 낸다. */
    private static String formNoticeRows() {
        TaskForm f = new TaskForm(null, java.time.LocalDate.of(2026, 11, 16), java.time.LocalDateTime.of(2026, 11, 16, 9, 0));
        String parents = (f.startNotice.getParent() == f.panel) + " " + (f.minutesNotice.getParent() == f.panel);
        int height = f.panel.getPreferredSize().height;
        for (javax.swing.JLabel l : new javax.swing.JLabel[] {f.startNotice, f.minutesNotice}) {
            if (f.panel.getComponentZOrder(l) < 1) {
                return parents + " 글 줄 없음";
            }
            f.panel.remove(f.panel.getComponentZOrder(l) - 1);
            f.panel.remove(l);
        }
        return parents + " " + (height == f.panel.getPreferredSize().height);
    }

    /** 두 창 모두 칸 아래 글이 뜨면 relayout(창의 pack)을 부른다. 입력 창 시작, 걸리는 시간, 시각 고치기 창 시작, 걸리는 시간. */
    private static String relayouts() {
        int[] n = new int[4];
        TaskForm f = new TaskForm(null, java.time.LocalDate.of(2026, 11, 16), java.time.LocalDateTime.of(2026, 11, 16, 9, 0));
        f.timed.setSelected(true);
        f.sync();
        f.relayout = () -> n[0]++;
        type(f.start, "09:03");
        f.relayout = () -> n[1]++;
        type(f.minutes, "27");
        TimeEditor e = new TimeEditor(MockData.sample().tasks.get(0), java.time.LocalDateTime.of(2026, 11, 14, 15, 23));
        e.relayout = () -> n[2]++;
        type(e.start, "09:03");
        e.relayout = () -> n[3]++;
        type(e.minutes, "27");
        return (n[0] > 0) + " " + (n[1] > 0) + " " + (n[2] > 0) + " " + (n[3] > 0);
    }

    /** 추가 창의 30분 안내는 값을 쳐 넣은 뒤에는 숨는다(D-98). */
    private static String thirtyHint() {
        TaskForm f = new TaskForm(null, java.time.LocalDate.of(2026, 11, 16), java.time.LocalDateTime.of(2026, 11, 16, 9, 0));
        boolean before = f.minutesHint.isVisible();
        type(f.minutes, "31");
        return before + " " + f.minutesHint.isVisible() + " " + notice(f.minutesNotice);
    }

    /** 시각 고치기 창에서 01:00(다음 날)로 실제 시각 글이 보인 뒤 23:58을 치면, 글이 사라져 좁아진 칸 묶음 폭 안으로 다시 접힌다. */
    private static String nextThen2358() {
        TimeEditor e = new TimeEditor(MockData.sample().tasks.get(0), java.time.LocalDateTime.of(2026, 11, 14, 15, 23));
        type(e.start, "01:00(다음 날)");
        Object v = type(e.start, "23:58");
        javax.swing.JLabel l = e.startNotice;
        return v + " " + (l.isVisible() && l.getPreferredSize().width <= l.getParent().getComponent(0).getPreferredSize().width);
    }

    /** 시작 시각 칸에 "1"을 치면 쳐 넣는 동안 "13:00"이고 뒤 "3:00"이 골라져 있다. */
    private static String oneDigitFill() {
        var field = ((javax.swing.JSpinner.DefaultEditor) new TimeEditor(MockData.sample().tasks.get(0),
                java.time.LocalDateTime.of(2026, 11, 14, 15, 23)).start.getEditor()).getTextField();
        field.setText("1");
        return field.getText() + " " + field.getSelectedText();
    }

    /** 받을 수 없는 글을 친 채 저장하면 막고, 빨간 글이 뜨고 이전 값으로 돌아간 뒤 저장하면 그 값으로 저장한다(D-98). */
    private static String saveTyped() {
        App app = Main.create(MockData.empty());
        TaskForm f = new TaskForm(null, java.time.LocalDate.of(2026, 11, 18), app.store.now);
        f.title.setText("보고서");
        var field = ((javax.swing.JSpinner.DefaultEditor) f.minutes.getEditor()).getTextField();
        field.setText("30abc");
        String first = TaskDialog.trySave(app, f, null) + " " + notice(f.minutesNotice);
        field.setValue(field.getValue());
        String second = TaskDialog.trySave(app, f, null) + " " + app.store.tasks.size();
        Task t = app.store.tasks.get(0);
        TimeEditor e = new TimeEditor(t, java.time.LocalDate.of(2026, 11, 18), app.store.now);
        ((javax.swing.JSpinner.DefaultEditor) e.start.getEditor()).getTextField().setText("25:00");
        return first + " / " + second + " / " + TimeEditor.save(app, t, e) + " " + notice(e.startNotice);
    }

    /** 저장 단추를 누른 채 칸의 글을 두고 포커스가 단추로 옮겨 간 뒤 떼는 것과 같다(실제 창의 저장 누름). 떼면 단추의 동작이 돈다. */
    static void pressSave(javax.swing.JSpinner s, String text, javax.swing.AbstractButton save) {
        save.getModel().setArmed(true);
        save.getModel().setPressed(true);
        leave(s, text, save);
        save.getModel().setPressed(false);
        save.getModel().setArmed(false);
    }

    /** 저장 단추를 붙인 일정 입력 창. 단추를 떼면 저장하고 그 결과를 out에 붙인다. */
    private static TaskForm formWithSave(App app, javax.swing.JButton save, StringBuilder out) {
        TaskForm f = new TaskForm(null, java.time.LocalDate.of(2026, 11, 18), app.store.now);
        TaskDialog.content(f, new javax.swing.JButton("취소"), save, null);
        f.title.setText("보고서");
        f.timed.setSelected(true);
        f.sync();
        save.addActionListener(x -> out.append(TaskDialog.trySave(app, f, null)).append(" "));
        return f;
    }

    /** 10:00, 60분 할 일의 시각 고치기 창. 저장 단추를 떼면 저장하고 그 결과를 out에 붙인다. */
    private static TimeEditor editorWithSave(App app, StringBuilder out) {
        Task t = app.store.book.add(Task.Kind.TASK, "과제", java.time.LocalDate.of(2026, 11, 18), java.time.LocalTime.of(10, 0), 60, "", null).get(0);
        TimeEditor e = new TimeEditor(t, app.store.now);
        e.save.addActionListener(x -> out.append(TimeEditor.save(app, t, e)).append(" "));
        return e;
    }

    /** 두 창의 시작 칸과 걸리는 시간 칸에 받을 수 없는 글을 친 채 저장을 누르면 막고 그 칸 아래 빨간 글을 보인다(D-98). */
    private static String pressToSave() {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < 2; i++) {
            javax.swing.JButton save = new javax.swing.JButton("저장");
            TaskForm f = formWithSave(Main.create(MockData.empty()), save, out);
            pressSave(i == 0 ? f.start : f.minutes, i == 0 ? "abc" : "30abc", save);
            out.append(notice(i == 0 ? f.startNotice : f.minutesNotice)).append(" / ");
        }
        for (int i = 0; i < 2; i++) {
            TimeEditor e = editorWithSave(Main.create(MockData.empty()), out);
            pressSave(i == 0 ? e.start : e.minutes, i == 0 ? "abc" : "30abc", e.save);
            out.append(notice(i == 0 ? e.startNotice : e.minutesNotice)).append(" / ");
        }
        return out.substring(0, out.length() - 3);
    }

    /** 칸에 글을 치고 포커스를 to로 옮긴 것과 같다. JFormattedTextField처럼 포커스 리스너를 부른 뒤 글을 확정하고, 받지 않으면 이전 값으로 돌린다. */
    static void leave(javax.swing.JSpinner s, String text, java.awt.Component to) {
        var field = ((javax.swing.JSpinner.DefaultEditor) s.getEditor()).getTextField();
        field.setText(text);
        var lost = new java.awt.event.FocusEvent(field, java.awt.event.FocusEvent.FOCUS_LOST, false, to);
        for (var l : field.getFocusListeners()) {
            l.focusLost(lost);
        }
        try {
            field.commitEdit();
        } catch (java.text.ParseException x) {
            field.setValue(field.getValue());
        }
    }

    /** 칸 하나에 받을 수 없는 글을 친 채 저장 단추를 누르거나 칸을 벗어나지 않고 저장하면 막고, 체크박스나 저장 단추로 포커스만 옮기거나 눌렀다 밖에서 뗀 뒤, 막힌 뒤 다시 저장하면 막지 않는다(D-98). */
    private static String typedBlocks() {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            javax.swing.JSpinner s = Ui.minutes(60);
            TypedField typed = new TypedField(s, Typed::minutes, () -> { });
            javax.swing.JButton save = new javax.swing.JButton("저장");
            typed.saveWith(save);
            if (i == 0) {
                save.addActionListener(x -> out.append(typed.blocks()).append(" "));
                pressSave(s, "30abc", save);
            } else if (i == 1) {
                ((javax.swing.JSpinner.DefaultEditor) s.getEditor()).getTextField().setText("30abc");
            } else {
                save.getModel().setPressed(i == 4);
                leave(s, "30abc", i == 2 ? new javax.swing.JCheckBox() : save);
                save.getModel().setPressed(false);
            }
            out.append(typed.blocks()).append(" ");
        }
        return out.toString().trim();
    }

    /** 수요일 22:00 요일 반복 사본에 "밤 4시"를 넣고 이후 반복 모두를 고르면 모든 사본이 각자 다음 날 04:00이다. */
    private static String nightSeries() {
        App app = Main.create(MockData.empty());
        java.util.List<Task> copies = app.store.book.add(Task.Kind.TASK, "야간 과제", java.time.LocalDate.of(2026, 11, 18),
                java.time.LocalTime.of(22, 0), 60, "", java.time.LocalDate.of(2026, 12, 2));
        TimeEditor e = new TimeEditor(copies.get(0), app.store.now);
        type(e.start, "밤 4시");
        app.schedule.chooser = (title, message, options) -> 1;
        TimeEditor.save(app, copies.get(0), e);
        return String.join(" ", copies.stream().map(c -> c.date + " " + c.startMinute()).toList());
    }

    /** 다시 친 "밤 4시"는 19일 그대로, "정하기"를 끄거나 다른 값으로 바꾸면 18일로 돌아간다. 시각 고치기 창은 화살표 뒤 원래 날이다. */
    private static String nightAfter() {
        TaskForm f = new TaskForm(null, java.time.LocalDate.of(2026, 11, 18), java.time.LocalDateTime.of(2026, 11, 18, 9, 0));
        f.timed.setSelected(true);
        f.sync();
        type(f.start, "밤 4시");
        type(f.start, "밤 4시");
        String again = Ui.dateOf(f.date).toString();
        f.timed.setSelected(false);
        f.sync();
        String off = Ui.dateOf(f.date) + " " + notice(f.startNotice);
        f.timed.setSelected(true);
        f.sync();
        type(f.start, "밤 4시");
        type(f.start, "22:03");
        String other = Ui.dateOf(f.date) + " " + notice(f.startNotice);
        // 날짜를 손으로 20일로 바꿨다가 19일로 돌려 놓으면 사용자가 고른 날이라 정하기를 꺼도 그대로다.
        type(f.start, "밤 4시");
        Ui.setDate(f.date, java.time.LocalDate.of(2026, 11, 20));
        Ui.setDate(f.date, java.time.LocalDate.of(2026, 11, 19));
        f.timed.setSelected(false);
        f.sync();
        String hand = Ui.dateOf(f.date).toString();
        App app = Main.create(MockData.empty());
        Task t = app.store.book.add(Task.Kind.TASK, "야간 과제", java.time.LocalDate.of(2026, 11, 18), java.time.LocalTime.of(22, 0), 60, "", null).get(0);
        TimeEditor e = new TimeEditor(t, app.store.now);
        type(e.start, "밤 4시");
        e.start.setValue(e.start.getNextValue());
        String arrowed = notice(e.startNotice);
        TimeEditor.save(app, t, e);
        // 기간 안에 하기를 켜면 정하기가 꺼지므로 그 순간 원래 날로 돌아가고 글도 지운다(D-98).
        TaskForm g = new TaskForm(null, java.time.LocalDate.of(2026, 11, 18), java.time.LocalDateTime.of(2026, 11, 18, 9, 0));
        g.timed.setSelected(true);
        g.sync();
        type(g.start, "밤 4시");
        g.period.setSelected(true);
        g.sync();
        String period = Ui.dateOf(g.date) + " " + notice(g.startNotice);
        return again + " / " + off + " / " + other + " / " + t.date + " " + t.startMinute() + " " + arrowed + " / " + hand + " / " + period;
    }

    /** "밤 4시"는 입력 창 날짜를 하루 뒤로 옮기고, 시각 고치기 창은 저장할 때 그 일을 다음 날로 옮긴다. */
    private static String nightForm() {
        TaskForm f = new TaskForm(null, java.time.LocalDate.of(2026, 11, 18), java.time.LocalDateTime.of(2026, 11, 18, 9, 0));
        f.timed.setSelected(true);
        f.sync();
        type(f.start, "밤 4시");
        String form = Ui.dateOf(f.date) + " " + f.start.getValue() + " " + notice(f.startNotice);
        App app = Main.create(MockData.empty());
        Task t = app.store.book.add(Task.Kind.TASK, "야간 과제", java.time.LocalDate.of(2026, 11, 18), java.time.LocalTime.of(22, 0), 60, "", null).get(0);
        TimeEditor e = new TimeEditor(t, app.store.now);
        type(e.start, "밤 4시");
        TimeEditor.save(app, t, e);
        return form + " / " + t.date + " " + t.startMinute();
    }

    /** 두 창의 두 칸에서 친 글을 모두 지워 비운 채 벗어나면 빨간 글 없이 치기 앞의 값, 칸 아래 글, "밤 4시"가 옮긴 날로 돌아간다(D-98). */
    private static String blanked() {
        TaskForm f = new TaskForm(null, java.time.LocalDate.of(2026, 11, 18), java.time.LocalDateTime.of(2026, 11, 18, 9, 0));
        f.timed.setSelected(true);
        f.sync();
        Object start = f.start.getValue();
        Object minutes = f.minutes.getValue();
        String plain = keys(f.start, "2\b").equals(start) + " " + notice(f.startNotice) + " " + keys(f.minutes, "4\b").equals(minutes) + " " + notice(f.minutesNotice);
        type(f.minutes, "27");
        String dim = keys(f.minutes, "\b") + " " + notice(f.minutesNotice);
        type(f.start, "밤 4시");
        keys(f.start, "\b");
        f.title.setText("보고서");
        String night = Ui.dateOf(f.date) + " " + f.start.getValue() + " " + notice(f.startNotice);
        App app = Main.create(MockData.empty());
        Task t = app.store.book.add(Task.Kind.TASK, "야간 과제", java.time.LocalDate.of(2026, 11, 18), java.time.LocalTime.of(22, 0), 60, "", null).get(0);
        TimeEditor e = new TimeEditor(t, app.store.now);
        type(e.start, "밤 4시");
        keys(e.start, "2\b");
        keys(e.minutes, "4\b");
        String editor = notice(e.startNotice) + " " + notice(e.minutesNotice);
        TimeEditor.save(app, t, e);
        return plain + " / " + dim + " / " + night + " / " + editor + " " + t.date + " " + t.startMinute();
    }

    /** 두 창의 시작 시각 칸에 키보드로 한 글자씩 친 값. 채워 보인 값이 아니라 친 글 뒤에 다음 글자가 붙는다(D-98). */
    private static String keyed() {
        TaskForm f = new TaskForm(null, java.time.LocalDate.of(2026, 11, 18), java.time.LocalDateTime.of(2026, 11, 18, 9, 0));
        f.timed.setSelected(true);
        f.sync();
        TimeEditor e = new TimeEditor(MockData.sample().tasks.get(0), java.time.LocalDateTime.of(2026, 11, 14, 15, 23));
        return keys(f.start, "21:30") + " " + keys(f.start, "20") + " " + keys(f.start, "2230") + " " + keys(f.start, "1430") + " " + keys(f.start, "730") + " "
                + keys(f.start, "2\b5") + " " + keys(f.start, "8\b1") + " " + keys(f.start, "14\b") + " / "
                + keys(e.start, "21:30") + " " + keys(e.start, "20") + " " + keys(e.start, "2\b5");
    }

    /** 칸의 글을 모두 고르고 EDT에서 한 글자씩 친 뒤(실제 키 입력과 같은 길, "\b"는 Backspace) 칸을 벗어난 것과 같다. */
    static Object keys(javax.swing.JSpinner s, String keys) {
        var field = ((javax.swing.JSpinner.DefaultEditor) s.getEditor()).getTextField();
        try {
            javax.swing.SwingUtilities.invokeAndWait(() -> {
                field.selectAll();
                for (char c : keys.toCharArray()) {
                    if (c == '\b') {
                        field.getActionMap().get(javax.swing.text.DefaultEditorKit.deletePrevCharAction).actionPerformed(new java.awt.event.ActionEvent(field, 0, ""));
                    } else {
                        field.replaceSelection(String.valueOf(c));
                    }
                }
                try {
                    field.commitEdit();
                } catch (java.text.ParseException ignored) {
                }
            });
        } catch (Exception x) {
            throw new IllegalStateException(x);
        }
        return s.getValue();
    }

    /** 칸에 글을 쳐 넣고 칸을 벗어난 것과 같다. 받지 않으면 이전 값이다. */
    static Object type(javax.swing.JSpinner s, String text) {
        var field = ((javax.swing.JSpinner.DefaultEditor) s.getEditor()).getTextField();
        field.setText(text);
        try {
            field.commitEdit();
        } catch (java.text.ParseException ignored) {
        }
        return s.getValue();
    }

    /** 칸 아래 글. 안 보이면 "없음". */
    static String notice(javax.swing.JLabel l) {
        return !l.isVisible() ? "없음" : (planningpet.common.view.Theme.SUNDAY.equals(l.getForeground()) ? "빨간 " : "흐린 ")
                + l.getText().replace("<br>", " ").replaceAll("<[^>]*>", "");
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
