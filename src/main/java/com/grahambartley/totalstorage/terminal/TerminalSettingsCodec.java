package com.grahambartley.totalstorage.terminal;

import com.grahambartley.totalstorage.config.TerminalAccessMode;
import com.grahambartley.totalstorage.config.TotalStorageConfig;
import com.grahambartley.totalstorage.storage.ContainerAssignmentCodec;
import com.grahambartley.totalstorage.storage.ContainerAssignments;
import com.grahambartley.totalstorage.tag.NetworkTagCodec;
import com.grahambartley.totalstorage.tag.NetworkTagSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.util.Uuids;

public final class TerminalSettingsCodec {
  private static final Codec<TerminalAccessMode> ACCESS_MODE =
      Codec.STRING.xmap(
          id ->
              TerminalAccessMode.fromId(id).orElse(TotalStorageConfig.DEFAULT_TERMINAL_ACCESS_MODE),
          TerminalAccessMode::id);

  public static final Codec<TerminalSettings> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.INT
                          .optionalFieldOf("radius", TerminalSettings.DEFAULT_RADIUS)
                          .forGetter(TerminalSettings::radius),
                      ACCESS_MODE
                          .optionalFieldOf(
                              "accessMode", TotalStorageConfig.DEFAULT_TERMINAL_ACCESS_MODE)
                          .forGetter(TerminalSettings::accessMode),
                      Uuids.INT_STREAM_CODEC
                          .optionalFieldOf("owner")
                          .forGetter(TerminalSettings::owner),
                      Uuids.INT_STREAM_CODEC
                          .listOf()
                          .optionalFieldOf("trusted", List.of())
                          .forGetter(settings -> List.copyOf(settings.trusted())),
                      ContainerAssignmentCodec.CODEC
                          .optionalFieldOf("containerRoles", ContainerAssignments.empty())
                          .forGetter(TerminalSettings::containers),
                      NetworkTagCodec.CODEC
                          .optionalFieldOf("tags", NetworkTagSet.defaults())
                          .forGetter(TerminalSettings::tags))
                  .apply(instance, TerminalSettingsCodec::assemble));

  private TerminalSettingsCodec() {}

  private static TerminalSettings assemble(
      int radius,
      TerminalAccessMode accessMode,
      Optional<UUID> owner,
      List<UUID> trusted,
      ContainerAssignments containers,
      NetworkTagSet tags) {
    return new TerminalSettings(
        radius, accessMode, owner, new LinkedHashSet<>(trusted), containers, tags);
  }
}
