package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.config.TerminalAccessMode;
import com.grahambartley.toomanychests.config.TooManyChestsConfig;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;

/**
 * Persists a terminal's settings.
 *
 * <p>Every field is optional on read and decoding routes through {@link TerminalSettings}, so a
 * save written by an older build, or edited by hand, loses only the values it cannot supply rather
 * than the whole block entity.
 */
public final class TerminalSettingsCodec {
  private static final Codec<TerminalAccessMode> ACCESS_MODE =
      Codec.STRING.xmap(
          id ->
              TerminalAccessMode.fromId(id)
                  .orElse(TooManyChestsConfig.DEFAULT_TERMINAL_ACCESS_MODE),
          TerminalAccessMode::id);

  private static final Codec<ContainerRole> ROLE =
      Codec.STRING.xmap(
          id -> ContainerRole.fromId(id).orElse(ContainerRole.DEFAULT), ContainerRole::id);

  private record RoleEntry(BlockPos container, ContainerRole role) {
    static final Codec<RoleEntry> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        BlockPos.CODEC.fieldOf("pos").forGetter(RoleEntry::container),
                        ROLE.fieldOf("role").forGetter(RoleEntry::role))
                    .apply(instance, RoleEntry::new));
  }

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
                      RoleEntry.CODEC
                          .listOf()
                          .optionalFieldOf("containerRoles", List.of())
                          .forGetter(TerminalSettingsCodec::roleEntries),
                      Codec.STRING
                          .listOf()
                          .optionalFieldOf("tags", List.of())
                          .forGetter(settings -> List.copyOf(settings.tags())))
                  .apply(instance, TerminalSettingsCodec::assemble));

  private TerminalSettingsCodec() {}

  private static TerminalSettings assemble(
      int radius,
      TerminalAccessMode accessMode,
      Optional<UUID> owner,
      List<UUID> trusted,
      List<RoleEntry> containerRoles,
      List<String> tags) {
    final Map<BlockPos, ContainerRole> roles = new LinkedHashMap<>();
    for (final RoleEntry entry : containerRoles) {
      roles.put(entry.container(), entry.role());
    }
    return new TerminalSettings(
        radius, accessMode, owner, new LinkedHashSet<>(trusted), roles, new LinkedHashSet<>(tags));
  }

  private static List<RoleEntry> roleEntries(TerminalSettings settings) {
    return settings.containerRoles().entrySet().stream()
        .map(entry -> new RoleEntry(entry.getKey(), entry.getValue()))
        .toList();
  }
}
