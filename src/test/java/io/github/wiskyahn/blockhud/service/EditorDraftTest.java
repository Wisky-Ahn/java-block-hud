package io.github.wiskyahn.blockhud.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.wiskyahn.blockhud.domain.action.ItemAction;
import io.github.wiskyahn.blockhud.domain.model.HudData;
import io.github.wiskyahn.blockhud.domain.model.ImageOffset;
import io.github.wiskyahn.blockhud.domain.model.Item;
import io.github.wiskyahn.blockhud.domain.model.SlotAddress;
import java.util.List;
import org.junit.jupiter.api.Test;

class EditorDraftTest {

    private static Item hotbar(int index, String label) {
        return new Item(new SlotAddress.HotbarSlot(index), label, ItemAction.NONE,
                "img.png", 0, ImageOffset.ZERO, false);
    }

    @Test
    void putReplacesExistingSlotByAddress() {
        EditorDraft draft = new EditorDraft(new HudData(List.of(hotbar(1, "old")), List.of()));

        draft.put(hotbar(1, "new"));

        assertEquals(1, draft.working().hotbar().size());
        assertEquals("new", draft.working().hotbar().get(0).label());
    }

    @Test
    void putAddsNewSlotWhenAbsent() {
        EditorDraft draft = new EditorDraft(HudData.empty());
        draft.put(hotbar(3, "added"));
        assertEquals(1, draft.working().hotbar().size());
        assertTrue(draft.find(new SlotAddress.HotbarSlot(3)).isPresent());
    }

    @Test
    void tracksDirtyState() {
        EditorDraft draft = new EditorDraft(new HudData(List.of(hotbar(1, "a")), List.of()));
        assertFalse(draft.isDirty());
        draft.put(hotbar(1, "b"));
        assertTrue(draft.isDirty());
        draft.markCommitted();
        assertFalse(draft.isDirty());
    }

    @Test
    void undoRedoRestoresState() {
        EditorDraft draft = new EditorDraft(new HudData(List.of(hotbar(1, "a")), List.of()));
        draft.put(hotbar(1, "b"));
        draft.put(hotbar(1, "c"));
        assertEquals("c", draft.working().hotbar().get(0).label());

        draft.undo();
        assertEquals("b", draft.working().hotbar().get(0).label());
        draft.undo();
        assertEquals("a", draft.working().hotbar().get(0).label());
        assertFalse(draft.canUndo());

        draft.redo();
        assertEquals("b", draft.working().hotbar().get(0).label());
    }

    @Test
    void putClearsRedoStack() {
        EditorDraft draft = new EditorDraft(new HudData(List.of(hotbar(1, "a")), List.of()));
        draft.put(hotbar(1, "b"));
        draft.undo();
        draft.put(hotbar(1, "c")); // 새 변경 → redo 무효화
        assertFalse(draft.canRedo());
        assertEquals("c", draft.working().hotbar().get(0).label());
    }

    private static Item grid(int col, int row, String label) {
        return new Item(new SlotAddress.GridSlot(col, row), label, ItemAction.NONE,
                "img.png", 0, ImageOffset.ZERO, false);
    }

    @Test
    void moveToEmptySlotRelocatesItem() {
        EditorDraft draft = new EditorDraft(new HudData(List.of(), List.of(grid(1, 1, "gold"))));

        draft.move(new SlotAddress.GridSlot(1, 1), new SlotAddress.GridSlot(3, 2));

        assertEquals(1, draft.working().inventory().size());
        Item moved = draft.working().inventory().get(0);
        assertEquals(new SlotAddress.GridSlot(3, 2), moved.address());
        assertEquals("gold", moved.label());
    }

    @Test
    void moveSwapsWhenTargetOccupied() {
        EditorDraft draft = new EditorDraft(new HudData(List.of(),
                List.of(grid(1, 1, "a"), grid(2, 2, "b"))));

        draft.move(new SlotAddress.GridSlot(1, 1), new SlotAddress.GridSlot(2, 2));

        assertEquals("b", draft.find(new SlotAddress.GridSlot(1, 1)).orElseThrow().label());
        assertEquals("a", draft.find(new SlotAddress.GridSlot(2, 2)).orElseThrow().label());
    }

    @Test
    void resetRestoresBaseline() {
        EditorDraft draft = new EditorDraft(new HudData(List.of(hotbar(1, "a")), List.of()));
        draft.put(hotbar(1, "b"));
        draft.reset();
        assertEquals("a", draft.working().hotbar().get(0).label());
        assertFalse(draft.isDirty());
    }
}
