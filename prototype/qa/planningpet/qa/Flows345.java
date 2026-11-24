package planningpet.qa;

import planningpet.common.view.Ui;
import planningpet.schedule.model.*;
import planningpet.schedule.view.TaskForm;
import static planningpet.qa.QaRun.st;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

/** docs/user-flows.md의 ③, ④, ⑤ 흐름. */
final class Flows345 {
    private Flows345() {
    }

    static void add(Map<String, Runnable> flows) {
        flows.put("UF-3-1", () -> uf31(new Qa("UF-3-1")));
        flows.put("UF-3-2", Flows345::uf32);
        flows.put("UF-3-3", Flows345::uf33);
        flows.put("UF-4-1", () -> uf41(new Qa("UF-4-1")));
        flows.put("UF-4-2", Flows345::uf42);
        flows.put("UF-4-3", Flows345::uf43);
        flows.put("UF-5-1", () -> uf51(new Qa("UF-5-1")));
        flows.put("UF-5-2", () -> uf52(new Qa("UF-5-2")));
        flows.put("UF-5-3", Flows345::uf53);
    }

    /** 그날 시각 있는 일정을 "17:00 팀 회의, 18:00 편의점 알바"처럼. */
    static String day(Qa q, String date) {
        return q.app.store.book.timedOn(QaDrive.d(date)).stream().sorted(Comparator.comparing((Task t) -> t.start))
                .map(t -> t.start + " " + t.title).collect(Collectors.joining(", "));
    }

    /** 시간표 열: "팀 회의 0/2, 편의점 알바 1/2". */
    static String cols(Qa q, String date) {
        var tasks = q.app.store.book.timedOn(QaDrive.d(date));
        var c = Planner.columns(tasks);
        return tasks.stream().sorted(Comparator.comparing((Task t) -> t.start))
                .map(t -> t.title + " " + c.get(t)[0] + "/" + c.get(t)[1]).collect(Collectors.joining(", "));
    }

    static String dates(Qa q, String title, java.time.DayOfWeek dow) {
        return q.app.store.tasks.stream().filter(t -> t.title.equals(title) && t.date.getDayOfWeek() == dow)
                .map(t -> t.date.toString()).sorted().collect(Collectors.joining(","));
    }

    /** 그 제목, 그 요일의 일정 수. 없으면 0이다. */
    static long count(Qa q, String title, java.time.DayOfWeek dow) {
        return q.app.store.tasks.stream().filter(t -> t.title.equals(title) && t.date.getDayOfWeek() == dow).count();
    }

    /** 일정관리에서 일정 추가로 고정 일정을 요일 반복(날짜의 요일이 골라진 채)으로 넣는다. */
    static boolean shift(Qa q, String date, String time, int minutes, String end) {
        TaskForm f = q.form(q.sched.defaultDate().toString());
        f.fixed.doClick();
        f.title.setText("편의점 알바");
        Ui.setDate(f.date, QaDrive.d(date));
        f.start.setValue(time);
        f.minutes.setValue(minutes);
        f.repeat.setSelectedItem("요일");
        Ui.setDate(f.repeatEnd, LocalDate.parse(end));
        return q.save(f, null);
    }

    /** 입력 창 반복 칸: "요일 [화, 목] 끝 2027-03-02". 반복 칸이 비활성이면 "(비활성)", 끝 날짜 칸이 꺼졌으면 "(흐림)"을 붙인다. */
    static String repeat(TaskForm f) {
        StringBuilder days = new StringBuilder();
        for (javax.swing.JToggleButton b : f.days) {
            if (b.isSelected()) {
                days.append(days.length() > 0 ? ", " : "").append(b.getText());
            }
        }
        return f.repeat.getSelectedItem() + (f.repeat.isEnabled() ? "" : "(비활성)") + " [" + days + "] 끝 "
                + Ui.dateOf(f.repeatEnd) + (f.repeatEnd.isEnabled() ? "" : "(흐림)");
    }

