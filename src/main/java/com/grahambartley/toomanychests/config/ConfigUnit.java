package com.grahambartley.toomanychests.config;

import com.grahambartley.toomanychests.TooManyChests;
import net.minecraft.text.Text;

public enum ConfigUnit {
  NONE,
  PERCENT;

  public Text display(int value) {
    return switch (this) {
      case NONE -> Text.literal(Integer.toString(value));
      case PERCENT ->
          Text.translatableWithFallback(
              "screen." + TooManyChests.MOD_ID + ".config.value.percent", "%s%%", value);
    };
  }
}
