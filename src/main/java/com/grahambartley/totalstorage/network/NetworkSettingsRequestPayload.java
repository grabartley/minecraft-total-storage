package com.grahambartley.totalstorage.network;

import com.grahambartley.totalstorage.TotalStorage;
import com.grahambartley.totalstorage.listing.ListingWindow;
import com.grahambartley.totalstorage.terminal.NetworkSettingsView;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record NetworkSettingsRequestPayload(int syncId, int offset, int length)
    implements CustomPayload {
  public static final CustomPayload.Id<NetworkSettingsRequestPayload> ID =
      new CustomPayload.Id<>(Identifier.of(TotalStorage.MOD_ID, "network_settings_request"));

  public static final PacketCodec<RegistryByteBuf, NetworkSettingsRequestPayload> CODEC =
      PacketCodec.of(NetworkSettingsRequestPayload::write, NetworkSettingsRequestPayload::read);

  public NetworkSettingsRequestPayload {
    offset = Math.max(0, offset);
    length = Math.max(0, Math.min(NetworkSettingsView.MAX_CONTAINERS, length));
  }

  public static NetworkSettingsRequestPayload showing(int syncId, int offset, int length) {
    return new NetworkSettingsRequestPayload(syncId, offset, length);
  }

  public ListingWindow window() {
    return new ListingWindow(offset, length);
  }

  @Override
  public CustomPayload.Id<NetworkSettingsRequestPayload> getId() {
    return ID;
  }

  private static void write(NetworkSettingsRequestPayload payload, RegistryByteBuf buf) {
    buf.writeVarInt(payload.syncId());
    buf.writeVarInt(payload.offset());
    buf.writeVarInt(payload.length());
  }

  private static NetworkSettingsRequestPayload read(RegistryByteBuf buf) {
    return new NetworkSettingsRequestPayload(buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
  }
}
