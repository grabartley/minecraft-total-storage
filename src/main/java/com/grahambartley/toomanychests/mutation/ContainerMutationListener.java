package com.grahambartley.toomanychests.mutation;

import com.grahambartley.toomanychests.storage.StorageOrigin;

/**
 * Told that one container's contents changed, and nothing more.
 *
 * <p>Deliberately not told what changed. A listener that receives a diff has to trust it; a
 * listener that receives only "this one is stale" re-reads the container and cannot drift.
 */
@FunctionalInterface
public interface ContainerMutationListener {
  void containerChanged(StorageOrigin origin);
}
