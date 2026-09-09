package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.config.TerminalAccessMode;
import com.grahambartley.toomanychests.config.TooManyChestsConfig;
import com.grahambartley.toomanychests.storage.ContainerAssignmentCodec;
import com.grahambartley.toomanychests.storage.ContainerAssignments;
import com.grahambartley.toomanychests.tag.NetworkTagCodec;
import com.grahambartley.toomanychests.tag.NetworkTagSet;
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
              TerminalAccessMode.fromId(id)
                  .orElse(TooManyChestsConfig.DEFAULT_TERMINAL_ACCESS_MODE),
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
                              "accessMode", TooManyChestsConfig.DEFAULT_TERMINAL_ACCESS_MODE)
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
