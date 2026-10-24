package com.grahambartley.totalstorage.network;

import com.grahambartley.totalstorage.TotalStorage;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;

public record TerminalNoticePayload(int syncId, Text message, boolean needsAnAnswer)
    implements CustomPayload {
  public static final CustomPayload.Id<TerminalNoticePayload> ID =
      new CustomPayload.Id<>(Identifier.of(TotalStorage.MOD_ID, "terminal_notice"));

  public static final PacketCodec<RegistryByteBuf, TerminalNoticePayload> CODEC =
      PacketCodec.tuple(
          PacketCodecs.VAR_INT,
          TerminalNoticePayload::syncId,
          TextCodecs.REGISTRY_PACKET_CODEC,
          TerminalNoticePayload::message,
          PacketCodecs.BOOL,
          TerminalNoticePayload::needsAnAnswer,
          TerminalNoticePayload::new);

  @Override
  public CustomPayload.Id<TerminalNoticePayload> getId() {
    return ID;
  }
}
