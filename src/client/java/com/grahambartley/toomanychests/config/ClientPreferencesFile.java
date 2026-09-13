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

public final class ClientPreferencesFile {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final Logger LOGGER = LoggerFactory.getLogger(ClientPreferencesFile.class);

  private ClientPreferencesFile() {}

  public static ClientPreferences loadOrCreate(Path path) {
    final boolean missing = path == null || !Files.exists(path);
    final ClientPreferences preferences = load(path);
    if (missing) {
      save(path, preferences);
    }
    return preferences;
  }

  public static ClientPreferences load(Path path) {
    if (path == null || !Files.exists(path)) {
      return ClientPreferences.defaults();
    }
    final String json;
    try {
      json = Files.readString(path, StandardCharsets.UTF_8);
    } catch (IOException unreadable) {
      LOGGER.warn(
          "Failed to read Too Many Chests client preferences at {}, using defaults: {}",
          path,
          unreadable.getMessage());
      return ClientPreferences.defaults();
    }
    try {
      final JsonObject root = GSON.fromJson(json, JsonObject.class);
      return root == null
          ? ClientPreferences.defaults()
          : ClientPreferencesSerializer.fromJson(root);
    } catch (JsonParseException | IllegalStateException malformed) {
      preserveBrokenFile(path);
      LOGGER.warn(
          "Malformed Too Many Chests client preferences at {} preserved and defaults loaded: {}",
          path,
          malformed.getMessage());
      return ClientPreferences.defaults();
    }
  }

  public static boolean save(Path path, ClientPreferences preferences) {
    if (path == null || preferences == null) {
      return false;
    }
    try {
      Files.createDirectories(path.getParent());
      final Path tempFile = path.resolveSibling(path.getFileName() + ".tmp");
      Files.writeString(
          tempFile,
          GSON.toJson(ClientPreferencesSerializer.toJson(preferences)),
          StandardCharsets.UTF_8);
      Files.move(
          tempFile, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      return true;
    } catch (IOException unwritable) {
      LOGGER.error(
          "Failed to save Too Many Chests client preferences at {}: {}",
          path,
          unwritable.getMessage());
      return false;
    }
  }

  private static void preserveBrokenFile(Path path) {
    try {
      Files.move(
          path,
          ConfigPaths.brokenBackupPath(path, Instant.now()),
          StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException unmovable) {
      LOGGER.warn(
          "Failed to preserve malformed Too Many Chests client preferences: {}",
          unmovable.getMessage());
    }
  }
}
