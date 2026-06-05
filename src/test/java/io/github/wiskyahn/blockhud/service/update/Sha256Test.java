package io.github.wiskyahn.blockhud.service.update;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Sha256Test {

    // "abc"의 SHA256
    private static final String ABC_SHA256 =
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad";

    @Test
    void computesKnownHash() {
        assertTrue(ABC_SHA256.equals(Sha256.ofBytes("abc".getBytes(StandardCharsets.UTF_8))));
    }

    @Test
    void matchesAcceptsPrefixedAndCaseInsensitive(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("abc.txt");
        Files.writeString(file, "abc");
        assertTrue(Sha256.matches(file, "sha256:" + ABC_SHA256.toUpperCase()));
        assertTrue(Sha256.matches(file, ABC_SHA256));
        assertFalse(Sha256.matches(file, "deadbeef"));
    }

    @Test
    void blankExpectedSkipsVerification(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("x.txt");
        Files.writeString(file, "anything");
        assertTrue(Sha256.matches(file, ""));
        assertTrue(Sha256.matches(file, null));
    }
}
