package io.github.wiskyahn.blockhud.service.update;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** SHA256 해시. 원본 Get-Sha256HashString 대체. (DESIGN.md §8.6 검증) */
public final class Sha256 {

    private Sha256() {
    }

    public static String ofBytes(byte[] bytes) {
        return toHex(digest().digest(bytes));
    }

    public static String ofFile(Path file) throws IOException {
        MessageDigest md = digest();
        try (InputStream in = Files.newInputStream(file)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) {
                md.update(buf, 0, n);
            }
        }
        return toHex(md.digest());
    }

    /** 기대 해시와 비교 ({@code sha256:} 접두사·대소문자 무시). 기대값 없으면 검증 생략(true). */
    public static boolean matches(Path file, String expected) throws IOException {
        if (expected == null || expected.isBlank()) {
            return true;
        }
        String want = expected.replaceFirst("(?i)^sha256:", "").trim().toLowerCase();
        return ofFile(file).equals(want);
    }

    private static MessageDigest digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }
}
