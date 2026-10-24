package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.config.ConfigSetting;
import com.grahambartley.totalstorage.config.TotalStorageConfig;
import java.util.Objects;

public final class ServerConfigEdits {
  private TotalStorageConfig baseline;
  private TotalStorageConfig edited;

  public ServerConfigEdits(TotalStorageConfig baseline) {
    this.baseline = Objects.requireNonNull(baseline, "baseline");
    this.edited = baseline;
  }

  public TotalStorageConfig edited() {
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

  public boolean adopt(TotalStorageConfig latest) {
    if (latest == null || isDirty() || latest.equals(baseline)) {
      return false;
    }
    baseline = latest;
    edited = latest;
    return true;
  }
}
