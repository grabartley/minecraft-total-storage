package com.grahambartley.toomanychests.network;

import com.grahambartley.toomanychests.TooManyChests;
import com.grahambartley.toomanychests.terminal.TerminalAction;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record TerminalActionPayload(int syncId, TerminalAction action) implements CustomPayload {
  public static final CustomPayload.Id<TerminalActionPayload> ID =
      new CustomPayload.Id<>(Identifier.of(TooManyChests.MOD_ID, "terminal_action"));

  public static final PacketCodec<RegistryByteBuf, TerminalActionPayload> CODEC =
      PacketCodec.of(TerminalActionPayload::write, TerminalActionPayload::read);

  public TerminalActionPayload {
    action = action == null ? TerminalAction.HIGHLIGHT : action;
  }

  @Override
  public CustomPayload.Id<TerminalActionPayload> getId() {
    return ID;
  }

  private static void write(TerminalActionPayload payload, RegistryByteBuf buf) {
    buf.writeVarInt(payload.syncId());
    buf.writeEnumConstant(payload.action());
  }

  private static TerminalActionPayload read(RegistryByteBuf buf) {
    return new TerminalActionPayload(buf.readVarInt(), buf.readEnumConstant(TerminalAction.class));
  }
}
