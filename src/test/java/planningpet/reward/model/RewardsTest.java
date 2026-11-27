package planningpet.reward.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import planningpet.app.model.AppData;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.PetState;
import planningpet.schedule.model.Category;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Task;

/** 06 8번과 docs/porting/rewards.md 2절의 E-RWD 경계. 펫은 고른 고양이 하나이고 complete와 done은 값을 한 줄로 잇는다. */
class RewardsTest {
    private final Task a = task("A", d(16));
    private final AppData data = new AppData(Clock.systemDefaultZone(), List.of(a), List.of());

    RewardsTest() {
        data.addPet(new PetState(PetKind.CAT, "나비"));
        data.setActiveKind(PetKind.CAT);
    }

    private static LocalDate d(int day) { return LocalDate.of(2026, 11, day); }

    private static Task task(String title, LocalDate date) {
        return new Task(title, 1, Kind.TASK, title, date, null, 30, "", false, Category.OTHER, null, null, null,
                null, false, null, 0, 0, null, null);
    }

    private String complete(Task t, LocalDate today) {
        RewardResult r = new Rewards(data).complete(t, today);
        return r.outcome() + " " + r.points() + " " + r.exp() + ", " + data.getPoints() + "P "
                + data.pet(PetKind.CAT).getExp() + " " + data.getRewardCountDay() + " " + data.getRewardCount();
    }

    private static String done(Task t) {
        return t.isDone() + " " + t.getDoneDay() + " " + t.getRewardPoints() + " " + t.getRewardExp() + " "
                + t.getRewardDay() + " " + t.getRewardPet();
    }

    @Test
    void T_RWD_1_today() {
        assertEquals("REWARDED 10 10, 10P 10 2026-11-16 1", complete(a, d(16)));
        assertEquals("true 2026-11-16 10 10 2026-11-16 CAT", done(a));
    }
}
