package com.grahambartley.totalstorage.operation;

import com.grahambartley.totalstorage.filter.FilterCandidate;
import com.grahambartley.totalstorage.storage.ContainerAcceptance;
import com.grahambartley.totalstorage.storage.ContainerAssignment;
import com.grahambartley.totalstorage.storage.StorageOrigin;
import com.grahambartley.totalstorage.storage.StorageProvider;
import com.grahambartley.totalstorage.tag.NetworkTagSet;
import java.util.Objects;
import java.util.Set;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

public final class QuickStackTarget {
  private final StorageProvider provider;
  private final ContainerAssignment assignment;
  private final NetworkTagSet tags;
  private final Set<ItemVariant> held;

  private QuickStackTarget(
      StorageProvider provider, ContainerAssignment assignment, NetworkTagSet tags) {
    this.provider = Objects.requireNonNull(provider, "provider");
    this.assignment = assignment == null ? ContainerAssignment.DEFAULT : assignment;
    this.tags = tags == null ? NetworkTagSet.defaults() : tags;
    this.held = HeldResources.of(provider.storage());
  }

  public static QuickStackTarget of(
      StorageProvider provider, ContainerAssignment assignment, NetworkTagSet tags) {
    return new QuickStackTarget(provider, assignment, tags);
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
    return variant != null && !variant.isBlank() && held.contains(variant);
  }
}
