package com.grahambartley.toomanychests.config;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ClientPreferencesSerializer {
  static final String KEY_SORT_BUTTON_VISIBLE = "sortButtonVisible";
  static final String KEY_SCREEN_BUTTON_ANCHOR = "screenButtonAnchor";
  static final String KEY_LOCKED_SLOT_TINT_VISIBLE = "lockedSlotTintVisible";

  private static final Logger LOGGER = LoggerFactory.getLogger(ClientPreferencesSerializer.class);

  private ClientPreferencesSerializer() {}

  public static JsonObject toJson(ClientPreferences preferences) {
    final JsonObject root = new JsonObject();
    root.addProperty(KEY_SORT_BUTTON_VISIBLE, preferences.sortButtonVisible());
    root.addProperty(KEY_SCREEN_BUTTON_ANCHOR, preferences.screenButtonAnchor().id());
    root.addProperty(KEY_LOCKED_SLOT_TINT_VISIBLE, preferences.lockedSlotTintVisible());
    return root;
  }

  public static ClientPreferences fromJson(JsonObject root) {
    final ClientPreferences defaults = ClientPreferences.defaults();
    if (root == null) {
      return defaults;
    }
    return new ClientPreferences(
        bool(root, KEY_SORT_BUTTON_VISIBLE, defaults.sortButtonVisible()),
        ScreenButtonAnchor.fromId(string(root, KEY_SCREEN_BUTTON_ANCHOR))
            .orElseGet(() -> warnUnknown(root, defaults.screenButtonAnchor())),
        bool(root, KEY_LOCKED_SLOT_TINT_VISIBLE, defaults.lockedSlotTintVisible()));
  }

  private static ScreenButtonAnchor warnUnknown(JsonObject root, ScreenButtonAnchor fallback) {
    if (root.has(KEY_SCREEN_BUTTON_ANCHOR)) {
      LOGGER.warn(
          "Unrecognised Too Many Chests client value for {}, using {}",
          KEY_SCREEN_BUTTON_ANCHOR,
          fallback.id());
    }
    return fallback;
  }

  private static String string(JsonObject root, String key) {
    return root.has(key) && root.get(key).isJsonPrimitive() ? root.get(key).getAsString() : null;
  }

  private static boolean bool(JsonObject root, String key, boolean fallback) {
    try {
      return root.has(key) && root.get(key).isJsonPrimitive()
          ? root.get(key).getAsBoolean()
          : fallback;
    } catch (RuntimeException unreadable) {
      LOGGER.warn("Unreadable Too Many Chests client value for {}, using {}", key, fallback);
      return fallback;
    }
  }
}
