package io.github.wiskyahn.blockhud.platform;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class AbstractDesktopShellTest {

    @Test
    void tokenizesPlainCommand() {
        assertEquals(List.of("calc.exe"), AbstractDesktopShell.tokenize("calc.exe"));
    }

    @Test
    void keepsQuotedArgumentTogether() {
        // 원본 Slot02: explorer.exe "shell:RecycleBinFolder"
        assertEquals(List.of("explorer.exe", "shell:RecycleBinFolder"),
                AbstractDesktopShell.tokenize("explorer.exe \"shell:RecycleBinFolder\""));
    }

    @Test
    void splitsMultipleUnquotedArguments() {
        assertEquals(List.of("explorer.exe", "shell:::{GUID}"),
                AbstractDesktopShell.tokenize("explorer.exe shell:::{GUID}"));
    }
}
