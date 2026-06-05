package io.github.wiskyahn.blockhud.service;

import io.github.wiskyahn.blockhud.domain.model.IndicatorSource;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.PowerSource;
import oshi.software.os.OSFileStore;

/**
 * 시스템 지표 수집 (OSHI). 원본 LoadComputerInfo.ps1 + measure 대체. (DESIGN.md §8.2)
 *
 * <p>각 소스를 0..1 비율로 반환. 미지원(GPU/VRAM 실시간, 배터리 없음 등)은 {@link Optional#empty()}.
 * CPU 부하는 직전 {@link #poll()} 이후의 평균이므로 1Hz로 주기 호출해야 한다.
 */
public final class SystemMetricsService {

    private static final Logger log = LoggerFactory.getLogger(SystemMetricsService.class);

    private final SystemInfo systemInfo = new SystemInfo();
    private final CentralProcessor processor = systemInfo.getHardware().getProcessor();

    private long[] prevTicks = processor.getSystemCpuLoadTicks();
    private double lastCpuLoad;

    /** CPU 부하 갱신 (틱 차분). 1Hz로 호출. */
    public void poll() {
        double load = processor.getSystemCpuLoadBetweenTicks(prevTicks);
        prevTicks = processor.getSystemCpuLoadTicks();
        if (!Double.isNaN(load)) {
            lastCpuLoad = clamp(load);
        }
    }

    public Optional<Double> read(IndicatorSource source, String diskTarget) {
        return switch (source) {
            case DISABLED -> Optional.empty();
            case CPU_LOAD -> Optional.of(lastCpuLoad);
            case CPU_IDLE -> Optional.of(1 - lastCpuLoad);
            case RAM_USED -> ram(true);
            case RAM_FREE -> ram(false);
            case DISK_USED -> disk(diskTarget, true);
            case DISK_FREE -> disk(diskTarget, false);
            case BATTERY_CHARGE -> battery(false);
            case BATTERY_DRAIN -> battery(true);
            // GPU/VRAM 실시간 사용률은 OS·드라이버별 제한 → 미지원 처리 (DESIGN.md 플랫폼 매트릭스)
            case GPU_USED, GPU_FREE, VRAM_USED, VRAM_FREE -> Optional.empty();
        };
    }

    private Optional<Double> ram(boolean used) {
        GlobalMemory mem = systemInfo.getHardware().getMemory();
        long total = mem.getTotal();
        if (total <= 0) {
            return Optional.empty();
        }
        double usedRatio = clamp((double) (total - mem.getAvailable()) / total);
        return Optional.of(used ? usedRatio : 1 - usedRatio);
    }

    private Optional<Double> battery(boolean drain) {
        try {
            List<PowerSource> sources = systemInfo.getHardware().getPowerSources();
            if (sources.isEmpty()) {
                return Optional.empty();
            }
            double charge = clamp(sources.get(0).getRemainingCapacityPercent());
            return Optional.of(drain ? 1 - charge : charge);
        } catch (Exception e) {
            log.debug("배터리 정보 없음: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<Double> disk(String target, boolean used) {
        try {
            List<OSFileStore> stores = systemInfo.getOperatingSystem().getFileSystem().getFileStores();
            OSFileStore store = pickStore(stores, target);
            if (store == null || store.getTotalSpace() <= 0) {
                return Optional.empty();
            }
            double freeRatio = clamp((double) store.getUsableSpace() / store.getTotalSpace());
            return Optional.of(used ? 1 - freeRatio : freeRatio);
        } catch (Exception e) {
            log.debug("디스크 정보 없음: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private static OSFileStore pickStore(List<OSFileStore> stores, String target) {
        if (target != null && !target.isBlank()) {
            for (OSFileStore s : stores) {
                if (target.equalsIgnoreCase(s.getMount()) || target.equalsIgnoreCase(s.getName())) {
                    return s;
                }
            }
        }
        // 대상 미지정 → 루트("/" 또는 가장 큰) 스토어
        OSFileStore largest = null;
        for (OSFileStore s : stores) {
            if (s.getTotalSpace() <= 0) {
                continue;
            }
            if ("/".equals(s.getMount())) {
                return s;
            }
            if (largest == null || s.getTotalSpace() > largest.getTotalSpace()) {
                largest = s;
            }
        }
        return largest;
    }

    private static double clamp(double v) {
        return Math.max(0, Math.min(1, v));
    }
}
