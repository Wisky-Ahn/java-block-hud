package io.github.wiskyahn.blockhud.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.wiskyahn.blockhud.domain.model.IndicatorSource;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SystemMetricsServiceTest {

    @Test
    void cpuAndRamAreInZeroToOneRange() {
        SystemMetricsService metrics = new SystemMetricsService();
        metrics.poll();

        assertRatio(metrics.read(IndicatorSource.CPU_LOAD, null));
        assertRatio(metrics.read(IndicatorSource.CPU_IDLE, null));
        assertRatio(metrics.read(IndicatorSource.RAM_USED, null));
        assertRatio(metrics.read(IndicatorSource.RAM_FREE, null));
    }

    @Test
    void disabledAndUnsupportedReturnEmpty() {
        SystemMetricsService metrics = new SystemMetricsService();
        assertTrue(metrics.read(IndicatorSource.DISABLED, null).isEmpty());
        // GPU/VRAM 실시간은 미지원 처리
        assertTrue(metrics.read(IndicatorSource.GPU_USED, null).isEmpty());
        assertTrue(metrics.read(IndicatorSource.VRAM_FREE, null).isEmpty());
    }

    private static void assertRatio(Optional<Double> value) {
        assertTrue(value.isPresent(), "값이 있어야 함");
        double v = value.get();
        assertTrue(v >= 0.0 && v <= 1.0, "0..1 범위여야 함: " + v);
    }
}
