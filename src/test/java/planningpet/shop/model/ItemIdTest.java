package planningpet.shop.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/** docs/porting/app-data.md 2절의 아이템 경계(D-52). */
class ItemIdTest {
    @Test
    void E_SHOP_1_itemsInPriceOrder() {
        assertEquals(List.of("털실 공", "뼈다귀", "쿠션", "화분", "러그", "액자", "걸이 화분", "조명", "책장", "책상", "소파",
                "캣타워", "우드 침대"), Arrays.stream(ItemId.values()).map(ItemId::label).toList());
    }
}
