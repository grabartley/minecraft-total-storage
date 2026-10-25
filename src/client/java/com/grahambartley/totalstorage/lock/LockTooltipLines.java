package com.grahambartley.totalstorage.lock;

import com.grahambartley.totalstorage.screen.HintLine;
import com.grahambartley.totalstorage.screen.HintTooltipData;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class LockTooltipLines {
  private static final String PREFIX = "screen.total-storage.lock.";

  private LockTooltipLines() {}

  public static boolean applies(boolean altDown, boolean locked) {
    return altDown && locked;
  }

  public static boolean applies(Slot slot, boolean altDown) {
    return applies(altDown, LockedSlotState.isLocked(slot));
  }

  public static List<HintLine> hints(boolean onMac) {
    return List.of(
        HintLine.of(HintLine.badge(), HintLine.words(Text.translatable(PREFIX + "locked"))),
        HintLine.of(
            HintLine.key(onMac ? "option" : "alt"),
            HintLine.words(Text.translatable(PREFIX + "unlock"))));
  }

  public static Optional<TooltipData> withHints(Optional<TooltipData> data, boolean onMac) {
    return data.isPresent() ? data : Optional.of(new HintTooltipData(hints(onMac)));
  }

  public static List<Text> withText(List<Text> lines, boolean itemHasItsOwnData, boolean onMac) {
    if (!itemHasItsOwnData) {
      return lines;
    }
    final List<Text> extended = new ArrayList<>(lines);
    extended.add(Text.translatable(PREFIX + "locked").formatted(Formatting.GRAY));
    extended.add(
        Text.translatable(PREFIX + (onMac ? "unlock.plain.mac" : "unlock.plain"))
            .formatted(Formatting.DARK_GRAY));
    return extended;
  }
}
