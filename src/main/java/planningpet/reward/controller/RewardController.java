package planningpet.reward.controller;

import planningpet.app.controller.AppController;
import planningpet.app.model.AppData;
import planningpet.pet.model.PetTalk;
import planningpet.pet.model.Situation;
import planningpet.reward.model.Rewards;

/** 보상 controller(07). 출석 단추를 받아 보상 model을 바꾸고 22번 change로 감싼다. 완료와 완료 취소는 S3이 더한다. */
public final class RewardController {
    private final AppData data;
    private final AppController app;
    private final Rewards rewards;
    private final PetTalk talk;

    public RewardController(AppData data, AppController app) {
        this.data = data;
        this.app = app;
        this.rewards = new Rewards(data);
        this.talk = new PetTalk(data);
    }

    /** 06 11번. 오늘 처음이면 20P를 주고 펫이 ATTENDED를 말한다. 이미 했으면 아무것도 바꾸지 않는다. */
    public void attend() {
        app.change("출석", () -> {
            if (rewards.attend(data.today())) {
                app.say(talk.say(Situation.ATTENDED), null);
            }
        });
    }
}
