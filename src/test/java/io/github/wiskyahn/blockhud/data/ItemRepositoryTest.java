package io.github.wiskyahn.blockhud.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.wiskyahn.blockhud.domain.action.ItemAction;
import io.github.wiskyahn.blockhud.domain.action.WellKnownTargetId;
import io.github.wiskyahn.blockhud.domain.model.HudData;
import io.github.wiskyahn.blockhud.domain.model.ImageOffset;
import io.github.wiskyahn.blockhud.domain.model.Item;
import io.github.wiskyahn.blockhud.domain.model.SlotAddress;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ItemRepositoryTest {

    @Test
    void loadReturnsEmptyWhenFileMissing(@TempDir Path dir) throws IOException {
        HudData data = new ItemRepository(dir.resolve("nope.json")).load();
        assertTrue(data.hotbar().isEmpty());
        assertTrue(data.inventory().isEmpty());
    }

    @Test
    void savesAndLoadsRoundTrip(@TempDir Path dir) throws IOException {
        ItemRepository repo = new ItemRepository(dir.resolve("sub").resolve("items.json"));

        Item hotbar = new Item(new SlotAddress.HotbarSlot(1), "내 컴퓨터",
                new ItemAction.WellKnownTarget(WellKnownTargetId.THIS_PC),
                "Computer.png", 0, ImageOffset.ZERO, false);
        Item grid = new Item(new SlotAddress.GridSlot(3, 2), "금 주괴",
                new ItemAction.OpenUrl("https://example.com"),
                "Gold_Ingot.png", 5, new ImageOffset(1, 2, -3), true);
        HudData original = new HudData(List.of(hotbar), List.of(grid));

        repo.save(original);
        HudData loaded = repo.load();

        assertEquals(original, loaded);
        // 디렉터리 자동 생성 확인
        assertTrue(dir.resolve("sub").resolve("items.json").toFile().exists());
    }

    @Test
    void preservesAllActionTypes(@TempDir Path dir) throws IOException {
        ItemRepository repo = new ItemRepository(dir.resolve("items.json"));
        HudData original = new HudData(List.of(
                slot(1, new ItemAction.LaunchProgram("calc.exe")),
                slot(2, new ItemAction.OpenPath("/tmp")),
                slot(3, new ItemAction.OpenUrl("https://a.b")),
                slot(4, new ItemAction.WellKnownTarget(WellKnownTargetId.RECYCLE_BIN)),
                slot(5, new ItemAction.InternalCommand("_OPEN_INVENTORY_")),
                slot(6, ItemAction.NONE)),
                List.of());

        repo.save(original);
        assertEquals(original, repo.load());
    }

    private static Item slot(int index, ItemAction action) {
        return new Item(new SlotAddress.HotbarSlot(index), "label", action,
                "img.png", 0, ImageOffset.ZERO, false);
    }
}
