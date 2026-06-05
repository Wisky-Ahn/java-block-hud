package io.github.wiskyahn.blockhud.domain.settings;

import java.util.HashMap;
import java.util.Map;

/**
 * 설정 값 보관 — 키→문자열(원본 Rainmeter 스타일, boolean은 "1"/"0"). 미설정 키는 스키마 기본값.
 * (DESIGN.md §8.3)
 */
public final class Settings {

    private final Map<String, String> values;

    public Settings(Map<String, String> values) {
        this.values = new HashMap<>(values);
    }

    public static Settings withDefaults() {
        return new Settings(SettingsSchema.defaults());
    }

    public String get(String key) {
        String v = values.get(key);
        return v != null ? v : SettingsSchema.defaults().get(key);
    }

    public boolean getBoolean(String key) {
        String v = get(key);
        return "1".equals(v) || "true".equalsIgnoreCase(v);
    }

    public int getInt(String key, int fallback) {
        try {
            return Integer.parseInt(get(key).trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    public void set(String key, String value) {
        values.put(key, value);
    }

    public void setBoolean(String key, boolean value) {
        values.put(key, value ? "1" : "0");
    }

    public Map<String, String> asMap() {
        return new HashMap<>(values);
    }

    public Settings copy() {
        return new Settings(values);
    }
}
