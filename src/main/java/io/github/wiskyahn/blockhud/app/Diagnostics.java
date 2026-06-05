package io.github.wiskyahn.blockhud.app;

import java.nio.file.Path;

/** 로그/진단 경로. 원본 Diagnostics + OpenSettingsLogFolder.ps1 대체. (DESIGN.md §6) */
public final class Diagnostics {

    private Diagnostics() {
    }

    public static Path logDir() {
        return AppPaths.configDir().resolve("logs");
    }
}
