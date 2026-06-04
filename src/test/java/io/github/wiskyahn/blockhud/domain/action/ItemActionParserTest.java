package io.github.wiskyahn.blockhud.domain.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;

class ItemActionParserTest {

    @Test
    void emptyOrNullBecomesNone() {
        assertInstanceOf(ItemAction.None.class, ItemActionParser.parse(null));
        assertInstanceOf(ItemAction.None.class, ItemActionParser.parse(""));
        assertInstanceOf(ItemAction.None.class, ItemActionParser.parse("   "));
    }

    @Test
    void httpBecomesOpenUrl() {
        ItemAction action = ItemActionParser.parse("https://www.google.com");
        ItemAction.OpenUrl url = assertInstanceOf(ItemAction.OpenUrl.class, action);
        assertEquals("https://www.google.com", url.url());
    }

    @Test
    void underscoreCommandBecomesInternal() {
        ItemAction action = ItemActionParser.parse("_OPEN_INVENTORY_");
        ItemAction.InternalCommand cmd = assertInstanceOf(ItemAction.InternalCommand.class, action);
        assertEquals("_OPEN_INVENTORY_", cmd.name());
    }

    @Test
    void shellFoldersBecomeWellKnownTargets() {
        assertEquals(WellKnownTargetId.THIS_PC,
                wellKnown("explorer.exe \"shell:MyComputerFolder\""));
        assertEquals(WellKnownTargetId.RECYCLE_BIN,
                wellKnown("explorer.exe \"shell:RecycleBinFolder\""));
        // 원본 Slot04: Downloads KNOWNFOLDERID
        assertEquals(WellKnownTargetId.DOWNLOADS,
                wellKnown("explorer.exe shell:::{374DE290-123F-4565-9164-39C4925E467B}"));
    }

    @Test
    void plainExecutableBecomesLaunchProgram() {
        ItemAction action = ItemActionParser.parse("calc.exe");
        ItemAction.LaunchProgram launch = assertInstanceOf(ItemAction.LaunchProgram.class, action);
        assertEquals("calc.exe", launch.command());
    }

    private static WellKnownTargetId wellKnown(String raw) {
        return assertInstanceOf(ItemAction.WellKnownTarget.class, ItemActionParser.parse(raw)).id();
    }
}
