package com.grahambartley.toomanychests.config;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class JsonValues {
  private static final Logger LOGGER = LoggerFactory.getLogger(JsonValues.class);

  private JsonValues() {}

  @Nullable
  static String string(JsonObject root, String key) {
    return root.has(key) && root.get(key).isJsonPrimitive() ? root.get(key).getAsString() : null;
  }

  static boolean bool(JsonObject root, String key, boolean fallback) {
    try {
      return root.has(key) && root.get(key).isJsonPrimitive()
          ? root.get(key).getAsBoolean()
          : fallback;
    } catch (RuntimeException unreadable) {
      LOGGER.warn("Unreadable Too Many Chests value for {}, using {}", key, fallback);
      return fallback;
    }
  }
}
