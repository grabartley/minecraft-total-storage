package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.filter.FilterCandidate;
import com.grahambartley.toomanychests.storage.ContainerAcceptance;
import com.grahambartley.toomanychests.storage.ContainerAssignment;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import com.grahambartley.toomanychests.storage.StorageProvider;
import com.grahambartley.toomanychests.tag.NetworkTagSet;
import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;

public record QuickStackTarget(
    StorageProvider provider, ContainerAssignment assignment, NetworkTagSet tags) {
  public QuickStackTarget {
    Objects.requireNonNull(provider, "provider");
    assignment = assignment == null ? ContainerAssignment.DEFAULT : assignment;
    tags = tags == null ? NetworkTagSet.defaults() : tags;
  }

  public static QuickStackTarget unclaimed(StorageProvider provider) {
    return new QuickStackTarget(provider, ContainerAssignment.DEFAULT, NetworkTagSet.defaults());
  }

  public StorageOrigin origin() {
    return provider.origin();
  }

  public Storage<ItemVariant> storage() {
    return provider.storage();
  }

  public boolean accepts(FilterCandidate candidate) {
    return ContainerAcceptance.accepts(
        assignment.role(),
        assignment.filter(),
        candidate.identifier(),
        VariantTags.of(tags, candidate));
  }

  public boolean holds(ItemVariant variant) {
    if (variant == null || variant.isBlank()) {
      return false;
    }
    for (final StorageView<ItemVariant> view : storage()) {
      if (!view.isResourceBlank() && view.getAmount() > 0L && variant.equals(view.getResource())) {
        return true;
      }
    }
    return false;
  }
}
