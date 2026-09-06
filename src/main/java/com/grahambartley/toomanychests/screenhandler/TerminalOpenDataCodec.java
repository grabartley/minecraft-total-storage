package com.grahambartley.toomanychests.screenhandler;

import com.grahambartley.toomanychests.tag.NetworkTagPacketCodec;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.math.BlockPos;

/**
 * The wire form of what a terminal hands its screen at the moment it opens.
 *
 * <p>A permission this build does not recognise decodes as {@link TerminalPermission#USE} rather
 * than refusing the packet, so a client one version behind gets a working terminal without the
 * administration controls instead of no terminal at all. Failing to the lesser permission is the
 * only safe direction, and it costs nothing because the server re-checks every operation anyway.
 */
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
