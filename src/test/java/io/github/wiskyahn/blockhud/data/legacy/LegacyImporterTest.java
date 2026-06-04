package io.github.wiskyahn.blockhud.data.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.wiskyahn.blockhud.domain.action.ItemAction;
import io.github.wiskyahn.blockhud.domain.model.HudData;
import io.github.wiskyahn.blockhud.domain.model.Item;
import io.github.wiskyahn.blockhud.domain.model.SlotAddress;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LegacyImporterTest {

    /** 원본 핫바 데이터는 UTF-16LE(BOM) 인코딩이다. */
    @Test
    void importsHotbarFromUtf16File(@TempDir Path dir) throws IOException {
        writeUtf16Le(dir.resolve("HotbarItems.inc"), """
                [Variables]
                HotbarItem_Slot01_Label=내 컴퓨터
                HotbarItem_Slot01_Action=explorer.exe "shell:MyComputerFolder"
                HotbarItem_Slot01_Image=Computer.png
                HotbarItem_Slot01_Qty=0
                HotbarItem_Slot05_Label=계산기
                HotbarItem_Slot05_Action=calc.exe
                HotbarItem_Slot05_Image=Cooked_Beef.png
                HotbarItem_Slot05_Qty=4
                HotbarItem_Slot09_Action=https://litt.ly/dmeloper
                """);

        HudData data = new LegacyImporter().importFromDataDir(dir);

        assertEquals(3, data.hotbar().size());
        Item slot1 = findHotbar(data, 1);
        assertEquals("내 컴퓨터", slot1.label());
        assertEquals("Computer.png", slot1.image());
        assertInstanceOf(ItemAction.WellKnownTarget.class, slot1.action());

        Item slot5 = findHotbar(data, 5);
        assertEquals(4, slot5.qty());
        assertInstanceOf(ItemAction.LaunchProgram.class, slot5.action());

        assertInstanceOf(ItemAction.OpenUrl.class, findHotbar(data, 9).action());
    }

    @Test
    void importsInventoryGridAndImageOffsets(@TempDir Path dir) throws IOException {
        writeUtf16Le(dir.resolve("InventoryItems.inc"), """
                [Variables]
                InventoryItem_SlotX3Y2_Label=금 주괴
                InventoryItem_SlotX3Y2_Image=Gold_Ingot.png
                InventoryItem_SlotX3Y2_Qty=0
                InventoryItem_SlotX8Y4_Label=
                InventoryItem_SlotX8Y4_Qty=
                """);
        // ImageAdjustments.inc는 원본에서 UTF-8 BOM 사용
        writeUtf8Bom(dir.resolve("ImageAdjustments.inc"), """
                [Variables]
                ImageAdjustKeys=Gold_Ingot|Diamond_Sword
                ImageAdjust_Gold_Ingot_OffsetX=1
                ImageAdjust_Gold_Ingot_OffsetY=2
                ImageAdjust_Gold_Ingot_SizeOffset=-3
                ImageAdjust_Diamond_Sword_OffsetX=0
                ImageAdjust_Diamond_Sword_OffsetY=0
                ImageAdjust_Diamond_Sword_SizeOffset=-6
                """);

        HudData data = new LegacyImporter().importFromDataDir(dir);

        // 빈 슬롯(X8Y4)은 제외되어 1개만 남는다
        assertEquals(1, data.inventory().size());
        Item gold = data.inventory().get(0);
        assertEquals(new SlotAddress.GridSlot(3, 2), gold.address());
        assertEquals("금 주괴", gold.label());
        // 언더스코어 포함 키(Gold_Ingot)의 오프셋이 정확히 매칭되는지
        assertEquals(1, gold.offset().x());
        assertEquals(2, gold.offset().y());
        assertEquals(-3, gold.offset().size());
    }

    @Test
    void missingFilesYieldEmptyData(@TempDir Path dir) throws IOException {
        HudData data = new LegacyImporter().importFromDataDir(dir);
        assertTrue(data.hotbar().isEmpty());
        assertTrue(data.inventory().isEmpty());
    }

    private static Item findHotbar(HudData data, int index) {
        return data.hotbar().stream()
                .filter(i -> i.address() instanceof SlotAddress.HotbarSlot h && h.index() == index)
                .findFirst()
                .orElseThrow();
    }

    private static void writeUtf16Le(Path path, String content) throws IOException {
        try (OutputStream out = Files.newOutputStream(path)) {
            out.write(0xFF);
            out.write(0xFE);
            out.write(content.getBytes(StandardCharsets.UTF_16LE));
        }
    }

    private static void writeUtf8Bom(Path path, String content) throws IOException {
        try (OutputStream out = Files.newOutputStream(path)) {
            out.write(0xEF);
            out.write(0xBB);
            out.write(0xBF);
            out.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }
}
