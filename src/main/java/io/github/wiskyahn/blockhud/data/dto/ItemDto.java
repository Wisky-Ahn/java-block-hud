package io.github.wiskyahn.blockhud.data.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * {@code Item}의 JSON 표현. 핫바는 {@code slot}, 인벤토리는 {@code col}/{@code row}를 채운다.
 * (DESIGN.md §4 / §8.4)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ItemDto(
        Integer slot,
        Integer col,
        Integer row,
        String label,
        ActionDto action,
        String image,
        int qty,
        OffsetDto offset,
        boolean confirmBeforeRun
) {
}
