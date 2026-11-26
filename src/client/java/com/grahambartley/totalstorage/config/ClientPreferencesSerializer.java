package com.grahambartley.totalstorage.config;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ClientPreferencesSerializer {
  private static final Logger LOGGER = LoggerFactory.getLogger(ClientPreferencesSerializer.class);

  private ClientPreferencesSerializer() {}

  public static JsonObject toJson(ClientPreferences preferences) {
    final JsonObject root = new JsonObject();
    root.addProperty(ClientPreference.SORT_BUTTON_VISIBLE.key(), preferences.sortButtonVisible());
    root.addProperty(
        ClientPreference.SCREEN_BUTTON_ANCHOR.key(), preferences.screenButtonAnchor().id());
    root.addProperty(
        ClientPreference.LOCKED_SLOT_TINT_VISIBLE.key(), preferences.lockedSlotTintVisible());
    return root;
  }

  public static ClientPreferences fromJson(JsonObject root) {
    final ClientPreferences defaults = ClientPreferences.defaults();
    if (root == null) {
      return defaults;
    }
    return new ClientPreferences(
        JsonValues.bool(
            root, ClientPreference.SORT_BUTTON_VISIBLE.key(), defaults.sortButtonVisible()),
        ScreenButtonAnchor.fromId(
                JsonValues.string(root, ClientPreference.SCREEN_BUTTON_ANCHOR.key()))
            .orElseGet(() -> warnUnknown(root, defaults.screenButtonAnchor())),
        JsonValues.bool(
            root,
            ClientPreference.LOCKED_SLOT_TINT_VISIBLE.key(),
            defaults.lockedSlotTintVisible()));
  }

  private static ScreenButtonAnchor warnUnknown(JsonObject root, ScreenButtonAnchor fallback) {
    if (root.has(ClientPreference.SCREEN_BUTTON_ANCHOR.key())) {
      LOGGER.warn(
          "Unrecognised Total Storage client value for {}, using {}",
          ClientPreference.SCREEN_BUTTON_ANCHOR.key(),
          fallback.id());
    }
    return fallback;
  }
}
