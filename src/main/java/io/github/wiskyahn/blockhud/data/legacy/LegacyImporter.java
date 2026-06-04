package io.github.wiskyahn.blockhud.data.legacy;

import io.github.wiskyahn.blockhud.domain.action.ItemActionParser;
import io.github.wiskyahn.blockhud.domain.model.HudData;
import io.github.wiskyahn.blockhud.domain.model.ImageOffset;
import io.github.wiskyahn.blockhud.domain.model.Item;
import io.github.wiskyahn.blockhud.domain.model.SlotAddress;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 구버전 Rainmeter 데이터({@code HotbarItems.inc}, {@code InventoryItems.inc},
 * {@code ImageAdjustments.inc})를 도메인 {@link HudData}로 변환한다.
 * 원본 {@code ImportFromOldVersion.ps1} 대체. (DESIGN.md §5, §8.4)
 *
 * <pre>
 *   핫바:     HotbarItem_Slot{NN}_{Label|Action|Image|Qty}
 *   인벤토리: InventoryItem_SlotX{col}Y{row}_{Field}
 *   오프셋:   ImageAdjustKeys=a|b|c , ImageAdjust_{key}_OffsetX/OffsetY/SizeOffset
 * </pre>
 */
public final class LegacyImporter {

    private static final Pattern HOTBAR_KEY =
            Pattern.compile("^HotbarItem_Slot(\\d+)_(Label|Action|Image|Qty)$");
    private static final Pattern INVENTORY_KEY =
            Pattern.compile("^InventoryItem_SlotX(\\d+)Y(\\d+)_(Label|Action|Image|Qty)$");

    /** 표준 데이터 디렉터리({@code @Resources/Customs/Data})에서 세 파일을 읽어 변환. */
    public HudData importFromDataDir(Path dataDir) throws IOException {
        Map<String, ImageOffset> offsets =
                readImageOffsets(dataDir.resolve("ImageAdjustments.inc"));
        List<Item> hotbar = importHotbar(dataDir.resolve("HotbarItems.inc"), offsets);
        List<Item> inventory = importInventory(dataDir.resolve("InventoryItems.inc"), offsets);
        return new HudData(hotbar, inventory);
    }

    List<Item> importHotbar(Path file, Map<String, ImageOffset> offsets) throws IOException {
        if (!Files.exists(file)) {
            return List.of();
        }
        Map<String, Map<String, String>> bySlot = new LinkedHashMap<>();
        Map<String, Integer> index = new LinkedHashMap<>();
        for (var entry : IncFile.read(file).variables().entrySet()) {
            Matcher m = HOTBAR_KEY.matcher(entry.getKey());
            if (!m.matches()) {
                continue;
            }
            String slot = m.group(1);
            index.putIfAbsent(slot, Integer.parseInt(slot));
            bySlot.computeIfAbsent(slot, k -> new LinkedHashMap<>()).put(m.group(2), entry.getValue());
        }
        List<Item> items = new ArrayList<>();
        for (var slot : bySlot.entrySet()) {
            Item item = buildItem(new SlotAddress.HotbarSlot(index.get(slot.getKey())),
                    slot.getValue(), offsets);
            if (!item.isEmpty()) {
                items.add(item);
            }
        }
        return items;
    }

    List<Item> importInventory(Path file, Map<String, ImageOffset> offsets) throws IOException {
        if (!Files.exists(file)) {
            return List.of();
        }
        Map<String, Map<String, String>> bySlot = new LinkedHashMap<>();
        Map<String, int[]> coords = new LinkedHashMap<>();
        for (var entry : IncFile.read(file).variables().entrySet()) {
            Matcher m = INVENTORY_KEY.matcher(entry.getKey());
            if (!m.matches()) {
                continue;
            }
            String key = "X" + m.group(1) + "Y" + m.group(2);
            coords.putIfAbsent(key, new int[]{Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))});
            bySlot.computeIfAbsent(key, k -> new LinkedHashMap<>()).put(m.group(3), entry.getValue());
        }
        List<Item> items = new ArrayList<>();
        for (var slot : bySlot.entrySet()) {
            int[] c = coords.get(slot.getKey());
            Item item = buildItem(new SlotAddress.GridSlot(c[0], c[1]), slot.getValue(), offsets);
            if (!item.isEmpty()) {
                items.add(item);
            }
        }
        return items;
    }

    private Item buildItem(SlotAddress address, Map<String, String> fields,
            Map<String, ImageOffset> offsets) {
        String label = fields.getOrDefault("Label", "");
        String image = fields.getOrDefault("Image", "");
        int qty = parseInt(fields.get("Qty"));
        ImageOffset offset = offsets.getOrDefault(imageKey(image), ImageOffset.ZERO);
        return new Item(address, label, ItemActionParser.parse(fields.get("Action")),
                image, qty, offset, false);
    }

    Map<String, ImageOffset> readImageOffsets(Path file) throws IOException {
        if (!Files.exists(file)) {
            return Map.of();
        }
        Map<String, String> vars = IncFile.read(file).variables();
        String keysRaw = vars.get("ImageAdjustKeys");
        if (keysRaw == null || keysRaw.isBlank()) {
            return Map.of();
        }
        Map<String, ImageOffset> result = new LinkedHashMap<>();
        for (String key : keysRaw.split("\\|")) {
            String k = key.trim();
            if (k.isEmpty()) {
                continue;
            }
            result.put(k, new ImageOffset(
                    parseInt(vars.get("ImageAdjust_" + k + "_OffsetX")),
                    parseInt(vars.get("ImageAdjust_" + k + "_OffsetY")),
                    parseInt(vars.get("ImageAdjust_" + k + "_SizeOffset"))));
        }
        return result;
    }

    /** 이미지 파일명에서 확장자를 제거한 오프셋 조회 키 (원본 getImageAdjustmentKey 동작). */
    static String imageKey(String image) {
        String s = image == null ? "" : image.trim();
        int dot = s.lastIndexOf('.');
        return dot > 0 ? s.substring(0, dot) : s;
    }

    private static int parseInt(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
