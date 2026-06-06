package io.github.wiskyahn.blockhud.domain.settings;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 설정 스키마 — 7탭 × 5컨트롤 타입의 데이터 드리븐 정의. 폼 생성과 기본값을 구동한다.
 * 원본 SettingsSchema.lua + Defaults/Settings/*.inc 대체. (DESIGN.md §8.3)
 *
 * <p>대표 필드 집합(현재 기능과 연동되는 키 중심). 나머지 키는 후속 단계에서 확장.
 */
public final class SettingsSchema {

    /** 인디케이터 소스 드롭다운 선택지 (원본 12종 + disabled). */
    public static final List<String> SOURCES = List.of(
            "disabled", "cpuIdle", "cpuLoad", "ramFree", "ramUsed", "diskFree", "diskUsed",
            "batteryCharge", "batteryDrain", "gpuFree", "gpuUsed", "vramFree", "vramUsed");

    private static final List<SettingsTab> TABS = List.of(
            new SettingsTab("general", List.of(
                    SettingField.dropdown("LanguageCode", "ko-KR", List.of("ko-KR", "en-US")),
                    SettingField.toggle("UseClickSound", true),
                    SettingField.toggle("HideHintTooltip", false),
                    SettingField.stepper("ItemCountTextFontSize", 18, 8, 48, 1),
                    SettingField.toggle("EnableRainmeterStartup", false),
                    SettingField.readonly("appVersion", "0.1.0"),
                    SettingField.action("openVersionManager"),
                    SettingField.action("openLogFolder"),
                    SettingField.action("resetAllSettings"))),

            new SettingsTab("lowSpec", List.of(
                    SettingField.toggle("LowSpecFreezeInventoryPlayerAnimation", false),
                    SettingField.toggle("LowSpecDisableSlotHoverHighlight", false),
                    SettingField.toggle("LowSpecDisableHoverTextTooltip", false))),

            new SettingsTab("hotbar", List.of(
                    SettingField.stepper("HotbarSlotSize", 60, 24, 120, 1),
                    SettingField.stepper("HotbarItemSizeOffset", -12, -64, 64, 1),
                    SettingField.stepper("HotbarTextFontSize", 18, 8, 48, 1),
                    SettingField.toggle("EnableHotbarSkin", true),
                    SettingField.toggle("AllowHotbarDrag", false))),

            new SettingsTab("indicators", List.of(
                    SettingField.dropdown("HealthBarSource", "cpuIdle", SOURCES),
                    SettingField.dropdown("ArmorBarSource", "cpuLoad", SOURCES),
                    SettingField.dropdown("FoodBarSource", "ramFree", SOURCES),
                    SettingField.dropdown("AirBarSource", "ramUsed", SOURCES),
                    SettingField.dropdown("ExpBarSource", "diskUsed", SOURCES),
                    SettingField.stepper("IndicatorBarScalePercent", 100, 50, 200, 5))),

            new SettingsTab("inventory", List.of(
                    SettingField.stepper("InventoryItemSize", 60, 24, 120, 1),
                    SettingField.stepper("TooltipTextFontSize", 22, 8, 48, 1),
                    SettingField.toggle("EnableInventorySkin", true),
                    SettingField.toggle("HideSteve", false),
                    SettingField.toggle("UseInventoryBottomRow", false),
                    SettingField.text("MinecraftSkinUsername", ""),
                    SettingField.toggle("HideSettingsButton", false),
                    SettingField.toggle("HideEditButton", false),
                    SettingField.toggle("AllowInventoryDrag", false))),

            new SettingsTab("clock", List.of(
                    SettingField.toggle("Use24HourClock", true),
                    SettingField.toggle("HideClockMeridiem", false),
                    SettingField.stepper("ClockTimeTextSize", 40, 16, 120, 1),
                    SettingField.stepper("ClockDateTextSize", 16, 8, 64, 1),
                    SettingField.stepper("ClockSpriteSize", 128, 32, 256, 8),
                    SettingField.toggle("EnableClockSkin", true),
                    SettingField.toggle("EnableClockTextSkin", true),
                    SettingField.toggle("EnableClockSpriteSkin", true))),

            new SettingsTab("ui", List.of(
                    SettingField.dropdown("settingsTheme", "dark", List.of("dark", "light")),
                    SettingField.text("BaseFont", "Galmuri9"),
                    SettingField.action("resetAllSkinPositions"))));

    private static final Map<String, String> DEFAULTS = buildDefaults();

    private SettingsSchema() {
    }

    public static List<SettingsTab> tabs() {
        return TABS;
    }

    public static Map<String, String> defaults() {
        return DEFAULTS;
    }

    private static Map<String, String> buildDefaults() {
        Map<String, String> map = new LinkedHashMap<>();
        for (SettingsTab tab : TABS) {
            for (SettingField field : tab.fields()) {
                if (field.type() != ControlType.ACTION) {
                    map.put(field.key(), field.defaultValue());
                }
            }
        }
        return Map.copyOf(map);
    }
}
