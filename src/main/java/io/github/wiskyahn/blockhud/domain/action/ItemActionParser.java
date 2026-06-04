package io.github.wiskyahn.blockhud.domain.action;

import io.github.wiskyahn.blockhud.domain.action.ItemAction.InternalCommand;
import io.github.wiskyahn.blockhud.domain.action.ItemAction.LaunchProgram;
import io.github.wiskyahn.blockhud.domain.action.ItemAction.OpenUrl;
import io.github.wiskyahn.blockhud.domain.action.ItemAction.WellKnownTarget;
import java.util.Locale;

/**
 * 원본 Action 문자열 ↔ {@link ItemAction} 변환. (DESIGN.md §4 파싱 규칙)
 *
 * <pre>
 *   빈 값                          → None
 *   http(s)://...                  → OpenUrl
 *   _XXX_ (앞뒤 underscore)         → InternalCommand
 *   shell: / ::{GUID} 형태          → WellKnownTarget (매핑되면)
 *   그 외                          → LaunchProgram
 * </pre>
 */
public final class ItemActionParser {

    private ItemActionParser() {
    }

    public static ItemAction parse(String raw) {
        if (raw == null) {
            return ItemAction.NONE;
        }
        String s = raw.trim();
        if (s.isEmpty()) {
            return ItemAction.NONE;
        }

        String lower = s.toLowerCase(Locale.ROOT);
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            return new OpenUrl(s);
        }

        if (s.length() > 2 && s.startsWith("_") && s.endsWith("_")) {
            return new InternalCommand(s);
        }

        WellKnownTargetId wellKnown = WellKnownTargetId.fromCommand(s);
        if (wellKnown != null) {
            return new WellKnownTarget(wellKnown);
        }

        return new LaunchProgram(s);
    }
}
