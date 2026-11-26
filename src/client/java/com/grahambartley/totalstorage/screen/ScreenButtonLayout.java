package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.config.ScreenButtonAnchor;
import java.util.List;
import java.util.stream.IntStream;

public final class ScreenButtonLayout {
  private final ScreenButtonAnchor anchor;
  private final int count;
  private boolean clearOfTheEffects;

  public ScreenButtonLayout(ScreenButtonAnchor anchor, int count) {
    this.anchor = anchor;
    this.count = count;
  }

  public List<ScreenButtonSpot> spots(
      ScreenPanel panel, int screenWidth, int screenHeight, int effects) {
    final List<ScreenButtonSpot> anchored =
        IntStream.range(0, count)
            .mapToObj(index -> ScreenButtonRow.at(anchor, panel, index, count))
            .toList();
    if (!clearOfTheEffects) {
      clearOfTheEffects =
          StatusEffectColumn.of(panel, screenWidth, effects)
              .filter(column -> anchored.stream().anyMatch(column::covers))
              .isPresent();
    }
    if (!clearOfTheEffects) {
      return anchored;
    }
    return IntStream.range(0, count)
        .mapToObj(index -> ScreenButtonRow.beneath(panel, index, count, screenHeight))
        .toList();
  }
}
