package com.grahambartley.totalstorage.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.storage.ContainerRole;
import com.grahambartley.totalstorage.terminal.HighlightedContainer;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class ContainerHighlightPayloadTest {
  private static RegistryByteBuf buffer() {
    return new RegistryByteBuf(Unpooled.buffer(), null);
  }

  private static ContainerHighlightPayload roundTrip(ContainerHighlightPayload payload) {
    RegistryByteBuf buffer = buffer();
    ContainerHighlightPayload.CODEC.encode(buffer, payload);
    return ContainerHighlightPayload.CODEC.decode(buffer);
  }

  @ParameterizedTest
  @EnumSource(ContainerRole.class)
  void everyOutlinedContainerKeepsItsPositionAndItsRole(ContainerRole role) {
    ContainerHighlightPayload sent =
        ContainerHighlightPayload.ofNetwork(
            100, List.of(new HighlightedContainer(new BlockPos(4, -60, 12), role)));

    assertEquals(sent, roundTrip(sent));
  }

  @ParameterizedTest
  @EnumSource(ContainerRole.class)
  void aLocatedContainerStaysLocatedAcrossTheWire(ContainerRole role) {
    ContainerHighlightPayload sent =
        ContainerHighlightPayload.ofOne(
            200, new HighlightedContainer(new BlockPos(4, -60, 12), role));

    assertTrue(roundTrip(sent).located());
    assertEquals(sent, roundTrip(sent));
  }

  @Test
  void aNetworkHighlightIsNotLocated() {
    ContainerHighlightPayload sent =
        ContainerHighlightPayload.ofNetwork(
            100, List.of(new HighlightedContainer(BlockPos.ORIGIN, ContainerRole.GENERAL)));

    assertFalse(roundTrip(sent).located());
  }

  @Test
  void theWholePayloadIsReadBackWithNothingLeftOver() {
    RegistryByteBuf buffer = buffer();

    ContainerHighlightPayload.CODEC.encode(
        buffer,
        ContainerHighlightPayload.ofNetwork(
            60, List.of(new HighlightedContainer(BlockPos.ORIGIN, ContainerRole.GENERAL))));
    ContainerHighlightPayload.CODEC.decode(buffer);

    assertEquals(0, buffer.readableBytes());
  }

  @Test
  void anEmptyHighlightIsCarriedRatherThanRefused() {
    ContainerHighlightPayload empty = ContainerHighlightPayload.ofNetwork(100, List.of());

    assertTrue(empty.containers().isEmpty());
    assertEquals(empty, roundTrip(empty));
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, Integer.MIN_VALUE})
  void aLifetimeBelowNothingBecomesNothing(int ticks) {
    assertEquals(0, ContainerHighlightPayload.ofNetwork(ticks, List.of()).ticks());
  }

  @Test
  void moreContainersThanAPacketShouldCarryIsRefusedRatherThanTruncated() {
    List<HighlightedContainer> tooMany =
        IntStream.rangeClosed(0, ContainerHighlightPayload.MAX_CONTAINERS)
            .mapToObj(
                index -> new HighlightedContainer(new BlockPos(index, 0, 0), ContainerRole.GENERAL))
            .toList();

    assertThrows(
        IllegalArgumentException.class, () -> ContainerHighlightPayload.ofNetwork(100, tooMany));
  }
}
