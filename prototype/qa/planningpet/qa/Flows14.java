package planningpet.qa;

import planningpet.home.view.CalendarPanel;
import planningpet.pet.model.Wardrobe;
import planningpet.schedule.model.TaskBook;
import planningpet.stats.model.Stats;
import java.awt.Component;
import java.awt.Container;
import javax.swing.AbstractButton;

/** UF-1-4(D-60~D-63): 오늘 버튼, 펫 데려오기와 고르기, 꾸미기 적용 물음, 이름 바꾸기, 오늘을 끝낸 것만 세는 통계. */
final class Flows14 {
    private Flows14() {
    }

    static void uf14() {
        Qa q = new Qa("UF-1-4");
        q.start(Personas.p1());
        q.open();
        q.step("1", "일정관리를 연다");
        q.app.show("일정관리");
        q.eq("오늘 버튼(이번 주)", "꺼짐 이번 주를 보고 있어요", onOff(q.sched.today) + " " + q.sched.today.getToolTipText());
        q.sched.move(q.sched.monday().plusWeeks(1));
        q.eq("오늘 버튼(다음 주)", "켜짐", onOff(q.sched.today));
        q.sched.today.doClick();
        q.eq("오늘을 누른 뒤", "2026-11-09 꺼짐", q.sched.monday() + " " + onOff(q.sched.today));
        q.step("2", "홈에서 달력 >를 누른다");
        q.app.show("홈");
        CalendarPanel cal = QaDrive.find(q.main, CalendarPanel.class);
        cal.next.doClick();
        String text = Flows8910.text(q.main);
        q.yes("달력 2026년 12월", text.contains("2026년 12월"));
        q.yes("목록은 그대로", text.contains("11월 14일 (토) 할 일"));
        q.eq("달력 오늘(12월)", "켜짐", onOff(cal.today));
        cal.today.doClick();
        q.yes("달력 2026년 11월", Flows8910.text(q.main).contains("2026년 11월"));
        q.eq("달력 오늘(오늘을 고름)", "2026-11-14 꺼짐 오늘을 보고 있어요", cal.selected() + " " + onOff(cal.today) + " " + cal.today.getToolTipText());
        q.step("3", "나의 방에서 강아지를 이름 없이 데려온다");
        q.app.show("나의 방");
        q.yes("데려옴", q.app.pet.adopt(true, ""));
        String hello = "안녕, 나는 바둑이야. 이제 경험치는 내가 받을게!";
        q.eq("고른 펫, 방 말풍선, 홈 말풍선", "DOG 바둑이 / " + hello + " / " + hello,
                q.app.store.activeKind + " " + q.app.store.pet().name + " / " + q.pet.room.bubble + " / " + q.app.store.talk);
        q.step("4", "방 왼쪽 위의 나비 칩을 누르고 홈으로 간다");
        java.util.List<String> opened = new java.util.ArrayList<>();
        q.pet.characterOpener = dress -> opened.add(dress ? "꾸미기" : "상태");
        chip(q, "CAT").doClick();
        q.eq("칩은 창을 열지 않는다(D-80)", "", String.join(", ", opened));
        q.app.show("홈");
        q.eq("홈 펫 카드", "나비 Lv. 3 / 이제 경험치는 내가 받을게! 오늘 할 일 3개 남았어.",
                q.main.pet.nameText() + " " + q.main.pet.levelText() + " / " + q.main.pet.bubbleText());
        q.eq("방 말풍선은 하트", "", q.pet.room.bubble);
        q.step("4-1", "나의 방에서 강아지를 누른다");
        q.app.show("나의 방");
        q.pet.room.pat("DOG");
        q.eq("강아지 반응, 고른 펫 그대로", "멍멍, 좋아! / DOG / CAT", q.pet.room.bubble + " / " + q.pet.room.bubbleKind + " / "
                + q.app.store.activeKind);
        q.step("5", "캐릭터 창에서 리본 목걸이를 입혀 보고 닫다가 취소한다");
        String[] asked = {""};
        q.pet.characterOpener = dress -> q.pet.character.opened(dress);
        tip(q.pet, null, "캐릭터에서 꾸미기").doClick();
        text(q.pet.character, "리본 목걸이").doClick();
        q.pet.character.ask = (title, message, options) -> {
            asked[0] = title + " / " + message + " / " + String.join(", ", options);
            return 2;
        };
        q.eq("닫기에 취소", "false 꾸미기 / 입혀 본 장신구를 적용할까요? / 적용, 버리기, 취소 / 리본 목걸이",
                q.pet.character.confirmLeave() + " " + asked[0] + " / " + q.pet.character.draft.get(Wardrobe.NECK));
        q.step("6", "다시 닫고 버리기를 누른다");
        q.pet.character.ask = (title, message, options) -> 1;
        q.eq("닫기에 버리기", "true 기본 목걸이 기본 목걸이", q.pet.character.confirmLeave() + " "
                + q.pet.character.draft.get(Wardrobe.NECK) + " " + q.app.store.pet().wear.get(Wardrobe.NECK));
        q.step("7", "다시 열어 리본 목걸이를 입혀 보고 닫다가 적용한다");
        tip(q.pet, null, "캐릭터에서 꾸미기").doClick();
        text(q.pet.character, "리본 목걸이").doClick();
        q.pet.character.ask = (title, message, options) -> 0;
        q.eq("닫기에 적용", "true 리본 목걸이", q.pet.character.confirmLeave() + " " + q.app.store.pet().wear.get(Wardrobe.NECK));
        q.step("8", "이름을 보리로 바꾼다");
        q.app.pet.rename(q.app.store.pet().kind, "보리");
        q.eq("이름과 홈 말풍선", "보리 / 이제 내 이름은 보리야.", q.app.store.pet().name + " / " + q.app.store.talk);
        q.step("9", "통계를 연다");
        q.app.show("통계");
        Stats.Rate r = Stats.rate(q.app.store, Stats.Period.week(q.app.store.today()));
        q.eq("이번 주 달성률", "2 / 9 22%", r.done() + " / " + r.total() + " " + r.percent() + "%");
        q.yes("이번 주 월요일", TaskBook.mondayOf(q.app.store.today()).toString().equals("2026-11-09"));
        q.end();
    }

