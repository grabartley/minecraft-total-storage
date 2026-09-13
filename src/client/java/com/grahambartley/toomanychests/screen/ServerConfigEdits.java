package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.config.ConfigSetting;
import com.grahambartley.toomanychests.config.TooManyChestsConfig;
import java.util.Objects;

public final class ServerConfigEdits {
  private TooManyChestsConfig baseline;
  private TooManyChestsConfig edited;

  public ServerConfigEdits(TooManyChestsConfig baseline) {
    this.baseline = Objects.requireNonNull(baseline, "baseline");
    this.edited = baseline;
  }

  public TooManyChestsConfig edited() {
    return edited;
  }

  public String valueOf(ConfigSetting setting) {
    return setting.valueIn(edited);
  }

  public boolean set(ConfigSetting setting, String rawValue) {
    return setting
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

  public boolean adopt(TooManyChestsConfig latest) {
    if (latest == null || isDirty() || latest.equals(baseline)) {
      return false;
    }
    baseline = latest;
    edited = latest;
    return true;
  }
}
