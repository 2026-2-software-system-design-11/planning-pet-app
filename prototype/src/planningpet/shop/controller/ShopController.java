package planningpet.shop.controller;

import planningpet.app.controller.AppController;
import planningpet.app.model.Store;
import planningpet.shop.model.Shop;

/** 상점 controller. 사기와 되팔기 버튼을 받아 Model을 바꾼다. */
public final class ShopController {
    private final AppController control;
    private final Shop shop;

    public ShopController(AppController control, Store store) {
        this.control = control;
        this.shop = new Shop(store);
    }

    /** 샀으면 thanks를 부르고 true. 포인트가 모자라거나 이미 있으면 false. */
    public boolean buy(String item, Runnable thanks) {
        boolean[] bought = {false};
        control.change(() -> {
            if (shop.buy(item) == 0) {
                thanks.run();
                bought[0] = true;
            }
        });
        return bought[0];
    }

    /** D-85. 산 값 그대로 되판다(방에 놓였으면 함께 뺀다). 되팔았으면 true. */
    public boolean sell(String item) {
        boolean[] sold = {false};
        control.change(() -> sold[0] = shop.sell(item) >= 0);
        return sold[0];
    }
}
