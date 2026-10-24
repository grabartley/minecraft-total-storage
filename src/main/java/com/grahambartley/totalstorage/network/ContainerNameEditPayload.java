package com.grahambartley.totalstorage.network;

import com.grahambartley.totalstorage.TotalStorage;
import com.grahambartley.totalstorage.storage.ContainerNameRule;
import java.util.Objects;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record ContainerNameEditPayload(int syncId, BlockPos container, String name)
    implements CustomPayload {
  public static final CustomPayload.Id<ContainerNameEditPayload> ID =
      new CustomPayload.Id<>(Identifier.of(TotalStorage.MOD_ID, "container_name_edit"));

  public static final PacketCodec<RegistryByteBuf, ContainerNameEditPayload> CODEC =
      PacketCodec.of(ContainerNameEditPayload::write, ContainerNameEditPayload::read);

  public ContainerNameEditPayload {
    Objects.requireNonNull(container, "container");
    container = container.toImmutable();
    name = ContainerNameRule.sanitise(name);
  }

  @Override
  public CustomPayload.Id<ContainerNameEditPayload> getId() {
    return ID;
  }

  private static void write(ContainerNameEditPayload payload, RegistryByteBuf buf) {
    buf.writeVarInt(payload.syncId());
    buf.writeBlockPos(payload.container());
    buf.writeString(payload.name(), ContainerNameRule.MAX_LENGTH);
  }

  private static ContainerNameEditPayload read(RegistryByteBuf buf) {
    return new ContainerNameEditPayload(
        buf.readVarInt(), buf.readBlockPos(), buf.readString(ContainerNameRule.MAX_LENGTH));
  }
}
