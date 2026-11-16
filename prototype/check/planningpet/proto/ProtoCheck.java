package planningpet.proto;

/** 06 검사 표 가운데 프로토타입이 따르는 줄을 확인한다. JUnit 없이 java로 돌린다. */
public final class ProtoCheck {
    private static int failed;

    static void eq(String id, Object want, Object got) {
        if (!String.valueOf(want).equals(String.valueOf(got))) {
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
        eq("T-SHOP-1", "0 0", g.buy("액자") + " " + s.points);
        eq("T-SHOP-2, T-SHOP-4", "150 -1", g.buy("조명") + " " + g.buy("쿠션"));
        eq("삭제 확인 조사", "을 는", Actions.josa("자소서 초안", "을", "를") + " " + Actions.josa("자료구조", "은", "는"));
        TaskForm f = new TaskForm(null, s.today());
        eq("T-VAL-2", "제목을 써 주세요", TaskCheck.check(f));
        f.title.setText("밤 과제");
        f.timed.setSelected(true);
        f.start.setValue("23:30");
        eq("T-VAL-7", null, TaskCheck.check(f));
        f.minutes.setValue(40);
        eq("T-VAL-8", "하루 안에 끝나야 해요", TaskCheck.check(f));
        System.out.println(failed == 0 ? "모두 통과" : failed + "개 실패");
        System.exit(failed == 0 ? 0 : 1);
    }
}
