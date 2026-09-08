package com.grahambartley.toomanychests.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.grahambartley.toomanychests.storage.ContainerRole;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ContainerRoleEditPayloadTest {

  @ParameterizedTest
  @EnumSource(ContainerRole.class)
  void everyRoleReachesTheContainerItNames(ContainerRole role) {
    ContainerRoleEditPayload sent =
        new ContainerRoleEditPayload(2, new BlockPos(-40, 12, 300), role);
    RegistryByteBuf buffer = new RegistryByteBuf(Unpooled.buffer(), null);

    ContainerRoleEditPayload.CODEC.encode(buffer, sent);

    assertEquals(sent, ContainerRoleEditPayload.CODEC.decode(buffer));
    assertEquals(0, buffer.readableBytes());
  }

  @Test
  void aRequestNamingNoRoleDefaultsToTheOneAContainerStartsWith() {
    assertEquals(
        ContainerRole.DEFAULT, new ContainerRoleEditPayload(1, BlockPos.ORIGIN, null).role());
  }

  @Test
  void aRequestNamingNoContainerIsRefusedRatherThanSent() {
    assertThrows(
        NullPointerException.class,
        () -> new ContainerRoleEditPayload(1, null, ContainerRole.GENERAL));
  }

  /**
   * Discovery scans reuse a mutable position, so one kept by a payload would drift as they walk.
   */
  @Test
  void aMutablePositionIsCopiedRatherThanHeld() {
    BlockPos.Mutable moving = new BlockPos.Mutable(1, 2, 3);
    ContainerRoleEditPayload payload =
        new ContainerRoleEditPayload(1, moving, ContainerRole.GENERAL);

    moving.set(9, 9, 9);

    assertEquals(new BlockPos(1, 2, 3), payload.container());
  }
}
