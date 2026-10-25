package com.grahambartley.totalstorage.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.operation.ExpensiveOperation;
import com.grahambartley.totalstorage.operation.NetworkOperationPhase;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class NetworkOperationStatusPayloadTest {
  private static final int SYNC_ID = 3;

  @ParameterizedTest
  @EnumSource(NetworkOperationPhase.class)
  void aStatusSurvivesTheTripToTheClient(NetworkOperationPhase phase) {
    final NetworkOperationStatusPayload sent =
        new NetworkOperationStatusPayload(SYNC_ID, ExpensiveOperation.CONSOLIDATE, phase, 42);
    final RegistryByteBuf buffer = new RegistryByteBuf(Unpooled.buffer(), null);

    NetworkOperationStatusPayload.CODEC.encode(buffer, sent);

    assertEquals(sent, NetworkOperationStatusPayload.CODEC.decode(buffer));
  }

  @ParameterizedTest
  @CsvSource({"-5, 0", "0, 0", "57, 57", "100, 100", "250, 100"})
  void theProgressIsKeptToAPercentage(int sent, int kept) {
    assertEquals(
        kept,
        new NetworkOperationStatusPayload(
                SYNC_ID, ExpensiveOperation.SORT_NETWORK, NetworkOperationPhase.RUNNING, sent)
            .percentDone());
  }

  @Test
  void aStatusMustNameItsOperationAndPhase() {
    assertThrows(
        NullPointerException.class,
        () -> new NetworkOperationStatusPayload(SYNC_ID, null, NetworkOperationPhase.IDLE, 0));
    assertThrows(
        NullPointerException.class,
        () -> new NetworkOperationStatusPayload(SYNC_ID, ExpensiveOperation.SORT_NETWORK, null, 0));
  }

  @Test
  void aStatusIsNamedUnderTheModsOwnIdentifier() {
    assertTrue(NetworkOperationStatusPayload.ID.id().toString().startsWith("total-storage:"));
  }
}
