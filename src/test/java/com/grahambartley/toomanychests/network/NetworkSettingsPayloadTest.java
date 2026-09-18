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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class NetworkSettingsPayloadTest {
  private static RegistryByteBuf buffer() {
    return new RegistryByteBuf(Unpooled.buffer(), null);
  }

  private static NetworkSettingsView view(TerminalAccessMode mode, List<ContainerRoleEntry> rows) {
    return NetworkSettingsView.answered(
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
        new BlockPos(x, 64, -x), "", role, List.of(), Optional.empty(), 3, 168L, 3, 27, 9.0);
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

  @ParameterizedTest
  @EnumSource(ContainerRole.class)
  void everyRoleReachesTheRowItBelongsTo(ContainerRole role) {
    NetworkSettingsPayload sent =
        new NetworkSettingsPayload(1, view(TerminalAccessMode.OPEN, List.of(row(4, role))));

    ContainerRoleEntry received = roundTrip(sent).view().containers().get(0);

    assertEquals(role, received.role());
    assertEquals(new BlockPos(4, 64, -4), received.container());
    assertEquals(3, received.variantCount());
    assertEquals(168L, received.itemCount());
  }

  @Test
  void whatOnlyTheServerNeedsIsNotCarried() {
    NetworkSettingsPayload sent =
        new NetworkSettingsPayload(
            1, view(TerminalAccessMode.OWNER, List.of(row(9, ContainerRole.GENERAL))));

    ContainerRoleEntry received = roundTrip(sent).view().containers().get(0);

    assertEquals(0.0, received.distanceSq());
  }

  @Test
  void howFullEachContainerIsReachesTheRowsThePanelDraws() {
    NetworkSettingsPayload sent =
        new NetworkSettingsPayload(
            1, view(TerminalAccessMode.OWNER, List.of(row(9, ContainerRole.GENERAL))));

    ContainerRoleEntry received = roundTrip(sent).view().containers().get(0);

    assertEquals(3, received.usedSlots());
    assertEquals(27, received.totalSlots());
  }

  @Test
  void aForgedRowUsingMoreSlotsThanItHasIsClampedRatherThanThrowing() {
    RegistryByteBuf buffer = buffer();
    writeHeader(buffer);
    buffer.writeVarInt(0);
    buffer.writeVarInt(0);
    buffer.writeVarInt(0);
    buffer.writeVarInt(1);
    buffer.writeVarInt(1);
    buffer.writeBlockPos(new BlockPos(1, 2, 3));
    buffer.writeString("");
    buffer.writeEnumConstant(ContainerRole.GENERAL);
    buffer.writeVarInt(0);
    buffer.writeVarInt(0);
    buffer.writeVarLong(0L);
    buffer.writeVarInt(900);
    buffer.writeVarInt(27);
    buffer.writeBoolean(false);

    ContainerRoleEntry received =
        NetworkSettingsPayload.CODEC.decode(buffer).view().containers().get(0);

    assertEquals(27, received.totalSlots());
    assertEquals(
        27,
        received.usedSlots(),
        "A forged used count should be clamped to the total, as a forged capacity is");
  }

  @Test
  void aPendingViewStaysPendingAcrossTheWire() {
    assertEquals(
        false,
        roundTrip(new NetworkSettingsPayload(1, NetworkSettingsView.pending())).view().answered());
  }

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

  private static void writeHeader(RegistryByteBuf buffer) {
    buffer.writeVarInt(1);
    buffer.writeBoolean(true);
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
