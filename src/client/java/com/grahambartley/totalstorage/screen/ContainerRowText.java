package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.storage.ContainerRole;
import com.grahambartley.totalstorage.terminal.ContainerRoleEntry;
import com.grahambartley.totalstorage.terminal.NetworkTagEntry;
import com.grahambartley.totalstorage.terminal.NetworkTagView;
import com.grahambartley.totalstorage.text.CountedNoun;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public final class ContainerRowText {
  private ContainerRowText() {}

  public static MutableText contents(ContainerRoleEntry entry) {
    return entry.isEmpty()
        ? TerminalScreenText.of("settings.container.empty")
        : CountedNoun.KINDS.of(entry.variantCount());
  }

  public static MutableText itemTotal(ContainerRoleEntry entry) {
    return CountedNoun.ITEMS.ofFormatted(
        ItemCountFormat.cell(entry.itemCount()), entry.itemCount());
  }

  public static MutableText slotsUsed(ContainerRoleEntry entry) {
    return TerminalScreenText.of("settings.container.slots", entry.usedSlots(), entry.totalSlots());
  }

  public static Text position(ContainerRoleEntry entry) {
    return TerminalScreenText.of("settings.container.at", place(entry.container()));
  }

  public static Text title(ContainerRoleEntry entry, ItemStack block) {
    if (entry.isNamed()) {
      return Text.literal(entry.name());
    }
    return block == null || block.isEmpty()
        ? TerminalScreenText.of("settings.container.unnamed")
        : block.getName();
  }

  public static MutableText place(BlockPos container) {
    return TerminalScreenText.of(
        "settings.container.place", container.getX(), container.getY(), container.getZ());
  }

  public static MutableText detail(ContainerRoleEntry entry) {
    return TerminalScreenText.of(
        "settings.container.detail", place(entry.container()), contents(entry));
  }

  public static MutableText role(ContainerRole role) {
    return TerminalScreenText.of("role." + safe(role).id());
  }

  public static MutableText roleChip(ContainerRoleEntry entry) {
    return entry.isFiltered()
        ? TerminalScreenText.of("role.filtered.count", entry.acceptedTags().size())
        : role(entry.role());
  }

  public static MutableText roleMeaning(ContainerRole role) {
    return TerminalScreenText.of("role." + safe(role).id() + ".meaning");
  }

  public static MutableText acceptedTags(ContainerRoleEntry entry, NetworkTagView tags) {
    if (entry.acceptsNothing()) {
      return TerminalScreenText.of("settings.container.filter.empty");
    }
    final MutableText listed = Text.empty();
    for (final String tagId : entry.acceptedTags()) {
      if (!listed.getSiblings().isEmpty()) {
        listed.append(Text.literal(", "));
      }
      listed.append(nameOf(tagId, tags));
    }
    return listed;
  }

  private static Text nameOf(String tagId, NetworkTagView tags) {
    for (final NetworkTagEntry tag : tags.tags()) {
      if (tag.id().equals(tagId)) {
        return FilterColumnText.nameOf(tag);
      }
    }
    return Text.literal(tagId);
  }

  private static ContainerRole safe(ContainerRole role) {
    return role == null ? ContainerRole.DEFAULT : role;
  }
}
