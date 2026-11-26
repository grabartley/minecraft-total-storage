package com.grahambartley.totalstorage.config;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum ClientPreference {
  SORT_BUTTON_VISIBLE("sortButtonVisible") {
    @Override
    public String valueIn(ClientPreferences preferences) {
      return Boolean.toString(preferences.sortButtonVisible());
    }

    @Override
    public List<String> choices() {
      return BOOLEAN_CHOICES;
    }

    @Override
    ClientPreferences apply(ClientPreferences preferences, String choice) {
      return new ClientPreferences(
          Boolean.parseBoolean(choice),
          preferences.screenButtonAnchor(),
          preferences.lockedSlotTintVisible());
    }
  },

  SCREEN_BUTTON_ANCHOR("screenButtonAnchor") {
    @Override
    public String valueIn(ClientPreferences preferences) {
      return preferences.screenButtonAnchor().id();
    }

    @Override
    public List<String> choices() {
      return Arrays.stream(ScreenButtonAnchor.values()).map(ScreenButtonAnchor::id).toList();
    }

    @Override
    ClientPreferences apply(ClientPreferences preferences, String choice) {
      return new ClientPreferences(
          preferences.sortButtonVisible(),
          ScreenButtonAnchor.fromId(choice).orElseThrow(),
          preferences.lockedSlotTintVisible());
    }
  },

  LOCKED_SLOT_TINT_VISIBLE("lockedSlotTintVisible") {
    @Override
    public String valueIn(ClientPreferences preferences) {
      return Boolean.toString(preferences.lockedSlotTintVisible());
    }

    @Override
    public List<String> choices() {
      return BOOLEAN_CHOICES;
    }

    @Override
    ClientPreferences apply(ClientPreferences preferences, String choice) {
      return new ClientPreferences(
          preferences.sortButtonVisible(),
          preferences.screenButtonAnchor(),
          Boolean.parseBoolean(choice));
    }
  };

  private static final List<String> BOOLEAN_CHOICES = List.of("true", "false");

  private final String key;

  ClientPreference(String key) {
    this.key = key;
  }

  public String key() {
    return key;
  }

  public abstract String valueIn(ClientPreferences preferences);

  public abstract List<String> choices();

  abstract ClientPreferences apply(ClientPreferences preferences, String choice);

  public String defaultValue() {
    return valueIn(ClientPreferences.defaults());
  }

  public Optional<ClientPreferences> withValue(ClientPreferences preferences, String rawValue) {
    if (preferences == null || rawValue == null) {
      return Optional.empty();
    }
    final String trimmed = rawValue.trim();
    return choices().stream()
        .filter(choice -> choice.equalsIgnoreCase(trimmed))
        .findFirst()
        .map(choice -> apply(preferences, choice));
  }
}
