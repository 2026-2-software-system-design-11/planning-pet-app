package planningpet.app.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import org.junit.jupiter.api.Test;

/** 공용 저장 대역 MemoryStore(#518). */
class MemoryStoreTest {
    @Test
    void loadsWhatItWasGiven() {
        assertEquals(LoadResult.firstRun(), new MemoryStore().load());
        LoadResult unreadable = LoadResult.unreadable(LoadResult.Reason.values()[0]);
        assertSame(unreadable, new MemoryStore(unreadable).load());
    }

    @Test
    void countsSavesAndFailsWhenAsked() throws IOException {
        MemoryStore store = new MemoryStore();
        AppData data = Apps.data("2026-11-18T10:00");
        store.save(data);
        store.failing = true;
        assertEquals("디스크가 찼다", assertThrows(IOException.class, () -> store.save(data)).getMessage());
        assertEquals(1, store.saves);
        assertSame(data, store.last);
    }
}
