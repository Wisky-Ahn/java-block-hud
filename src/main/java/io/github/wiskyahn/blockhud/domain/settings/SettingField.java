package io.github.wiskyahn.blockhud.domain.settings;

import java.util.List;

/**
 * 설정 필드 디스크립터. 폼 생성과 기본값을 모두 구동한다. (DESIGN.md §8.3 데이터 드리븐)
 *
 * @param key          설정 키 (settings.json/원본 변수명)
 * @param type         컨트롤 종류
 * @param defaultValue 기본값(문자열). boolean은 "1"/"0".
 * @param min,max,step STEPPER용 범위
 * @param options      DROPDOWN용 선택지(값 목록)
 */
public record SettingField(
        String key,
        ControlType type,
        String defaultValue,
        int min,
        int max,
        int step,
        List<String> options) {

    public static SettingField toggle(String key, boolean def) {
        return new SettingField(key, ControlType.TOGGLE, def ? "1" : "0", 0, 1, 1, List.of());
    }

    public static SettingField stepper(String key, int def, int min, int max, int step) {
        return new SettingField(key, ControlType.STEPPER, Integer.toString(def), min, max, step, List.of());
    }

    public static SettingField text(String key, String def) {
        return new SettingField(key, ControlType.TEXT, def, 0, 0, 0, List.of());
    }

    public static SettingField dropdown(String key, String def, List<String> options) {
        return new SettingField(key, ControlType.DROPDOWN, def, 0, 0, 0, options);
    }

    public static SettingField action(String key) {
        return new SettingField(key, ControlType.ACTION, "", 0, 0, 0, List.of());
    }

    public static SettingField readonly(String key, String value) {
        return new SettingField(key, ControlType.READONLY, value, 0, 0, 0, List.of());
    }

    /** i18n 라벨 키. */
    public String labelKey() {
        return "settings.field." + key;
    }
}
