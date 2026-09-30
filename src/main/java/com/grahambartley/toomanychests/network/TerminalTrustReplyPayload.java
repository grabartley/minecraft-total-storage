package com.grahambartley.toomanychests.network;

import com.grahambartley.toomanychests.TooManyChests;
import com.grahambartley.toomanychests.access.PlayerNameRule;
import com.grahambartley.toomanychests.access.TrustEditOutcome;
import java.util.Objects;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record TerminalTrustReplyPayload(int syncId, String player, TrustEditOutcome outcome)
    implements CustomPayload {
  public static final CustomPayload.Id<TerminalTrustReplyPayload> ID =
      new CustomPayload.Id<>(Identifier.of(TooManyChests.MOD_ID, "terminal_trust_reply"));

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