    static Qa uf31(Qa q) {
        q.start(Personas.p3());
        q.open();
        q.app.show("일정관리");
        q.step("1", "일정관리에서 일정 추가를 누른다");
        q.has("주", "11월 16일 ~ 11월 22일", Flows8910.text(q.sched));
        TaskForm f = q.form(q.sched.defaultDate().toString());
        q.eq("날짜, 종류", "2026-11-16 할 일", Ui.dateOf(f.date) + (f.task.isSelected() ? " 할 일" : " 고정 일정"));
        q.eq("반복 칸(D-64)", "안 함 [] 끝 2026-12-14(흐림)", repeat(f));
        q.step("2", "종류를 고정 일정으로 바꾼다");
        f.fixed.doClick();
        q.eq("시작 시각 정하기(켜짐, 비활성), 기간 안에 하기(꺼짐, 비활성)", "true false / false false",
                f.timed.isSelected() + " " + f.timed.isEnabled() + " / " + f.period.isSelected() + " " + f.period.isEnabled());
        q.eq("반복 칸", "안 함 [] 끝 2027-03-01(흐림)", repeat(f));
        q.step("3", "편의점 알바, 11-17, 18:00, 240분을 넣고 반복에서 요일을 고른다");
        f.title.setText("편의점 알바");
        Ui.setDate(f.date, QaDrive.d("2026-11-17"));
        f.start.setValue("18:00");
        f.minutes.setValue(240);
        f.repeat.setSelectedItem("요일");
        q.eq("끝 시각, 반복 칸", "끝 22:00 / 요일 [화] 끝 2027-03-02", f.endLabel.getText() + " / " + repeat(f));
        q.step("4", "끝 날짜를 2026-11-10으로 바꿔 저장한다");
        Ui.setDate(f.repeatEnd, QaDrive.d("2026-11-10"));
        q.eq("저장", "false 끝 날짜가 시작 날짜보다 앞이에요", q.save(f, null) + " " + f.errorText());
        q.step("5", "끝 날짜를 2027-05-18로 바꾼다");
        Ui.setDate(f.repeatEnd, QaDrive.d("2027-05-18"));
        q.eq("오류 글(D-43, 화요일 27개는 120개 안)", "", f.errorText());
        q.step("6", "요일에 목을 더 고르고 끝 날짜를 2026-12-17로 바꿔 저장한다");
        f.days[3].doClick();
        Ui.setDate(f.repeatEnd, QaDrive.d("2026-12-17"));
        q.yes("저장", q.save(f, null));
        q.eq("화요일", "2026-11-17,2026-11-24,2026-12-01,2026-12-08,2026-12-15",
                dates(q, "편의점 알바", java.time.DayOfWeek.TUESDAY));
        q.step("7", "목요일 알바를 넣는다");
        q.yes("저장", shift(q, "2026-11-19", "18:00", 240, "2026-12-17"));
        q.eq("목요일 개수", 5, count(q, "편의점 알바", java.time.DayOfWeek.THURSDAY));
        q.step("8", "토요일 알바를 넣는다(끝은 일요일)");
        q.yes("저장", shift(q, "2026-11-21", "10:00", 360, "2026-12-20"));
        q.eq("토요일", "2026-11-21,2026-11-28,2026-12-05,2026-12-12,2026-12-19",
                dates(q, "편의점 알바", java.time.DayOfWeek.SATURDAY));
        q.step("9", "시간표를 본다");
        q.eq("화 17, 목 19, 토 21", "18:00 편의점 알바 / 17:00 팀 회의, 18:00 편의점 알바 / 10:00 편의점 알바",
                day(q, "2026-11-17") + " / " + day(q, "2026-11-19") + " / " + day(q, "2026-11-21"));
        q.end();
        return q;
    }

    static void uf32() {
        Qa q = new Qa("UF-3-2").after(Flows345::uf31);
        q.step("1", "목 19 칸을 본다");
        q.eq("열", "팀 회의 0/2, 편의점 알바 1/2", cols(q, "2026-11-19"));
        q.step("2", "자동 배치를 누른다");
        Planner.Placement p = q.place();
        q.eq("결과 창", "11월 16일 12:30부터 11월 22일까지, 할 일마다 그 날짜 안의 빈 시간에 놓았어요"
                + " / 설계 과제|11월 19일|09:00 ~ 17:00|480분", q.placement(p));
        q.step("3", "적용을 누른다");
        q.apply(p);
        q.eq("열", "설계 과제 0/1, 팀 회의 0/2, 편의점 알바 1/2", cols(q, "2026-11-19"));
        q.end();
    }

