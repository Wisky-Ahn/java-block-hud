package io.github.wiskyahn.blockhud.data.dto;

import java.util.List;

public record HudDataDto(List<ItemDto> hotbar, List<ItemDto> inventory) {
}
