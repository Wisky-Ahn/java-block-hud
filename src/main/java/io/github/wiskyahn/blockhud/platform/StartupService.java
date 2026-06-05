package io.github.wiskyahn.blockhud.platform;

import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OS별 로그인 시 자동 실행 등록/해제. 원본 StartupAutoRun.ps1 대체. (DESIGN.md §5)
 *
 * <ul>
 *   <li>Windows: 레지스트리 {@code HKCU\...\Run}</li>
 *   <li>macOS: {@code ~/Library/LaunchAgents/*.plist}</li>
 *   <li>Linux: {@code ~/.config/autostart/*.desktop}</li>
 * </ul>
 *
 * <p>실행 명령은 현재 프로세스에서 추정(best-effort). 네이티브 설치본 기준 확정은 Phase 7.
 */
public final class StartupService {

    private static final Logger log = LoggerFactory.getLogger(StartupService.class);
    private static final String APP_NAME = "JavaBlockHud";

    public void setEnabled(boolean enabled) {
        try {
            switch (Platforms.current()) {
                case WINDOWS -> windows(enabled);
                case MAC -> mac(enabled);
                case LINUX -> linux(enabled);
            }
        } catch (Exception e) {
            log.warn("자동 실행 설정 실패: {}", e.getMessage());
        }
    }

    private void windows(boolean enabled) throws Exception {
        if (enabled) {
            new ProcessBuilder("reg", "add",
                    "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run",
                    "/v", APP_NAME, "/t", "REG_SZ", "/d", launchCommand(), "/f").start();
        } else {
            new ProcessBuilder("reg", "delete",
                    "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run",
                    "/v", APP_NAME, "/f").start();
        }
    }

    private void mac(boolean enabled) throws Exception {
        Path plist = Path.of(System.getProperty("user.home"),
                "Library", "LaunchAgents", "com.blockhud.startup.plist");
        if (enabled) {
            Files.createDirectories(plist.getParent());
            Files.writeString(plist, plistContent(launchCommand()));
        } else {
            Files.deleteIfExists(plist);
        }
    }

    private void linux(boolean enabled) throws Exception {
        Path desktop = Path.of(System.getProperty("user.home"),
                ".config", "autostart", "java-block-hud.desktop");
        if (enabled) {
            Files.createDirectories(desktop.getParent());
            Files.writeString(desktop, desktopEntry(launchCommand()));
        } else {
            Files.deleteIfExists(desktop);
        }
    }

    /** 현재 실행 명령 추정. */
    static String launchCommand() {
        return ProcessHandle.current().info().commandLine().orElse("java -jar java-block-hud.jar");
    }

    static String desktopEntry(String command) {
        return "[Desktop Entry]\n"
                + "Type=Application\n"
                + "Name=" + APP_NAME + "\n"
                + "Exec=" + command + "\n"
                + "X-GNOME-Autostart-enabled=true\n";
    }

    static String plistContent(String command) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\""
                + " \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n"
                + "<plist version=\"1.0\"><dict>\n"
                + "  <key>Label</key><string>com.blockhud.startup</string>\n"
                + "  <key>ProgramArguments</key><array><string>" + command + "</string></array>\n"
                + "  <key>RunAtLoad</key><true/>\n"
                + "</dict></plist>\n";
    }
}
