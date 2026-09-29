package com.grahambartley.toomanychests.config;

import com.grahambartley.toomanychests.TooManyChests;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.text.Text;

public enum SortOrdering implements ConfigChoice {
  REGISTRY_ID,
  DISPLAY_NAME;

  @Override
  public String id() {
    return name().toLowerCase(Locale.ROOT);
  }

  @Override
  public Text displayName() {
    return Text.translatable("screen." + TooManyChests.MOD_ID + ".config.value.sort." + id());
  }

  public static Optional<SortOrdering> fromId(String id) {
    if (id == null) {
      return Optional.empty();
    }
    for (SortOrdering ordering : values()) {
      if (ordering.id().equalsIgnoreCase(id.trim())) {
        return Optional.of(ordering);
      }
    }
    return Optional.empty();
  }
}
