package planningpet.shop.model;

/** 방 아이템(D-52). 가격 순서이고 같은 가격은 D-52에 적은 순서다. 가격과 분류는 `Item`(M15)이 정한다. */
public enum ItemId {
    YARN_BALL("털실 공"),
    BONE("뼈다귀"),
    CUSHION("쿠션"),
    PLANT("화분"),
    RUG("러그"),
    FRAME("액자"),
    HANGING_PLANT("걸이 화분"),
    LAMP("조명"),
    BOOKSHELF("책장"),
    DESK("책상"),
    SOFA("소파"),
    CAT_TOWER("캣타워"),
    WOOD_BED("우드 침대");

    private final String label;

    ItemId(String label) {
        this.label = label;
    }

    /** 화면에 보이는 이름. */
    public String label() {
        return label;
    }
}
