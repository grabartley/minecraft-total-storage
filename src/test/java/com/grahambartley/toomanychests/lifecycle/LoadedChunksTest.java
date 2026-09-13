package com.grahambartley.toomanychests.lifecycle;

import static org.junit.jupiter.api.Assertions.assertFalse;

import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

class LoadedChunksTest {
  @Test
  void nothingIsHeldByAWorldThatIsNotThere() {
    assertFalse(LoadedChunks.holds(null, BlockPos.ORIGIN));
  }

  @Test
  void aPositionThatIsNotThereIsNeverHeld() {
    assertFalse(LoadedChunks.holds(null, null));
  }
}
