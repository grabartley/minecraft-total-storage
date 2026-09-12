package com.grahambartley.toomanychests.config;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;

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
    buf.writeBoolean(config.quickStackEnabled());
    buf.writeBoolean(config.depositAllSkipsHotbar());
    buf.writeVarInt(config.nearCapacityWarningPercent());
    buf.writeVarInt(config.expensiveOperationCooldownTicks());
    buf.writeVarInt(config.movesPerTickBudget());
    buf.writeVarInt(config.largeOperationContainers());
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
        buf.readBoolean(),
        buf.readVarInt(),
        buf.readVarInt(),
        buf.readVarInt(),
        buf.readVarInt(),
        buf.readMap(PacketByteBuf::readString, PacketByteBuf::readBoolean));
  }
}
