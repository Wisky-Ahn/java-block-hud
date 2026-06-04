package io.github.wiskyahn.blockhud.data;

import io.github.wiskyahn.blockhud.data.dto.ActionDto;
import io.github.wiskyahn.blockhud.data.dto.HudDataDto;
import io.github.wiskyahn.blockhud.data.dto.ItemDto;
import io.github.wiskyahn.blockhud.data.dto.OffsetDto;
import io.github.wiskyahn.blockhud.domain.action.ItemAction;
import io.github.wiskyahn.blockhud.domain.action.WellKnownTargetId;
import io.github.wiskyahn.blockhud.domain.model.HudData;
import io.github.wiskyahn.blockhud.domain.model.ImageOffset;
import io.github.wiskyahn.blockhud.domain.model.Item;
import io.github.wiskyahn.blockhud.domain.model.SlotAddress;
import java.util.List;
import java.util.Locale;

/** DTO ↔ 도메인 변환. (sealed 타입을 Jackson에 직접 노출하지 않기 위한 경계) */
public final class ItemMapper {

    private ItemMapper() {
    }

    // ---- 도메인 → DTO ----

    public static HudDataDto toDto(HudData data) {
        return new HudDataDto(
                data.hotbar().stream().map(ItemMapper::toDto).toList(),
                data.inventory().stream().map(ItemMapper::toDto).toList());
    }

    static ItemDto toDto(Item item) {
        Integer slot = null;
        Integer col = null;
        Integer row = null;
        switch (item.address()) {
            case SlotAddress.HotbarSlot h -> slot = h.index();
            case SlotAddress.GridSlot g -> {
                col = g.col();
                row = g.row();
            }
        }
        OffsetDto offset = item.offset().isZero()
                ? null
                : new OffsetDto(item.offset().x(), item.offset().y(), item.offset().size());
        return new ItemDto(slot, col, row, emptyToNull(item.label()),
                toDto(item.action()), emptyToNull(item.image()), item.qty(), offset,
                item.confirmBeforeRun());
    }

    static ActionDto toDto(ItemAction action) {
        return switch (action) {
            case ItemAction.None ignored -> null;
            case ItemAction.LaunchProgram a -> new ActionDto("launch", a.command());
            case ItemAction.OpenPath a -> new ActionDto("path", a.path());
            case ItemAction.OpenUrl a -> new ActionDto("url", a.url());
            case ItemAction.WellKnownTarget a -> new ActionDto("wellKnown", a.id().name());
            case ItemAction.InternalCommand a -> new ActionDto("internal", a.name());
        };
    }

    // ---- DTO → 도메인 ----

    public static HudData toDomain(HudDataDto dto) {
        return new HudData(toDomainList(dto.hotbar()), toDomainList(dto.inventory()));
    }

    private static List<Item> toDomainList(List<ItemDto> dtos) {
        return dtos == null ? List.of() : dtos.stream().map(ItemMapper::toDomain).toList();
    }

    static Item toDomain(ItemDto dto) {
        SlotAddress address;
        if (dto.slot() != null) {
            address = new SlotAddress.HotbarSlot(dto.slot());
        } else if (dto.col() != null && dto.row() != null) {
            address = new SlotAddress.GridSlot(dto.col(), dto.row());
        } else {
            throw new IllegalArgumentException("ItemDto에 slot 또는 col/row가 필요합니다: " + dto);
        }
        ImageOffset offset = dto.offset() == null
                ? ImageOffset.ZERO
                : new ImageOffset(dto.offset().x(), dto.offset().y(), dto.offset().size());
        return new Item(address, dto.label(), toDomain(dto.action()), dto.image(),
                dto.qty(), offset, dto.confirmBeforeRun());
    }

    static ItemAction toDomain(ActionDto dto) {
        if (dto == null || dto.type() == null) {
            return ItemAction.NONE;
        }
        String target = dto.target() == null ? "" : dto.target();
        return switch (dto.type().toLowerCase(Locale.ROOT)) {
            case "launch" -> new ItemAction.LaunchProgram(target);
            case "path" -> new ItemAction.OpenPath(target);
            case "url" -> new ItemAction.OpenUrl(target);
            case "wellknown" -> new ItemAction.WellKnownTarget(WellKnownTargetId.valueOf(target));
            case "internal" -> new ItemAction.InternalCommand(target);
            default -> ItemAction.NONE;
        };
    }

    private static String emptyToNull(String value) {
        return (value == null || value.isEmpty()) ? null : value;
    }
}
