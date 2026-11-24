package planningpet.qa;

import planningpet.common.view.Ui;
import planningpet.schedule.model.*;
import planningpet.schedule.view.*;
import static planningpet.qa.QaRun.st;

/** docs/user-flows.md의 기간 할 일(D-65)과 할 일 반복(D-64) 흐름: UF-2-6, UF-5-4. 단계 번호와 문장은 문서와 같다. */
final class FlowsPeriod {
    private FlowsPeriod() {
    }

    /** 기간 칸: "기간 안에 하기 켜짐(활성) 마감 2026-11-22", 마감 칸이 안 보이면 "마감 없음". */
    static String period(TaskForm f) {
        return "기간 안에 하기 " + (f.period.isSelected() ? "켜짐" : "꺼짐") + (f.period.isEnabled() ? "(활성)" : "(비활성)")
                + (f.due.isVisible() && f.dueLabel.isVisible() ? " 마감 " + Ui.dateOf(f.due) : " 마감 없음");
    }

    static void uf26() {
        Qa q = new Qa("UF-2-6");
        q.start(Personas.p2());
        q.step("1", "앱을 열고 목록 제목 줄의 일정 추가를 누른다");
        q.open();
        q.eq("말풍선(UF-2-1의 1단계와 같다)", "오늘 할 일 3개 남았어. 어제 못 끝낸 일 1개는 달력에 남아 있어. 'B사 자소서 제출' 마감이 내일이야.",
                q.app.store.talk);
        q.has("연 창", "새 일정 2026-11-16", q.addFromList());
        TaskForm f = q.form(q.main.calendar.selected().toString());
        q.eq("날짜, 기간", "2026-11-16 기간 안에 하기 꺼짐(활성) 마감 없음", Ui.dateOf(f.date) + " " + period(f));
        q.step("2", "제목 E사 지원서, 걸리는 시간 120을 넣고 기간 안에 하기를 켠다");
        f.title.setText("E사 지원서");
        f.minutes.setValue(120);
        f.period.doClick();
        q.eq("기간(T-DEF-11)", "기간 안에 하기 켜짐(활성) 마감 2026-11-22", period(f));
        q.eq("반복 칸(D-65)", "안 함(비활성) [] 끝 2026-12-14(흐림) / true 기간 안에 하는 할 일은 반복할 수 없어요",
                Flows345.repeat(f) + " / " + f.repeatHint.isVisible() + " " + f.repeatHint.getText());
        q.step("3", "마감을 2026-11-15로 바꿔 저장한다");
        Ui.setDate(f.due, QaDrive.d("2026-11-15"));
        q.eq("저장, 마감 칸 아래", "false 마감이 날짜보다 앞이에요 true", q.save(f, null) + " " + f.errors.get("마감").getText()
                + " " + f.errors.get("마감").isVisible());
        q.step("4", "마감을 2026-11-22로 바꿔 저장한다");
        Ui.setDate(f.due, QaDrive.d("2026-11-22"));
        q.yes("저장", q.save(f, null));
        q.eq("목록", "[ ] 고정 졸업 세미나 15:00~16:40 100분 / [ ] B사 자소서 1번 19:00~21:00 120분 / 시각 없는 할 일"
                + " / [ ] C사 서류 결과 확인 시각 정하기 30분 / [ ] E사 지원서 ~11/22까지 시각 정하기 120분", q.rows());
        q.eq("성취도", "0/4", q.progress());
        q.eq("달력 16, 22", "16 B사 자소…(진함), 22 E사 지원…(진함)", q.cell(16) + ", " + q.cell(22));
        Task e = q.task("E사 지원서");
        q.eq("E사 지원서(T-ADD-7)", "2026-11-16 2026-11-22 null", e.date + " " + e.dueDate + " " + e.seriesId);
        q.step("5", "달력 18을 누른다");
        q.select("2026-11-18");
        q.eq("목록", "[ ] 고정 경영전략 13:00~14:15 75분 / 시각 없는 할 일 / [ ] E사 지원서 ~11/22까지 시각 정하기 120분", q.rows());
        q.eq("말풍선", "11월 18일 할 일은 2개야.", q.app.store.talk);
        q.step("6", "앱을 끝내고 시계를 2026-11-17 22:30으로 옮겨 다시 연다");
        q.at("2026-11-17T22:30");
        q.start(q.app.store);
        q.open();
        q.eq("고른 날짜", "2026-11-17", q.main.calendar.selected());
        q.eq("목록", "시각 없는 할 일 / [ ] B사 자소서 제출 시각 정하기 30분 / [ ] E사 지원서 ~11/22까지 시각 정하기 120분", q.rows());
        q.eq("말풍선", "오늘 할 일 2개 남았어. 어제 못 끝낸 일 2개는 달력에 남아 있어. '2026 하반기 공채 자기…' 마감이 모레야.",
                q.app.store.talk);
        q.has("위 막대", "연속 접속 11일", q.top());
        q.eq("성취도", "0/2", q.progress());
        q.step("7", "일정관리를 연다");
        q.app.show("일정관리");
        q.has("주", "11월 16일 ~ 11월 22일", Flows8910.text(q.sched));
        q.eq("오른쪽 목록(T-WEEK-2)", "[ ] 11/16 C사 서류 결과 확인 30분 지난 날 / [ ] 11/16 E사 지원서 ~11/22까지 120분"
                + " / [ ] 11/17 B사 자소서 제출 30분 / [ ] 11/19 2026 하반기 공채 자기소개서 1번 문항 90분", q.schedRows());
        q.eq("펫", "이번 주 남은 날의 시각 없는 할 일이 3개 있어. 자동 배치를 눌러 볼래?", q.schedBubble());
        q.step("8", "자동 배치를 누른다");
        Planner.Placement p = q.place();
        q.eq("결과 창(T-AUTO-11)", "11월 17일 22:30부터 11월 22일까지, 할 일마다 그 날짜(기간 할 일은 기간) 안의 빈 시간에 놓았어요"
                + " / E사 지원서|11월 18일|09:00 ~ 11:00|120분 / B사 자소서 제출|11월 17일|22:30 ~ 23:00|30분"
                + " / 2026 하반기 공채 자기소개서 1번 문항|11월 19일|09:00 ~ 10:30|90분", q.placement(p));
        q.step("9", "적용을 누른다");
        q.apply(p);
        q.eq("시간표 화 17, 수 18, 목 19", "22:30 B사 자소서 제출 / 09:00 E사 지원서, 13:00 경영전략 / 09:00 2026 하반기 공채 자기소개서 1번 문항",
                Flows345.day(q, "2026-11-17") + " / " + Flows345.day(q, "2026-11-18") + " / " + Flows345.day(q, "2026-11-19"));
        q.eq("E사 지원서 날짜와 마감", "2026-11-18 2026-11-22", e.date + " " + e.dueDate);
        q.eq("오른쪽 목록", "[ ] 11/16 C사 서류 결과 확인 30분 지난 날", q.schedRows());
        q.eq("펫", "지금 자동 배치할 할 일은 없어.", q.schedBubble());
        q.step("10", "홈에서 달력 18을 누르고 E사 지원서를 체크한다");
        q.select("2026-11-18");
        q.eq("체크 전 목록", "[ ] E사 지원서 ~11/22까지 09:00~11:00 120분 / [ ] 고정 경영전략 13:00~14:15 75분", q.rows());
        q.check(e);
        q.eq("상태", "350P 경험치530 횟수1 성취도1/2", st(q));
        q.eq("말풍선", "잘했어! +10P", q.app.store.talk);
        q.eq("끝낸 날로(T-RWD-11)", "2026-11-17 / [ ] 고정 경영전략 13:00~14:15 75분", e.date + " / " + q.rows());
        q.step("11", "달력 오늘을 누른다");
        q.main.calendar.today.doClick();
        q.eq("목록", "[x] E사 지원서 ~11/22까지 09:00~11:00 120분 / [ ] B사 자소서 제출 22:30~23:00 30분", q.rows());
