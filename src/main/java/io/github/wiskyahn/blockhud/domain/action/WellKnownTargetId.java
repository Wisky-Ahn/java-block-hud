package io.github.wiskyahn.blockhud.domain.action;

import java.util.Locale;

/**
 * OS 독립적인 "잘 알려진 실행 대상" — 원본의 Windows 셸 명령
 * ({@code explorer.exe shell:RecycleBinFolder}, {@code shell:::{GUID}})을
 * 플랫폼별로 매핑하기 위한 식별자. (DESIGN.md §1 "크로스플랫폼 1:1", §4)
 *
 * <p>실제 OS별 동작은 Phase 2의 {@code platform.PlatformShell}이 담당하고,
 * 여기서는 원본 문자열 → 식별자 인식만 책임진다.
 */
public enum WellKnownTargetId {
    THIS_PC,
    RECYCLE_BIN,
    DOWNLOADS,
    DOCUMENTS,
    PICTURES,
    MUSIC,
    VIDEOS,
    DESKTOP,
    HOME;

    /** Windows Downloads 폴더의 KNOWNFOLDERID (원본 Slot04에서 사용). */
    private static final String DOWNLOADS_GUID = "374de290-123f-4565-9164-39c4925e467b";

    /**
     * 원본 Action 문자열이 셸 폴더 형태이면 해당 식별자를 반환, 아니면 {@code null}.
     * 셸 폴더가 아닌(=일반 실행파일/경로) 경우 호출 측에서 LaunchProgram 등으로 처리한다.
     */
    public static WellKnownTargetId fromCommand(String command) {
        if (command == null) {
            return null;
        }
        String c = command.toLowerCase(Locale.ROOT);
        boolean shellForm = c.contains("shell:") || c.contains("::{");
        if (!shellForm) {
            return null;
        }
        if (c.contains(DOWNLOADS_GUID) || c.contains("shell:downloads")) {
            return DOWNLOADS;
        }
        if (c.contains("mycomputerfolder") || c.contains("shell:mycomputerfolder")
                || c.contains("thispcfolder")) {
            return THIS_PC;
        }
        if (c.contains("recyclebinfolder")) {
            return RECYCLE_BIN;
        }
        if (c.contains("shell:personal") || c.contains("mydocuments") || c.contains("shell:documents")) {
            return DOCUMENTS;
        }
        if (c.contains("shell:mypictures") || c.contains("shell:pictures")) {
            return PICTURES;
        }
        if (c.contains("shell:mymusic") || c.contains("shell:music")) {
            return MUSIC;
        }
        if (c.contains("shell:myvideo") || c.contains("shell:videos")) {
            return VIDEOS;
        }
        if (c.contains("shell:desktop")) {
            return DESKTOP;
        }
        if (c.contains("shell:profile") || c.contains("shell:userprofile")) {
            return HOME;
        }
        // 셸 형태지만 매핑 미정 → null (호출 측에서 LaunchProgram으로 폴백, Windows에서만 동작)
        return null;
    }

    /**
     * 에디터 표시/저장용 정규 명령 문자열. {@link #fromCommand}으로 다시 파싱하면 동일 식별자가 된다.
     */
    public String toShellCommand() {
        return switch (this) {
            case THIS_PC -> "shell:MyComputerFolder";
            case RECYCLE_BIN -> "shell:RecycleBinFolder";
            case DOWNLOADS -> "shell:Downloads";
            case DOCUMENTS -> "shell:Personal";
            case PICTURES -> "shell:MyPictures";
            case MUSIC -> "shell:MyMusic";
            case VIDEOS -> "shell:MyVideo";
            case DESKTOP -> "shell:Desktop";
            case HOME -> "shell:Profile";
        };
    }
}
