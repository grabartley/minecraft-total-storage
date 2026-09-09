package com.grahambartley.toomanychests.network;

import com.grahambartley.toomanychests.TooManyChests;
import com.grahambartley.toomanychests.config.TerminalAccessMode;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.terminal.ContainerRoleEntry;
import com.grahambartley.toomanychests.terminal.NetworkCapacity;
import com.grahambartley.toomanychests.terminal.NetworkSettingsView;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record NetworkSettingsPayload(int syncId, NetworkSettingsView view)
    implements CustomPayload {
  public static final CustomPayload.Id<NetworkSettingsPayload> ID =
      new CustomPayload.Id<>(Identifier.of(TooManyChests.MOD_ID, "network_settings"));

  public static final PacketCodec<RegistryByteBuf, NetworkSettingsPayload> CODEC =
      PacketCodec.of(NetworkSettingsPayload::write, NetworkSettingsPayload::read);

  private static final int MAX_NAME_LENGTH = 64;

  public NetworkSettingsPayload {
    Objects.requireNonNull(view, "view");
  }

  @Override
  public CustomPayload.Id<NetworkSettingsPayload> getId() {
    return ID;
  }

  private static void write(NetworkSettingsPayload payload, RegistryByteBuf buf) {
    final NetworkSettingsView view = payload.view();
    buf.writeVarInt(payload.syncId());
    buf.writeVarInt(view.radius());
    buf.writeVarInt(view.effectiveRadius());
    buf.writeVarInt(view.maxRadius());
    buf.writeEnumConstant(view.accessMode());
    buf.writeBoolean(view.mayAdminister());
    buf.writeBoolean(view.powered());
    buf.writeVarInt(view.trusted().size());
    for (final String name : view.trusted()) {
      buf.writeString(name, MAX_NAME_LENGTH);
    }
    buf.writeVarInt(view.capacity().usedSlots());
    buf.writeVarInt(view.capacity().totalSlots());
    buf.writeVarInt(view.containerOffset());
    buf.writeVarInt(view.containerTotal());
    buf.writeVarInt(view.containers().size());
    for (final ContainerRoleEntry row : view.containers()) {
      buf.writeBlockPos(row.container());
      buf.writeEnumConstant(row.role());
      buf.writeVarInt(row.variantCount());
      buf.writeVarLong(row.itemCount());
      buf.writeBoolean(row.icon().isPresent());
      row.icon().ifPresent(icon -> ItemVariant.PACKET_CODEC.encode(buf, icon));
    }
  }

  private static NetworkSettingsPayload read(RegistryByteBuf buf) {
    final int syncId = buf.readVarInt();
    final int radius = buf.readVarInt();
    final int effectiveRadius = buf.readVarInt();
    final int maxRadius = buf.readVarInt();
    final TerminalAccessMode mode = buf.readEnumConstant(TerminalAccessMode.class);
    final boolean mayAdminister = buf.readBoolean();
    final boolean powered = buf.readBoolean();
    final List<String> trusted = readTrusted(buf);
    final NetworkCapacity capacity = readCapacity(buf);
    final int offset = buf.readVarInt();
    final int total = buf.readVarInt();
    final List<ContainerRoleEntry> containers = readContainers(buf);
    return new NetworkSettingsPayload(
        syncId,
        new NetworkSettingsView(
            radius,
            effectiveRadius,
            maxRadius,
            mode,
            mayAdminister,
            powered,
            trusted,
            capacity,
            offset,
            total,
            containers));
  }

  private static NetworkCapacity readCapacity(RegistryByteBuf buf) {
    final int used = Math.max(0, buf.readVarInt());
    final int total = Math.max(0, buf.readVarInt());
    return new NetworkCapacity(Math.min(used, total), total);
  }

  private static List<String> readTrusted(RegistryByteBuf buf) {
    final int count = buf.readVarInt();
    if (count < 0 || count > NetworkSettingsView.MAX_TRUSTED) {
      throw new IllegalArgumentException("settings named " + count + " trusted players");
    }
    final List<String> trusted = new ArrayList<>(count);
    for (int entry = 0; entry < count; entry++) {
      trusted.add(buf.readString(MAX_NAME_LENGTH));
    }
    return trusted;
  }

  private static List<ContainerRoleEntry> readContainers(RegistryByteBuf buf) {
    final int count = buf.readVarInt();
    if (count < 0 || count > NetworkSettingsView.MAX_CONTAINERS) {
      throw new IllegalArgumentException("settings carried " + count + " containers");
    }
    final List<ContainerRoleEntry> containers = new ArrayList<>(count);
    for (int row = 0; row < count; row++) {
      final BlockPos container = buf.readBlockPos();
      final ContainerRole role = buf.readEnumConstant(ContainerRole.class);
      final int variants = buf.readVarInt();
      final long items = buf.readVarLong();
      final Optional<ItemVariant> icon =
          buf.readBoolean() ? Optional.of(ItemVariant.PACKET_CODEC.decode(buf)) : Optional.empty();
      containers.add(new ContainerRoleEntry(container, role, icon, variants, items, 0, 0, 0.0));
    }
    return containers;
  }
}
