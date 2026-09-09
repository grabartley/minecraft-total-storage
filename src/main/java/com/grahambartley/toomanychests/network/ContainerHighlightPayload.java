package com.grahambartley.toomanychests.network;

import com.grahambartley.toomanychests.TooManyChests;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.terminal.HighlightedContainer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ContainerHighlightPayload(int ticks, List<HighlightedContainer> containers)
    implements CustomPayload {

  public static final int MAX_CONTAINERS = 256;

  public static final CustomPayload.Id<ContainerHighlightPayload> ID =
      new CustomPayload.Id<>(Identifier.of(TooManyChests.MOD_ID, "container_highlight"));

  public static final PacketCodec<RegistryByteBuf, ContainerHighlightPayload> CODEC =
      PacketCodec.of(ContainerHighlightPayload::write, ContainerHighlightPayload::read);

  public ContainerHighlightPayload {
    Objects.requireNonNull(containers, "containers");
    ticks = Math.max(0, ticks);
    containers = List.copyOf(containers);
    if (containers.size() > MAX_CONTAINERS) {
      throw new IllegalArgumentException(
          "a highlight may not carry " + containers.size() + " containers");
    }
  }

  @Override
  public CustomPayload.Id<ContainerHighlightPayload> getId() {
    return ID;
  }

  private static void write(ContainerHighlightPayload payload, RegistryByteBuf buf) {
    buf.writeVarInt(payload.ticks());
    buf.writeVarInt(payload.containers().size());
    for (final HighlightedContainer container : payload.containers()) {
      buf.writeBlockPos(container.container());
      buf.writeEnumConstant(container.role());
    }
  }

  private static ContainerHighlightPayload read(RegistryByteBuf buf) {
    final int ticks = buf.readVarInt();
    final int count = buf.readVarInt();
    if (count < 0 || count > MAX_CONTAINERS) {
      throw new IllegalArgumentException("a highlight carried " + count + " containers");
    }
    final List<HighlightedContainer> containers = new ArrayList<>(count);
    for (int entry = 0; entry < count; entry++) {
      containers.add(
          new HighlightedContainer(buf.readBlockPos(), buf.readEnumConstant(ContainerRole.class)));
    }
    return new ContainerHighlightPayload(ticks, containers);
  }
}
