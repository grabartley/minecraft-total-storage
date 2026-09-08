package com.grahambartley.toomanychests.network;

import com.grahambartley.toomanychests.TooManyChests;
import com.grahambartley.toomanychests.storage.ContainerRole;
import java.util.Objects;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * A player giving one container a role from the terminal's settings panel.
 *
 * <p>This is the one request in the mod that names a block position, and it is not the thing ADR
 * 0009 forbids. That rule keeps a client from choosing where items come from; here the position is
 * the subject of the change rather than a route to somebody's items, and the server refuses any
 * container its network does not already reach.
 *
 * <p>Naming a position it should not reach therefore gains nothing: the worst a forged packet can
 * do is have its own terminal refuse it.
 */
public record ContainerRoleEditPayload(int syncId, BlockPos container, ContainerRole role)
    implements CustomPayload {
  public static final CustomPayload.Id<ContainerRoleEditPayload> ID =
      new CustomPayload.Id<>(Identifier.of(TooManyChests.MOD_ID, "container_role_edit"));

  public static final PacketCodec<RegistryByteBuf, ContainerRoleEditPayload> CODEC =
      PacketCodec.of(ContainerRoleEditPayload::write, ContainerRoleEditPayload::read);

  public ContainerRoleEditPayload {
    Objects.requireNonNull(container, "container");
    container = container.toImmutable();
    role = role == null ? ContainerRole.DEFAULT : role;
  }

  @Override
  public CustomPayload.Id<ContainerRoleEditPayload> getId() {
    return ID;
  }

  private static void write(ContainerRoleEditPayload payload, RegistryByteBuf buf) {
    buf.writeVarInt(payload.syncId());
    buf.writeBlockPos(payload.container());
    buf.writeEnumConstant(payload.role());
  }

  private static ContainerRoleEditPayload read(RegistryByteBuf buf) {
    return new ContainerRoleEditPayload(
        buf.readVarInt(), buf.readBlockPos(), buf.readEnumConstant(ContainerRole.class));
  }
}
