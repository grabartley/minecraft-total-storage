package com.grahambartley.totalstorage.network;

import com.grahambartley.totalstorage.TotalStorage;
import com.grahambartley.totalstorage.operation.TerminalNoticeTone;
import java.util.Objects;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;

public record TerminalNoticePayload(
    int syncId, TerminalNoticeTone tone, Text headline, Text message) implements CustomPayload {
  public static final CustomPayload.Id<TerminalNoticePayload> ID =
      new CustomPayload.Id<>(Identifier.of(TotalStorage.MOD_ID, "terminal_notice"));

  public static final PacketCodec<RegistryByteBuf, TerminalNoticePayload> CODEC =
      PacketCodec.of(TerminalNoticePayload::write, TerminalNoticePayload::read);

  public TerminalNoticePayload {
    Objects.requireNonNull(tone, "tone");
    Objects.requireNonNull(message, "message");
    headline = headline == null ? message : headline;
  }

  @Override
  public CustomPayload.Id<TerminalNoticePayload> getId() {
    return ID;
  }

  private static void write(TerminalNoticePayload payload, RegistryByteBuf buf) {
    buf.writeVarInt(payload.syncId());
    buf.writeEnumConstant(payload.tone());
    TextCodecs.REGISTRY_PACKET_CODEC.encode(buf, payload.headline());
    TextCodecs.REGISTRY_PACKET_CODEC.encode(buf, payload.message());
  }

  private static TerminalNoticePayload read(RegistryByteBuf buf) {
    return new TerminalNoticePayload(
        buf.readVarInt(),
        buf.readEnumConstant(TerminalNoticeTone.class),
        TextCodecs.REGISTRY_PACKET_CODEC.decode(buf),
        TextCodecs.REGISTRY_PACKET_CODEC.decode(buf));
  }
}
