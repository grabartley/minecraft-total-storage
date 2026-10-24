package com.grahambartley.totalstorage.network;

import com.grahambartley.totalstorage.TotalStorage;
import com.grahambartley.totalstorage.access.PlayerNameRule;
import com.grahambartley.totalstorage.access.TrustEditOutcome;
import java.util.Objects;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record TerminalTrustReplyPayload(int syncId, String player, TrustEditOutcome outcome)
    implements CustomPayload {
  public static final CustomPayload.Id<TerminalTrustReplyPayload> ID =
      new CustomPayload.Id<>(Identifier.of(TotalStorage.MOD_ID, "terminal_trust_reply"));

  public static final PacketCodec<RegistryByteBuf, TerminalTrustReplyPayload> CODEC =
      PacketCodec.of(TerminalTrustReplyPayload::write, TerminalTrustReplyPayload::read);

  public TerminalTrustReplyPayload {
    player = PlayerNameRule.clamp(player);
    Objects.requireNonNull(outcome, "outcome");
  }

  @Override
  public CustomPayload.Id<TerminalTrustReplyPayload> getId() {
    return ID;
  }

  private static void write(TerminalTrustReplyPayload payload, RegistryByteBuf buf) {
    buf.writeVarInt(payload.syncId());
    buf.writeString(payload.player(), PlayerNameRule.MAX_SHOWN_LENGTH);
    buf.writeEnumConstant(payload.outcome());
  }

  private static TerminalTrustReplyPayload read(RegistryByteBuf buf) {
    return new TerminalTrustReplyPayload(
        buf.readVarInt(),
        buf.readString(PlayerNameRule.MAX_SHOWN_LENGTH),
        buf.readEnumConstant(TrustEditOutcome.class));
  }
}
