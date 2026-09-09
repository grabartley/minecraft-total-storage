package com.grahambartley.toomanychests.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.toomanychests.operation.DepositSource;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class DepositRequestPayloadTest {
  private static RegistryByteBuf buffer() {
    return new RegistryByteBuf(Unpooled.buffer(), null);
  }

  private static DepositRequestPayload roundTrip(DepositRequestPayload payload) {
    RegistryByteBuf buffer = buffer();
    DepositRequestPayload.CODEC.encode(buffer, payload);
    return DepositRequestPayload.CODEC.decode(buffer);
  }

  @ParameterizedTest
  @EnumSource(DepositSource.class)
  void everySourceSurvivesTheWireUnchanged(DepositSource source) {
    DepositRequestPayload sent = new DepositRequestPayload(7, source, 21, 32);

    assertEquals(sent, roundTrip(sent));
  }

  @Test
  void theWholePayloadIsReadBackWithNothingLeftOver() {
    RegistryByteBuf buffer = buffer();

    DepositRequestPayload.CODEC.encode(buffer, DepositRequestPayload.wholeSlot(3, 12));
    DepositRequestPayload.CODEC.decode(buffer);

    assertEquals(0, buffer.readableBytes());
  }

  @ParameterizedTest
  @ValueSource(ints = {Integer.MAX_VALUE, 1000, DepositRequestPayload.MAX_AMOUNT + 1})
  void anAmountLargerThanAStackIsClampedToOne(int amount) {
    assertEquals(
        DepositRequestPayload.MAX_AMOUNT,
        new DepositRequestPayload(1, DepositSource.CURSOR, 0, amount).amount());
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, Integer.MIN_VALUE})
  void anAmountBelowNothingBecomesNothing(int wrong) {
    assertEquals(0, new DepositRequestPayload(1, DepositSource.SLOT, 0, wrong).amount());
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, Integer.MIN_VALUE, 36, 40})
  void anIndexOutsideTheLockableRangeIsCarriedRatherThanClampedIntoIt(int outside) {
    assertEquals(outside, new DepositRequestPayload(1, DepositSource.SLOT, outside, 1).slot());
  }

  @Test
  void aRequestNamingNoSourceDefaultsToTheCursor() {
    assertEquals(DepositSource.CURSOR, new DepositRequestPayload(1, null, 0, 1).source());
  }

  @Test
  void aWholeStackRequestAsksForMoreThanAnySlotCouldHold() {
    assertEquals(DepositRequestPayload.MAX_AMOUNT, DepositRequestPayload.wholeSlot(1, 4).amount());
    assertEquals(DepositRequestPayload.MAX_AMOUNT, DepositRequestPayload.wholeCursor(1).amount());
    assertEquals(1, DepositRequestPayload.oneFromCursor(1).amount());
  }

  @Test
  void theGesturesNameTheSourceTheyTakeFrom() {
    assertEquals(DepositSource.SLOT, DepositRequestPayload.wholeSlot(1, 4).source());
    assertEquals(4, DepositRequestPayload.wholeSlot(1, 4).slot());
    assertEquals(DepositSource.CURSOR, DepositRequestPayload.wholeCursor(1).source());
    assertEquals(DepositSource.CURSOR, DepositRequestPayload.oneFromCursor(1).source());
  }
}
