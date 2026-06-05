package io.github.wiskyahn.blockhud.domain.update;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SemanticVersionTest {

    @Test
    void parsesCommonTagForms() {
        assertEquals(new SemanticVersion(1, 2, 1), SemanticVersion.parse("v1.2.1"));
        assertEquals(new SemanticVersion(1, 2, 0), SemanticVersion.parse("1.2"));
        assertEquals(new SemanticVersion(1, 2, 1), SemanticVersion.parse("1.2.1-beta"));
        assertEquals(new SemanticVersion(0, 0, 0), SemanticVersion.parse("nonsense"));
        assertEquals(new SemanticVersion(0, 0, 0), SemanticVersion.parse(null));
    }

    @Test
    void comparesCorrectly() {
        assertTrue(SemanticVersion.parse("1.2.1").isNewerThan(SemanticVersion.parse("1.2.0")));
        assertTrue(SemanticVersion.parse("2.0.0").isNewerThan(SemanticVersion.parse("1.9.9")));
        assertFalse(SemanticVersion.parse("1.0.0").isNewerThan(SemanticVersion.parse("1.0.0")));
        assertFalse(SemanticVersion.parse("0.9.0").isNewerThan(SemanticVersion.parse("1.0.0")));
    }

    @Test
    void toStringIsMajorMinorPatch() {
        assertEquals("1.2.3", new SemanticVersion(1, 2, 3).toString());
    }
}
