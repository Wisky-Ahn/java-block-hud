package io.github.wiskyahn.blockhud.domain.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SettingsSchemaTest {

    @Test
    void hasSevenTabs() {
        assertEquals(7, SettingsSchema.tabs().size());
    }

    @Test
    void defaultsCoverEveryNonActionField() {
        for (SettingsTab tab : SettingsSchema.tabs()) {
            for (SettingField field : tab.fields()) {
                if (field.type() != ControlType.ACTION) {
                    assertTrue(SettingsSchema.defaults().containsKey(field.key()),
                            "기본값 누락: " + field.key());
                }
            }
        }
    }

    @Test
    void actionFieldsHaveNoDefault() {
        assertFalse(SettingsSchema.defaults().containsKey("resetAllSettings"));
        assertFalse(SettingsSchema.defaults().containsKey("resetAllSkinPositions"));
    }

    @Test
    void usesAllFiveControlTypesPlusDropdown() {
        boolean toggle = false;
        boolean stepper = false;
        boolean text = false;
        boolean dropdown = false;
        boolean action = false;
        boolean readonly = false;
        for (SettingsTab tab : SettingsSchema.tabs()) {
            for (SettingField f : tab.fields()) {
                switch (f.type()) {
                    case TOGGLE -> toggle = true;
                    case STEPPER -> stepper = true;
                    case TEXT -> text = true;
                    case DROPDOWN -> dropdown = true;
                    case ACTION -> action = true;
                    case READONLY -> readonly = true;
                }
            }
        }
        assertTrue(toggle && stepper && text && dropdown && action && readonly,
                "모든 컨트롤 타입이 스키마에 존재해야 함");
    }
}
