package com.grahambartley.totalstorage.terminal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.network.ContainerHighlightPayload;
import com.grahambartley.totalstorage.storage.ContainerRole;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ContainerHighlightServiceTest {
  private static final int TEN_SECONDS = 200;

  @ParameterizedTest
  @EnumSource(ContainerRole.class)
  void aLocatedContainerIsOutlinedAloneForTenSeconds(ContainerRole role) {
    final HighlightedContainer container = new HighlightedContainer(new BlockPos(1, 2, 3), role);

    final ContainerHighlightPayload payload = ContainerHighlightService.locatedPayload(container);

    assertEquals(TEN_SECONDS, payload.ticks());
    assertEquals(List.of(container), payload.containers());
    assertTrue(payload.located());
  }
}
