package io.github.wiskyahn.blockhud.data.dto;

/**
 * {@code ItemAction}의 JSON 표현. type: none|launch|path|url|wellKnown|internal.
 */
public record ActionDto(String type, String target) {
}
