package com.grahambartley.toomanychests.network;

import com.grahambartley.toomanychests.TooManyChests;
import com.grahambartley.toomanychests.access.PlayerNameRule;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record TerminalTrustEditPayload(int syncId, String player, boolean trusted)
    implements CustomPayload {
  public static final CustomPayload.Id<TerminalTrustEditPayload> ID =
      new CustomPayload.Id<>(Identifier.of(TooManyChests.MOD_ID, "terminal_trust_edit"));

  public static final PacketCodec<RegistryByteBuf, TerminalTrustEditPayload> CODEC =
      PacketCodec.of(TerminalTrustEditPayload::write, TerminalTrustEditPayload::read);

  public TerminalTrustEditPayload {
    player = PlayerNameRule.clamp(player);
  }

  @Override
  public CustomPayload.Id<TerminalTrustEditPayload> getId() {
    return ID;
  }

  private static void write(TerminalTrustEditPayload payload, RegistryByteBuf buf) {
    buf.writeVarInt(payload.syncId());
    buf.writeString(payload.player(), PlayerNameRule.MAX_SHOWN_LENGTH);
    buf.writeBoolean(payload.trusted());
  }

  private static TerminalTrustEditPayload read(RegistryByteBuf buf) {
    return new TerminalTrustEditPayload(
        buf.readVarInt(), buf.readString(PlayerNameRule.MAX_SHOWN_LENGTH), buf.readBoolean());
  }
}