    static void uf33() {
        Qa q = new Qa("UF-3-3").after(Flows345::uf31);
        q.step("1", "토 28 알바를 삭제한다");
        q.sched.move(QaDrive.d("2026-11-23"));
        q.step("2", "이후 반복 모두를 누른다");
        q.yes("지움", q.delete(q.task("편의점 알바", "2026-11-28"), 1));
        q.eq("확인 창", "'편의점 알바'는 매주 반복 일정이에요. / 이 일정만, 이후 반복 모두, 취소", q.lastQuestion + " / " + q.lastOptions);
        q.eq("토요일", "2026-11-21", dates(q, "편의점 알바", java.time.DayOfWeek.SATURDAY));
        q.step("3", "토 21을 이 일정만으로 삭제한다");
        q.sched.move(QaDrive.d("2026-11-16"));
        q.yes("지움", q.delete(q.task("편의점 알바", "2026-11-21"), 0));
        q.eq("토, 화, 목 개수", "0 5 5", count(q, "편의점 알바", java.time.DayOfWeek.SATURDAY)
                + " " + count(q, "편의점 알바", java.time.DayOfWeek.TUESDAY)
                + " " + count(q, "편의점 알바", java.time.DayOfWeek.THURSDAY));
        q.end();
    }

    static Qa uf41(Qa q) {
        q.start(Personas.p4());
        q.step("1", "앱을 연다");
        q.open();
        q.eq("말풍선", "오늘 할 일 3개 남았어.", q.app.store.talk);
        q.eq("위 막대", "연속 접속 30일, 보유 600P, 출석 완료(비활성)", q.top());
        q.eq("성취도, 레벨", "19/22 보리 Lv. 3 경험치 100 / 110", q.progress() + " " + q.level());
        q.eq("펫 카드(할 일이 열 개를 넘으면 이어진 막대)", "오늘 끝낸 일 19 / 22, 86% 0칸", q.petCard());
        q.has("목록", "시각 없는 할 일 / [x] 국어 문법 1 시각 정하기 30분", q.rows());
        q.step("2", "행정법 기출을 체크한다");
        q.check(q.task("행정법 기출"));
        q.eq("상태", "610P 경험치240 횟수20 성취도20/22", st(q));
        q.eq("말풍선", "잘했어! 레벨이 올랐어. 이제 Lv.4, 나 좀 자란 것 같아.", q.app.store.talk);
        q.eq("레벨", "보리 Lv. 4 경험치 0 / 140", q.level());
        q.step("3", "한국사 회독을 체크한다");
        q.check(q.task("한국사 회독"));
        q.eq("상태", "610P 경험치240 횟수20 성취도21/22", st(q));
        q.eq("말풍선", "잘했어! 오늘 보상은 다 받았지만 체크는 계속 돼.", q.app.store.talk);
        q.step("4", "한국사 회독 체크를 푼다");
        q.check(q.task("한국사 회독"));
        q.eq("상태", "610P 경험치240 횟수20 성취도20/22", st(q));
        q.eq("말풍선", "체크를 풀었어.", q.app.store.talk);
        q.end();
        return q;
    }

    static void uf42() {
        Qa q = new Qa("UF-4-2").after(Flows345::uf41);
        q.step("1", "01:30에 오답 정리를 체크한다");
        q.at("2026-11-18T01:30");
        q.check(q.task("오답 정리"));
        q.eq("말풍선", "잘했어! 오늘 보상은 다 받았지만 체크는 계속 돼.", q.app.store.talk);
        q.eq("상태", "610P 경험치240 횟수20 성취도21/22", st(q));
        q.step("2", "창을 켜 둔 채 04:00을 넘긴다");
        q.tick("2026-11-18T04:00:30");
        q.eq("말풍선", "오늘은 할 일이 없어. 어제 못 끝낸 일 1개는 달력에 남아 있어. 아래 칸에 하나 적어 볼까?", q.app.store.talk);
        q.eq("위 막대", "연속 접속 31일, 보유 610P, 출석 +20P", q.top());
        q.eq("고른 날짜", "2026-11-18", q.main.calendar.selected());
        q.eq("성취도", "비어 있음", q.progress());
        q.step("3", "출석한다");
        q.attend();
        q.eq("포인트", 630, q.app.store.points);
        q.step("4", "국어 문법 5를 쓰고 추가를 눌러 넣은 뒤 체크한다");
        q.eq("칸이 비면 추가는 흐림", false, q.main.quickButton.isEnabled());
        q.quickButton("국어 문법 5");
        q.eq("칸", "", q.main.quick.getText());
        q.check(q.task("국어 문법 5"));
        q.eq("상태", "640P 경험치250 횟수1 성취도1/1", st(q));
        q.eq("말풍선", "오늘 할 일을 다 끝냈어! 푹 쉬어.", q.app.store.talk);
        q.end();
    }

