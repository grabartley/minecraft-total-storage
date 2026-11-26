package com.grahambartley.totalstorage.operation;

import com.grahambartley.totalstorage.filter.FilterCandidate;
import com.grahambartley.totalstorage.storage.ContainerAcceptance;
import com.grahambartley.totalstorage.storage.ContainerAssignment;
import com.grahambartley.totalstorage.storage.StorageOrigin;
import com.grahambartley.totalstorage.storage.StorageProvider;
import com.grahambartley.totalstorage.tag.NetworkTagSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;

public record QuickStackTarget(
    StorageProvider provider,
    ContainerAssignment assignment,
    NetworkTagSet tags,
    Set<ItemVariant> held) {
  public QuickStackTarget {
    Objects.requireNonNull(provider, "provider");
    assignment = assignment == null ? ContainerAssignment.DEFAULT : assignment;
    tags = tags == null ? NetworkTagSet.defaults() : tags;
    held = held == null ? Set.of() : Set.copyOf(held);
  }

  public static QuickStackTarget of(
      StorageProvider provider, ContainerAssignment assignment, NetworkTagSet tags) {
    Objects.requireNonNull(provider, "provider");
    return new QuickStackTarget(provider, assignment, tags, heldBy(provider));
  }

  public static QuickStackTarget unclaimed(StorageProvider provider) {
    return of(provider, ContainerAssignment.DEFAULT, NetworkTagSet.defaults());
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

  private static Set<ItemVariant> heldBy(StorageProvider provider) {
    final Set<ItemVariant> found = new HashSet<>();
    for (final StorageView<ItemVariant> view : provider.storage().nonEmptyViews()) {
      if (!view.isResourceBlank()) {
        found.add(view.getResource());
      }
    }
    return found;
  }
}
