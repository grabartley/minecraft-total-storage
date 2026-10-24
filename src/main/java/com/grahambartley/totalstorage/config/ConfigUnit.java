package com.grahambartley.totalstorage.config;

import com.grahambartley.totalstorage.TotalStorage;
import net.minecraft.text.Text;

public enum ConfigUnit {
  NONE,
  PERCENT;

  public Text display(int value) {
    return switch (this) {
      case NONE -> Text.literal(Integer.toString(value));
      case PERCENT ->
          Text.translatableWithFallback(
              "screen." + TotalStorage.MOD_ID + ".config.value.percent", "%s%%", value);
    };
  }
}
