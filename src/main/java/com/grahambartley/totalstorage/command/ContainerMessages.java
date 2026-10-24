package com.grahambartley.totalstorage.command;

import com.grahambartley.totalstorage.text.CountedNoun;
import java.util.Collection;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public final class ContainerMessages {
  private static final String PREFIX = "command.total-storage.";

  private ContainerMessages() {}

  public static Text containerName(BlockPos container, String name) {
    return line("container.name", "The container at %s is called '%s'", at(container), name);
  }

  public static Text containerUnnamed(BlockPos container) {
    return line("container.name.none", "The container at %s has no name", at(container));
  }

  public static Text containerNameSet(BlockPos container, String name) {
    return line("container.name.set", "Called the container at %s '%s'", at(container), name);
  }

  public static Text containerNameCleared(BlockPos container) {
    return line("container.name.cleared", "Took the name off the container at %s", at(container));
  }

  public static Text containerNameUnchanged(BlockPos container) {
    return line(
        "container.name.unchanged", "The container at %s already reads that way", at(container));
  }

  public static Text containerNameRefused(int maximum) {
    return line(
        "container.name.refused",
        "A container name has to be readable text of at most %s",
        CountedNoun.CHARACTERS.of(maximum));
  }

  public static Text containerFilter(BlockPos container, Collection<String> tagIds) {
    return line(
        "container.filter",
        "The container at %s accepts %s",
        at(container),
        String.join(", ", tagIds));
  }

  public static Text containerFilterEmpty(BlockPos container) {
    return line(
        "container.filter.none",
        "The container at %s accepts nothing, so nothing is deposited there",
        at(container));
  }

  public static Text containerNotFiltered(BlockPos container) {
    return line(
        "container.filter.wrong-role",
        "The container at %s is not filtered, so it has nothing to accept",
        at(container));
  }

  public static Text containerFilterAdded(BlockPos container, String tagId) {
    return line(
        "container.filter.added", "The container at %s now accepts %s", at(container), tagId);
  }

  public static Text containerFilterRemoved(BlockPos container, String tagId) {
    return line(
        "container.filter.removed",
        "The container at %s no longer accepts %s",
        at(container),
        tagId);
  }

  public static Text containerFilterUnchanged(BlockPos container, String tagId) {
    return line(
        "container.filter.unchanged",
        "The container at %s already reads that way for %s",
        at(container),
        tagId);
  }

  public static Text containerFilterCleared(BlockPos container) {
    return line(
        "container.filter.cleared", "The container at %s now accepts nothing", at(container));
  }

  public static Text unknownTag(String requested) {
    return line("container.filter.unknown-tag", "'%s' is not a tag on this network", requested);
  }

  private static String at(BlockPos pos) {
    return pos.getX() + " " + pos.getY() + " " + pos.getZ();
  }

  private static Text line(String key, String fallback, Object... arguments) {
    return Text.translatableWithFallback(PREFIX + key, fallback, arguments);
  }
}
