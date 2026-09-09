package com.grahambartley.toomanychests.storage;

public sealed interface StorageOrigin permits BlockStorageOrigin {
  String describe();
}
