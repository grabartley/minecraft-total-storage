package com.grahambartley.totalstorage.mutation;

import com.grahambartley.totalstorage.storage.StorageOrigin;

@FunctionalInterface
public interface ContainerMutationListener {
  void containerChanged(StorageOrigin origin);
}
