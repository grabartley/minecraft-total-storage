package com.grahambartley.toomanychests.config;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;

/**
 * The wire form of the server configuration, shared by the sync and edit payloads.
 *
 * <p>Enums travel as their string ids rather than their ordinals, so reordering a constant cannot
 * quietly turn one setting into another for a client on a different build.
 *
 * <p>Decoding runs every value back through the record's own constructor, which clamps. A client
 * that forges a wildly out-of-range setting therefore hands the server a value inside the
 * documented bounds, and the server still has to decide whether to accept it at all.
 */
public final class ConfigPacketCodec {
  public static final PacketCodec<RegistryByteBuf, TooManyChestsConfig> INSTANCE =
      PacketCodec.of(ConfigPacketCodec::write, ConfigPacketCodec::read);

  private ConfigPacketCodec() {}

  private static void write(TooManyChestsConfig config, RegistryByteBuf buf) {
    buf.writeVarInt(config.maxTerminalRadius());
    buf.writeString(config.defaultTerminalAccessMode().id());
    buf.writeString(config.sortOrdering().id());
    buf.writeBoolean(config.sortButtonEnabled());
    buf.writeBoolean(config.middleClickSortEnabled());
    buf.writeVarInt(config.quickStackRadius());
    buf.writeBoolean(config.depositAllSkipsHotbar());
    buf.writeVarInt(config.nearCapacityWarningPercent());
    buf.writeVarInt(config.expensiveOperationCooldownTicks());
    buf.writeVarInt(config.movesPerTickBudget());
    buf.writeMap(
        config.containerEligibilityOverrides(),
        PacketByteBuf::writeString,
        PacketByteBuf::writeBoolean);
  }

  private static TooManyChestsConfig read(RegistryByteBuf buf) {
    return new TooManyChestsConfig(
        buf.readVarInt(),
        TerminalAccessMode.fromId(buf.readString())
            .orElse(TooManyChestsConfig.DEFAULT_TERMINAL_ACCESS_MODE),
        SortOrdering.fromId(buf.readString()).orElse(TooManyChestsConfig.DEFAULT_SORT_ORDERING),
        buf.readBoolean(),
        buf.readBoolean(),
        buf.readVarInt(),
        buf.readBoolean(),
        buf.readVarInt(),
        buf.readVarInt(),
        buf.readVarInt(),
        buf.readMap(PacketByteBuf::readString, PacketByteBuf::readBoolean));
  }
}
