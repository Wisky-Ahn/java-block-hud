package io.github.wiskyahn.blockhud.app;

import javafx.application.Application;

/**
 * 실제 진입점. {@link Application}을 직접 extends 하지 않는 별도 런처를 둠으로써
 * fat-jar / jpackage 배포 시 발생하는 "JavaFX runtime components are missing" 문제를 회피한다.
 * (DESIGN.md §3 app, Phase 7 패키징 대비)
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        Application.launch(HudApplication.class, args);
    }
}
