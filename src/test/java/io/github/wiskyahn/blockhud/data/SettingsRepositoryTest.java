package io.github.wiskyahn.blockhud.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.wiskyahn.blockhud.domain.settings.Settings;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SettingsRepositoryTest {

    @Test
    void loadReturnsDefaultsWhenMissing(@TempDir Path dir) throws IOException {
        Settings s = new SettingsRepository(dir.resolve("none.json")).load();
        // 스키마 기본값
        assertTrue(s.getBoolean("Use24HourClock"));
        assertEquals("cpuIdle", s.get("HealthBarSource"));
        assertEquals(60, s.getInt("HotbarSlotSize", 0));
    }

    @Test
    void savedOverridesLayerOverDefaults(@TempDir Path dir) throws IOException {
        SettingsRepository repo = new SettingsRepository(dir.resolve("settings.json"));
        Settings s = Settings.withDefaults();
        s.setBoolean("Use24HourClock", false);
        s.set("HealthBarSource", "ramUsed");
        repo.save(s);

        Settings loaded = repo.load();
        assertEquals(false, loaded.getBoolean("Use24HourClock"));
        assertEquals("ramUsed", loaded.get("HealthBarSource"));
        // 저장 안 한 키는 여전히 기본값
        assertEquals(60, loaded.getInt("HotbarSlotSize", 0));
    }
}
