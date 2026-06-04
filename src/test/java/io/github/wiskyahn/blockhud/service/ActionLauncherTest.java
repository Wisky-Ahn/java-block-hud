package io.github.wiskyahn.blockhud.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.wiskyahn.blockhud.domain.action.ItemAction;
import io.github.wiskyahn.blockhud.domain.action.WellKnownTargetId;
import io.github.wiskyahn.blockhud.platform.PlatformShell;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ActionLauncherTest {

    /** 호출 내역을 기록하는 가짜 셸. */
    private static final class FakeShell implements PlatformShell {
        final List<String> calls = new ArrayList<>();

        @Override public void launchProgram(String c) {
            calls.add("launch:" + c);
        }

        @Override public void openPath(String p) {
            calls.add("path:" + p);
        }

        @Override public void openUrl(String u) {
            calls.add("url:" + u);
        }

        @Override public void openWellKnown(WellKnownTargetId id) {
            calls.add("wellKnown:" + id);
        }
    }

    @Test
    void dispatchesExternalActionsToShell() {
        FakeShell shell = new FakeShell();
        ActionLauncher launcher = new ActionLauncher(shell);

        launcher.launch(new ItemAction.LaunchProgram("calc.exe"));
        launcher.launch(new ItemAction.OpenUrl("https://a.b"));
        launcher.launch(new ItemAction.WellKnownTarget(WellKnownTargetId.RECYCLE_BIN));

        assertEquals(List.of("launch:calc.exe", "url:https://a.b", "wellKnown:RECYCLE_BIN"),
                shell.calls);
    }

    @Test
    void runsRegisteredInternalCommand() {
        FakeShell shell = new FakeShell();
        ActionLauncher launcher = new ActionLauncher(shell);
        boolean[] ran = {false};
        launcher.registerInternal("_OPEN_INVENTORY_", () -> ran[0] = true);

        launcher.launch(new ItemAction.InternalCommand("_OPEN_INVENTORY_"));

        assertTrue(ran[0]);
        assertTrue(shell.calls.isEmpty());
    }

    @Test
    void unknownInternalCommandIsIgnoredGracefully() {
        ActionLauncher launcher = new ActionLauncher(new FakeShell());
        // 등록 안 된 명령이어도 예외 없이 넘어가야 함
        launcher.launch(new ItemAction.InternalCommand("_NOPE_"));
        launcher.launch(ItemAction.NONE);
    }
}
