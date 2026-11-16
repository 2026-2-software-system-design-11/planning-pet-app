package planningpet.proto;

/** docs/personas.md의 열 명. 각 함수는 그 사람의 처음 상태를 새로 만든다. */
final class Personas {
    private Personas() {
    }

    /** ① 한도윤. 프로토타입 목 데이터와 같다. */
    static Store p1() {
        return MockData.sample();
    }

    static Store p2() {
        Store s = Fx.store("2026-11-16T20:10", 340, 520, 9, "2026-11-15", null, 0, "CAT", "모카", "책장", "책장");
        Fx.weekly(s, "졸업 세미나", "2026-09-07", "15:00", 100, "2026-12-14");
        Fx.weekly(s, "경영전략", "2026-09-02", "13:00", 75, "2026-12-16");
        Fx.task(s, "2026-11-15", "A사 인적성 대비", null, 60);
        Fx.task(s, "2026-11-16", "B사 자소서 1번", "19:00", 120);
        Fx.task(s, "2026-11-16", "C사 서류 결과 확인", null, 30);
        Fx.task(s, "2026-11-17", "B사 자소서 제출", null, 30);
        Fx.task(s, "2026-11-19", "2026 하반기 공채 자기소개서 1번 문항", null, 90);
        Fx.task(s, "2026-11-24", "D사 면접 준비", null, 60);
        return s;
    }

    static Store p3() {
        Store s = Fx.store("2026-11-16T12:30", 80, 140, 2, "2026-11-15", null, 0, "CAT", "치즈", "화분", "화분");
        Fx.weekly(s, "열역학", "2026-08-31", "10:30", 75, "2026-12-16");
        Fx.weekly(s, "열역학", "2026-09-02", "10:30", 75, "2026-12-16");
        Fx.task(s, "2026-11-19", "설계 과제", null, 480);
        Fx.task(s, "2026-11-19", "팀 회의", "17:00", 120);
        return s;
    }

    static Store p4() {
        Store s = Fx.store("2026-11-17T21:40", 600, 230, 30, "2026-11-17", "2026-11-17", 19, "CAT", "보리",
                "쿠션,화분,러그", "쿠션,화분,러그");
        String[][] done = {{"국어 문법", "4", "30"}, {"영어 단어", "6", "30"}, {"행정학", "6", "60"}, {"한국사", "3", "60"}};
        for (String[] d : done) {
            for (int i = 1; i <= Integer.parseInt(d[1]); i++) {
                Fx.done(Fx.task(s, "2026-11-17", d[0] + " " + i, null, Integer.parseInt(d[2])), "2026-11-17");
            }
        }
        Fx.task(s, "2026-11-17", "행정법 기출", null, 60);
        Fx.task(s, "2026-11-17", "한국사 회독", null, 60);
        Fx.task(s, "2026-11-17", "오답 정리", null, 30);
        return s;
    }

    static Store p5() {
        Store s = Fx.store("2026-11-18T10:20", 5, 55, 3, "2026-11-18", null, 1, "DOG", "초코", "쿠션", "쿠션");
        Fx.done(Fx.task(s, "2026-11-17", "포트폴리오 정리", null, 60), "2026-11-17");
        Fx.done(Fx.task(s, "2026-11-18", "잡코리아 공고 정리", null, 30), "2026-11-18");
        return s;
    }
}
