package com.grahambartley.toomanychests.network;

import com.grahambartley.toomanychests.TooManyChests;
import com.grahambartley.toomanychests.terminal.TerminalSettings;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * A player moving one terminal's reach from its settings panel.
 *
 * <p>Bounded to the range a radius can take while decoding, and clamped again server-side against
 * the live server maximum per ADR 0021. This bound only stops a packet naming a number that would
 * be absurd to walk at all; the maximum that really applies is the server's, not this one.
 */
public record TerminalRadiusEditPayload(int syncId, int blocks) implements CustomPayload {
  public static final CustomPayload.Id<TerminalRadiusEditPayload> ID =
      new CustomPayload.Id<>(Identifier.of(TooManyChests.MOD_ID, "terminal_radius_edit"));

  public static final PacketCodec<RegistryByteBuf, TerminalRadiusEditPayload> CODEC =
      PacketCodec.of(TerminalRadiusEditPayload::write, TerminalRadiusEditPayload::read);

  public TerminalRadiusEditPayload {
    blocks = Math.max(TerminalSettings.MIN_RADIUS, Math.min(TerminalSettings.MAX_RADIUS, blocks));
  }

  @Override
  public CustomPayload.Id<TerminalRadiusEditPayload> getId() {
    return ID;
  }

  private static void write(TerminalRadiusEditPayload payload, RegistryByteBuf buf) {
    buf.writeVarInt(payload.syncId());
    buf.writeVarInt(payload.blocks());
  }

  private static TerminalRadiusEditPayload read(RegistryByteBuf buf) {
    return new TerminalRadiusEditPayload(buf.readVarInt(), buf.readVarInt());
  }
}
