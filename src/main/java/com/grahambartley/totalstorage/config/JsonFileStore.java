package com.grahambartley.totalstorage.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JsonFileStore<T> {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final Logger LOGGER = LoggerFactory.getLogger(JsonFileStore.class);

  private final String label;
  private final Supplier<T> defaults;
  private final Function<T, JsonObject> toJson;
  private final Function<JsonObject, T> fromJson;

  public JsonFileStore(
      String label,
      Supplier<T> defaults,
      Function<T, JsonObject> toJson,
      Function<JsonObject, T> fromJson) {
    this.label = Objects.requireNonNull(label, "label");
    this.defaults = Objects.requireNonNull(defaults, "defaults");
    this.toJson = Objects.requireNonNull(toJson, "toJson");
    this.fromJson = Objects.requireNonNull(fromJson, "fromJson");
  }

  public T loadOrCreate(Path path) {
    final T loaded = load(path);
    if (path != null && !Files.exists(path)) {
      save(path, loaded);
    }
    return loaded;
  }

  public T load(Path path) {
    if (path == null || !Files.exists(path)) {
      return defaults.get();
    }
    final String json;
    try {
      json = Files.readString(path, StandardCharsets.UTF_8);
    } catch (IOException unreadable) {
      LOGGER.warn(
          "Failed to read Total Storage {} at {}, using defaults: {}",
          label,
          path,
          unreadable.getMessage());
      return defaults.get();
    }
    try {
      final JsonObject root = GSON.fromJson(json, JsonObject.class);
      return root == null ? defaults.get() : fromJson.apply(root);
    } catch (JsonParseException | IllegalStateException malformed) {
      preserveBrokenFile(path);
      LOGGER.warn(
          "Malformed Total Storage {} at {} preserved and defaults loaded: {}",
          label,
          path,
          malformed.getMessage());
      return defaults.get();
    }
  }

  public boolean save(Path path, T value) {
    if (path == null || value == null) {
      return false;
    }
    try {
      Files.createDirectories(path.getParent());
      final Path tempFile = path.resolveSibling(path.getFileName() + ".tmp");
      Files.writeString(tempFile, GSON.toJson(toJson.apply(value)), StandardCharsets.UTF_8);
      Files.move(
          tempFile, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      return true;
    } catch (IOException unwritable) {
      LOGGER.error(
          "Failed to save Total Storage {} at {}: {}", label, path, unwritable.getMessage());
      return false;
    }
  }

  private void preserveBrokenFile(Path path) {
    try {
      Files.move(
          path,
          ConfigPaths.brokenBackupPath(path, Instant.now()),
          StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException unmovable) {
      LOGGER.warn(
          "Failed to preserve malformed Total Storage {}: {}", label, unmovable.getMessage());
    }
  }
}
