package com.grahambartley.toomanychests.mutation;

import com.grahambartley.toomanychests.storage.StorageOrigin;

@FunctionalInterface
public interface ContainerMutationListener {
  void containerChanged(StorageOrigin origin);
}
