package io.github.wiskyahn.blockhud.domain.action;

/**
 * {@link ItemAction} → 에디터 표시/편집용 문자열. 원본 에디터가 Action을 단일 자유 문자열로
 * 다루는 것에 대응한다. {@link ItemActionParser#parse}의 역방향. (DESIGN.md §8.4)
 */
public final class ActionText {

    private ActionText() {
    }

    public static String format(ItemAction action) {
        return switch (action) {
            case ItemAction.None ignored -> "";
            case ItemAction.LaunchProgram a -> a.command();
            case ItemAction.OpenPath a -> a.path();
            case ItemAction.OpenUrl a -> a.url();
            case ItemAction.WellKnownTarget a -> a.id().toShellCommand();
            case ItemAction.InternalCommand a -> a.name();
        };
    }
}
