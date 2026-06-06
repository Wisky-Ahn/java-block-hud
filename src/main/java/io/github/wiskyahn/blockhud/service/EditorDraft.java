package io.github.wiskyahn.blockhud.service;

import io.github.wiskyahn.blockhud.domain.model.HudData;
import io.github.wiskyahn.blockhud.domain.model.Item;
import io.github.wiskyahn.blockhud.domain.model.SlotAddress;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

/**
 * 에디터 작업본. 실데이터를 직접 수정하지 않고 working 사본을 편집한 뒤 커밋한다.
 * 실행취소/재실행을 지원한다. 원본 EditorDraft + SettingsState 대체. (DESIGN.md §8.4)
 */
public final class EditorDraft {

    private HudData baseline;
    private HudData working;
    private final Deque<HudData> undoStack = new ArrayDeque<>();
    private final Deque<HudData> redoStack = new ArrayDeque<>();

    public EditorDraft(HudData initial) {
        this.baseline = initial;
        this.working = initial;
    }

    public HudData working() {
        return working;
    }

    public Optional<Item> find(SlotAddress address) {
        return items(address).stream().filter(i -> i.address().equals(address)).findFirst();
    }

    /** 슬롯 데이터를 변경(또는 추가). 변경 전 상태를 실행취소 스택에 저장. */
    public void put(Item item) {
        undoStack.push(working);
        redoStack.clear();
        working = replace(working, item);
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public void undo() {
        if (canUndo()) {
            redoStack.push(working);
            working = undoStack.pop();
        }
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public void redo() {
        if (canRedo()) {
            undoStack.push(working);
            working = redoStack.pop();
        }
    }

    public boolean isDirty() {
        return !working.equals(baseline);
    }

    /** 모든 변경을 폐기하고 baseline으로 복원 (원본 Reset). */
    public void reset() {
        if (isDirty()) {
            undoStack.push(working);
            redoStack.clear();
            working = baseline;
        }
    }

    /** 커밋: 현재 working을 새 baseline으로 확정하고 이력 비움. */
    public void markCommitted() {
        baseline = working;
        undoStack.clear();
        redoStack.clear();
    }

    /**
     * 슬롯 {@code from}의 아이템을 {@code to}로 이동. {@code to}에 아이템이 있으면 서로 교환.
     * 같은 목록(둘 다 핫바 또는 둘 다 인벤토리) 내에서만 처리. (원본 에디터 드래그앤드롭)
     */
    public void move(SlotAddress from, SlotAddress to) {
        if (from.equals(to)) {
            return;
        }
        boolean hotbar = from instanceof SlotAddress.HotbarSlot;
        List<Item> source = hotbar ? working.hotbar() : working.inventory();
        Optional<Item> fromItem = source.stream().filter(i -> i.address().equals(from)).findFirst();
        if (fromItem.isEmpty()) {
            return;
        }
        Optional<Item> toItem = source.stream().filter(i -> i.address().equals(to)).findFirst();

        undoStack.push(working);
        redoStack.clear();

        List<Item> result = new ArrayList<>();
        for (Item item : source) {
            if (item.address().equals(from) || item.address().equals(to)) {
                continue;
            }
            result.add(item);
        }
        result.add(fromItem.get().withAddress(to));
        toItem.ifPresent(item -> result.add(item.withAddress(from)));

        working = hotbar
                ? new HudData(result, working.inventory())
                : new HudData(working.hotbar(), result);
    }

    private List<Item> items(SlotAddress address) {
        return address instanceof SlotAddress.HotbarSlot ? working.hotbar() : working.inventory();
    }

    private static HudData replace(HudData data, Item item) {
        boolean hotbar = item.address() instanceof SlotAddress.HotbarSlot;
        List<Item> target = new ArrayList<>(hotbar ? data.hotbar() : data.inventory());
        int idx = indexOf(target, item.address());
        if (idx >= 0) {
            target.set(idx, item);
        } else {
            target.add(item);
        }
        return hotbar
                ? new HudData(target, data.inventory())
                : new HudData(data.hotbar(), target);
    }

    private static int indexOf(List<Item> items, SlotAddress address) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).address().equals(address)) {
                return i;
            }
        }
        return -1;
    }
}
