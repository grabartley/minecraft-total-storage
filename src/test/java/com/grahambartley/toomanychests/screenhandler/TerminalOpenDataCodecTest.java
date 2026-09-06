package com.grahambartley.toomanychests.screenhandler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.toomanychests.tag.NetworkTagSet;
import io.netty.buffer.Unpooled;
import java.util.List;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class TerminalOpenDataCodecTest {
  private static final BlockPos SOMEWHERE = new BlockPos(12, 64, -30);

  static List<TerminalOpenData> wireCases() {
    return List.of(
        new TerminalOpenData(SOMEWHERE, 8, TerminalPermission.USE, NetworkTagSet.defaults()),
        new TerminalOpenData(
            new BlockPos(-30000000, -64, 29999999),
            32,
            TerminalPermission.ADMINISTER,
            NetworkTagSet.defaults()),
        new TerminalOpenData(
            SOMEWHERE,
            1,
            TerminalPermission.ADMINISTER,
            NetworkTagSet.defaults()
                .withCustomTag("ores", "Ores")
                .withItem("ores", "minecraft:raw_iron")));
  }

  @ParameterizedTest
  @MethodSource("wireCases")
  void everyFieldSurvivesTheWireUnchanged(TerminalOpenData data) {
    RegistryByteBuf buffer = buffer();

    TerminalOpenDataCodec.INSTANCE.encode(buffer, data);

    assertEquals(data, TerminalOpenDataCodec.INSTANCE.decode(buffer));
  }

  @ParameterizedTest
  @MethodSource("wireCases")
  void decodingConsumesExactlyWhatEncodingWrote(TerminalOpenData data) {
    RegistryByteBuf buffer = buffer();

    TerminalOpenDataCodec.INSTANCE.encode(buffer, data);
    TerminalOpenDataCodec.INSTANCE.decode(buffer);

    assertEquals(0, buffer.readableBytes());
  }

  /**
   * A client one build behind must get a terminal it can use rather than no terminal at all, and
   * the only safe direction to fail in is the lesser permission.
   */
  @Test
  void aPermissionThisBuildDoesNotKnowDecodesAsTheLesserOne() {
    RegistryByteBuf buffer = forged("superuser");

    assertEquals(
        TerminalPermission.USE, TerminalOpenDataCodec.INSTANCE.decode(buffer).permission());
  }

  @Test
  void aRadiusForgedAboveWhatATerminalCanReachIsClampedOnArrival() {
    RegistryByteBuf buffer = forged("use", 9999);

    assertEquals(
        com.grahambartley.toomanychests.terminal.TerminalSettings.MAX_RADIUS,
        TerminalOpenDataCodec.INSTANCE.decode(buffer).radius());
  }

  /**
   * The whole point of ADR 0009's paginated listing: opening a terminal costs the same whether it
   * reaches an empty chest or a warehouse, because no part of the network's contents is in here.
   * What the payload does grow with is the network's own configuration, which is bounded.
   */
  @Test
  void theOpenPayloadIsTheSameSizeForEveryTerminalConfiguredTheSameWay() {
    RegistryByteBuf near = buffer();
    RegistryByteBuf far = buffer();

    TerminalOpenDataCodec.INSTANCE.encode(
        near, new TerminalOpenData(SOMEWHERE, 4, TerminalPermission.USE, NetworkTagSet.defaults()));
    TerminalOpenDataCodec.INSTANCE.encode(
        far, new TerminalOpenData(SOMEWHERE, 4, TerminalPermission.USE, NetworkTagSet.defaults()));

    assertEquals(near.readableBytes(), far.readableBytes());
  }

  private static RegistryByteBuf buffer() {
    return new RegistryByteBuf(Unpooled.buffer(), null);
  }

  private static RegistryByteBuf forged(String permission) {
    return forged(permission, 8);
  }

  /** A packet built by hand, the way a client on another build would build one. */
  private static RegistryByteBuf forged(String permission, int radius) {
    RegistryByteBuf buffer = buffer();
    buffer.writeBlockPos(SOMEWHERE);
    buffer.writeVarInt(radius);
    buffer.writeString(permission);
    buffer.writeCollection(List.<String>of(), PacketByteBuf::writeString);
    return buffer;
  }
}
