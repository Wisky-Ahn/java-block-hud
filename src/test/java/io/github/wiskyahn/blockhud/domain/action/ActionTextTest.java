package io.github.wiskyahn.blockhud.domain.action;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ActionTextTest {

    @Test
    void formatsEachActionType() {
        assertEquals("", ActionText.format(ItemAction.NONE));
        assertEquals("calc.exe", ActionText.format(new ItemAction.LaunchProgram("calc.exe")));
        assertEquals("https://a.b", ActionText.format(new ItemAction.OpenUrl("https://a.b")));
        assertEquals("_OPEN_INVENTORY_",
                ActionText.format(new ItemAction.InternalCommand("_OPEN_INVENTORY_")));
    }

    @Test
    void wellKnownRoundTripsThroughParse() {
        for (WellKnownTargetId id : WellKnownTargetId.values()) {
            String text = ActionText.format(new ItemAction.WellKnownTarget(id));
            ItemAction parsed = ItemActionParser.parse(text);
            assertEquals(new ItemAction.WellKnownTarget(id), parsed,
                    "round-trip 실패: " + id + " → '" + text + "'");
        }
    }

    @Test
    void launchAndUrlRoundTrip() {
        var launch = new ItemAction.LaunchProgram("notepad.exe");
        assertEquals(launch, ItemActionParser.parse(ActionText.format(launch)));

        var url = new ItemAction.OpenUrl("https://example.com");
        assertEquals(url, ItemActionParser.parse(ActionText.format(url)));
    }
}
