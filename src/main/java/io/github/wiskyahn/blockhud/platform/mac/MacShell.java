package io.github.wiskyahn.blockhud.platform.mac;

import io.github.wiskyahn.blockhud.domain.action.WellKnownTargetId;
import io.github.wiskyahn.blockhud.platform.AbstractDesktopShell;
import java.nio.file.Path;

/** macOS 구현 — {@code open}으로 폴더/대상을 연다. */
public final class MacShell extends AbstractDesktopShell {

    @Override
    public void openWellKnown(WellKnownTargetId id) throws Exception {
        switch (id) {
            case THIS_PC -> run("open", "/");
            case RECYCLE_BIN -> run("open", home().resolve(".Trash").toString());
            case DOWNLOADS -> openHome("Downloads");
            case DOCUMENTS -> openHome("Documents");
            case PICTURES -> openHome("Pictures");
            case MUSIC -> openHome("Music");
            case VIDEOS -> openHome("Movies");
            case DESKTOP -> openHome("Desktop");
            case HOME -> run("open", home().toString());
        }
    }

    private void openHome(String dir) throws Exception {
        run("open", home().resolve(dir).toString());
    }

    @Override
    protected void openPathFallback(String path) throws Exception {
        run("open", path);
    }

    @Override
    protected void openUrlFallback(String url) throws Exception {
        run("open", url);
    }
}
