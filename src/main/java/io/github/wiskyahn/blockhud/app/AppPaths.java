package io.github.wiskyahn.blockhud.app;

import java.nio.file.Path;
import java.util.Locale;

/**
 * OS별 설정/데이터 디렉터리. (DESIGN.md §4)
 *
 * <ul>
 *   <li>Windows: {@code %APPDATA%\JavaBlockHud}</li>
 *   <li>macOS: {@code ~/Library/Application Support/JavaBlockHud}</li>
 *   <li>Linux: {@code $XDG_CONFIG_HOME/java-block-hud} 또는 {@code ~/.config/java-block-hud}</li>
 * </ul>
 */
public final class AppPaths {

    private static final String APP_DIR = "JavaBlockHud";
    private static final String APP_DIR_NIX = "java-block-hud";

    private AppPaths() {
    }

    public static Path configDir() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String home = System.getProperty("user.home", "");

        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            Path base = (appData != null && !appData.isBlank())
                    ? Path.of(appData)
                    : Path.of(home, "AppData", "Roaming");
            return base.resolve(APP_DIR);
        }
        if (os.contains("mac") || os.contains("darwin")) {
            return Path.of(home, "Library", "Application Support", APP_DIR);
        }
        String xdg = System.getenv("XDG_CONFIG_HOME");
        Path base = (xdg != null && !xdg.isBlank()) ? Path.of(xdg) : Path.of(home, ".config");
        return base.resolve(APP_DIR_NIX);
    }

    public static Path itemsFile() {
        return configDir().resolve("items.json");
    }

    public static Path settingsFile() {
        return configDir().resolve("settings.json");
    }
}
