package planningpet.pet.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static planningpet.shop.model.ItemId.CUSHION;

import java.time.Clock;
import java.util.Set;
import org.junit.jupiter.api.Test;
import planningpet.app.model.AppData;

/** 06 20번 toggle과 docs/porting/shop.md 2절의 E-ROOM 경계. */
class RoomTest {
    private final AppData data = new AppData(Clock.systemDefaultZone());
    private final Room room = new Room(data);

    @Test
    void T_SHOP_6_toggleTwice() {
        data.setOwnedItems(Set.of(CUSHION));
        data.setPlacedItems(Set.of(CUSHION));
        room.toggle(CUSHION);
        String first = data.getPlacedItems().toString();
        room.toggle(CUSHION);
        assertEquals("[] [CUSHION]", first + " " + data.getPlacedItems());
    }

    @Test
    void E_ROOM_1_toggleItemNotOwned() {
        assertThrows(IllegalStateException.class, () -> room.toggle(CUSHION));
        assertEquals("[] []", data.getOwnedItems() + " " + data.getPlacedItems());
    }
}
