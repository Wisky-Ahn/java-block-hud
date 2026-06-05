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
    VRAM_USED
}
