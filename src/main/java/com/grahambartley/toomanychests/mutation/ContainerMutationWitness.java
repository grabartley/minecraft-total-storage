package com.grahambartley.toomanychests.mutation;

import com.grahambartley.toomanychests.storage.StorageOrigin;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class ContainerMutationWitness implements ContainerMutationListener, AutoCloseable {
  private final Set<StorageOrigin> changed = new LinkedHashSet<>();

  private Runnable stopListening;

  private ContainerMutationWitness() {}

  public static ContainerMutationWitness over(
      ContainerMutationRegistry registry, Collection<StorageOrigin> origins) {
    Objects.requireNonNull(registry, "registry");
    final List<StorageOrigin> watching =
        origins == null ? List.of() : List.copyOf(new LinkedHashSet<>(origins));
    final ContainerMutationWitness witness = new ContainerMutationWitness();
    watching.forEach(origin -> registry.watch(origin, witness));
    witness.stopListening = () -> watching.forEach(origin -> registry.unwatch(origin, witness));
    return witness;
  }

  public static ContainerMutationWitness overEverything(ContainerMutationRegistry registry) {
    Objects.requireNonNull(registry, "registry");
    final ContainerMutationWitness witness = new ContainerMutationWitness();
    registry.watchEverywhere(witness);
    witness.stopListening = () -> registry.stopWatchingEverywhere(witness);
    return witness;
  }

  public Set<StorageOrigin> changed() {
    return Collections.unmodifiableSet(changed);
  }

  @Override
  public void containerChanged(StorageOrigin origin) {
    if (origin != null) {
      changed.add(origin);
    }
  }

  @Override
  public void close() {
    final Runnable stopping = stopListening;
    stopListening = null;
    if (stopping != null) {
      stopping.run();
    }
  }
}
