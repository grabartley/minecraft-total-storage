package com.grahambartley.toomanychests.storage;

/**
 * Identity of the place a {@link StorageProvider}'s items live. Implementations are immutable value
 * types so they are safe to use as network index keys.
 */
public sealed interface StorageOrigin permits BlockStorageOrigin {
  String describe();
}
