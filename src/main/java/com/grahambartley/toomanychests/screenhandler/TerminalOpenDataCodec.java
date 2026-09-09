package com.grahambartley.toomanychests.screenhandler;

import com.grahambartley.toomanychests.tag.NetworkTagPacketCodec;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.math.BlockPos;

public final class TerminalOpenDataCodec {
  public static final PacketCodec<RegistryByteBuf, TerminalOpenData> INSTANCE =
      PacketCodec.of(TerminalOpenDataCodec::write, TerminalOpenDataCodec::read);

  private TerminalOpenDataCodec() {}

  private static void write(TerminalOpenData data, RegistryByteBuf buf) {
    buf.writeBlockPos(data.terminal());
    buf.writeVarInt(data.radius());
    buf.writeString(data.permission().id());
    NetworkTagPacketCodec.INSTANCE.encode(buf, data.tags());
  }

  private static TerminalOpenData read(RegistryByteBuf buf) {
    final BlockPos terminal = buf.readBlockPos();
    final int radius = buf.readVarInt();
    final TerminalPermission permission =
        TerminalPermission.fromId(buf.readString()).orElse(TerminalPermission.USE);
    return new TerminalOpenData(
        terminal, radius, permission, NetworkTagPacketCodec.INSTANCE.decode(buf));
  }
}
