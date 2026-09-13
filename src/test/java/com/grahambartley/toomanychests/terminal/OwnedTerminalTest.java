package com.grahambartley.toomanychests.terminal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

class OwnedTerminalTest {
  @Test
  void aTerminalHoldingCustomTagsIsWorthCopyingFrom() {
    assertTrue(new OwnedTerminal(BlockPos.ORIGIN, 3).holdsAnythingToCopy());
  }

  @Test
  void aTerminalHoldingNoCustomTagsIsNot() {
    assertFalse(new OwnedTerminal(BlockPos.ORIGIN, 0).holdsAnythingToCopy());
  }

  @Test
  void aNegativeCountIsNeverReportedBack() {
    assertEquals(0, new OwnedTerminal(BlockPos.ORIGIN, -4).customTags());
  }

  @Test
  void aMutablePositionIsPinnedSoItCannotMoveUnderTheList() {
    final BlockPos.Mutable moving = new BlockPos.Mutable(1, 2, 3);
    final OwnedTerminal terminal = new OwnedTerminal(moving, 1);

    moving.set(9, 9, 9);

    assertEquals(new BlockPos(1, 2, 3), terminal.pos());
  }
}
