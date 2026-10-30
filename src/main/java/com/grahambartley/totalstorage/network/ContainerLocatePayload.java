package com.grahambartley.totalstorage.network;

import com.grahambartley.totalstorage.TotalStorage;
import java.util.Objects;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record ContainerLocatePayload(int syncId, BlockPos container) implements CustomPayload {
  public static final CustomPayload.Id<ContainerLocatePayload> ID =
      new CustomPayload.Id<>(Identifier.of(TotalStorage.MOD_ID, "container_locate"));

  public static final PacketCodec<RegistryByteBuf, ContainerLocatePayload> CODEC =
      PacketCodec.of(ContainerLocatePayload::write, ContainerLocatePayload::read);

  public ContainerLocatePayload {
    Objects.requireNonNull(container, "container");
    container = container.toImmutable();
  }

  @Override
  public CustomPayload.Id<ContainerLocatePayload> getId() {
    return ID;
  }

  private static void write(ContainerLocatePayload payload, RegistryByteBuf buf) {
    buf.writeVarInt(payload.syncId());
    buf.writeBlockPos(payload.container());
  }

  private static ContainerLocatePayload read(RegistryByteBuf buf) {
    return new ContainerLocatePayload(buf.readVarInt(), buf.readBlockPos());
  }
}
