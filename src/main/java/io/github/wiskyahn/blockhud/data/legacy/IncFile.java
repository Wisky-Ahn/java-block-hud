package io.github.wiskyahn.blockhud.data.legacy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Rainmeter {@code .inc} 파일 파서. 원본 데이터는 UTF-16LE/UTF-8 BOM이 혼재하므로
 * BOM으로 인코딩을 자동 감지한다. (DESIGN.md §8.4, "인코딩 혼재 주의")
 *
 * <p>{@code [Section]} 헤더와 {@code Key=Value} 라인을 읽어 {@code section → (key → value)}로 보관한다.
 * 대부분의 원본 데이터는 단일 {@code [Variables]} 섹션을 사용한다.
 */
public final class IncFile {

    private final Map<String, Map<String, String>> sections;

    private IncFile(Map<String, Map<String, String>> sections) {
        this.sections = sections;
    }

    public static IncFile read(Path path) throws IOException {
        return parse(decode(Files.readAllBytes(path)));
    }

    /** {@code [Variables]} 섹션의 키맵(없으면 빈 맵). */
    public Map<String, String> variables() {
        return sections.getOrDefault("Variables", Map.of());
    }

    public Map<String, Map<String, String>> sections() {
        return sections;
    }

    static String decode(byte[] bytes) {
        if (bytes.length >= 2 && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xFE) {
            return new String(bytes, 2, bytes.length - 2, StandardCharsets.UTF_16LE);
        }
        if (bytes.length >= 2 && (bytes[0] & 0xFF) == 0xFE && (bytes[1] & 0xFF) == 0xFF) {
            return new String(bytes, 2, bytes.length - 2, StandardCharsets.UTF_16BE);
        }
        if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xEF && (bytes[1] & 0xFF) == 0xBB
                && (bytes[2] & 0xFF) == 0xBF) {
            return new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    static IncFile parse(String text) {
        Map<String, Map<String, String>> result = new LinkedHashMap<>();
        String current = "Variables";
        result.put(current, new LinkedHashMap<>());

        for (String rawLine : text.split("\\R")) {
            String line = stripBom(rawLine).trim();
            if (line.isEmpty() || line.startsWith(";")) {
                continue;
            }
            if (line.startsWith("[") && line.endsWith("]")) {
                current = line.substring(1, line.length() - 1).trim();
                result.computeIfAbsent(current, k -> new LinkedHashMap<>());
                continue;
            }
            int eq = line.indexOf('=');
            if (eq < 0) {
                continue;
            }
            String key = line.substring(0, eq).trim();
            String value = line.substring(eq + 1).trim();
            if (!key.isEmpty()) {
                result.get(current).put(key, value);
            }
        }
        return new IncFile(result);
    }

    private static String stripBom(String s) {
        return (!s.isEmpty() && s.charAt(0) == '﻿') ? s.substring(1) : s;
    }
}
