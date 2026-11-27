package planningpet.shop.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static planningpet.shop.model.ItemId.CUSHION;
import static planningpet.shop.model.ItemId.RUG;
import static planningpet.shop.model.ItemId.SOFA;
import static planningpet.shop.model.ItemId.YARN_BALL;

import java.time.Clock;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import planningpet.app.model.AppData;

/** 06 20번과 docs/porting/shop.md 2절의 E-SHOP 경계. */
class ShopTest {
    private final AppData data = new AppData(Clock.systemDefaultZone());
    private final Shop shop = new Shop(data);

    /** find 결과의 이름. */
    private List<String> find(String category, String query, boolean ownedOnly) {
        return shop.find(category, query, ownedOnly).stream().map(i -> i.id().label()).toList();
    }

    /** points로 시작해 item을 산 결과, 그 뒤의 포인트, 보유, 놓임. */
    private String buy(int points, ItemId item) {
        data.setPoints(points);
        BuyResult r = shop.buy(item);
        return r.outcome() + " " + r.lack() + ", " + data.getPoints() + "P " + data.getOwnedItems() + " "
                + data.getPlacedItems();
    }

    @Test
    void T_SHOP_1_buyPlacesItem() {
        assertEquals("BOUGHT 0, 70P [CUSHION] [CUSHION]", buy(120, CUSHION));
    }

    @Test
    void T_SHOP_2_notEnough() {
        assertEquals("NOT_ENOUGH 10, 40P [] []", buy(40, CUSHION));
    }

    @Test
    void T_SHOP_3_negativePoints() {
        assertEquals("NOT_ENOUGH 70, -20P [] []", buy(-20, CUSHION));
    }

    @Test
    void T_SHOP_4_alreadyOwned() {
        data.setOwnedItems(Set.of(CUSHION));
        data.setPlacedItems(Set.of(CUSHION));
        assertEquals("ALREADY_OWNED 0, 200P [CUSHION] [CUSHION]", buy(200, CUSHION));
    }

    @Test
    void T_SHOP_7_thirteenItemsSum2210() {
        List<Item> all = shop.find("전체", "", false);
        assertEquals("13 2210", all.size() + " " + all.stream().mapToInt(Item::price).sum());
    }

    @Test
    void T_SHOP_8_satisfaction() {
        assertEquals(List.of(0, 14, 100), List.of(shop.satisfaction(Set.of()), shop.satisfaction(Set.of(SOFA)),
                shop.satisfaction(EnumSet.allOf(ItemId.class))));
    }

    @Test
    void T_SHOP_9_categoryAndQuery() {
        assertEquals(List.of("털실 공", "뼈다귀"), find("장난감", "", false));
        assertEquals(List.of("걸이 화분"), find("전체", "걸이", false));
    }

    @Test
    void T_SHOP_10_ownedOnly() {
        data.setOwnedItems(Set.of(CUSHION));
        assertEquals(List.of("쿠션"), find("전체", "", true));
    }

    @Test
    void E_SHOP_2_pointsEqualPrice() {
        assertEquals("BOUGHT 0, 0P [CUSHION] [CUSHION]", buy(50, CUSHION));
    }

    @Test
    void E_SHOP_3_oneShort() {
        assertEquals("NOT_ENOUGH 1, 49P [] []", buy(49, CUSHION));
    }

    @Test
    void E_SHOP_4_ownedButPutAwayStaysAway() {
        data.setOwnedItems(Set.of(CUSHION));
        assertEquals("ALREADY_OWNED 0, 0P [CUSHION] []", buy(0, CUSHION));
    }

    @Test
    void E_SHOP_6_satisfactionRounds() {
        assertEquals("2 2", shop.satisfaction(Set.of(YARN_BALL)) + " " + shop.satisfaction(Set.of(CUSHION)));
    }

    @Test
    void E_SHOP_7_allButYarnBall() {
        assertEquals(98, shop.satisfaction(EnumSet.complementOf(EnumSet.of(YARN_BALL))));
    }

    @Test
    void E_SHOP_8_oneCategoryInPriceOrder() {
        assertEquals(List.of("책장", "책상", "소파", "캣타워", "우드 침대"), find("가구", "", false));
    }

    @Test
    void E_SHOP_9_queryIsStripped() {
        assertEquals(List.of("걸이 화분"), find("전체", "  걸이 ", false));
        assertEquals(13, find("전체", "   ", false).size());
    }

    @Test
    void E_SHOP_10_partOfName() {
        assertEquals(List.of("화분", "걸이 화분"), find("전체", "화분", false));
    }

    @Test
    void E_SHOP_11_nothingMatches() {
        assertEquals(List.of(), find("전체", "없는", false));
        assertEquals(List.of(), find("전체", "", true));
        data.setOwnedItems(Set.of(CUSHION, RUG));
        assertEquals(List.of("러그"), find("장식", "", true));
    }
}
