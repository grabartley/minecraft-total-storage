package com.grahambartley.totalstorage.network;

import com.grahambartley.totalstorage.TotalStorage;
import com.grahambartley.totalstorage.config.TerminalAccessMode;
import com.grahambartley.totalstorage.storage.ContainerNameRule;
import com.grahambartley.totalstorage.storage.ContainerRole;
import com.grahambartley.totalstorage.tag.TagNameRule;
import com.grahambartley.totalstorage.terminal.ContainerRoleEntry;
import com.grahambartley.totalstorage.terminal.NetworkCapacity;
import com.grahambartley.totalstorage.terminal.NetworkSettingsView;
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
      new CustomPayload.Id<>(Identifier.of(TotalStorage.MOD_ID, "network_settings"));

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
    buf.writeBoolean(view.answered());
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
      buf.writeString(row.name(), ContainerNameRule.MAX_LENGTH);
      buf.writeEnumConstant(row.role());
      buf.writeVarInt(row.acceptedTags().size());
      for (final String tagId : row.acceptedTags()) {
        buf.writeString(tagId, TagNameRule.MAX_LENGTH);
      }
      buf.writeVarInt(row.variantCount());
      buf.writeVarLong(row.itemCount());
      buf.writeVarInt(row.usedSlots());
      buf.writeVarInt(row.totalSlots());
      buf.writeBoolean(row.icon().isPresent());
      row.icon().ifPresent(icon -> ItemVariant.PACKET_CODEC.encode(buf, icon));
    }
  }

  private static NetworkSettingsPayload read(RegistryByteBuf buf) {
    final int syncId = buf.readVarInt();
    final boolean answered = buf.readBoolean();
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
            answered,
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

  private static List<String> readAcceptedTags(RegistryByteBuf buf) {
    final int count = buf.readVarInt();
    if (count < 0 || count > ContainerRoleEntry.MAX_ACCEPTED_TAGS) {
      throw new IllegalArgumentException("a container row named " + count + " accepted tags");
    }
    final List<String> accepted = new ArrayList<>(count);
    for (int entry = 0; entry < count; entry++) {
      accepted.add(buf.readString(TagNameRule.MAX_LENGTH));
    }
    return accepted;
  }

  private static List<ContainerRoleEntry> readContainers(RegistryByteBuf buf) {
    final int count = buf.readVarInt();
    if (count < 0 || count > NetworkSettingsView.MAX_CONTAINERS) {
      throw new IllegalArgumentException("settings carried " + count + " containers");
    }
    final List<ContainerRoleEntry> containers = new ArrayList<>(count);
    for (int row = 0; row < count; row++) {
      final BlockPos container = buf.readBlockPos();
      final String name = buf.readString(ContainerNameRule.MAX_LENGTH);
      final ContainerRole role = buf.readEnumConstant(ContainerRole.class);
      final List<String> acceptedTags = readAcceptedTags(buf);
      final int variants = buf.readVarInt();
      final long items = buf.readVarLong();
      final int usedSlots = Math.max(0, buf.readVarInt());
      final int totalSlots = Math.max(0, buf.readVarInt());
      final Optional<ItemVariant> icon =
          buf.readBoolean() ? Optional.of(ItemVariant.PACKET_CODEC.decode(buf)) : Optional.empty();
      containers.add(
          new ContainerRoleEntry(
              container,
              name,
              role,
              acceptedTags,
              icon,
              variants,
              items,
              Math.min(usedSlots, totalSlots),
              totalSlots,
              0.0));
    }
    return containers;
  }
}
