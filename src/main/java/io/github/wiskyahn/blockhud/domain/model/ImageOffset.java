package io.github.wiskyahn.blockhud.domain.model;

/**
 * 슬롯 이미지의 위치/크기 미세 조정. 원본 {@code OffsetX / OffsetY / SizeOffset}. (DESIGN.md §8.2)
 */
public record ImageOffset(int x, int y, int size) {

    public static final ImageOffset ZERO = new ImageOffset(0, 0, 0);

    public boolean isZero() {
        return x == 0 && y == 0 && size == 0;
    }
}
