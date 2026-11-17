package planningpet.shop.controller;

import planningpet.app.model.Store;
import planningpet.shop.model.Shop;

/** 상점 controller. 사기 버튼을 받아 Model을 바꾸고 알린다. */
public final class ShopController {
    private final Store store;
    private final Shop shop;

    public ShopController(Store store) {
        this.store = store;
        this.shop = new Shop(store);
    }

    /** 샀으면 thanks를 부르고 화면에 알린 뒤 true. 포인트가 모자라거나 이미 있으면 false. */
    public boolean buy(String item, Runnable thanks) {
        if (shop.buy(item) != 0) {
            return false;
        }
        thanks.run();
        store.changed();
        return true;
    }
}
