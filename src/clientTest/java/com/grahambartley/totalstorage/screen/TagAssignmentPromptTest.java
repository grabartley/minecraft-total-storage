package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.terminal.NetworkTagEntry;
import com.grahambartley.totalstorage.terminal.NetworkTagView;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TagAssignmentPromptTest {
  private static final int ROW_LEFT = TagPromptLayout.LIST.left();

  private static NetworkTagEntry custom(String id) {
    return new NetworkTagEntry(id, id, false, 0, 0);
  }

  private static NetworkTagEntry preset(String id) {
    return new NetworkTagEntry(id, id, true, 0, 0);
  }

  private static NetworkTagView viewOf(List<NetworkTagEntry> tags) {
    return new NetworkTagView(true, tags, Optional.empty(), Set.of());
  }

  private static TagAssignmentPrompt scrolledToTheTop() {
    TagAssignmentPrompt prompt = new TagAssignmentPrompt();
    prompt.open(null);
    return prompt;
  }

  @Test
  void theFirstRowIsTheFirstCustomTagRatherThanAPreset() {
    TagAssignmentPrompt prompt = scrolledToTheTop();
    NetworkTagView view = viewOf(List.of(preset("wood"), custom("castle"), preset("stone")));
    prompt.fit(view);

    assertEquals(custom("castle"), prompt.tagAt(view, ROW_LEFT, TagPromptLayout.LIST.rowY(0)));
  }

  @Test
  void aPresetIsNotOfferedOnAnyRow() {
    TagAssignmentPrompt prompt = scrolledToTheTop();
    NetworkTagView view = viewOf(List.of(preset("wood"), custom("castle"), preset("stone")));
    prompt.fit(view);

    for (int row = 1; row < TagPromptLayout.VISIBLE_ROWS; row++) {
      assertNull(prompt.tagAt(view, ROW_LEFT, TagPromptLayout.LIST.rowY(row)));
    }
  }

  @Test
  void aNetworkOfPresetsAloneOffersNoRowsToTick() {
    TagAssignmentPrompt prompt = scrolledToTheTop();
    NetworkTagView view = viewOf(List.of(preset("wood"), preset("stone")));
    prompt.fit(view);

    assertNull(prompt.tagAt(view, ROW_LEFT, TagPromptLayout.LIST.rowY(0)));
    assertFalse(prompt.list().scrollBy(1));
  }

  @Test
  void thePanelOnlyScrollsOnceThereAreMoreCustomTagsThanRows() {
    TagAssignmentPrompt prompt = scrolledToTheTop();
    NetworkTagView tooMany =
        viewOf(
            List.of(
                custom("a"), custom("b"), custom("c"), custom("d"), custom("e"), preset("wood")));
    prompt.fit(tooMany);

    assertTrue(prompt.list().scrollBy(1));
    assertEquals(custom("b"), prompt.tagAt(tooMany, ROW_LEFT, TagPromptLayout.LIST.rowY(0)));
  }
}
