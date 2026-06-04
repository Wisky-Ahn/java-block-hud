package io.github.wiskyahn.blockhud.platform.windows;

import io.github.wiskyahn.blockhud.domain.action.WellKnownTargetId;
import io.github.wiskyahn.blockhud.platform.AbstractDesktopShell;

/** Windows 구현 — 원본과 동일하게 {@code explorer.exe shell:...}를 사용. */
public final class WindowsShell extends AbstractDesktopShell {

    @Override
    public void openWellKnown(WellKnownTargetId id) throws Exception {
        String shell = switch (id) {
            case THIS_PC -> "shell:MyComputerFolder";
            case RECYCLE_BIN -> "shell:RecycleBinFolder";
            case DOWNLOADS -> "shell:Downloads";
            case DOCUMENTS -> "shell:Personal";
            case PICTURES -> "shell:My Pictures";
            case MUSIC -> "shell:My Music";
            case VIDEOS -> "shell:My Video";
            case DESKTOP -> "shell:Desktop";
            case HOME -> "shell:Profile";
        };
        run("explorer.exe", shell);
    }

    @Override
    protected void openPathFallback(String path) throws Exception {
        run("explorer.exe", path);
    }

    @Override
    protected void openUrlFallback(String url) throws Exception {
        run("rundll32", "url.dll,FileProtocolHandler", url);
    }
}
