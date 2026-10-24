package com.grahambartley.totalstorage.network;

import com.grahambartley.totalstorage.TotalStorage;
import com.grahambartley.totalstorage.terminal.TerminalSettings;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record TerminalRadiusEditPayload(int syncId, int blocks) implements CustomPayload {
  public static final CustomPayload.Id<TerminalRadiusEditPayload> ID =
      new CustomPayload.Id<>(Identifier.of(TotalStorage.MOD_ID, "terminal_radius_edit"));

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
