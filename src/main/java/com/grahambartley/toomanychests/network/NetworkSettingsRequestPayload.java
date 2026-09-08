package com.grahambartley.toomanychests.network;

import com.grahambartley.toomanychests.TooManyChests;
import com.grahambartley.toomanychests.listing.ListingWindow;
import com.grahambartley.toomanychests.terminal.NetworkSettingsView;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * A settings panel asking for the terminal it is drawing.
 *
 * <p>Carries the rows of the container list it is about to show and nothing else. Everything the
 * panel displays is decided on the server, including whether the caller may change any of it, so
 * there is nothing here for a client to assert about itself.
 *
 * <p>The sync id names which screen is asking and is checked against the handler the server already
 * holds for that player, so the request cannot reach a terminal the sender does not have open.
 */
public record NetworkSettingsRequestPayload(int syncId, int offset, int length)
    implements CustomPayload {

  public static final CustomPayload.Id<NetworkSettingsRequestPayload> ID =
      new CustomPayload.Id<>(Identifier.of(TooManyChests.MOD_ID, "network_settings_request"));

  public static final PacketCodec<RegistryByteBuf, NetworkSettingsRequestPayload> CODEC =
      PacketCodec.of(NetworkSettingsRequestPayload::write, NetworkSettingsRequestPayload::read);

  public NetworkSettingsRequestPayload {
    offset = Math.max(0, offset);
    length = Math.max(0, Math.min(NetworkSettingsView.MAX_CONTAINERS, length));
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
