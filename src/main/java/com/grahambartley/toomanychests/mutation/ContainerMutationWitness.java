package com.grahambartley.toomanychests.mutation;

import com.grahambartley.toomanychests.storage.StorageOrigin;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Which of a named set of containers really changed while somebody was watching.
 *
 * <p>An operation knows the containers it might touch and never the ones it did, because how much
 * leaves each of them is decided inside the transaction by what is found there. Listening to them
 * for the length of the operation turns the first into the second without threading bookkeeping
 * through every transfer.
 *
 * <p>What makes the answer trustworthy is when a container announces itself. A provisional move
 * announces nothing and a committed one announces on commit, so an operation that rolled back is
 * witnessed as having changed nothing, and one that partly succeeded names exactly the containers
 * whose contents really differ.
 *
 * <p>Closing is not optional. A witness left registered would keep hearing about containers for the
 * rest of the server's life, and would keep the operation that opened it alive with them.
 */
public final class ContainerMutationWitness implements ContainerMutationListener, AutoCloseable {
  private final Set<StorageOrigin> changed = new LinkedHashSet<>();

  private Runnable stopListening;

  private ContainerMutationWitness() {}

  /** Starts listening straight away, so nothing between construction and use goes unheard. */
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

  /**
   * Listens to every container instead of to named ones, for an operation that cannot name what it
   * is about to touch. Sorting is the case: a screen over a double chest names one inventory
   * spanning two block entities, and only the mutations say which those were.
   */
  public static ContainerMutationWitness overEverything(ContainerMutationRegistry registry) {
    Objects.requireNonNull(registry, "registry");
    final ContainerMutationWitness witness = new ContainerMutationWitness();
    registry.watchEverywhere(witness);
    witness.stopListening = () -> registry.stopWatchingEverywhere(witness);
    return witness;
  }

  /** The containers that announced a change, in the order they announced it. */
  public Set<StorageOrigin> changed() {
    return Collections.unmodifiableSet(changed);
  }

  @Override
  public void containerChanged(StorageOrigin origin) {
    if (origin != null) {
      changed.add(origin);
    }
  }

  /** Whichever way this witness started listening is the way it stops, and it stops only once. */
  @Override
  public void close() {
    final Runnable stopping = stopListening;
    stopListening = null;
    if (stopping != null) {
      stopping.run();
    }
  }
}
