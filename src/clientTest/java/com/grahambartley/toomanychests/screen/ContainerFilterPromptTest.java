package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.config.TerminalAccessMode;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.terminal.ContainerRoleEntry;
import com.grahambartley.toomanychests.terminal.NetworkCapacity;
import com.grahambartley.toomanychests.terminal.NetworkSettingsView;
import com.grahambartley.toomanychests.terminal.NetworkTagEntry;
import com.grahambartley.toomanychests.terminal.NetworkTagView;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

class ContainerFilterPromptTest {
  private static final BlockPos WHERE = new BlockPos(-6, -60, 6);
  private static final BlockPos ELSEWHERE = new BlockPos(9, -60, 3);

  private final ContainerFilterPrompt prompt = new ContainerFilterPrompt();

  private static ContainerRoleEntry row(BlockPos container, List<String> accepted) {
    return new ContainerRoleEntry(
        container, "", ContainerRole.FILTERED, accepted, Optional.empty(), 1, 1L, 1, 27, 1.0);
  }

  private static NetworkSettingsView viewOf(ContainerRoleEntry... rows) {
    return NetworkSettingsView.answered(
        16,
        16,
        32,
        TerminalAccessMode.OWNER,
        true,
        true,
        List.of(),
        NetworkCapacity.EMPTY,
        0,
        rows.length,
        List.of(rows));
  }

  private static NetworkTagView tagsOf(String... ids) {
    return new NetworkTagView(
        true,
        List.of(ids).stream().map(id -> new NetworkTagEntry(id, id, false, 0, 0)).toList(),
        Optional.empty(),
        java.util.Set.of());
  }

  @Test
  void aFreshPromptIsClosed() {
    assertFalse(prompt.isOpen());
    assertNull(prompt.container());
  }

  @Test
  void openingOnNoContainerLeavesItClosed() {
    prompt.open(null);

    assertFalse(prompt.isOpen());
  }

  @Test
  void anOpenPromptFindsTheRowItIsEditing() {
    ContainerRoleEntry mine = row(WHERE, List.of("ores"));
    prompt.open(WHERE);

    assertSame(mine, prompt.rowIn(viewOf(row(ELSEWHERE, List.of()), mine)));
  }

  @Test
  void aRowThatIsNotOnThisPageIsReportedAsMissingRatherThanGuessed() {
    prompt.open(WHERE);

    assertNull(prompt.rowIn(viewOf(row(ELSEWHERE, List.of()))));
  }

  @Test
  void aClosedPromptEditsNoRowAtAll() {
    assertNull(prompt.rowIn(viewOf(row(WHERE, List.of()))));
  }

  @Test
  void theFirstVisibleRowNamesTheFirstTag() {
    prompt.open(WHERE);
    NetworkTagView tags = tagsOf("ores", "wood", "food");

    assertEquals(
        "ores",
        prompt
            .tagAt(tags, TagPromptLayout.left() + TagPromptLayout.PADDING, TagPromptLayout.rowY(0))
            .id());
  }

  @Test
  void aClickOutsideTheRowsNamesNoTag() {
    prompt.open(WHERE);

    assertNull(prompt.tagAt(tagsOf("ores"), 0, 0));
  }

  @Test
  void aRowPastTheEndOfTheTagListNamesNoTag() {
    prompt.open(WHERE);

    assertNull(
        prompt.tagAt(
            tagsOf("ores"),
            TagPromptLayout.left() + TagPromptLayout.PADDING,
            TagPromptLayout.rowY(1)));
  }

  @Test
  void scrollingPastTheEndOfAShortListChangesNothing() {
    prompt.open(WHERE);
    prompt.fit(tagsOf("ores", "wood"));

    assertFalse(prompt.list().scrollBy(1));
  }

  @Test
  void scrollingALongListMovesTheFirstVisibleTagAlong() {
    prompt.open(WHERE);
    NetworkTagView tags = tagsOf("a", "b", "c", "d", "e", "f");
    prompt.fit(tags);

    assertTrue(prompt.list().scrollBy(1));
    assertEquals(
        "b",
        prompt
            .tagAt(tags, TagPromptLayout.left() + TagPromptLayout.PADDING, TagPromptLayout.rowY(0))
            .id());
  }
}
