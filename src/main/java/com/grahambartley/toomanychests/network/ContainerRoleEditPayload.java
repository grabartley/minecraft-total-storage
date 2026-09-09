package com.grahambartley.toomanychests.network;

import com.grahambartley.toomanychests.TooManyChests;
import com.grahambartley.toomanychests.storage.ContainerRole;
import java.util.Objects;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

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
