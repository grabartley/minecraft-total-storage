package com.grahambartley.toomanychests.network;

import com.grahambartley.toomanychests.TooManyChests;
import com.grahambartley.toomanychests.config.TerminalAccessMode;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * A player moving one terminal's access mode from its settings panel.
 *
 * <p>Per ADR 0008 only the owner and operators may do this, whatever mode the terminal is in. An
 * open terminal letting everybody in is not a way for any of them to lock it again, so the check
 * happens where the packet lands rather than being inferred from the fact that the screen opened.
 */
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
