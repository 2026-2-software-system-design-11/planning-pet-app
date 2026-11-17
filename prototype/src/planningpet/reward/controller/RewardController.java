package planningpet.reward.controller;

import planningpet.app.model.Store;
import planningpet.reward.model.Game;

/** 보상 controller. 출석 버튼을 받아 Model을 바꾸고 알린다. */
public final class RewardController {
    private final Store store;
    private final Game game;

    public RewardController(Store store, Game game) {
        this.store = store;
        this.game = game;
    }

    /** 06의 11번. 오늘 처음이면 포인트를 주고 펫이 말한다. */
    public void attend() {
        if (game.attend()) {
            store.say("출석했어! +20P");
            store.changed();
        }
    }
}