    /** 나의 방 왼쪽 위의 그 펫 칩(툴팁이나 글에 이름이 있다). */
    private static AbstractButton chip(Qa q, String kind) {
        planningpet.pet.view.PetScreen pet = q.pet;
        String name = q.app.store.pet(kind).name;
        for (AbstractButton b : QaRules.all(pet.room, AbstractButton.class)) {
            if (name.equals(b.getToolTipText()) || name.equals(b.getText())) {
                return b;
            }
        }
        throw new IllegalStateException("펫 칩을 찾지 못함: " + kind);
    }

    private static String onOff(AbstractButton b) {
        return b.isEnabled() ? "켜짐" : "꺼짐";
    }

    /** 툴팁이나 접근 이름이 name인 버튼(원 화살표 등). */
    private static AbstractButton tip(Container root, String name) {
        return tip(root, name, null);
    }

    /** 툴팁이나 접근 이름이 name인 버튼, 또는 글이 text인 버튼. */
    private static AbstractButton tip(Container root, String name, String text) {
        for (AbstractButton b : QaRules.all(root, AbstractButton.class)) {
            String an = b.getAccessibleContext().getAccessibleName();
            if (name != null && (name.equals(b.getToolTipText()) || name.equals(an))) {
                return b;
            }
            if (text != null && text.equals(b.getText())) {
                return b;
            }
        }
        throw new IllegalStateException("버튼을 찾지 못함: " + (name != null ? name : text));
    }

    private static AbstractButton text(Component root, String text) {
        return tip((Container) root, null, text);
    }
}
