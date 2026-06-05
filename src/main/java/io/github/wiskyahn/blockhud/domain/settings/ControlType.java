package io.github.wiskyahn.blockhud.domain.settings;

/** 설정 필드 컨트롤 종류. 원본 SettingsSchema controlType. (DESIGN.md §8.3) */
public enum ControlType {
    TOGGLE,
    STEPPER,
    TEXT,
    DROPDOWN,
    ACTION,
    READONLY
}
