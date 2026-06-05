package io.github.wiskyahn.blockhud.domain.settings;

import java.util.List;

/** 설정 탭 (id + 필드 목록). 라벨은 {@code settings.tab.<id>}. (DESIGN.md §8.3) */
public record SettingsTab(String id, List<SettingField> fields) {

    public String labelKey() {
        return "settings.tab." + id;
    }
}
