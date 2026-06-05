package io.github.wiskyahn.blockhud.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class IndicatorSourceTest {

    @Test
    void mapsOriginalSourceKeys() {
        assertEquals(IndicatorSource.CPU_IDLE, IndicatorSource.fromKey("cpuIdle"));
        assertEquals(IndicatorSource.RAM_USED, IndicatorSource.fromKey("ramUsed"));
        assertEquals(IndicatorSource.DISK_USED, IndicatorSource.fromKey("diskUsed"));
        assertEquals(IndicatorSource.BATTERY_CHARGE, IndicatorSource.fromKey("batteryCharge"));
        assertEquals(IndicatorSource.VRAM_FREE, IndicatorSource.fromKey("vramFree"));
    }

    @Test
    void unknownOrNullBecomesDisabled() {
        assertEquals(IndicatorSource.DISABLED, IndicatorSource.fromKey("nonsense"));
        assertEquals(IndicatorSource.DISABLED, IndicatorSource.fromKey(null));
        assertEquals(IndicatorSource.DISABLED, IndicatorSource.fromKey("disabled"));
    }
}
