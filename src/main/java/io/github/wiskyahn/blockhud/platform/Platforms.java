package io.github.wiskyahn.blockhud.platform;

import io.github.wiskyahn.blockhud.platform.linux.LinuxShell;
import io.github.wiskyahn.blockhud.platform.mac.MacShell;
import io.github.wiskyahn.blockhud.platform.windows.WindowsShell;
import java.util.Locale;

/** 현재 OS를 감지해 적절한 {@link PlatformShell}을 제공. */
public final class Platforms {

    public enum Os { WINDOWS, MAC, LINUX }

    private Platforms() {
    }

    public static Os current() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            return Os.WINDOWS;
        }
        if (os.contains("mac") || os.contains("darwin")) {
            return Os.MAC;
        }
        return Os.LINUX;
    }

    public static PlatformShell shell() {
        return switch (current()) {
            case WINDOWS -> new WindowsShell();
            case MAC -> new MacShell();
            case LINUX -> new LinuxShell();
        };
    }
}
