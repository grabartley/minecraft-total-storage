package com.grahambartley.totalstorage.storage;

public sealed interface StorageOrigin permits BlockStorageOrigin {
  String describe();
}
