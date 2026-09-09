package com.grahambartley.toomanychests.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.util.math.BlockPos;

public final class ContainerAssignmentCodec {
  private static final Codec<ContainerRole> ROLE =
      Codec.STRING.xmap(
          id -> ContainerRole.fromId(id).orElse(ContainerRole.DEFAULT), ContainerRole::id);

  private record StoredAssignment(
      BlockPos container, ContainerRole role, List<String> tags, List<String> items) {
    static final Codec<StoredAssignment> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        BlockPos.CODEC.fieldOf("pos").forGetter(StoredAssignment::container),
                        ROLE.optionalFieldOf("role", ContainerRole.DEFAULT)
                            .forGetter(StoredAssignment::role),
                        Codec.STRING
                            .listOf()
                            .optionalFieldOf("tags", List.of())
                            .forGetter(StoredAssignment::tags),
                        Codec.STRING
                            .listOf()
                            .optionalFieldOf("items", List.of())
                            .forGetter(StoredAssignment::items))
                    .apply(instance, StoredAssignment::new));
  }

  public static final Codec<ContainerAssignments> CODEC =
      StoredAssignment.CODEC
          .listOf()
          .xmap(ContainerAssignmentCodec::assemble, ContainerAssignmentCodec::disassemble);

  private ContainerAssignmentCodec() {}

  private static ContainerAssignments assemble(List<StoredAssignment> stored) {
    final Map<BlockPos, ContainerAssignment> assignments = new LinkedHashMap<>(stored.size());
    for (final StoredAssignment entry : stored) {
      assignments.put(
          entry.container(),
          new ContainerAssignment(entry.role(), ContainerFilter.of(entry.tags(), entry.items())));
    }
    return new ContainerAssignments(assignments);
  }

  private static List<StoredAssignment> disassemble(ContainerAssignments assignments) {
    return assignments.byPosition().entrySet().stream()
        .map(
            entry ->
                new StoredAssignment(
                    entry.getKey(),
                    entry.getValue().role(),
                    List.copyOf(entry.getValue().filter().tagIds()),
                    List.copyOf(entry.getValue().filter().itemIds())))
        .toList();
  }
}
