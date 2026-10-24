package com.grahambartley.totalstorage.config;

import com.grahambartley.totalstorage.TotalStorage;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.text.Text;

public enum TerminalAccessMode implements ConfigChoice {
  OWNER,
  TRUSTED,
  OPEN;

  @Override
  public String id() {
    return name().toLowerCase(Locale.ROOT);
  }

  @Override
  public Text displayName() {
    return Text.translatable("screen." + TotalStorage.MOD_ID + ".terminal.access." + id());
  }

  public static Optional<TerminalAccessMode> fromId(String id) {
    if (id == null) {
      return Optional.empty();
    }
    for (TerminalAccessMode mode : values()) {
      if (mode.id().equalsIgnoreCase(id.trim())) {
        return Optional.of(mode);
      }
    }
    return Optional.empty();
  }
}
