package io.github.wiskyahn.blockhud.service;

import io.github.wiskyahn.blockhud.domain.action.ItemAction;
import io.github.wiskyahn.blockhud.platform.PlatformShell;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 슬롯 {@link ItemAction}을 실제 동작으로 디스패치한다. (DESIGN.md §5)
 *
 * <p>외부 동작(실행/열기)은 {@link PlatformShell}에 위임하고, 내부 명령
 * ({@code _OPEN_INVENTORY_} 등)은 등록된 핸들러로 처리한다.
 */
public final class ActionLauncher {

    private static final Logger log = LoggerFactory.getLogger(ActionLauncher.class);

    private final PlatformShell shell;
    private final Map<String, Runnable> internalCommands = new HashMap<>();

    public ActionLauncher(PlatformShell shell) {
        this.shell = shell;
    }

    /** 내부 명령 핸들러 등록 (예: {@code _OPEN_INVENTORY_} → 인벤토리 토글). */
    public void registerInternal(String name, Runnable handler) {
        internalCommands.put(name, handler);
    }

    public void launch(ItemAction action) {
        try {
            switch (action) {
                case ItemAction.None ignored -> {
                    // 동작 없음
                }
                case ItemAction.LaunchProgram a -> shell.launchProgram(a.command());
                case ItemAction.OpenPath a -> shell.openPath(a.path());
                case ItemAction.OpenUrl a -> shell.openUrl(a.url());
                case ItemAction.WellKnownTarget a -> shell.openWellKnown(a.id());
                case ItemAction.InternalCommand a -> runInternal(a.name());
            }
        } catch (Exception e) {
            log.warn("액션 실행 실패: {} ({})", action, e.getMessage());
        }
    }

    private void runInternal(String name) {
        Runnable handler = internalCommands.get(name);
        if (handler != null) {
            handler.run();
        } else {
            log.warn("등록되지 않은 내부 명령: {}", name);
        }
    }
}
