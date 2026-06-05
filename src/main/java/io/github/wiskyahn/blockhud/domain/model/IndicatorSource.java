package io.github.wiskyahn.blockhud.domain.model;

/**
 * 인디케이터 바에 바인딩 가능한 시스템 지표 소스. 원본 IndicatorSourceResolver의 12종 + disabled.
 * (DESIGN.md §8.2)
 */
public enum IndicatorSource {
    DISABLED,
    CPU_IDLE,
    CPU_LOAD,
    RAM_FREE,
    RAM_USED,
    DISK_FREE,
    DISK_USED,
    BATTERY_CHARGE,
    BATTERY_DRAIN,
    GPU_FREE,
    GPU_USED,
    VRAM_FREE,
    VRAM_USED;

    /** 원본 소스 키(camelCase, 예: {@code cpuIdle})를 enum으로 변환. 미인식 시 {@link #DISABLED}. */
    public static IndicatorSource fromKey(String key) {
        if (key == null) {
            return DISABLED;
        }
        return switch (key.trim()) {
            case "cpuIdle" -> CPU_IDLE;
            case "cpuLoad" -> CPU_LOAD;
            case "ramFree" -> RAM_FREE;
            case "ramUsed" -> RAM_USED;
            case "diskFree" -> DISK_FREE;
            case "diskUsed" -> DISK_USED;
            case "batteryCharge" -> BATTERY_CHARGE;
            case "batteryDrain" -> BATTERY_DRAIN;
            case "gpuFree" -> GPU_FREE;
            case "gpuUsed" -> GPU_USED;
            case "vramFree" -> VRAM_FREE;
            case "vramUsed" -> VRAM_USED;
            default -> DISABLED;
        };
    }
}