    static void uf43() {
        Qa q = new Qa("UF-4-3");
        q.start(Personas.p4());
        q.open();
        q.step("1", "일정관리를 연다");
        q.app.show("일정관리");
        q.eq("펫", "이번 주 남은 날의 시각 없는 할 일이 3개 있어. 자동 배치를 눌러 볼래?", q.schedBubble());
        q.step("2", "자동 배치를 누른다");
        Planner.Placement p = q.place();
        q.eq("결과 창", "11월 17일 21:40부터 11월 22일까지, 할 일마다 그 날짜 안의 빈 시간에 놓았어요"
                + " / 행정법 기출|11월 17일|21:40 ~ 22:40|60분 / 한국사 회독|11월 17일|22:40 ~ 23:40|60분"
                + " / 오답 정리|놓지 못함|그날 빈 시간이 모자라요|30분", q.placement(p));
        q.step("3", "적용을 누른다");
        q.apply(p);
        q.app.show("홈");
        q.has("목록 위쪽", "[ ] 행정법 기출 21:40~22:40 60분 / [ ] 한국사 회독 22:40~23:40 60분 / 시각 없는 할 일", q.rows());
        q.has("오답 정리는 그대로", "[ ] 오답 정리 시각 정하기 30분", q.rows());
        q.end();
    }

    static Qa uf51(Qa q) {
        q.start(Personas.p5());
        q.step("1", "앱을 연다");
        q.open();
        q.eq("말풍선", "오늘 할 일은 다 끝냈어!", q.app.store.talk);
        q.eq("위 막대", "연속 접속 3일, 보유 5P, 출석 +20P", q.top());
        q.eq("성취도, 레벨", "1/1 초코 Lv. 2 경험치 5 / 80", q.progress() + " " + q.level());
        q.step("2", "잡코리아 공고 정리 체크를 푼다");
        q.check(q.task("잡코리아 공고 정리"));
        q.eq("상태", "-5P 경험치45 횟수0 성취도0/1", st(q));
        q.eq("말풍선", "체크를 풀었어. -10P, 이제 Lv.1", q.app.store.talk);
        q.eq("레벨", "초코 Lv. 1 경험치 45 / 50", q.level());
        q.step("3", "달력 17에서 포트폴리오 정리 체크를 푼다");
        q.select("2026-11-17");
        q.check(q.task("포트폴리오 정리"));
        q.eq("상태", "-15P 경험치35 횟수0 성취도0/1", st(q));
        q.eq("말풍선", "체크를 풀었어. -10P", q.app.store.talk);
        q.end();
        return q;
    }

    static Qa uf52(Qa q) {
        q.after(Flows345::uf51);
        q.step("1", "달력 18에서 잡코리아 공고 정리를 다시 체크한다");
        q.select("2026-11-18");
        q.check(q.task("잡코리아 공고 정리"));
        q.eq("상태", "-5P 경험치45 횟수1 성취도1/1", st(q));
        q.eq("말풍선", "오늘 할 일을 다 끝냈어! 푹 쉬어.", q.app.store.talk);
        q.end();
        return q;
    }

    static void uf53() {
        Qa q = new Qa("UF-5-3").after(Flows345::uf52);
        q.step("1", "출석한다");
        q.attend();
        q.eq("포인트, 말풍선", "15 출석했어! +20P", q.app.store.points + " " + q.app.store.talk);
        q.step("2", "pet을 연다");
        q.app.show("나의 방");
        q.eq("pet", "배치 아이템 1 / 13, 하트, 쿠션 보유 중 ✓", q.pet());
        q.step("3", "상점을 연다");
        q.eq("상점", "털실 공 25P 모자라요 꺼짐 / 뼈다귀 25P 모자라요 꺼짐 / 쿠션 보유 중 방에서 치우기 켜짐 / 화분 65P 모자라요 꺼짐 / 러그 85P 모자라요 꺼짐 / 액자 105P 모자라요 꺼짐"
                + " / 걸이 화분 115P 모자라요 꺼짐 / 조명 135P 모자라요 꺼짐 / 책장 185P 모자라요 꺼짐 / 책상 235P 모자라요 꺼짐 / 소파 285P 모자라요 꺼짐 / 캣타워 335P 모자라요 꺼짐 / 우드 침대 385P 모자라요 꺼짐", q.shop());
        q.end();
    }
}
