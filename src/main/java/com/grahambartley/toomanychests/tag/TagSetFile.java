package com.grahambartley.toomanychests.tag;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TagSetFile {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final Logger LOGGER = LoggerFactory.getLogger(TagSetFile.class);
  private static final String TAGS = "tags";

  private TagSetFile() {}

  public static boolean save(Path path, NetworkTagSet tags) {
    if (path == null || tags == null) {
      return false;
    }
    final Optional<JsonElement> encoded =
        NetworkTagCodec.CODEC.encodeStart(JsonOps.INSTANCE, tags).result();
    if (encoded.isEmpty()) {
      return false;
    }
    final JsonObject root = new JsonObject();
    root.add(TAGS, encoded.get());
    try {
      Files.createDirectories(path.getParent());
      Files.writeString(path, GSON.toJson(root), StandardCharsets.UTF_8);
      return true;
    } catch (IOException unwritable) {
      LOGGER.error("Failed to export a Too Many Chests tag set to {}", path, unwritable);
      return false;
    }
  }

  public static Optional<TagSetContents> load(Path path) {
    if (path == null || !Files.exists(path)) {
      return Optional.empty();
    }
    try {
      final JsonObject root =
          GSON.fromJson(Files.readString(path, StandardCharsets.UTF_8), JsonObject.class);
      if (root == null || !root.has(TAGS)) {
        return Optional.empty();
      }
      return NetworkTagCodec.CONTENTS_CODEC.parse(JsonOps.INSTANCE, root.get(TAGS)).result();
    } catch (IOException | JsonParseException | IllegalStateException unusable) {
      LOGGER.warn("Failed to import a Too Many Chests tag set from {}", path, unusable);
      return Optional.empty();
    }
  }
}
