package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.config.ClientPreference;
import com.grahambartley.totalstorage.config.ClientPreferences;
import java.util.Objects;

public final class ClientPreferenceEdits {
  private final ClientPreferences baseline;
  private ClientPreferences edited;

  public ClientPreferenceEdits(ClientPreferences baseline) {
    this.baseline = Objects.requireNonNull(baseline, "baseline");
    this.edited = baseline;
  }

  public ClientPreferences edited() {
    return edited;
  }

  public String valueOf(ClientPreference preference) {
    return preference.valueIn(edited);
  }

  public boolean set(ClientPreference preference, String rawValue) {
    return preference
        .withValue(edited, rawValue)
        .map(
            updated -> {
              edited = updated;
              return true;
            })
        .orElse(false);
  }

  public boolean isDirty() {
    return !edited.equals(baseline);
  }
}
