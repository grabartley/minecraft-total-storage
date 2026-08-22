package com.grahambartley.toomanychests.config;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Reads and writes the server config file, treating any unreadable file as recoverable. */
public final class ConfigFile {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigFile.class);

  private ConfigFile() {}

  /** Loads the config, writing defaults out when no file exists yet. */
  public static TooManyChestsConfig loadOrCreate(Path path) {
    final boolean missing = path == null || !Files.exists(path);
    final TooManyChestsConfig config = load(path);
    if (missing) {
      save(path, config);
    }
    return config;
  }

  public static TooManyChestsConfig load(Path path) {
    if (path == null || !Files.exists(path)) {
      return TooManyChestsConfig.defaults();
    }
    final String json;
    try {
      json = Files.readString(path, StandardCharsets.UTF_8);
    } catch (IOException ex) {
      LOGGER.warn(
          "Failed to read Too Many Chests config at {}, using defaults: {}", path, ex.getMessage());
      return TooManyChestsConfig.defaults();
    }
    try {
      final JsonObject root = GSON.fromJson(json, JsonObject.class);
      if (root == null) {
        return TooManyChestsConfig.defaults();
      }
      return ConfigSerializer.fromJson(root);
    } catch (JsonParseException | IllegalStateException ex) {
      preserveBrokenFile(path);
      LOGGER.warn(
          "Malformed Too Many Chests config at {} preserved and defaults loaded: {}",
          path,
          ex.getMessage());
      return TooManyChestsConfig.defaults();
    }
  }

  public static boolean save(Path path, TooManyChestsConfig config) {
    if (path == null || config == null) {
      return false;
    }
    try {
      Files.createDirectories(path.getParent());
      final Path tempFile = path.resolveSibling(path.getFileName() + ".tmp");
      Files.writeString(
          tempFile, GSON.toJson(ConfigSerializer.toJson(config)), StandardCharsets.UTF_8);
      Files.move(
          tempFile, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      return true;
    } catch (IOException ex) {
      LOGGER.error("Failed to save Too Many Chests config at {}: {}", path, ex.getMessage());
      return false;
    }
  }

  private static void preserveBrokenFile(Path path) {
    try {
      Files.move(
          path,
          ConfigPaths.brokenBackupPath(path, Instant.now()),
          StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException ex) {
      LOGGER.warn("Failed to preserve malformed Too Many Chests config: {}", ex.getMessage());
    }
  }
}
