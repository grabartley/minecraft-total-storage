package com.grahambartley.toomanychests.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.grahambartley.toomanychests.config.TerminalAccessMode;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.terminal.ContainerRoleEntry;
import com.grahambartley.toomanychests.terminal.NetworkCapacity;
import com.grahambartley.toomanychests.terminal.NetworkSettingsView;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

/**
 * What a settings answer looks like once it has been across the wire.
 *
 * <p>Every row here is sent without an icon, because building an {@code ItemVariant} needs the
 * registries a plain JVM does not have. What an iconed row does on the wire is proven in {@code
 * NetworkSettingsPayloadGameTest} instead.
 */
class NetworkSettingsPayloadTest {

  private static RegistryByteBuf buffer() {
    return new RegistryByteBuf(Unpooled.buffer(), null);
  }

  private static NetworkSettingsView view(TerminalAccessMode mode, List<ContainerRoleEntry> rows) {
    return new NetworkSettingsView(
        24,
        16,
        32,
        mode,
        true,
        true,
        List.of("Alice", "Bob"),
        new NetworkCapacity(12, 40),
        7,
        40,
        rows);
  }

  private static ContainerRoleEntry row(int x, ContainerRole role) {
    return new ContainerRoleEntry(
        new BlockPos(x, 64, -x), role, Optional.empty(), 3, 168L, 3, 27, 9.0);
  }

  private static NetworkSettingsPayload roundTrip(NetworkSettingsPayload payload) {
    RegistryByteBuf buffer = buffer();
    NetworkSettingsPayload.CODEC.encode(buffer, payload);
    return NetworkSettingsPayload.CODEC.decode(buffer);
  }

  @Test
  void everythingThePanelDrawsSurvivesTheWire() {
    NetworkSettingsPayload sent =
        new NetworkSettingsPayload(
            5,
            view(
                TerminalAccessMode.TRUSTED,
                List.of(row(1, ContainerRole.GENERAL), row(2, ContainerRole.EXCLUDED))));

    NetworkSettingsView received = roundTrip(sent).view();

    assertEquals(24, received.radius());
    assertEquals(16, received.effectiveRadius());
    assertEquals(32, received.maxRadius());
    assertEquals(TerminalAccessMode.TRUSTED, received.accessMode());
    assertEquals(List.of("Alice", "Bob"), received.trusted());
    assertEquals(7, received.containerOffset());
    assertEquals(40, received.containerTotal());
    assertEquals(2, received.containers().size());
    assertEquals(new NetworkCapacity(12, 40), received.capacity());
  }

  /**
   * A used count above the total could only be forged, and a record that refused to hold one would
   * drop the connection over a number nobody draws. The bytes are written by hand because a payload
   * built through the record cannot carry the case: its own constructor refuses it first.
   */
  @Test
  void aForgedCapacityIsClampedRatherThanTakingTheConnectionDown() {
    RegistryByteBuf buffer = buffer();
    writeHeader(buffer);
    buffer.writeVarInt(900);
    buffer.writeVarInt(27);
    buffer.writeVarInt(0);
    buffer.writeVarInt(0);
    buffer.writeVarInt(0);

    NetworkCapacity received = NetworkSettingsPayload.CODEC.decode(buffer).view().capacity();

    assertEquals(27, received.totalSlots());
    assertEquals(27, received.usedSlots(), "A forged used count should be clamped to the total");
  }

  /** A negative count is the other way the numbers could arrive impossible. */
  @Test
  void aForgedNegativeCapacityBecomesNothingRatherThanThrowing() {
    RegistryByteBuf buffer = buffer();
    writeHeader(buffer);
    buffer.writeVarInt(-5);
    buffer.writeVarInt(-9);
    buffer.writeVarInt(0);
    buffer.writeVarInt(0);
    buffer.writeVarInt(0);

    NetworkCapacity received = NetworkSettingsPayload.CODEC.decode(buffer).view().capacity();

    assertEquals(0, received.usedSlots());
    assertEquals(0, received.totalSlots());
  }

  /** Everything a settings answer carries before its capacity, so a case can start from there. */
  private static void writeHeader(RegistryByteBuf buffer) {
    buffer.writeVarInt(1);
    buffer.writeVarInt(16);
    buffer.writeVarInt(16);
    buffer.writeVarInt(32);
    buffer.writeEnumConstant(TerminalAccessMode.OWNER);
    buffer.writeBoolean(true);
    buffer.writeBoolean(true);
    buffer.writeVarInt(0);
  }

  @Test
  void theWholePayloadIsReadBackWithNothingLeftOver() {
    RegistryByteBuf buffer = buffer();

    NetworkSettingsPayload.CODEC.encode(
        buffer, new NetworkSettingsPayload(2, view(TerminalAccessMode.OWNER, List.of())));
    NetworkSettingsPayload.CODEC.decode(buffer);

    assertEquals(0, buffer.readableBytes());
  }

  @Test
  void aPanelWithoutAViewIsRefusedRatherThanSent() {
    assertThrows(NullPointerException.class, () -> new NetworkSettingsPayload(1, null));
  }
}
