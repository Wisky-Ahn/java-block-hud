package io.github.wiskyahn.blockhud.platform.linux;

import io.github.wiskyahn.blockhud.domain.action.WellKnownTargetId;
import io.github.wiskyahn.blockhud.platform.AbstractDesktopShell;

/** Linux 구현 — {@code xdg-open}으로 폴더/URL/대상을 연다. */
public final class LinuxShell extends AbstractDesktopShell {

    @Override
    public void openWellKnown(WellKnownTargetId id) throws Exception {
        switch (id) {
            case THIS_PC -> run("xdg-open", "/");
            case RECYCLE_BIN -> run("xdg-open", "trash:///");
            case DOWNLOADS -> openHome("Downloads");
            case DOCUMENTS -> openHome("Documents");
            case PICTURES -> openHome("Pictures");
            case MUSIC -> openHome("Music");
            case VIDEOS -> openHome("Videos");
            case DESKTOP -> openHome("Desktop");
            case HOME -> run("xdg-open", home().toString());
        }
    }

    private void openHome(String dir) throws Exception {
        run("xdg-open", home().resolve(dir).toString());
    }

    @Override
    protected void openPathFallback(String path) throws Exception {
        run("xdg-open", path);
    }

    @Override
    protected void openUrlFallback(String url) throws Exception {
        run("xdg-open", url);
    }
}
