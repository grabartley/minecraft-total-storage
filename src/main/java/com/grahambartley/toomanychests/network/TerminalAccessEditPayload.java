package com.grahambartley.toomanychests.network;

import com.grahambartley.toomanychests.TooManyChests;
import com.grahambartley.toomanychests.config.TerminalAccessMode;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record TerminalAccessEditPayload(int syncId, TerminalAccessMode mode)
    implements CustomPayload {
  public static final CustomPayload.Id<TerminalAccessEditPayload> ID =
      new CustomPayload.Id<>(Identifier.of(TooManyChests.MOD_ID, "terminal_access_edit"));

  public static final PacketCodec<RegistryByteBuf, TerminalAccessEditPayload> CODEC =
      PacketCodec.of(TerminalAccessEditPayload::write, TerminalAccessEditPayload::read);

  public TerminalAccessEditPayload {
    mode = mode == null ? TerminalAccessMode.OWNER : mode;
  }

  @Override
  public CustomPayload.Id<TerminalAccessEditPayload> getId() {
    return ID;
  }

  private static void write(TerminalAccessEditPayload payload, RegistryByteBuf buf) {
    buf.writeVarInt(payload.syncId());
    buf.writeEnumConstant(payload.mode());
  }

  private static TerminalAccessEditPayload read(RegistryByteBuf buf) {
    return new TerminalAccessEditPayload(
        buf.readVarInt(), buf.readEnumConstant(TerminalAccessMode.class));
  }
}
