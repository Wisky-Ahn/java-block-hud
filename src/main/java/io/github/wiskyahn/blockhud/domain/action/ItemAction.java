package io.github.wiskyahn.blockhud.domain.action;

/**
 * 슬롯 클릭 시 수행할 동작. 원본의 자유 문자열 {@code Action}을 타입 안전하게 표현한다.
 * (DESIGN.md §4)
 *
 * <ul>
 *   <li>{@link None} — 동작 없음(빈 값)</li>
 *   <li>{@link LaunchProgram} — 실행파일/명령 (예: {@code calc.exe})</li>
 *   <li>{@link OpenPath} — 파일/폴더 경로 열기</li>
 *   <li>{@link OpenUrl} — 웹 링크</li>
 *   <li>{@link WellKnownTarget} — 내 PC/휴지통 등 OS별 매핑 대상</li>
 *   <li>{@link InternalCommand} — 앱 내부 명령 (예: {@code _OPEN_INVENTORY_})</li>
 * </ul>
 */
public sealed interface ItemAction
        permits ItemAction.None, ItemAction.LaunchProgram, ItemAction.OpenPath,
                ItemAction.OpenUrl, ItemAction.WellKnownTarget, ItemAction.InternalCommand {

    /** 공유 가능한 "동작 없음" 인스턴스. */
    None NONE = new None();

    record None() implements ItemAction {
    }

    record LaunchProgram(String command) implements ItemAction {
    }

    record OpenPath(String path) implements ItemAction {
    }

    record OpenUrl(String url) implements ItemAction {
    }

    record WellKnownTarget(WellKnownTargetId id) implements ItemAction {
    }

    record InternalCommand(String name) implements ItemAction {
    }
}
