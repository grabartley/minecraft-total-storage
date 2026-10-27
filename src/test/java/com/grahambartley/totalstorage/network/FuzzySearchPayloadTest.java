package com.grahambartley.totalstorage.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FuzzySearchPayloadTest {
  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void theWantedStateSurvivesTheWireAndNothingElseIsSent(boolean enabled) {
    final RegistryByteBuf buffer = new RegistryByteBuf(Unpooled.buffer(), null);

    FuzzySearchPayload.CODEC.encode(buffer, new FuzzySearchPayload(enabled));

    assertEquals(enabled, FuzzySearchPayload.CODEC.decode(buffer).enabled());
    assertEquals(0, buffer.readableBytes());
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void everyPayloadReportsTheSharedChannelId(boolean enabled) {
    assertSame(FuzzySearchPayload.ID, new FuzzySearchPayload(enabled).getId());
    assertEquals("total-storage:fuzzy_search", FuzzySearchPayload.ID.id().toString());
  }
}
