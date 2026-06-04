package io.github.wiskyahn.blockhud.platform;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 공통 동작(프로세스 실행, URL/경로 열기) 기본 구현. OS별 구현은 {@link #openWellKnown}만 채운다.
 */
public abstract class AbstractDesktopShell implements PlatformShell {

    @Override
    public void launchProgram(String command) throws Exception {
        new ProcessBuilder(tokenize(command)).start();
    }

    @Override
    public void openPath(String path) throws Exception {
        File file = new File(path);
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            Desktop.getDesktop().open(file);
        } else {
            openPathFallback(path);
        }
    }

    @Override
    public void openUrl(String url) throws Exception {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(new URI(url));
        } else {
            openUrlFallback(url);
        }
    }

    /** Desktop API 미지원 환경(주로 Linux)에서의 경로 열기. */
    protected abstract void openPathFallback(String path) throws Exception;

    /** Desktop API 미지원 환경에서의 URL 열기. */
    protected abstract void openUrlFallback(String url) throws Exception;

    protected void run(String... args) throws Exception {
        new ProcessBuilder(args).start();
    }

    protected static Path home() {
        return Path.of(System.getProperty("user.home", ""));
    }

    /** 따옴표를 존중하는 단순 명령 토크나이저 ({@code explorer.exe "shell:..."} 대응). */
    static List<String> tokenize(String command) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < command.length(); i++) {
            char c = command.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (Character.isWhitespace(c) && !inQuotes) {
                if (current.length() > 0) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) {
            tokens.add(current.toString());
        }
        return tokens;
    }
}
