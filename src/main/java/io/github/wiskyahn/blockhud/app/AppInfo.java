package io.github.wiskyahn.blockhud.app;

import io.github.wiskyahn.blockhud.domain.update.SemanticVersion;

/** 앱 메타데이터 (버전, 업데이트 소스). (DESIGN.md §8.6) */
public final class AppInfo {

    public static final String VERSION = "1.0.0";
    public static final String GITHUB_OWNER = "Wisky-Ahn";
    public static final String GITHUB_REPO = "java-block-hud";

    private AppInfo() {
    }

    public static SemanticVersion currentVersion() {
        return SemanticVersion.parse(VERSION);
    }
}
