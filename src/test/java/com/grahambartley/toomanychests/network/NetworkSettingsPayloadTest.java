package com.grahambartley.toomanychests.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.grahambartley.toomanychests.config.TerminalAccessMode;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.terminal.ContainerRoleEntry;
import com.grahambartley.toomanychests.terminal.NetworkSettingsView;
import com.grahambartley.toomanychests.terminal.RoleTally;
import io.netty.buffer.Unpooled;
import java.util.List;
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
    return new NetworkSettingsView(
        24, 16, 32, mode, true, true, List.of("Alice", "Bob"), new RoleTally(3, 2, 1), 7, 40, rows);
  }

  private static ContainerRoleEntry row(int x, ContainerRole role) {
    return new ContainerRoleEntry(new BlockPos(x, 64, -x), role, 3, 168L, 9.0);
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
    assertEquals(new RoleTally(3, 2, 1), received.tally());
    assertEquals(7, received.containerOffset());
    assertEquals(40, received.containerTotal());
    assertEquals(2, received.containers().size());
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

  /** The distance orders the list on the server and has no use on the client, so it is not sent. */
  @Test
  void theDistanceIsNotCarried() {
    NetworkSettingsPayload sent =
        new NetworkSettingsPayload(
            1, view(TerminalAccessMode.OWNER, List.of(row(9, ContainerRole.GENERAL))));

    assertEquals(0.0, roundTrip(sent).view().containers().get(0).distanceSq());
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
