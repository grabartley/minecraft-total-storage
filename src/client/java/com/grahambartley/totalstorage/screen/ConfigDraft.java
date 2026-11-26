package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.config.ConfigOption;
import java.util.Objects;

public final class ConfigDraft<S> {
  private S baseline;
  private S edited;

  public ConfigDraft(S baseline) {
    this.baseline = Objects.requireNonNull(baseline, "baseline");
    this.edited = baseline;
  }

  public S edited() {
    return edited;
  }

  public String valueOf(ConfigOption<S> option) {
    return option.valueIn(edited);
  }

  public boolean set(ConfigOption<S> option, String rawValue) {
    return option
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

  public boolean adopt(S latest) {
    if (latest == null || isDirty() || latest.equals(baseline)) {
      return false;
    }
    baseline = latest;
    edited = latest;
    return true;
  }
}
